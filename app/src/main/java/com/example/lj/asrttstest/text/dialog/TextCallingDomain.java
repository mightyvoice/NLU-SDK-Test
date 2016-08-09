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
public class TextCallingDomain extends TextBaseDomain {

    public String phoneNumber = "";

    public String phoneNumberID = "";

    public ArrayList<String> userClickCommands = new ArrayList<>();

    public TextCallingDomain(JSONArray _actionArray) {
        super(_actionArray);
    }

    @Override
    public void parseAllUsefulInfo() {
        getContactInfo();
        parseAmbiguityList();
    }

    @Override
    protected void parseAmbiguityList() {
        JSONArray curArray = actionArray;
        ambiguityList = new ArrayList<String>();
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

    private void getContactInfo(){
        JSONArray curArray = actionArray;
        for(int i = 0; i < curArray.length(); i++){
            JSONObject curObject = curArray.optJSONObject(i);
            if(curObject.has("contact")){
                curObject = curObject.optJSONObject("contact");
                phoneNumber = curObject.optString("phoneNumber");
                phoneNumberID = curObject.optString("phoneNumberId");
                return;
            }
        }
    }
}
