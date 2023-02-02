package com.kabc.desktop.database;

import com.kabc.desktop.database.sqlite.SQLiteDB;
import com.kabc.desktop.database.tableitems.DoctorsList;
import com.kabc.desktop.database.tableitems.UsersMap;
import com.kabc.desktop.unit.Doctor;

public class DoctorsDatabase {

    public static void add(Doctor doctor) {
        SQLiteDB.add(doctor);
        DoctorsList.add(doctor);
        UsersMap.addDoctor(doctor);
    }

    public static void edit(Doctor oldDoctor, Doctor newDoctor) {
        SQLiteDB.edit(oldDoctor, newDoctor);
        DoctorsList.edit(oldDoctor, newDoctor);
    }

    public static void delete(Doctor doctor) {
        SQLiteDB.delete(doctor);
        DoctorsList.delete(doctor);
        UsersMap.deleteDoctor(doctor);
    }
}
