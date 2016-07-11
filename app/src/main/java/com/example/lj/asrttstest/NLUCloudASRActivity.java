package com.example.lj.asrttstest;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.lj.asrttstest.asr.CloudTextRecognizer;
import com.example.lj.asrttstest.asr.text.HttpAsrClient;
import com.example.lj.asrttstest.dialog.JsonParser;
import com.example.lj.asrttstest.info.AppInfo;
import com.nuance.dragon.toolkit.audio.AudioChunk;
import com.nuance.dragon.toolkit.audio.AudioType;
import com.nuance.dragon.toolkit.audio.pipes.EndPointerPipe;
import com.nuance.dragon.toolkit.audio.pipes.SpeexEncoderPipe;
import com.nuance.dragon.toolkit.audio.sources.RecorderSource;
import com.nuance.dragon.toolkit.cloudservices.CloudConfig;
import com.nuance.dragon.toolkit.cloudservices.CloudServices;
import com.nuance.dragon.toolkit.cloudservices.DictionaryParam;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognitionError;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognitionResult;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognizer;
import com.nuance.dragon.toolkit.cloudservices.recognizer.RecogSpec;


/////differnt SDK different path
import com.nuance.dragon.toolkit.util.WorkerThread;
//import com.nuance.dragon.toolkit.oem.api.Logger;
//import com.nuance.dragon.toolkit.oem.api.WorkerThread;
import com.nuance.dragon.toolkit.data.Data;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class NLUCloudASRActivity extends AppCompatActivity {

    private final String TAG = "NLUCloud";

    private TextView resultEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_asr);
        resultEditText = (TextView) findViewById(R.id.cloudResultEditText);
        final Button startRecognitionButton = (Button) findViewById(R.id.startCloudRecognitionButton);
        final Button stopRecognitionButton = (Button) findViewById(R.id.stopCloudRecognitionButton);
        final Button cancelButton = (Button) findViewById(R.id.cancelCloudRecognitionButton);
        final Spinner resultModeSpinner = (Spinner) findViewById(R.id.resultModeSpinner);

        startRecognitionButton.setEnabled(true);
        stopRecognitionButton.setEnabled(false);
        cancelButton.setEnabled(false);
        resultModeSpinner.setEnabled(false);

        AppInfo.applicationSessionID = String.valueOf(UUID.randomUUID());

        startRecognitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecognitionButton.setEnabled(false);
//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        startTextRecognition("Call Teddy");
//                    }
//                });
//                thread.start();

                new TextRecognitionAsyncTask().execute();

//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

//                try{
//                    sendJsonToEmail(result[0].toString(4));
//                }catch (JSONException e){
//                    e.printStackTrace();
//                }
            }
        });

    }

    private class TextRecognitionAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            startTextRecognition("Call Teddy");
            return null;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    private void sendJsonToEmail(String result){
        Intent data=new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse("mailto:rpbloom@gmail.com"));
        data.putExtra(Intent.EXTRA_SUBJECT, "title");
        data.putExtra(Intent.EXTRA_TEXT, result);
        startActivity(data);
    }

    String intToString(int x){
        Integer X = x;
        return X.toString();
    }

    private JSONObject startTextRecognition(String _text) {
        Log.d(TAG, "startTextRecognition: " + _text);
        String appKey = "89e9b1b619dfc7d682237e701da7ada48316f675f73c5ecd23a41fc40782bc212ed3562022c23e75214dcb9010286c23afe100e00d4464873e004d1f4c8a5883";
        boolean useTLS = true;
        boolean requireTrustedRootCert = false;
        String topic = "nma_dm_main";
        String langCode = "eng-USA";
        boolean enableProfanityFiltering = false;
        boolean enableNLU = true;
        boolean batchMode = false;
        boolean resetUserProfile = false;
        String application = AppInfo.Application;
        String nluTextString = _text;

        HttpAsrClient asrClient = new HttpAsrClient(
                AppInfo.TextHost,
                AppInfo.Port,
                useTLS,
                AppInfo.AppId,
                appKey,
                topic,
                langCode );

        if( !requireTrustedRootCert )
            asrClient.disableTrustedRootCert();

        // Reset User Profile requests take precedence over any other conflicting command-line args
        if( resetUserProfile ) {
            asrClient.resetUserProfile();
        }

        if( batchMode )
            asrClient.enableBatchMode();

        if( enableProfanityFiltering )	// profanity filtering is disabled by default
            asrClient.enableProfanityFiltering();

        if( !enableNLU )	// NLU is enabled by default
            asrClient.disableNLU();

        if( application != null && !application.isEmpty() )	// default application is full.6.2 which likely won't work since customer-specific provisioning is necessary :)
            asrClient.setApplication(application);

        // Command-line args indicating NLU Text mode take precedence over args for Audio
        if( nluTextString != null ) {
            asrClient.enableTextNLU();
            asrClient.sendNluTextRequest(nluTextString);
            System.exit(0);
        }

        return asrClient.serverResponseJSON;
    }

}
