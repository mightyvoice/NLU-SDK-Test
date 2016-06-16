package com.example.lj.asrttstest.dialog;

import android.content.Context;
import android.util.Log;

import com.example.lj.asrttstest.info.AllContactInfo;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lj on 16/6/16.
 */
public class CallingDomainProc extends DomainProc {

    private static final String TAG = "CallingDomainProc";
    private String phoneNumber;

    public CallingDomainProc(Context _context, JSONArray _actionArray, String _ttsText) {
        super(_context, _actionArray, _ttsText);
    }

    @Override
    public void process() {
        phoneNumber = getPhoneNumber();
        Log.d(TAG, "phoneNum: "+phoneNumber);
        Log.d(TAG, "tts: "+getTtsText());
    }

    public String getPhoneNumber(){
        JSONArray curArray = actionArray;
        for(int i = 0; i < curArray.length(); i++){
            JSONObject curObject = curArray.optJSONObject(i);
            curObject = curObject.optJSONObject("value");
            if(curObject.has("contact")){
                curObject = curObject.optJSONObject("contact");
                curObject = curObject.optJSONObject("value");
                JSONObject tmp = curObject.optJSONObject("phoneNumber");
                String tmpResult = "";
                if(tmp != null){
                    tmpResult = tmp.optString("value");
                }
                if(!tmpResult.equals("")){
                    phoneNumber = tmpResult;
                    return phoneNumber;
                }
                curObject = curObject.optJSONObject("phoneNumberId");
                String result = "";
                if(curObject != null){
                    result = curObject.optString("value");
                }
                if(result.equals("")) return "";
                if(AllContactInfo.allPhoneIDtoPhoneNum.containsKey(result)){
                    phoneNumber = AllContactInfo.allPhoneIDtoPhoneNum.get(result);
                    return phoneNumber;
                }
                else return "";
            }
        }
        return "";
    }
}
