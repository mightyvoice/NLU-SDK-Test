package com.example.lj.asrttstest;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.telephony.TelephonyManager;

import com.example.lj.asrttstest.dialog.AmbiguityActivity;
import com.example.lj.asrttstest.dialog.CallingDomainProc;
import com.example.lj.asrttstest.dialog.DisambiguationActivity;
import com.example.lj.asrttstest.dialog.JsonParser;
import com.example.lj.asrttstest.dialog.MessageDomainProc;
import com.example.lj.asrttstest.info.AllContactInfo;
import com.example.lj.asrttstest.info.AppInfo;
import com.example.lj.asrttstest.info.ContactInfo;
import com.example.lj.asrttstest.info.Global;
import com.nuance.dragon.toolkit.audio.AudioChunk;
import com.nuance.dragon.toolkit.audio.AudioType;
import com.nuance.dragon.toolkit.audio.SpeechDetectionListener;
import com.nuance.dragon.toolkit.audio.pipes.EndPointerPipe;
import com.nuance.dragon.toolkit.audio.pipes.SpeexEncoderPipe;
import com.nuance.dragon.toolkit.audio.sources.MicrophoneRecorderSource;
import com.nuance.dragon.toolkit.audio.sources.RecorderSource;
import com.nuance.dragon.toolkit.cloudservices.CloudConfig;
import com.nuance.dragon.toolkit.cloudservices.CloudServices;
import com.nuance.dragon.toolkit.cloudservices.DataParam;
import com.nuance.dragon.toolkit.cloudservices.DictionaryParam;
import com.nuance.dragon.toolkit.cloudservices.Transaction;
import com.nuance.dragon.toolkit.cloudservices.TransactionError;
import com.nuance.dragon.toolkit.cloudservices.TransactionResult;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognitionError;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognitionResult;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognizer;
import com.nuance.dragon.toolkit.cloudservices.recognizer.RecogSpec;
import com.nuance.dragon.toolkit.data.Data;
import com.nuance.dragon.toolkit.data.Data.Sequence;
import com.nuance.dragon.toolkit.data.Data.Dictionary;
import com.nuance.dragon.toolkit.util.Logger;
import com.nuance.dragon.toolkit.util.WorkerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

public class NLUCloudASRActivity extends AppCompatActivity {

    private int DMA_GRAMMAR_VERSION = 1;
    private final String TAG = "NLUCloud";

    private CloudServices _cloudServices;
    private CloudRecognizer _cloudRecognizer;
    private RecorderSource<AudioChunk> _recorder;
    private SpeexEncoderPipe _speexPipe;
    private EndPointerPipe _endpointerPipe;

    private String _uniqueId;
    private Data.Sequence _grammarList, _checksumList;
    private WorkerThread _workerThread;
    private JsonParser jsonParser;
    private TTSService _ttsService;

    private EditText resultEditText;

    /** The default NCS NLU App Server command. Default is DRAGON_NLU_APPSERVER_CMD. */
    private static final String DEFAULT_APPSERVER_COMMAND = "DRAGON_NLU_APPSERVER_CMD";

    /** The default nlu profile name. Default value is REFERENCE_NCS. */
    private static final String DEFAULT_NLU_PROFILE = "REFERENCE_NCS";

    /** The default language to use. Set to eng-USA. Override available languages in the configuration file. */
    private static final String DEFAULT_LANGUAGE = "eng-USA";

    private Integer ambiguityChosenID = new Integer(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_asr);
        resultEditText = (EditText) findViewById(R.id.cloudResultEditText);
        final Button startRecognitionButton = (Button) findViewById(R.id.startCloudRecognitionButton);
        final Button stopRecognitionButton = (Button) findViewById(R.id.stopCloudRecognitionButton);
        final Button cancelButton = (Button) findViewById(R.id.cancelCloudRecognitionButton);
        final Spinner resultModeSpinner = (Spinner) findViewById(R.id.resultModeSpinner);

        startRecognitionButton.setEnabled(false);
        stopRecognitionButton.setEnabled(false);
        cancelButton.setEnabled(false);
        resultModeSpinner.setEnabled(false);

        //my code here for jason parser initialization
        _ttsService = new TTSService(getApplicationContext());
        AppInfo.applicationSessionID = String.valueOf(UUID.randomUUID());

        getAllContactList();
        try{
            getAllContactJsonArrayAndObject();
        }catch (JSONException e){
            e.printStackTrace();
        }

        // First, grammar set-up
        final Handler uiHandler = new Handler();

        _workerThread = new WorkerThread();
        _workerThread.start();
        _workerThread.getHandler().post(new Runnable() {
            @Override
            public void run() {
                readDragonData();
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Cloud services initialization
                        _cloudServices = CloudServices.createCloudServices(NLUCloudASRActivity.this,
                                new CloudConfig(AppInfo.Host, AppInfo.Port, AppInfo.AppId, AppInfo.AppKey,
                                        _uniqueId, AudioType.SPEEX_WB, AudioType.SPEEX_WB));
                        _cloudRecognizer = new CloudRecognizer(_cloudServices);
                        startRecognitionButton.setEnabled(true);
                    }
                });
            }
        });

        startRecognitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecognitionButton.setEnabled(false);
                stopRecognitionButton.setEnabled(true);
                cancelButton.setEnabled(true);

                _recorder = new MicrophoneRecorderSource(AudioType.PCM_16k);
                _speexPipe = new SpeexEncoderPipe();
                _endpointerPipe = new EndPointerPipe(new SpeechDetectionListener() {
                    @Override
                    public void onStartOfSpeech() {
                        resultEditText.setText("Start of Speech...");
                    }

                    @Override
                    public void onEndOfSpeech() {
                        resultEditText.setText("End of Speech...");

                        _cloudRecognizer.processResult();
                        stopRecording();
                    }
                });

                // Start recording and recognition
                _recorder.startRecording();
                _speexPipe.connectAudioSource(_recorder);
                _endpointerPipe.connectAudioSource(_speexPipe);

                _cloudRecognizer.startRecognition(createCustomSpec(), _endpointerPipe, new CloudRecognizer.Listener() {
                    private int resultCount = 0;

                    @Override
                    public void onResult(CloudRecognitionResult result) {
//                        Logger.debug(this, "NLU Cloud Recognition succeeded!");
                        stopRecording();
                        resultCount++;

                        startRecognitionButton.setEnabled(true);
                        stopRecognitionButton.setEnabled(false);
                        cancelButton.setEnabled(false);

                        if (resultCount == 1) {
                            ///////// 1st result is the transcription

                        } else {
                            ///////// 2nd result is the ADK/NLU action
                            String readableResult = "Error";
                            try {
                                readableResult = result.getDictionary().toJSON().toString(4);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
//                            Log.d("sss", readableResult);
//                            sendJsonToEmail(readableResult);
                            String feedback = "";
                            String phoneNumber = "";
                            jsonParser = new JsonParser(result.getDictionary().toJSON());
                            feedback = jsonParser.getTtsText();
                            _ttsService.performTTS(getApplicationContext(), feedback);
                            showResults(resultEditText, feedback);
                            if(jsonParser.getDomain().equals("calling")){
                                CallingDomainProc callingDomain
                                        = new CallingDomainProc(getApplicationContext(), jsonParser.getActions(), jsonParser.getTtsText());
                                callingDomain.process();
                                phoneNumber = callingDomain.phoneNumber;
                                Log.d("sss", phoneNumber);
                                if(jsonParser.getDialogPhase().equals("disambiguation")){
                                    Intent localIntent = new Intent(NLUCloudASRActivity.this, AmbiguityActivity.class);
                                    NLUCloudASRActivity.this.startActivity(localIntent);
                                }
                                if (jsonParser.getIntent().equals("call") && !phoneNumber.equals("") && ActivityCompat.checkSelfPermission(getApplicationContext(),
                                        Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                                    callIntent.setData(Uri.parse("tel:" + phoneNumber));
                                    startActivity(callIntent);
                                }
                            }
                            if(jsonParser.getDomain().equals("messaging")){
                                MessageDomainProc messageDomainProc
                                        = new MessageDomainProc(getApplicationContext(), jsonParser.getActions(), jsonParser.getTtsText());
                                messageDomainProc.process();
                                phoneNumber = messageDomainProc.getPhoneNumber();
                                Log.d("sss", phoneNumber);
                                Log.d("sss", messageDomainProc.getMessageContent());
                                if(jsonParser.getDialogPhase().equals("disambiguation")){
//                                    sendJsonToEmail(readableResult);
                                    Intent localIntent = new Intent(NLUCloudASRActivity.this, AmbiguityActivity.class);
                                    NLUCloudASRActivity.this.startActivity(localIntent);
                                }
                                if (jsonParser.getIntent().equals("send") &&
                                        !phoneNumber.equals("") &&
                                        ActivityCompat.checkSelfPermission(getApplicationContext(),
                                        Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(phoneNumber, null, messageDomainProc.getMessageContent(), null, null);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(CloudRecognitionError error)
                    {
                        Logger.debug(this, "NLU Cloud Recognition failed!");
                        stopRecording();

                        startRecognitionButton.setEnabled(true);
                        stopRecognitionButton.setEnabled(false);
                        cancelButton.setEnabled(false);

                        resultEditText.setText("ERROR");
                    }

                    @Override
                    public void onTransactionIdGenerated(String transactionId)
                    {
                        // TODO Auto-generated method stub
                    }
                });
            }
        });

        stopRecognitionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startRecognitionButton.setEnabled(false);
                stopRecognitionButton.setEnabled(false);
                cancelButton.setEnabled(true);

                _cloudRecognizer.processResult();
                stopRecording();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startRecognitionButton.setEnabled(false);
                stopRecognitionButton.setEnabled(false);
                cancelButton.setEnabled(false);
                _cloudRecognizer.cancel();
                stopRecording();
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (_cloudRecognizer != null)
            _cloudRecognizer.cancel();
        _cloudRecognizer = null;

        if (_recorder != null)
            _recorder.stopRecording();
        _recorder = null;

        if (_cloudServices != null)
            _cloudServices.release();
        _cloudServices = null;
    }

    private void sendJsonToEmail(String result){
        Intent data=new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse("mailto:rpbloom@gmail.com"));
        data.putExtra(Intent.EXTRA_SUBJECT, "这是标题");
        data.putExtra(Intent.EXTRA_TEXT, result);
        startActivity(data);
    }

    private void readDragonData()
    {
        try {
            Context dmaContext = createPackageContext("com.nuance.balerion", Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            File grammarFile = new File(dmaContext.getExternalFilesDir(null), "grammars.ncs");
            FileInputStream fis = new FileInputStream(grammarFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            int version = ois.readInt();
            if (version == DMA_GRAMMAR_VERSION)
            {
                _uniqueId = ois.readUTF();
                _grammarList = new Data.Sequence();
                int grammarListLen = ois.readInt();
                for (int grammarIdx = 0; grammarIdx < grammarListLen; grammarIdx++)
                    _grammarList.add((Data.Dictionary)ois.readObject());
                _checksumList = new Data.Sequence();
                int checksumListLen = ois.readInt();
                for (int checksumIdx = 0; checksumIdx < checksumListLen; checksumIdx++)
                    _checksumList.add((Data.Dictionary)ois.readObject());
            }
            ois.close();
            fis.close();
        } catch (Exception e) {
            Logger.error(this, "Error reading DMA data.", e);
        }
    }

    private void showResults(EditText textbox, String result)
    {
        Logger.debug(this, "" + result);
        if (result == null)
        {
            textbox.setText("no results");
            textbox.setVisibility(View.GONE);
            return;
        }

        textbox.setVisibility(View.VISIBLE);
        textbox.setText(result);
    }

    private void stopRecording()
    {
        if (_endpointerPipe  != null)
        {
            _endpointerPipe.disconnectAudioSource();
            _endpointerPipe = null;
        }

        if (_speexPipe  != null)
        {
            _speexPipe.disconnectAudioSource();
            _speexPipe = null;
        }

        if (_recorder  != null)
        {
            _recorder.stopRecording();
            _recorder = null;
        }
    }

    private RecogSpec createCustomSpec()
    {
        // Customize ASR command spec
        Data.Dictionary customSettings = new  Data.Dictionary();
        customSettings.put("application", "TCL");
        //original one from sample app;
//        customSettings.put("application_session_id", String.valueOf(UUID.randomUUID()));
        customSettings.put("dictation_language", "eng-USA");
        //original one from sample app
//        customSettings.put("dictation_type", "searchormessaging");
        customSettings.put("dictation_type", "nma_dm_main");

        //Ji Li's setting
        // without the followings it still works fine
        customSettings.put("uid", AppInfo.IMEInumber);
        customSettings.put("nmaid", AppInfo.AppId);
        customSettings.put("application_name",  getApplicationContext().getString(R.string.app_name));
        //enable or disable dialog, seems no effection
        customSettings.put("nlps_use_adk", 1);
        //should be the same during each dialog in an APP;
        customSettings.put("application_session_id", AppInfo.applicationSessionID);

        RecogSpec retRecogSpec = new RecogSpec("DRAGON_NLU_ASR_CMD", customSettings, "AUDIO_INFO")
        {
            @Override
            public List<DataParam> getDelayedParams()
            {
                // The creation of the REQUEST_INFO param is delayed to ensure
                // that it references the up-to-date grammar checksums
                Time now = new Time();
                now.setToNow();

                Data.Dictionary appServerData = new Data.Dictionary();
                appServerData.put("timezone", new Data.String(now.timezone));
                appServerData.put("time", new Data.String(now.format("%Y-%m-%dT%H:%M:%S%z")));
                appServerData.put("action_mode", new Data.String("default"));

                Data.Dictionary requestInfo = new Data.Dictionary();
                requestInfo.put("start", 0);
                requestInfo.put("end", 0);
                requestInfo.put("text", "");
                requestInfo.put("appserver_data", appServerData);

                // Pass grammar lists from DMA
                if (_checksumList != null && _checksumList.size() > 0)
                {
                    requestInfo.put("grammar_list", _checksumList);
                    appServerData.put("grammar_list", _checksumList);
                }

                List<DataParam> params = new ArrayList<DataParam>();
                params.add(new DictionaryParam("REQUEST_INFO", requestInfo));
                return params;
            }
        };
        return retRecogSpec;
    }


    private static String getTranscription(CloudRecognitionResult result)
    {
        Data.Dictionary appServerResults = result.getDictionary().getDictionary("appserver_results");
        Data.Dictionary transcriptionDict = appServerResults.getDictionary("payload").getSequence("actions").getDictionary(0);
        return transcriptionDict.getString("text").value;
    }

    private void getAllContactList(){
        AllContactInfo.allContactList = new ArrayList<ContactInfo>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        ContactInfo contact = new ContactInfo();
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                contact = new ContactInfo();

                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                String[] nameList = name.split(" ");
                contact.setFirstName(nameList[0]);
                contact.setLastName(nameList[1]);
                contact.setMobilePhone("0");

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String phoneType = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.TYPE));
                        contact.setMobilePhone(phoneNo);
                        phoneType = contact.phoneTypeTable.get(phoneType);
                        contact.phoneNumberTable.put(phoneType, phoneNo);
                    }
                    pCur.close();
                }
                AllContactInfo.allContactList.add(contact);
            }
        }
    }

    private void getAllContactJsonArrayAndObject() throws JSONException {
        int curID = -1;
        AllContactInfo.allContactJsonArray = new JSONArray();
        AllContactInfo.allPhoneIDtoPhoneNum = new Hashtable<String, String>();
        AllContactInfo.allContactJsonObject = new JSONObject();
        for (ContactInfo contact: AllContactInfo.allContactList){
            curID++;
            JSONObject tmp = new JSONObject();
            JSONObject all = new JSONObject();
            tmp.put("fn", contact.getFirstName());
            tmp.put("ln", contact.getLastName());
            JSONArray phoneTypeArray = new JSONArray();
            JSONArray phoneNumArray = new JSONArray();
            Set<String> types = contact.phoneNumberTable.keySet();
            int phoneID = -1; //starts from 0
            for(String phoneType: types){
                phoneTypeArray.put(phoneType);
                phoneID++;
                String phId = new Integer(curID).toString()+"_"+new Integer(phoneID).toString();
                phoneNumArray.put(phId);
                AllContactInfo.allPhoneIDtoPhoneNum.put(phId, contact.phoneNumberTable.get(phoneType));
            }
            tmp.put("phId", phoneNumArray);
            tmp.put("ph", phoneTypeArray);
            all.put("content", tmp);
            all.put("content_id", curID);
            AllContactInfo.allContactJsonArray.put(all);
        }
        AllContactInfo.allContactJsonObject.put("list", AllContactInfo.allContactJsonArray);
        resultEditText.setText(AllContactInfo.allContactJsonObject.toString(4));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d("ssss", "return from the other activity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ssss", "restart NLU");
        if (Global.ambiguityListChosenID > -1) {
            JSONObject data = new JSONObject();
            try {
                data.putOpt("message", "SLOTS:GENERIC_ORDER:1");
                DisambiguationActivity disambiguation = new DisambiguationActivity(getApplicationContext(), Global.ambiguityList);
                disambiguation.startAdkSubdialog(data, null);
                Global.ambiguityListChosenID = -1;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
