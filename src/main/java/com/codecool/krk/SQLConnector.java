package com.codecool.krk;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnector {
    private static Connection connection = null;

    public static Connection getConnection(){
        if(connection == null){
            try {
                createConnection();
            } catch (SQLException e) {
                System.out.println("Couldn't create connection to database");
            }
        } return connection;
    }

    private static void createConnection() throws SQLException{
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/codecool", "queststore", "admin123");
        System.out.println("Connected to DB");
    }
}
