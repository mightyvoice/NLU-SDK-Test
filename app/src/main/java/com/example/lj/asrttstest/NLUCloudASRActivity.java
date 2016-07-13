package com.example.lj.asrttstest;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lj.asrttstest.text.CloudTextRecognizer;
import com.example.lj.asrttstest.text.dialog.TextDialogManager;
import com.example.lj.asrttstest.text.http.HttpAsrClient;
import com.example.lj.asrttstest.info.AppInfo;


/////differnt SDK different path
//import com.nuance.dragon.toolkit.oem.api.Logger;
//import com.nuance.dragon.toolkit.oem.api.WorkerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class NLUCloudASRActivity extends AppCompatActivity {

    private final String TAG = "NLUCloud";

    private EditText inputEditText = null;

    private Button startRecognitionButton = null;

    private JSONObject serverResponseJSON = null;

    private String textForRecognition = null;

    private TextView resultTextView = null;

    private CloudTextRecognizer cloudTextRecognizer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nlu_cloud_asr);
        inputEditText = (EditText) findViewById(R.id.textForRecognitionView);
        startRecognitionButton = (Button) findViewById(R.id.startTextRecognitionButton);
        resultTextView = (TextView)findViewById(R.id.textRecognitionResultView);

        startRecognitionButton.setEnabled(true);
        inputEditText.setText("Call Teddy");
        resultTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        AppInfo.applicationSessionID = String.valueOf(UUID.randomUUID());

        startRecognitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    textForRecognition = inputEditText.getText().toString();
                    resultTextView.setText("");
                    new TextRecognitionAsyncTask().execute();
            }
        });

    }

    private class TextRecognitionAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            startRecognitionButton.setEnabled(false);
            inputEditText.clearComposingText();
            inputEditText.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
//            serverResponseJSON = startTextRecognition(textForRecognition);
            if(textForRecognition != null && !textForRecognition.equals(""));
            serverResponseJSON = new CloudTextRecognizer().startTextRecognition(textForRecognition);
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            startRecognitionButton.setEnabled(true);
            inputEditText.setEnabled(true);
            inputEditText.setText("");
            if(serverResponseJSON != null) {
                try {
                    resultTextView.setText(serverResponseJSON.toString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            TextDialogManager textDialogManager = new TextDialogManager(serverResponseJSON);
        }
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
        }

        return asrClient.serverResponseJSON;
    }

}
