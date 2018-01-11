package net.ninjacat.simim.di;

import dagger.Module;
import dagger.Provides;
import net.ninjacat.simim.app.Application;
import net.ninjacat.simim.core.ImageDatabase;
import net.ninjacat.simim.core.ImageDatabaseException;
import net.ninjacat.utils.Os;
import org.flywaydb.core.Flyway;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Module
public class DatabaseModule {


    @Provides
    @Named("databaseUrl")
    public String providesDatabaseUrl(){
        final Path dataPath = Os.current().getAppData(Application.NAME).resolve("data");
        try {
            Files.createDirectories(dataPath);
        } catch (final IOException e) {
        }
        return "jdbc:hsqldb:file:" + dataPath.resolve("data").toString();
    }

    @Provides
    @Singleton
    public Connection provideDb(@Named("databaseUrl") final String url)  {
        try {
            return DriverManager.getConnection(url, "SA", "");
        } catch (final SQLException e) {
            throw new ImageDatabaseException("Failed to get connection", e);
        }
    }

    @Provides
    @Singleton
    public Flyway providesFlyway(@Named("databaseUrl") final String url) {
        final Flyway flyway = new Flyway();
        flyway.setDataSource(url, "SA", "");
        return flyway;
    }

    @Provides
    @Singleton
    public ImageDatabase providesImageDatabase(final Connection db, final Flyway flyway) {
        return new ImageDatabase(db, flyway);
    }
}
