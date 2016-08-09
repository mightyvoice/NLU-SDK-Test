package com.example.lj.asrttstest.text.dialog;

import android.content.Context;

import com.example.lj.asrttstest.dialog.Domain;
import com.example.lj.asrttstest.info.AllContactInfo;
import com.example.lj.asrttstest.info.Global;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by lj on 16/7/13.
 */
public class TextMessageDomain extends TextBaseDomain {
    public String phoneNumber = "";
    public String messageContent = "";
    public String phoneNumberID = "";
    public ArrayList<String> userClickCommands = new ArrayList<>();

    public TextMessageDomain(JSONArray _actionArray) {
        super(_actionArray);
    }

    private void getPhoneNumberAndMessage() {
        JSONArray curArray = actionArray;
        for (int i = 0; i < curArray.length(); i++) {
            JSONObject curObject = curArray.optJSONObject(i);
            if (curObject.has("recipients")) {
                JSONArray tmpArray = curObject.optJSONArray("recipients");
                for (int j = 0; j < tmpArray.length(); j++) {
                    curObject = tmpArray.optJSONObject(i);
                    phoneNumberID = curObject.optString("phoneNumberId");
                    phoneNumber = curObject.optString("phoneNumber");
                    break;
                }
            }
            curObject = curArray.optJSONObject(i);
            if (curObject.has("body")) {
                messageContent = curObject.optString("body");
                break;
            }
        }
    }

    @Override
    public void parseAmbiguityList() {
        ambiguityList = new ArrayList<String>();
        JSONArray curArray = actionArray;
        for(int i = 0; i < curArray.length(); i++){
            JSONObject curObject = curArray.optJSONObject(i);
            if(curObject.has("entries")){
                JSONArray entries = curObject.optJSONArray("entries");
                for(int j = 0; j < entries.length(); j++){
                    JSONObject entry = entries.optJSONObject(j);
                    if(entry.has("action")){
                        userClickCommands.add(entry.optString("action"));
                    }
                    entry = entry.optJSONObject("item");
                    //if there are several names
                    if(entry.has("firstName")){
                        String name = entry.optString("firstName");
                        if(entry.has("lastName")){
                            name = name + " " + entry.optString("lastName");
                        }
                        ambiguityList.add(name);
                        continue;
                    }
                    //if there are several phone types
                    if(entry.has("type")){
                        String phoneType = entry.optString("type");
                        ambiguityList.add(phoneType);
                        continue;
                    }
                }
                break;
            }
        }
    }

    @Override
    public String getTtsText() {
        return super.getTtsText();
    }

    @Override
    public void parseAllUsefulInfo() {
        getPhoneNumberAndMessage();
        parseAmbiguityList();
    }

    public String getMessageContent(){
        return messageContent;
    }
}
