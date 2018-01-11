package net.ninjacat.simim.core;

import com.google.common.collect.ImmutableList;
import org.flywaydb.core.Flyway;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

public class ImageDatabase {

    private final PreparedStatement insertStatement;
    private final PreparedStatement selectByHash;
    private final PreparedStatement selectByPath;
    private final PreparedStatement countByPath;
    private final Connection connection;

    @Inject
    public ImageDatabase(final Connection db, final Flyway flyway) {
        flyway.migrate();
        this.connection = db;

        try {
            this.insertStatement = db.prepareStatement("insert into image values(?, ?, ?)");
            this.selectByHash = db.prepareStatement("select path, thumbnail from image where hash = ?");
            this.selectByPath = db.prepareStatement("select hash, thumbnail from image where path = ?");
            this.countByPath = db.prepareStatement("select count(*) from image where path = ?");
        } catch (final SQLException ex) {
            throw new ImageDatabaseException("Failed to create prepared statements", ex);
        }
    }

    public void insertImage(final SimImage simImage) {
        try {
            this.insertStatement.setLong(1, simImage.getSignature().getSignature());
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

    public Collection<SimImage> loadByHash(final ImageHash hash) {
        try {
            this.selectByHash.setLong(1, hash.getSignature());
            try (final ResultSet resultSet = this.selectByHash.executeQuery()) {
                final ImmutableList.Builder<SimImage> builder = ImmutableList.builder();
                resultSet.beforeFirst();
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
                final ImageHash hash = new ImageHash(resultSet.getLong(1));
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
