package net.ninjacat.simim.core;

import com.google.common.collect.ImmutableList;
import net.ninjacat.simim.app.Duplicates;
import org.flywaydb.core.Flyway;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ImageDatabase {

    private final PreparedStatement insertStatement;
    private final PreparedStatement selectHashes;
    private final PreparedStatement selectByPath;
    private final PreparedStatement countByPath;
    private final PreparedStatement selectByHash;
    private final Connection connection;

    @Inject
    public ImageDatabase(final Connection db, final Flyway flyway) {
        flyway.migrate();
        this.connection = db;

        try {
            this.insertStatement = db.prepareStatement("insert into image values(?, ?, ?)");
            this.selectHashes = db.prepareStatement("select distinct(hash) from image");
            this.selectByHash = db.prepareStatement("select path, thumbnail from image where hash = ?");
            this.selectByPath = db.prepareStatement("select hash, thumbnail from image where path = ?");
            this.countByPath = db.prepareStatement("select count(*) from image where path = ?");
        } catch (final SQLException ex) {
            throw new ImageDatabaseException("Failed to create prepared statements", ex);
        }
    }

    public void insertImage(final SimImage simImage) {
        try {
            this.insertStatement.setString(1, simImage.getSignature().getSignature().toString());
            this.insertStatement.setString(2, simImage.getPath().toString());
            this.insertStatement.setBlob(3, simImage.getThumbnailBlob());
            this.insertStatement.execute();
            this.connection.commit();
        } catch (final Exception ex) {
            throw new ImageDatabaseException("Failed to insert image " + simImage, ex);
        } finally {
            try {
                this.connection.rollback();
            } catch (final SQLException ignored) {

            }
        }
    }

    public Collection<ImageHash> loadHashes() {
        try {
            try (final ResultSet resultSet = this.selectHashes.executeQuery()) {
                final ImmutableList.Builder<ImageHash> builder = ImmutableList.builder();
                while (resultSet.next()) {
                    final BigInteger hash = new BigInteger(resultSet.getString(1));
                    builder.add(new ImageHash(hash));
                }
                return builder.build();
            }
        } catch (final Exception ex) {
            throw new ImageDatabaseException("Failed to load hashes", ex);
        }
    }

    public List<SimImage> loadByHash(final ImageHash hash) {
        try {
            this.selectByHash.setString(1, hash.getSignature().toString());
            try (final ResultSet resultSet = this.selectByHash.executeQuery()) {
                final ImmutableList.Builder<SimImage> builder = ImmutableList.builder();
                while (resultSet.next()) {
                    final Path path = Paths.get(resultSet.getString(1));
                    try (InputStream is = resultSet.getBlob(2).getBinaryStream()) {
                        builder.add(new SimImage(path, hash, is));
                    }
                }
                return builder.build();
            }
        } catch (final Exception ex) {
            throw new ImageDatabaseException("Failed to load by hash " + hash.toString(), ex);
        }
    }

    public Optional<SimImage> loadByPath(final Path path) throws SQLException, IOException {
        this.selectByPath.setString(1, path.toString());
        try (final ResultSet resultSet = this.selectByPath.executeQuery()) {
            if (resultSet.next()) {
                final ImageHash hash = new ImageHash(new BigInteger(resultSet.getString(1)));
                try (InputStream is = resultSet.getBlob(2).getBinaryStream()) {
                    return Optional.of(new SimImage(path, hash, is));
                }
            } else {
                return Optional.empty();
            }
        }
    }


    public boolean exists(final Path path) {
        try {
            this.countByPath.setString(1, path.toString());
            try (final ResultSet resultSet = this.countByPath.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (final SQLException e) {
            throw new ImageDatabaseException("Failed to check for existence of image " + path, e);
        }

    }
}
