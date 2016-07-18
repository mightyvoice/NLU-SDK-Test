package com.example.lj.asrttstest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lj.asrttstest.info.Global;
import com.example.lj.asrttstest.text.CloudTextRecognizer;
import com.example.lj.asrttstest.text.dialog.TextCallingDomain;
import com.example.lj.asrttstest.text.dialog.TextDialogManager;
import com.example.lj.asrttstest.text.dialog.TextMessageDomain;
import com.example.lj.asrttstest.info.AppInfo;


/////differnt SDK different path
//import com.nuance.dragon.toolkit.oem.api.Logger;
//import com.nuance.dragon.toolkit.oem.api.WorkerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class NLUCloudTextActivity extends AppCompatActivity {

    private final String TAG = "NLUCloud";

    private EditText inputEditText = null;

    private Button startRecognitionButton = null;

    private JSONObject serverResponseJSON = null;

    private String textForRecognition = null;

    private TextView resultTextView = null;

    private CloudTextRecognizer cloudTextRecognizer = null;

    private TTSService ttsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nlu_cloud_text);
        inputEditText = (EditText) findViewById(R.id.textForRecognitionView);
        startRecognitionButton = (Button) findViewById(R.id.startTextRecognitionButton);
        resultTextView = (TextView)findViewById(R.id.textRecognitionResultView);

        startRecognitionButton.setEnabled(true);
        inputEditText.setText("Call Teddy");
        resultTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        AppInfo.applicationSessionID = String.valueOf(UUID.randomUUID());

        ttsService = new TTSService(this);

        startRecognitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    textForRecognition = inputEditText.getText().toString();
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
                    onGetDataResult(serverResponseJSON);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

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

    /**
     * Process the returned json each time
     * @param result the result
     */
    private void onGetDataResult(JSONObject result) {
        String feedback = "";
        String phoneNumber = "";
        TextDialogManager textDialogManager = new TextDialogManager(result);
        feedback = textDialogManager.getTtsText();
        ttsService.performTTS(getApplicationContext(), feedback);

        String curIntent = textDialogManager.getIntent();
        String curDialogPhase = textDialogManager.getDialogPhase();
        String curDomain = textDialogManager.getDomain();

        //calling domain process
        if(curDomain.equals("calling")){
            TextCallingDomain callingDomain
                    = new TextCallingDomain(getApplicationContext(), textDialogManager.getActions(), textDialogManager.getTtsText());
            callingDomain.parseAllUsefulInfo();
            phoneNumber = callingDomain.phoneNumber;

            //if there is ambiguty
            if(curDialogPhase.equals("disambiguation")){
//                ambiguityListAdapter.notifyDataSetChanged();
                Log.d("sss", Global.ambiguityList.toString());
            }

            //if it is ready to call
            if (curIntent.equals("call") && !phoneNumber.equals("")
                    && ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            }
        }

//        //message domain process
        if(curDomain.equals("messaging")) {
            TextMessageDomain messageDomainProc
                    = new TextMessageDomain(getApplicationContext(), textDialogManager.getActions(), textDialogManager.getTtsText());
            messageDomainProc.parseAllUsefulInfo();
            phoneNumber = messageDomainProc.phoneNumber;

            //if there is ambiguity
            if (curDialogPhase.equals("disambiguation")) {
//                ambiguityListAdapter.notifyDataSetChanged();
            }

            if (curIntent.equals("display")) {
                Global.ambiguityList.clear();
//                Log.d("sss", "message: "+messageDomainProc.getMessageContent());
//                Global.ambiguityList.add(messageDomainProc.getMessageContent());
//                ambiguityListAdapter.notifyDataSetChanged();
            }

            //if it is ready to send the message
            if (curIntent.equals("send") && !phoneNumber.equals("") &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                SmsManager smsManager = SmsManager.getDefault();
//                smsManager.sendTextMessage(phoneNumber, null, messageDomainProc.getMessageContent(), null, null);
            }
        }
    }
}
