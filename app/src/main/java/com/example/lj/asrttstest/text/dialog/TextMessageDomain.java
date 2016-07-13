package com.example.lj.asrttstest.text.dialog;

import android.content.Context;

import com.example.lj.asrttstest.dialog.DomainProc;

import org.json.JSONArray;

/**
 * Created by lj on 16/7/13.
 */
public class TextMessageDomain extends DomainProc {

    public TextMessageDomain(Context _context, JSONArray _actionArray, String _ttsText) {
        super(_context, _actionArray, _ttsText);
    }

    @Override
    public void getAmbiguityList() {

    }

    @Override
    public String getTtsText() {
        return super.getTtsText();
    }

    @Override
    public void process() {

    }

    @Override
    public void resetActionArray(Context _context, JSONArray _actionArray, String _ttsText) {
        super.resetActionArray(_context, _actionArray, _ttsText);
    }

}
