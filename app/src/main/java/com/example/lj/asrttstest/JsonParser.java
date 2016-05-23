package com.example.lj.asrttstest;

import android.util.JsonReader;

import java.io.InputStream;

/**
 * Created by lj on 16/5/23.
 */
public class JsonParser {
    private String data;
    private JsonReader jsonReader;
    private InputStream dataInputStream;

    public JsonParser(String input){
        data = input;
    }
}
