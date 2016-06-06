package com.example.lj.asrttstest;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nuance.dragon.toolkit.audio.AudioType;
import com.nuance.dragon.toolkit.cloudservices.CloudConfig;
import com.nuance.dragon.toolkit.cloudservices.CloudServices;
import com.nuance.dragon.toolkit.file.FileManager;
import com.nuance.dragon.toolkit.grammar.GrammarDepot;
import com.nuance.dragon.toolkit.grammar.Word;
import com.nuance.dragon.toolkit.grammar.content.AppsManager;
import com.nuance.dragon.toolkit.grammar.content.SimpleContentManager;
import com.nuance.dragon.toolkit.grammar.content.SongManager;
import com.nuance.dragon.toolkit.grammar.content.VariantContactManager;
import com.nuance.dragon.toolkit.util.JSONUtils;
import com.nuance.dragon.toolkit.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by lj on 16/6/3.
 */
public class CloudDataUpload{
    public static final String GRAMMAR_DEPOT_FILE = "grammardepot.json";

    public static Word[] WAKEUP_WORDS = new Word[] { new Word("One Touch") };

    private static VariantContactManager _contactManager;
    private static SongManager _songManager;
    private static AppsManager _appManager;
    private static SimpleContentManager _wakeupManager;
    private static GrammarDepot _grammarDepot;
    private Context _context;

    private CloudServices _cloudServices;
    private GrammarDepot.GrammarUploadListener _listener;
    private EditText resultBox;

    public CloudDataUpload(Context _context, final EditText _resultBox){
        resultBox = _resultBox;
        getContentAndSetup(_context);
    }

    public void startDataUpload(){
        if (_grammarDepot == null) {
            if (_grammarDepot == null) {
                resultBox.setText("Cannot open GrammarDepot.");
                return;
            }
        }
        resultBox.setText("uploading...");
        _grammarDepot.uploadServerWordLists(_cloudServices, _listener);
    }

    public void clearDataUploaded(){
        if (_grammarDepot == null) {
            if (_grammarDepot == null) {
                resultBox.setText("Cannot open GrammarDepot.");
                return;
            }
        }
        _grammarDepot.clearServerWordLists(_cloudServices, _listener);
        resultBox.setText("Clear successful");
    }

    public void close()
    {
        if (_cloudServices != null)
            _cloudServices.release();
        _cloudServices = null;
    }

    private void getContentAndSetup(Context _context){
        _contactManager=new VariantContactManager("contacts.lst",new FileManager(_context,"contacts"),_context);
        _songManager=new SongManager("songlist.lst",new FileManager(_context,"songlist"),_context);
        _appManager=new AppsManager("app.lst",new FileManager(_context,"applist"),_context);
        _wakeupManager=new SimpleContentManager("wakeup.lst",new FileManager(_context,"wakeuplist"),false,true,_context,WAKEUP_WORDS);
        _wakeupManager.forceRefresh();

        JSONObject json;
        try{
            json= JSONUtils.readFromStream(_context.getAssets().open(GRAMMAR_DEPOT_FILE));
//            _grammarDepot=GrammarDepot.createFromJSON(json,new FileManager(_context,"grammardepot"),
//                    new GrammarDepot.ContentManagerEntry("name",_contactManager),
//                    new GrammarDepot.ContentManagerEntry("song",_songManager),
//                    new GrammarDepot.ContentManagerEntry("wakeup",_wakeupManager));
            _grammarDepot=GrammarDepot.createFromJSON(json,new FileManager(_context,"grammardepot"),
                    new GrammarDepot.ContentManagerEntry("name",_contactManager));
        }
        catch(IOException e){
            Logger.error(this,"Error reading GrammarDepot file "+GRAMMAR_DEPOT_FILE+": "+e.toString());
        }
        catch(JSONException e){
            Logger.error(this,"Error loading GrammarDepot file "+GRAMMAR_DEPOT_FILE+": "+e.toString());
        }

        // Cloud services initialization
        _cloudServices = CloudServices.createCloudServices(_context,
                                                            new CloudConfig(
                                                                    AppInfo.Host,
                                                                    AppInfo.Port,
                                                                    AppInfo.AppId,
                                                                    AppInfo.AppKey,
                                                                    AudioType.SPEEX_WB,
                                                                    AudioType.SPEEX_WB));

        // listener
        _listener = new GrammarDepot.GrammarUploadListener()
        {
            @Override
            public void onComplete(String grammarId, int status) {
                if (status == GrammarDepot.GrammarUploadStatus.SUCCESS ||
                        status == GrammarDepot.GrammarUploadStatus.NOTHING_TO_UPLOAD) {
                    resultBox.setText("success");
                } else {
                    resultBox.setText("error");
                }
            }
        };
    }
}
