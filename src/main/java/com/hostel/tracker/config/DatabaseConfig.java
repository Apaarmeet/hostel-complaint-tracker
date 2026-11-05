package com.hostel.tracker.config;

import org.springframework.beans.factory.annotation.Value;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Chooses the database:
 *  - DATABASE_URL set (e.g. postgresql://user:pass@host/db) -> PostgreSQL (data survives restarts)
 *  - otherwise -> H2 in-memory database in PostgreSQL mode (zero setup, demo data re-seeded on start)
 */
@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource(@Value("${DATABASE_URL:}") String databaseUrl) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return small(DataSourceBuilder.create().type(HikariDataSource.class)
                    .driverClassName("org.h2.Driver")
                    .url("jdbc:h2:mem:hostel;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
                    .username("sa").password("")
                    .build());
        }
        // Convert "postgresql://user:pass@host:port/db?sslmode=require" into a JDBC URL
        URI uri = URI.create(databaseUrl.replaceFirst("^postgres(ql)?://", "http://"));
        String[] userInfo = uri.getUserInfo().split(":", 2);
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost()
                + (uri.getPort() > 0 ? ":" + uri.getPort() : "")
                + uri.getPath()
                + (uri.getQuery() != null ? "?" + uri.getQuery() : "?sslmode=require");
        return small(DataSourceBuilder.create().type(HikariDataSource.class)
                .driverClassName("org.postgresql.Driver")
                .url(jdbcUrl)
                .username(userInfo[0])
                .password(userInfo.length > 1 ? userInfo[1] : "")
                .build());
    }

    /** A small connection pool is plenty for a college demo and saves memory on the free server. */
    private static HikariDataSource small(HikariDataSource ds) {
        ds.setMaximumPoolSize(4);
        ds.setMinimumIdle(1);
        return ds;
    }
}
