package com.example.lj.asrttstest.dialog;

/**
 * Created by lj on 16/6/15.
 */
import com.example.lj.asrttstest.dialog.BaseDialogManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by lj on 16/5/23.
 */
public class JsonParser extends BaseDialogManager{

    private String data;
    private JSONObject actionObject = null;
    public String textForTTS;
    public String textForDisplay;


    public JsonParser(){
        data = "";
    }

    JsonParser(String input) throws JSONException {
        data = input;
    }

    JsonParser(JSONObject input){
        processServerResponse(input);
    }

    public void setData(String input) throws JSONException{
        data = input;
        getActionObject(input);
    }

    private void getActionObject(String input) throws JSONException{
        JSONObject curObject = new JSONObject(input);
        curObject = curObject.getJSONObject("value");
        curObject = curObject.getJSONObject("appserver_results");
        curObject = curObject.getJSONObject("value");
        curObject = curObject.getJSONObject("payload");
        curObject = curObject.getJSONObject("value");
        curObject = curObject.getJSONObject("actions");
        actionObject = curObject;
    }

    private boolean ifContainConversationTag(JSONObject curObject){
        try {
            curObject = curObject.getJSONObject("value");
        }catch (JSONException e){
            return false;
        }
        try {
            curObject = curObject.getJSONObject("type");
        }catch (JSONException e){
            return false;
        }
        String result = "";
        try {
            result = curObject.getString("value");
        }catch (JSONException e){
            return false;
        }
        if(result.equals("conversation")){
            return true;
        }
        return false;
    }

    private boolean ifCcontainContactTag(){
        JSONObject curObject = actionObject;
        JSONArray curArray;
        try {
            curArray = curObject.getJSONArray("value");
        } catch (JSONException e) {
            return false;
        }
        for(int i = 0; i < curArray.length(); i++){
            try {
                curObject = curArray.getJSONObject(i);
            }catch (JSONException e){
                return false;
            }
            try {
                curObject = curObject.getJSONObject("value");
            }catch (JSONException e){
                return false;
            }
            if(curObject.has("contact")){
                return true;
            }
        }
        return false;
    }

    public String getConverstationFeedback(String input) throws JSONException{
        setData(input);
        JSONObject curObject = actionObject;
        JSONArray curArray = curObject.getJSONArray("value");
        for(int i = 0; i < curArray.length(); i++) {
            curObject = curArray.getJSONObject(i);
            if (ifContainConversationTag(curObject)) {
                curObject = curObject.getJSONObject("value");
                curObject = curObject.getJSONObject("text");
                String result = curObject.getString("value");
                return result;
            }
        }
        return "Error";
    }

    public String getPhoneNumberFromActionObject() throws JSONException {
        JSONArray curArray = actionObject.getJSONArray("value");
        for(int i = 0; i < curArray.length(); i++){
            JSONObject curObject = curArray.getJSONObject(i);
            curObject = curObject.getJSONObject("value");
            if(curObject.has("contact")){
                curObject = curObject.getJSONObject("contact");
                curObject = curObject.getJSONObject("value");
                curObject = curObject.getJSONObject("phoneNumberId");
                String result = curObject.getString("value");
                return result;
            }
        }
        return "Error";
    }
}
