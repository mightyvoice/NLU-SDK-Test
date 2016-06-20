package com.example.lj.asrttstest.info;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

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

    public Hashtable<String, String> phoneNumberTable;
    public Hashtable<String, String> phoneTypeTable;

    private int id;

    public ContactInfo(){
        phoneNumberTable = new Hashtable<>();
        phoneTypeTable = new Hashtable<>();
        phoneTypeTable.put("3", "work");
        phoneTypeTable.put("1", "home");
        phoneTypeTable.put("2", "mobile");
    }

    public JSONArray[] getPhoneTypeArray(){
        JSONArray[] res = new JSONArray[2];
        Set<String> keys = phoneTypeTable.keySet();
        for(String key:keys){
            res[0].put(key);
            res[1].put(phoneNumberTable.get(key));
        }
        return res;
    }

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
