package com.example.lj.asrttstest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.lj.asrttstest.info.AppInfo;
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

public class CloudDataUploadActivity extends AppCompatActivity {

    public static final String GRAMMAR_DEPOT_FILE = "grammardepot.json";

    public static Word[] WAKEUP_WORDS = new Word[] { new Word("One Touch") };

    private static VariantContactManager _contactManager;
    private static SongManager _songManager;
    private static AppsManager _appManager;
    private static SimpleContentManager _wakeupManager;
    private static GrammarDepot _grammarDepot;
    private Context _context;

    private CloudServices                      _cloudServices;
    private GrammarDepot.GrammarUploadListener _listener;

    private EditText resultEditText;
    private Button startDataUploadButton;
    private Button clearDataUploadButton;
    private Button cancelButton;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_data_upload);
        resultEditText = (EditText)findViewById(R.id.cloudResultEditText);
        startDataUploadButton = (Button) findViewById(R.id.startCloudDataUploadButton);
        clearDataUploadButton = (Button) findViewById(R.id.clearCloudDataUploadButton);
        cancelButton = (Button) findViewById(R.id.cancelCloudDataUploadButton);
        startDataUploadButton.setEnabled(true);
        clearDataUploadButton.setEnabled(true);
        cancelButton.setEnabled(false);

        getContentAndSetup();

        // upload button
        startDataUploadButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (_grammarDepot == null) {
                    if (_grammarDepot == null) {
                        resultEditText.setText("Cannot open GrammarDepot.");
                        return;
                    }
                }
                startDataUploadButton.setEnabled(false);
                clearDataUploadButton.setEnabled(false);
                cancelButton.setEnabled(true);
                resultEditText.setText("uploading...");
                _grammarDepot.uploadServerWordLists(_cloudServices, _listener);
            }
        });

        // clear button
        clearDataUploadButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (_grammarDepot == null) {
                    if (_grammarDepot == null) {
                        resultEditText.setText("Cannot open GrammarDepot.");
                        return;
                    }
                }

                startDataUploadButton.setEnabled(false);
                clearDataUploadButton.setEnabled(false);
                cancelButton.setEnabled(true);
                resultEditText.setText("deleting...");

                _grammarDepot.clearServerWordLists(_cloudServices, _listener);
            }
        });

        // cancel button
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (_grammarDepot == null) {
                    if (_grammarDepot == null) {
                        resultEditText.setText("Cannot open GrammarDepot.");
                        return;
                    }
                }
                startDataUploadButton.setEnabled(true);
                clearDataUploadButton.setEnabled(true);
                cancelButton.setEnabled(false);
                _grammarDepot.cancelServerWordListsUpload();
                resultEditText.setText("canceled");
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (_cloudServices != null)
            _cloudServices.release();
        _cloudServices = null;
    }


    private void getContentAndSetup(){

        //
        Context _context = getApplicationContext();
        _contactManager=new VariantContactManager("contacts.lst",new FileManager(_context,"contacts"),_context);
        _songManager=new SongManager("songlist.lst",new FileManager(_context,"songlist"),_context);
        _appManager=new AppsManager("app.lst",new FileManager(_context,"applist"),_context);
        _wakeupManager=new SimpleContentManager("wakeup.lst",new FileManager(_context,"wakeuplist"),false,true,_context,WAKEUP_WORDS);
        _wakeupManager.forceRefresh();

        JSONObject json;
        try{
            json=JSONUtils.readFromStream(_context.getAssets().open(GRAMMAR_DEPOT_FILE));
            _grammarDepot=GrammarDepot.createFromJSON(json,new FileManager(_context,"grammardepot"),
                    new GrammarDepot.ContentManagerEntry("name",_contactManager),
                    new GrammarDepot.ContentManagerEntry("song",_songManager),
                    new GrammarDepot.ContentManagerEntry("wakeup",_wakeupManager));
//            _grammarDepot=GrammarDepot.createFromJSON(json,new FileManager(_context,"grammardepot"),
//                    new GrammarDepot.ContentManagerEntry("name",_contactManager));

        }
        catch(IOException e){
            Logger.error(this,"Error reading GrammarDepot file "+GRAMMAR_DEPOT_FILE+": "+e.toString());
        }
        catch(JSONException e){
            Logger.error(this,"Error loading GrammarDepot file "+GRAMMAR_DEPOT_FILE+": "+e.toString());
        }

        // Cloud services initialization
        _cloudServices = CloudServices.createCloudServices(CloudDataUploadActivity.this,
                new CloudConfig(
                        AppInfo.Host,
                        AppInfo.Port,
                        AppInfo.AppId,
                        AppInfo.AppKey,
                        AppInfo.IMEInumber,
                        AudioType.SPEEX_WB,
                        AudioType.SPEEX_WB));

        // listener
        _listener = new GrammarDepot.GrammarUploadListener()
        {
            @Override
            public void onComplete(String grammarId, int status) {
                startDataUploadButton.setEnabled(true);
                clearDataUploadButton.setEnabled(true);
                cancelButton.setEnabled(false);
                if (status == GrammarDepot.GrammarUploadStatus.SUCCESS ||
                        status == GrammarDepot.GrammarUploadStatus.NOTHING_TO_UPLOAD) {
                    resultEditText.setText("success");
                } else {
                    resultEditText.setText("error");
                }
            }
        };
    }
}
