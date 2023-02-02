package com.kabc.desktop.database.tableitems;

import com.kabc.desktop.database.sqlite.SQLiteDB;
import com.kabc.desktop.unit.Doctor;
import com.kabc.desktop.unit.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersMap {

    private static final Map<Integer, ObservableList<User>> items = getUsers();

    public static ObservableList<User> getByDoctorId(int doctorId) {
        return items.get(doctorId);
    }

    private static Map<Integer, ObservableList<User>> getUsers() {
        try {
            Map<Integer, ObservableList<User>> users = new HashMap<>();
            for (int doctorId : getDoctorIds()) {
                users.put(doctorId, FXCollections.observableArrayList());
                String doctorIdCondition = String.format("%s = %d", SQLiteDB.DOCTOR_ID_FIELD, doctorId);
                ResultSet usersSet = SQLiteDB.select(SQLiteDB.ALL_FIELDS, SQLiteDB.USERS_TABLE, doctorIdCondition);
                while (usersSet.next()) {
                    users.get(doctorId).add(new User(usersSet));
                }
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Integer> getDoctorIds() {
        try {
            List<Integer> doctorIds = new ArrayList<>();
            ResultSet doctorIdsSet = SQLiteDB.select(SQLiteDB.ID_FIELD, SQLiteDB.DOCTORS_TABLE, null);
            while (doctorIdsSet.next()) {
                doctorIds.add(doctorIdsSet.getInt("id"));
            }
            return doctorIds;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void add(User user) {
        items.get(user.getDoctorId()).add(user);
    }

    public static void edit(User oldUser, User newUser) {
        if (newUser.getDoctorId() == oldUser.getDoctorId()) {
            ObservableList<User> currentUserList = getByDoctorId(newUser.getDoctorId());
            currentUserList.set(currentUserList.indexOf(oldUser), newUser);
        } else {
            delete(oldUser);
            add(newUser);
        }
    }

    public static void delete(User user) {
        items.get(user.getDoctorId()).remove(user);
    }

    public static void addDoctor(Doctor doctor) {
        items.put(doctor.getId(), FXCollections.observableArrayList());
    }

    public static void deleteDoctor(Doctor doctor) {
        items.remove(doctor.getId());
    }
}