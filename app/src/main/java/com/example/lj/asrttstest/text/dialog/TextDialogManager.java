package com.example.lj.asrttstest.text.dialog;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lj on 16/7/13.
 */
public class TextDialogManager extends TextBaseDialogManager {

    private String dialogPhaseDetail;

    public TextDialogManager(JSONObject input){
        processServerResponse(input);
        parseDialogPhaseDetail();
        Log.d("sss", "##############################\n"+
                "Dialog Phase: " + getDialogPhase()+"\n"+
                "Domain: " + getDomain()+"\n"+
                "Intent: " + getIntent()+"\n"+
                "Status: " + getStatus()+"\n"+
                "System Text: " + getSystemText()+"\n"+
                "TTS Text: " + getTtsText() + "\n"+
                "##############################"
        );
    }

    public String getDialogPhaseDetail(){
        return dialogPhaseDetail;
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
