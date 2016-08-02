package com.example.lj.asrttstest.text.dialog;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lj on 16/7/13.
 */
public class TextDialogManager extends TextBaseDialogManager {

    private String dialogPhaseDetail;
    private TextServerResponse textServerResponse;

    public TextDialogManager(JSONObject input){
        textServerResponse = null;
        processServerResponse(input);
        parseDialogPhaseDetail();
        textServerResponse = parseTextServerResponse(input);
        Log.d("sss", "##############################\n"+
                "Domain: " + textServerResponse.getDomain()+"\n"+
                "Dialog Phase: " + textServerResponse.getDialogPhase()+"\n"+
                "Dialog Phase Detail: " + textServerResponse.getDialogPhaseDetail()+"\n"+
                "Intent: " + textServerResponse.getIntent()+"\n"+
                "TTS Text: " + textServerResponse.getTtsText() + "\n"+
                "Phone ID: " + textServerResponse.getPhoneID() + "\n"+
                "Phone Number: " + textServerResponse.getPhoneNumber() + "\n"+
                "Ambiguity List: " + textServerResponse.getAmbiguityList().toString() + "\n"+
                "##############################"
        );
    }

    public TextServerResponse getTextServerResponse(){
        return textServerResponse;
    }

    public TextServerResponse parseTextServerResponse(JSONObject input){
        processServerResponse(input);
        parseDialogPhaseDetail();
        textServerResponse = new TextServerResponse();

        textServerResponse.setDomain(getDomain());
        textServerResponse.setIntent(getIntent());
        textServerResponse.setDialogPhase(getDialogPhase());
        textServerResponse.setDialogPhaseDetail(dialogPhaseDetail);
        textServerResponse.setStatus(getStatus());
        textServerResponse.setTtsText(getTtsText());
        textServerResponse.setSystemText(getSystemText());

        if(getDomain() != null && getDomain().equals("calling")){
            TextCallingDomain callingDomain
                    = new TextCallingDomain(getActions());
            callingDomain.parseAllUsefulInfo();
            textServerResponse.setPhoneID(callingDomain.phoneNumberID);
            textServerResponse.setPhoneNumber(callingDomain.phoneNumber);
            textServerResponse.setAmbiguityList(callingDomain.ambiguityList);
        }

        if(getDomain() != null && getDomain().equals("messaging")){
            TextMessageDomain messageDomain
                    = new TextMessageDomain(getActions());
            messageDomain.parseAllUsefulInfo();
            textServerResponse.setPhoneID(messageDomain.phoneNumberID);
            textServerResponse.setPhoneNumber(messageDomain.phoneNumber);
            textServerResponse.setMessageContent(messageDomain.messageContent);
            textServerResponse.setAmbiguityList(messageDomain.ambiguityList);
        }

        return textServerResponse;
    }

    private void parseDialogPhaseDetail() {
        dialogPhaseDetail = "";
        JSONArray curArray = getActions();
        for (int i = 0; i < curArray.length(); i++) {
            JSONObject curObject = curArray.optJSONObject(i);
            if (curObject.has("key")) {
                dialogPhaseDetail = curObject.optString("key");
                return;
            }
        }
    }
}
