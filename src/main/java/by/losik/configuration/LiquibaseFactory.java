package by.losik.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.sql.Connection;
import java.sql.DriverManager;

@ApplicationScoped
@Slf4j
public class LiquibaseFactory {

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String jdbcUrl;

    @ConfigProperty(name = "quarkus.datasource.username")
    String username;

    @ConfigProperty(name = "quarkus.datasource.password")
    String password;

    @ConfigProperty(name = "quarkus.liquibase.change-log")
    String changeLogFile;

    public Liquibase createLiquibase() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            return new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Liquibase instance", e);
        }
    }

    public void closeLiquibase(Liquibase liquibase) {
        if (liquibase != null && liquibase.getDatabase() != null) {
            try {
                liquibase.getDatabase().close();
            } catch (Exception e) {
                log.error("Warning: Failed to close database connection: " + e.getMessage());
            }
        }
    }
}