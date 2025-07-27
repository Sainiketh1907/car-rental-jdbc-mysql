package com.example.carrental.config;

import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DataSourceFactory {
    private static HikariDataSource ds;

    private DataSourceFactory() {}

    public static DataSource get(Properties props) {
        if (ds == null) {
            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(props.getProperty("jdbc.url"));
            cfg.setUsername(props.getProperty("jdbc.user"));
            cfg.setPassword(props.getProperty("jdbc.password"));
            cfg.setMaximumPoolSize(Integer.parseInt(props.getProperty("jdbc.pool.size", "10")));
            ds = new HikariDataSource(cfg);
        }
        return ds;
    }
}
