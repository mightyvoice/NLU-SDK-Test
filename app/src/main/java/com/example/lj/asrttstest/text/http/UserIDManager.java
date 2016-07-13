package com.example.lj.asrttstest.text.http;

/**
 * Created by lj on 16/6/30.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class UserIDManager {

    static protected UserIDManager _userIDManager = null;
    protected String _userID = null;

    private UserIDManager() {
    }

    public static UserIDManager createUserIDManager() {
        if( _userIDManager == null )
            _userIDManager = new UserIDManager();

        return _userIDManager;
    }

    public String getUserID() {
        return _userID;
    }

    public String initUserID() {

        // Check to see if an existing User ID has been saved to cache...
        if( null == retrieveUserID() ) {
            // Create a unique User ID for each device or user. Cache and re-use it for future transactions.
            // The User ID must be <40 chars in length and can contain [a-z,A-Z,0-9] and "_" underscores.
            _userID = java.util.UUID.randomUUID().toString().replaceAll("-", "");
            cacheUserID();
            System.out.println("Created and cached new User ID: " + _userID);
        }
        else
            System.out.println("Using cached User ID: " + _userID);

        return _userID;
    }

    public void cacheUserID() {

        // Implement some algorithm to capture/re-use user id across app usages...
        // For purposes of this sample app, we're just using a simple text file. Real-world solutions
        // will likely leverage a customer database or some other app-specific data store
        if( _userID == null || _userID.isEmpty() ) return;

        try {
            File dir = new File(".");
            File fout = new File(dir.getCanonicalPath() + File.separator + "cache.txt");

            FileOutputStream fos = new FileOutputStream(fout);
            Writer writer = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
            writer.write(_userID);
            writer.close();

        } catch(Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public String retrieveUserID() {

        // Implement some algorithm to capture/re-use user id across app usages...
        String userID = null;

        try {
            File dir = new File(".");
            File fin = new File(dir.getCanonicalPath() + File.separator + "cache.txt");

            FileInputStream fis = new FileInputStream(fin);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            userID = br.readLine();
            br.close();

            return userID;

        } catch(FileNotFoundException e) {
            // Ignore as this is not really a hard error...
            System.out.println("Cached UserID does not exist");
            return null;

        } catch(Exception e) {
            e.printStackTrace(System.out);
            return null;

        } finally {
            _userID = userID;

        }
    }

}

