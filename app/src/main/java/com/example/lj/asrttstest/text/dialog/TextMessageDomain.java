package com.example.lj.asrttstest.text.dialog;

import android.content.Context;

import com.example.lj.asrttstest.dialog.DomainProc;
import com.example.lj.asrttstest.info.AllContactInfo;
import com.example.lj.asrttstest.info.Global;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lj on 16/7/13.
 */
public class TextMessageDomain extends DomainProc {
    public String phoneNumber = null;
    public String messageContent = null;
    private String phoneNumberID = null;

    public TextMessageDomain(Context _context, JSONArray _actionArray, String _ttsText) {
        super(_context, _actionArray, _ttsText);
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
                    if (phoneNumberID != null && AllContactInfo.allPhoneIDtoPhoneNum.containsKey(phoneNumberID)) {
                        phoneNumber = AllContactInfo.allPhoneIDtoPhoneNum.get(phoneNumberID);
                        break;
                    }
                    phoneNumber = curObject.optString("phoneNumber");
                    if (phoneNumber != null) {
                        break;
                    }
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
    protected void parseDialogPhaseDetail(){
        dialogPhaseDetail = "";
        JSONArray curArray = actionArray;
        for(int i = 0; i < curArray.length(); i++){
            JSONObject curObject = curArray.optJSONObject(i);
            if(curObject.has("key")){
                dialogPhaseDetail = curObject.optString("key");
                return;
            }
        }
    }

    @Override
    public void parseAmbiguityList() {
        Global.ambiguityList.clear();
        JSONArray curArray = actionArray;
        for(int i = 0; i < curArray.length(); i++){
            JSONObject curObject = curArray.optJSONObject(i);
            if(curObject.has("entries")){
                JSONArray entries = curObject.optJSONArray("entries");
                for(int j = 0; j < entries.length(); j++){
                    JSONObject entry = entries.optJSONObject(j);
                    entry = entry.optJSONObject("item");
                    //if there are several names
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
    public String getTtsText() {
        return super.getTtsText();
    }

    @Override
    public void parseAllUsefulInfo() {
        getPhoneNumberAndMessage();
        parseAmbiguityList();
        parseDialogPhaseDetail();
    }

    @Override
    public void resetActionArray(Context _context, JSONArray _actionArray, String _ttsText) {
        super.resetActionArray(_context, _actionArray, _ttsText);
    }

}
