package com.example.lj.asrttstest.dialog;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by lj on 16/6/16.
 */
public abstract class DomainProc {
    protected JSONArray actionArray = null;
    protected String ttsText = null;
    protected Context context = null;
    protected String dialogPhaseDetail = null;

    public DomainProc(Context _context, JSONArray _actionArray, String _ttsText){
        context = _context;
        actionArray = _actionArray;
        ttsText = _ttsText;
    }

    public void resetActionArray(Context _context, JSONArray _actionArray, String _ttsText){
        context = _context;
        actionArray = _actionArray;
        ttsText = _ttsText;
    }

    public String getTtsText(){
        return ttsText;
    }

    public String getDialogPhaseDetail(){
        return dialogPhaseDetail;
    }

    //parse json to get all useful information
    protected abstract void parseAllUsefulInfo();

    //parse json to get ambiguity list
    protected abstract void parseAmbiguityList();

    //get the detail state of current dialog phase
    protected abstract void parseDialogPhaseDetail();



}
