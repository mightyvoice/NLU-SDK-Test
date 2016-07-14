package com.example.lj.asrttstest.text.dialog;

import android.util.Log;

import com.example.lj.asrttstest.dialog.BaseDialogManager;

import org.json.JSONObject;

/**
 * Created by lj on 16/7/13.
 */
public class TextDialogManager extends BaseTextDialogManager {

    public TextDialogManager(JSONObject input){
        processServerResponse(input);
        Log.d("sss", "##############################\n"+
                "Dialog Phase: " + getDialogPhase()+"\n"+
                "Domain: " + getDomain()+"\n"+
                "Intent: " + getIntent()+"\n"+
                "NlpsVersion: " + getNlpsVersion()+"\n"+
                "Status: " + getStatus()+"\n"+
                "System Text: " + getSystemText()+"\n"+
                "TTS Text: " + getTtsText() + "\n"+
                "##############################"
        );
    }
}
