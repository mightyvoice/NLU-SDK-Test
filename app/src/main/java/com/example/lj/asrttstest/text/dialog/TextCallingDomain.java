package com.example.lj.asrttstest.text.dialog;

import android.content.Context;

import com.example.lj.asrttstest.dialog.Domain;
import com.example.lj.asrttstest.info.AllContactInfo;
import com.example.lj.asrttstest.info.Global;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lj on 16/7/13.
 */
public class TextCallingDomain extends Domain {

    public String phoneNumber;

    public String phoneNumberID;

    public String contactName;

    public TextCallingDomain(Context _context, JSONArray _actionArray, String _ttsText) {
        super(_context, _actionArray, _ttsText);
        phoneNumber = "";
        phoneNumberID = "";
        contactName = "";
    }

    @Override
    protected void parseAmbiguityList() {
        Global.ambiguityList.clear();
        JSONArray curArray = actionArray;
        for(int i = 0; i < curArray.length(); i++){
            JSONObject curObject = curArray.optJSONObject(i);
            if(curObject.has("entries")){
                JSONArray entries = curObject.optJSONArray("entries");
                for(int j = 0; j < entries.length(); j++){
                    JSONObject entry = entries.optJSONObject(j);
                    entry = entry.optJSONObject("item");
                    if(entry.has("firstName")){
                        String name = entry.optString("firstName");
                        if(entry.has("lastName")){
                            name = name + " " + entry.optString("lastName");
                        }
                        Global.ambiguityList.add(name);
                        continue;
                    }
                    //if there are several phone types
                    if(entry.has("type")){
                        String phoneType = entry.optString("type");
                        Global.ambiguityList.add(phoneType);
                        continue;
                    }
                }
                break;
            }
        }
    }

    @Override
    public void parseAllUsefulInfo() {
        getContactInfo();
        parseAmbiguityList();
    }

    private void getContactInfo(){
        phoneNumber = "";
        JSONArray curArray = actionArray;
        for(int i = 0; i < curArray.length(); i++){
            JSONObject curObject = curArray.optJSONObject(i);
            if(curObject.has("contact")){
                curObject = curObject.optJSONObject("contact");
                phoneNumber = curObject.optString("phoneNumber");
                if(phoneNumber != null && !phoneNumber.equals("")){
                    return;
                }
                phoneNumberID = curObject.optString("phoneNumberId");
                if(phoneNumberID != null && !phoneNumberID.equals("")
                        &&AllContactInfo.allPhoneIDtoPhoneNum.containsKey(phoneNumberID)){
                    phoneNumber = AllContactInfo.allPhoneIDtoPhoneNum.get(phoneNumberID);
                    return;
                }
                else return;
            }
        }
    }
}
