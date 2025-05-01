package com.yourorg.scheduler.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() throws Exception {
        String url = "jdbc:mysql://localhost:3306/timetable";
        String user = "root";
        String password = "75053";
        return DriverManager.getConnection(url, user, password);
    }
}
