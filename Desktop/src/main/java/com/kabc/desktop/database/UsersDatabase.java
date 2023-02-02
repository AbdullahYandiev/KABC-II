package com.kabc.desktop.database;

import com.kabc.desktop.database.firebase.FirebaseUsers;
import com.kabc.desktop.database.sqlite.SQLiteDB;
import com.kabc.desktop.database.tableitems.UsersMap;
import com.kabc.desktop.unit.User;

public class UsersDatabase  {

    public static void add(User user) {
        SQLiteDB.add(user);
        FirebaseUsers.add(user);
        UsersMap.add(user);
    }

    public static void edit(User oldUser, User newUser) {
        SQLiteDB.edit(oldUser, newUser);
        FirebaseUsers.edit(oldUser, newUser);
        UsersMap.edit(oldUser, newUser);
    }

    public static void delete(User user){
        SQLiteDB.delete(user);
        FirebaseUsers.delete(user);
        UsersMap.delete(user);
    }
}