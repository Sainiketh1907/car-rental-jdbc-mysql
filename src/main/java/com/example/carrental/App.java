package com.example.carrental;

import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import com.example.carrental.config.DataSourceFactory;

public class App {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (InputStream in = App.class.getResourceAsStream("/application.properties")) {
            props.load(in);
        }
        DataSource ds = DataSourceFactory.get(props);
        System.out.println("DataSource created: " + ds);
    }
}
