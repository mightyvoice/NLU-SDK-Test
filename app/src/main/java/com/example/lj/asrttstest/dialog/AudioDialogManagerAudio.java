package com.example.lj.asrttstest.dialog;

/**
 * Created by lj on 16/6/15.
 */
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lj on 16/5/23.
 */
public class AudioDialogManagerAudio extends AudioBaseDialogManager {

    private String dialogPhaseDetail;

    public AudioDialogManagerAudio(JSONObject input){
        processServerResponse(input);
        parseDialogPhaseDetail();
        Log.d("haha", "##############################\n"+
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

    private void parseDialogPhaseDetail(){
        dialogPhaseDetail = "";
        JSONArray curArray = getActions();
        for(int i = 0; i < curArray.length(); i++){
            JSONObject curObject = curArray.optJSONObject(i);
            curObject = curObject.optJSONObject("value");
            if(curObject.has("key")){
                curObject = curObject.optJSONObject("key");
                dialogPhaseDetail = curObject.optString("value");
                return;
            }
        }
    }
}
