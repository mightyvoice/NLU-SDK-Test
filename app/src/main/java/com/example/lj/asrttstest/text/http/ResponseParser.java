package com.example.lj.asrttstest.text.http;

/**
 * Created by lj on 16/6/30.
 */

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


//import org.apache.commons.fileupload.MultipartStream;
//import org.apache.http.HttpEntity;


public class ResponseParser {

    /**
     * This header parser works well for parsing raw socket responses that are text-based (like Dictation and NLU requests)
     *  However, it does not work well for mixed response streams that contain both text and binary audio (i.e. TTS requests)
     *
     * @param br
     * @return
     * @throws IOException
     */
    public Map<String, String> parseHeaders(BufferedReader br) throws IOException {

        String t;
        int crlfCounter = 0;
        Map<String, String> headers = new HashMap<String, String>();

        while((t = br.readLine()) != null) {
            // Headers are following by two line-breaks...
            if( t.isEmpty() ) crlfCounter++;
            if( crlfCounter == 2 ) break;

            String[] arr = t.split(":", 2);
            if( arr != null & arr.length == 2 )
                headers.put(arr[0].trim().toLowerCase(), arr[1].trim().toLowerCase());
        }

        // Extract the boundary, if it exists, and continue reading thru the buffered response stream up to the boundary
        if( headers.containsKey("content-type") ) {
            String contentType = headers.get("content-type");
            if( contentType.toLowerCase().contains("boundary=") ) {
                String boundary = null;
                String[] arr = contentType.split("=", 2);
                boundary = arr[1];
                while((t = br.readLine()) != null) {
                    if(t.toLowerCase().contains(boundary))
                        break;
                }
            }
        }

        return headers;
    }

    protected boolean isJSON(String str) {
        try
        {
            JSONObject json = new JSONObject(str);
        }
        catch (JSONException e) {
            return false;
        }
        return true;
    }
    protected boolean isNumeric(String str) {
        try
        {
            int size = Integer.parseInt(str, 16);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
    /**
     *
     * This body parser works well for parsing raw socket responses that are text-based (like Dictation and NLU requests)
     *  However, it does not work well for mixed response streams that contain both text and binary audio (i.e. TTS requests)
     *  For TTS requests, it's a bit easier to use the
     *
     * @param br
     * @param boundary
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public Map<String, String> parseBodyPart(BufferedReader br, String boundary) throws IOException, JSONException {

        String t;
        Map<String, String> parts = new HashMap<String, String>();
        boolean entityHeadersParsed = false;
        int bodyPartCounter = 1;
        int crlfCounter = 0;

        String contentType = "";

        while((t = br.readLine()) != null) {

            // Stop parsing if we've hit the boundary...
            if( boundary != null && t.toLowerCase().contains(boundary.toLowerCase()) )
                break;

            // Parse out the entity headers...
            if( !entityHeadersParsed ) {

                String[] arr = t.split(":", 2);
                if( arr != null & arr.length == 1 && !arr[0].isEmpty() ) {
                    // Grab the entity header size which will is represented in Hex
                    int size = Integer.parseInt(arr[0], 16);
                    parts.put("size", String.valueOf(size));

                    // If size is 0, this was the last response from the server...
                    if( size == 0 )
                        return parts;
                }
                else if( arr != null & arr.length == 2 ) {
                    // Grab the entity header
                    parts.put(arr[0].trim().toLowerCase(), arr[1].trim().toLowerCase());
                }
                else	{
                    // No more entity headers...
                    entityHeadersParsed = true;
                    contentType = parts.containsKey("content-type") ? parts.get("content-type") : "";
                }

                continue;
            }

            // After headers are parsed, get past any line-feeds
            //if( entityHeadersParsed && t.isEmpty() && crlfCounter++ < 2 ) {
            if( entityHeadersParsed && t.isEmpty() ) {
                continue;
            }

            // After headers and line-feeds are parsed, grab the size of the remaining body content
            //if( entityHeadersParsed && !t.isEmpty() && crlfCounter++ < 2 ) {
            if( entityHeadersParsed && !t.isEmpty() ) {

                if( isNumeric(t) ) {
                    int size = Integer.parseInt(t, 16);
                    parts.put("contentSize", String.valueOf(size));
                    continue;
                }
                else if( contentType.contains("json") && isJSON(t) ) {
                    JSONObject json = new JSONObject(t);
                    t = json.toString(4);
                    parts.put("json_" + bodyPartCounter++, t);
                    continue;
                }
                else if( contentType.contains("audio") ) {
                    // Should just stream audio to file here....
                    if( parts.containsKey("audio") ) {
                        t = parts.get("audio") + t;
                    }
                    parts.put("audio", t);
                }
            }

        }

        return parts;
    }

}

