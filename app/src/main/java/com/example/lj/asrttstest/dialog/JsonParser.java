package com.example.lj.asrttstest.dialog;

/**
 * Created by lj on 16/6/15.
 */
import android.util.Log;

import com.example.lj.asrttstest.dialog.BaseDialogManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by lj on 16/5/23.
 */
public class JsonParser extends BaseDialogManager{

    private JSONObject actionObject = null;
    public String textForTTS;
    public String textForDisplay;

    public JsonParser(JSONObject input){
        processServerResponse(input);
        Log.d("haha", "##############################\n"+
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
