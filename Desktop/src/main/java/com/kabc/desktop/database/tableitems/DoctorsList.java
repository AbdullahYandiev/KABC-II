package com.kabc.desktop.database.tableitems;

import com.kabc.desktop.database.sqlite.SQLiteDB;
import com.kabc.desktop.unit.Doctor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DoctorsList {

    private static final ObservableList<Doctor> items = getDoctors();

    public static ObservableList<Doctor> get() {
        return items;
    }

    private static ObservableList<Doctor> getDoctors() {
        try {
            ObservableList<Doctor> doctors = FXCollections.observableArrayList();
            ResultSet doctorsSet = SQLiteDB.select(SQLiteDB.ALL_FIELDS, SQLiteDB.DOCTORS_TABLE, null);
            while (doctorsSet.next()) {
                doctors.add(new Doctor(doctorsSet));
            }
            return doctors;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void add(Doctor doctor) {
        items.add(doctor);
    }

    public static void edit(Doctor oldDoctor, Doctor newDoctor) {
        items.set(items.indexOf(oldDoctor), newDoctor);
    }

    public static void delete(Doctor doctor) {
        items.remove(doctor);
    }
}