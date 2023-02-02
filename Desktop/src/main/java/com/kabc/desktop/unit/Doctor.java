package com.kabc.desktop.unit;

import com.kabc.desktop.database.sqlite.SQLiteDB;
import com.kabc.desktop.database.sqlite.SQLiteUnit;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Doctor implements SQLiteUnit {

    private final int id;
    private final String surname;
    private final String name;
    private final String lastname;
    private final String fullName;

    public Doctor(int id, String surname, String name, String lastname) {
        this.id = id;
        this.surname = surname;
        this.name = name;
        this.lastname = lastname;
        this.fullName = String.format("%s %s %s", surname, name, lastname);
    }

    public Doctor(ResultSet resultSet) throws SQLException {
        id = resultSet.getInt("id");
        surname = resultSet.getString("surname");
        name = resultSet.getString("name");
        lastname = resultSet.getString("lastname");
        fullName = String.format("%s %s %s", surname, name, lastname);
    }

    public int getId() {
        return id;
    }

    public String getSurname() {
        return surname;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return getFullName();
    }

    @Override
    public String getSQLTableName() {
        return SQLiteDB.DOCTORS_TABLE;
    }

    @Override
    public String getSQLValues() {
        return String.format("(%d, '%s', '%s', '%s')", id, surname, name, lastname);
    }

    @Override
    public String getSQLSet() {
        return String.format("surname = '%s', name = '%s', lastname = '%s'", surname, name, lastname);
    }

    @Override
    public String getSQLWhere() {
        return String.format("id = %d", id);
    }
}