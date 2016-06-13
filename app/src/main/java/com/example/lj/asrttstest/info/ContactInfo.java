package com.example.lj.asrttstest.info;

import java.util.ArrayList;

/**
 * Created by lj on 16/6/10.
 */
public class ContactInfo {

    private String firstName;
    private String lastName;
    private String mobilePhone;
    private String homePhone;
    private String workPhone;
    private String phoneID;
    private ArrayList<String> phoneTypeArray;
    private int id;

    public int getId() {
        return id;
    }

    public String getPhoneID() {
        return phoneID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setId(int _id) {
        this.id = _id;
    }

    public void setPhoneID(String phoneID) {
        this.phoneID = phoneID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String toString(){
        return "Name: " + firstName + " " + lastName + ", "+
                "Phone: " + mobilePhone + "\n";
    }
}
