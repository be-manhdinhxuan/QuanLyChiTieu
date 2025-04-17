package com.example.quanlychitieu.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Spending {
    private String id;
    private int money;
    private int type;
    private String note;
    private Date dateTime;
    private String image;
    private String typeName;
    private String location;
    private List<String> friends;

    // Constructor
    public Spending(String id, int money, int type, Date dateTime, String note,
                   String image, String typeName, String location, List<String> friends) {
        this.id = id;
        this.money = money;
        this.type = type;
        this.dateTime = dateTime;
        this.note = note;
        this.image = image;
        this.typeName = typeName;
        this.location = location;
        this.friends = friends != null ? new ArrayList<>(friends) : null;
    }

    // Required fields constructor
    public Spending(int money, int type, Date dateTime) {
        this(null, money, type, dateTime, null, null, null, null, null);
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("money", money);
        map.put("type", type);
        map.put("note", note);
        map.put("date", dateTime);
        map.put("image", image);
        map.put("typeName", typeName);
        map.put("location", location);
        map.put("friends", friends);
        return map;
    }

    // Create from Firestore document
    public static Spending fromFirestore(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }

        Map<String, Object> data = snapshot.getData();
        if (data == null) {
            return null;
        }

        List<String> friendsList = new ArrayList<>();
        Object friendsObj = data.get("friends");
        if (friendsObj instanceof List<?>) {
            for (Object item : (List<?>) friendsObj) {
                if (item != null) {
                    friendsList.add(item.toString());
                }
            }
        }

        Date date = null;
        Object dateObj = data.get("date");
        if (dateObj instanceof Timestamp) {
            date = ((Timestamp) dateObj).toDate();
        }

        return new Spending(
            snapshot.getId(),
            ((Number) data.get("money")).intValue(),
            ((Number) data.get("type")).intValue(),
            date,
            (String) data.get("note"),
            (String) data.get("image"),
            (String) data.get("typeName"),
            (String) data.get("location"),
            friendsList
        );
    }

    // copyWith method
    public Spending copyWith(Integer money, Integer type, Date dateTime,
                           String note, String image, String typeName,
                           String location, List<String> friends) {
        return new Spending(
            this.id,
            money != null ? money : this.money,
            type != null ? type : this.type,
            dateTime != null ? dateTime : this.dateTime,
            note != null ? note : this.note,
            image != null ? image : this.image,
            typeName != null ? typeName : this.typeName,
            location != null ? location : this.location,
            friends != null ? friends : this.friends
        );
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDateTime() {
        return dateTime != null ? (Date) dateTime.clone() : null;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime != null ? (Date) dateTime.clone() : null;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getFriends() {
        return friends != null ? new ArrayList<>(friends) : null;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends != null ? new ArrayList<>(friends) : null;
    }
}