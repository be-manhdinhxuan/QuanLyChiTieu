package com.example.quanlychitieu.models;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Filter {
    private List<Integer> chooseIndex;
    private int money;
    private int finishMoney;
    private Date time;
    private Date finishTime;
    private String note;
    private List<Integer> colors;  // Using Integer for Color in Java
    private List<String> friends;

    // Constructor
    public Filter(List<Integer> chooseIndex) {
        this.chooseIndex = new ArrayList<>(chooseIndex);
        this.money = 0;
        this.finishMoney = 0;
        this.time = null;
        this.finishTime = null;
        this.note = "";
        this.colors = null;
        this.friends = null;
    }

    // Full constructor
    public Filter(List<Integer> chooseIndex, int money, int finishMoney, 
                 Date time, Date finishTime, String note, 
                 List<Integer> colors, List<String> friends) {
        this.chooseIndex = new ArrayList<>(chooseIndex);
        this.money = money;
        this.finishMoney = finishMoney;
        this.time = time;
        this.finishTime = finishTime;
        this.note = note;
        this.colors = colors != null ? new ArrayList<>(colors) : null;
        this.friends = friends != null ? new ArrayList<>(friends) : null;
    }

    // Getters
    public List<Integer> getChooseIndex() {
        return new ArrayList<>(chooseIndex);
    }

    public int getMoney() {
        return money;
    }

    public int getFinishMoney() {
        return finishMoney;
    }

    public Date getTime() {
        return time != null ? (Date) time.clone() : null;
    }

    public Date getFinishTime() {
        return finishTime != null ? (Date) finishTime.clone() : null;
    }

    public String getNote() {
        return note;
    }

    public List<Integer> getColors() {
        return colors != null ? new ArrayList<>(colors) : null;
    }

    public List<String> getFriends() {
        return friends != null ? new ArrayList<>(friends) : null;
    }

    // Setters
    public void setChooseIndex(List<Integer> chooseIndex) {
        this.chooseIndex = new ArrayList<>(chooseIndex);
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public void setFinishMoney(int finishMoney) {
        this.finishMoney = finishMoney;
    }

    public void setTime(Date time) {
        this.time = time != null ? (Date) time.clone() : null;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime != null ? (Date) finishTime.clone() : null;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setColors(List<Integer> colors) {
        this.colors = colors != null ? new ArrayList<>(colors) : null;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends != null ? new ArrayList<>(friends) : null;
    }

    // copyWith method (similar to Dart's copyWith)
    public Filter copyWith(List<Integer> chooseIndex, Integer money, Integer finishMoney,
                         Date time, Date finishTime, String note,
                         List<Integer> colors, List<String> friends) {
        return new Filter(
            chooseIndex != null ? chooseIndex : this.chooseIndex,
            money != null ? money : this.money,
            finishMoney != null ? finishMoney : this.finishMoney,
            time != null ? time : this.time,
            finishTime != null ? finishTime : this.finishTime,
            note != null ? note : this.note,
            colors != null ? colors : this.colors,
            friends != null ? friends : this.friends
        );
    }
}