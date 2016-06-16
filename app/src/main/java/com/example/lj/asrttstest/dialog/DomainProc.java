package com.example.lj.asrttstest.dialog;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lj on 16/6/16.
 */
public abstract class DomainProc {
    protected JSONArray actionArray = null;
    protected String ttsText = null;
    protected Context context = null;

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

    public abstract void process();
}
