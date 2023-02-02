package com.kabc.desktop.database.sqlite;

import java.sql.*;

public class SQLiteDB {

    private static final Connection CONNECTION = initializeDatabase();

    public static final String USERS_TABLE = "users";
    public static final String DOCTORS_TABLE = "doctors";

    public static final String ALL_FIELDS = "*";
    public static final String ID_FIELD = "id";
    public static final String DOCTOR_ID_FIELD = "doctor_id";

    private static Connection initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:kabc.sqlite");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ResultSet select(String fieldName, String tableName, String whereCondition) {
        try {
            String query = "SELECT " + fieldName + " FROM " + tableName;
            if (whereCondition != null) {
                query += " WHERE " + whereCondition;
            }
            Statement statement = CONNECTION.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void add(SQLiteUnit unit) {
        try {
            String query = "INSERT INTO " + unit.getSQLTableName() + " VALUES " + unit.getSQLValues();
            Statement statement = CONNECTION.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ignored) {}
    }

    public static void edit(SQLiteUnit oldUnit, SQLiteUnit newUnit) {
        try {
            String tableName = oldUnit.getSQLTableName();
            String query = "UPDATE " + tableName + " SET " + newUnit.getSQLSet() + " WHERE " + oldUnit.getSQLWhere();
            Statement statement = CONNECTION.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ignored) {}
    }

    public static void delete(SQLiteUnit unit) {
        try {
            String query = "DELETE FROM " + unit.getSQLTableName() + " WHERE " + unit.getSQLWhere();
            Statement statement = CONNECTION.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ignored) {}
    }
}