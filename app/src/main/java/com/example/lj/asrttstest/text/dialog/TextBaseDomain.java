package com.example.lj.asrttstest.text.dialog;

import android.content.Context;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by lj on 16/8/8.
 */
public abstract class TextBaseDomain {

    protected JSONArray actionArray = null;
    protected String ttsText = null;
    protected String dialogPhaseDetail = null;
    protected Context contex;
    protected ArrayList<String> ambiguityList;

    public TextBaseDomain(JSONArray _actionArray){
        actionArray = _actionArray;
    }

    public void resetActionArray(JSONArray _actionArray, String _ttsText){
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

}
