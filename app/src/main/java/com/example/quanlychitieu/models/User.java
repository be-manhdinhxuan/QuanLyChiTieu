package com.example.quanlychitieu.models;

import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class User {
    private static final String DEFAULT_AVATAR = "https://firebasestorage.googleapis.com/v0/b/spending-management-c955a.appspot.com/o/FVK7wz5aIAA25l8.jpg?alt=media&token=ddceb8f7-7cf7-4c42-a806-5d0d48ce58f5";

    private String name;
    private String birthday;
    private String avatar;
    private boolean gender;
    private long money;

    // Constructor
    public User(String name, String birthday, String avatar, long money, boolean gender) {
        this.name = name;
        this.birthday = birthday;
        this.avatar = avatar != null ? avatar : DEFAULT_AVATAR;
        this.money = money;
        this.gender = gender;
    }

    // Default constructor
    public User() {
        this.avatar = DEFAULT_AVATAR;
        this.gender = true;
        this.money = 0;
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("birthday", birthday);
        map.put("avatar", avatar);
        map.put("gender", gender);
        map.put("money", money);
        return map;
    }

    // Create from Firestore document
    public static User fromFirestore(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }

        Map<String, Object> data = snapshot.getData();
        if (data == null) {
            return null;
        }

        return new User(
            (String) data.get("name"),
            (String) data.get("birthday"),
            (String) data.get("avatar"),
            ((Number) data.get("money")).longValue(),
            Boolean.TRUE.equals(data.get("gender"))
        );
    }

    // copyWith method
    public User copyWith(String name, String birthday, String avatar, Boolean gender, Long money) {
        return new User(
            name != null ? name : this.name,
            birthday != null ? birthday : this.birthday,
            avatar != null ? avatar : DEFAULT_AVATAR,
            money != null ? money : this.money,
            gender != null ? gender : this.gender
        );
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar != null ? avatar : DEFAULT_AVATAR;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }
}