package com.example.lj.asrttstest.asr.text;

/**
 * Created by lj on 16/6/30.
 */

public class ContentBoundaryParser {

    public static String getBoundary(String contentType) {
        int start = contentType.indexOf("boundary=");
        String boundary = null;
        if (start > -1) {
            boundary = contentType.substring(start+9).trim();
        }
        return boundary;
    }

}

