package com.kabc.desktop.unit;

import com.kabc.desktop.database.firebase.FirebaseUnit;
import com.kabc.desktop.database.sqlite.SQLiteDB;
import com.kabc.desktop.database.sqlite.SQLiteUnit;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User implements SQLiteUnit, FirebaseUnit {

    private final String login;
    private final String surname;
    private final String name;
    private final String lastname;
    private final String birthDate;
    private final int doctorId;

    public User(String login, String surname, String name, String lastname, String date, int doctor_id) {
        this.login = login;
        this.surname = surname;
        this.name = name;
        this.lastname = lastname;
        this.birthDate = date;
        this.doctorId = doctor_id;
    }

    public User(ResultSet resultSet) throws SQLException {
        login = resultSet.getString("login");
        surname = resultSet.getString("surname");
        name =  resultSet.getString("name");
        lastname = resultSet.getString("lastname");
        birthDate = resultSet.getString("birth_date");
        doctorId = resultSet.getInt("doctor_id");
    }

    public String getLogin() {
        return login;
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

    public String getBirthDate() {
        return birthDate;
    }

    public int getDoctorId() {
        return doctorId;
    }

    @Override
    public String getSQLTableName() {
        return SQLiteDB.USERS_TABLE;
    }

    @Override
    public String getSQLValues() {
        return String.format(
                "('%s', '%s', '%s', '%s', '%s', %d)",
                login, surname, name, lastname, birthDate, doctorId
        );
    }

    @Override
    public String getSQLSet() {
        return String.format(
                "login = '%s', surname = '%s', name = '%s', lastname = '%s', birth_date = '%s', doctor_id = %d",
                login, surname, name, lastname, birthDate, doctorId
        );
    }

    @Override
    public String getSQLWhere() {
        return String.format("login = '%s'", login);
    }

    @Override
    public boolean equals(Object object) {
        if (getClass() != object.getClass()) {
            return false;
        }
        User user = (User) object;
        return login.equals(user.getLogin()) && birthDate.equals(user.getBirthDate()) && doctorId == user.getDoctorId();
    }
}