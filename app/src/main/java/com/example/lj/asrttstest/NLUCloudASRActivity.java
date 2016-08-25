package com.example.lj.asrttstest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.lj.asrttstest.asr.CloudTextRecognizer;
import com.example.lj.asrttstest.dialog.CallingDomainProc;
import com.example.lj.asrttstest.dialog.JsonParser;
import com.example.lj.asrttstest.dialog.MessageDomainProc;
import com.example.lj.asrttstest.info.AppInfo;
import com.example.lj.asrttstest.info.Global;
import com.example.lj.asrttstest.upload.Grammar;
import com.nuance.dragon.toolkit.audio.AudioChunk;
import com.nuance.dragon.toolkit.audio.AudioEnergyListener;
import com.nuance.dragon.toolkit.audio.AudioType;
import com.nuance.dragon.toolkit.audio.SpeechDetectionListener;
import com.nuance.dragon.toolkit.audio.pipes.AudioEnergyCalculatorPipe;
import com.nuance.dragon.toolkit.audio.pipes.BufferingDuplicatorPipe;
import com.nuance.dragon.toolkit.audio.pipes.DuplicatorPipe;
import com.nuance.dragon.toolkit.audio.pipes.EndPointerPipe;
import com.nuance.dragon.toolkit.audio.pipes.NoiseSuppressionPipe;
import com.nuance.dragon.toolkit.audio.pipes.NormalizerPipe;
import com.nuance.dragon.toolkit.audio.pipes.OpusEncoderPipe;
import com.nuance.dragon.toolkit.audio.pipes.PrecisionClearBufferingPipe;
import com.nuance.dragon.toolkit.audio.pipes.SpeexEncoderPipe;
import com.nuance.dragon.toolkit.audio.pipes.TimeoutPipe;
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
import com.nuance.dragon.toolkit.elvis.ElvisConfig;
import com.nuance.dragon.toolkit.elvis.ElvisRecognizer;
import com.nuance.dragon.toolkit.elvis.EndpointingParam;


/////differnt SDK different path
import com.nuance.dragon.toolkit.util.Logger;
import com.nuance.dragon.toolkit.util.WorkerThread;
//import com.nuance.dragon.toolkit.oem.api.Logger;
//import com.nuance.dragon.toolkit.oem.api.WorkerThread;
import com.nuance.dragon.toolkit.data.Data;
import com.nuance.dragon.toolkit.data.Data.Dictionary;
//import com.nuance.dragon.toolkit.core.data.Data;
//import com.nuance.dragon.toolkit.core.data.Data.Dictionary;

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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class NLUCloudASRActivity extends AppCompatActivity {

    private int DMA_GRAMMAR_VERSION = 1;
    private final String TAG = "NLUCloud";

    private CloudServices mCloudServices = null;
    private CloudRecognizer mCloudRecognizer = null;
    private RecorderSource<AudioChunk> _recorder;
    private SpeexEncoderPipe _speexPipe;
    private EndPointerPipe _endpointerPipe;
    private String _uniqueId;
    private Data.Sequence _grammarList, _checksumList;
    private WorkerThread _workerThread;

    //Ji Li's parameters
    private JsonParser jsonParser;
    private TTSService ttsService;

    private TextView resultEditText;

    private ListView ambiguityListView;
    private ArrayAdapter<String> ambiguityListAdapter;

    /** for mix NLU **/
    private CloudServices mMixCloudServices = null;
    private CloudRecognizer mMixCloudRecognizer = null;

    /** A flag identifying if nlu is enabled for a transaction. */
    private boolean mNluEnabled = true;

    /** The default language to use. Set to eng-USA. Override available languages in the configuration file. */
    private static final String DEFAULT_LANGUAGE = "eng-USA";

    /** An instance of the selected language to use. */
    private String mLanguage = null;

    /** The default nlu profile name. Default value is REFERENCE_NCS. */
    private static final String DEFAULT_NLU_PROFILE = "REFERENCE_NCS";

    /** The default dictation type. Default value is nma_dm_main. */
    private static final String DEFAULT_DICTATION_TYPE = "nma_dm_main";

    private static final String MIX_CONTEXT = "M2807_A1443";
    /** The nlu profile. */
    private String mNluProfile = null;

    /** The default NCS ASR command. Default value is NMDP_ASR_CMD. */
    private static final String DEFAULT_ASR_COMMAND = "NMDP_ASR_CMD";

    /** The default NCS ASR+NLU command. Default value is DRAGON_NLU_ASR_CMD. */
    private static final String DEFAULT_NLU_ASR_COMMAND = "DRAGON_NLU_ASR_CMD";

    /** The default NCS NLU App Server command. Default is DRAGON_NLU_APPSERVER_CMD. */
    private static final String DEFAULT_APPSERVER_COMMAND = "DRAGON_NLU_APPSERVER_CMD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_asr);
        resultEditText = (TextView) findViewById(R.id.cloudResultEditText);
        final Button startRecognitionButton = (Button) findViewById(R.id.startCloudRecognitionButton);
        final Button stopRecognitionButton = (Button) findViewById(R.id.stopCloudRecognitionButton);
        final Button cancelButton = (Button) findViewById(R.id.cancelCloudRecognitionButton);
        final Spinner resultModeSpinner = (Spinner) findViewById(R.id.resultModeSpinner);

        startRecognitionButton.setEnabled(false);
        stopRecognitionButton.setEnabled(false);
        cancelButton.setEnabled(false);
        resultModeSpinner.setEnabled(false);

        //my code here for jason parser initialization
        ttsService = new TTSService(getApplicationContext());
        AppInfo.applicationSessionID = String.valueOf(UUID.randomUUID());
        AppInfo.mixApplicationSessionID = String.valueOf(UUID.randomUUID());

        Global.ambiguityList = new ArrayList<>();
        ambiguityListView = (ListView) findViewById(R.id.ambiguityListView);
        ambiguityListAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, Global.ambiguityList);
        ambiguityListView.setAdapter(ambiguityListAdapter);
        ambiguityListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //start from 1
                Global.ambiguityList.clear();
                ambiguityListAdapter.notifyDataSetChanged();
                Global.ambiguityListChosenID = (int)id+1;
                Log.d("sss", "Click "+new Integer((int)id).toString());
                startAdkSubdialog(null);
            }
        });

        // First, grammar set-up
        final Handler uiHandler = new Handler();

        _workerThread = new WorkerThread();
        _workerThread.start();
        _workerThread.getHandler().post(new Runnable() {
            @Override
            public void run() {
//                readDragonData();
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Cloud services initialization
//                        mCloudServices = CloudServices.createCloudServices(NLUCloudASRActivity.this,
//                                new CloudConfig(AppInfo.Host, AppInfo.Port, AppInfo.AppId, AppInfo.AppKey,
//                                        _uniqueId, AudioType.SPEEX_WB, AudioType.SPEEX_WB));
//                        mCloudRecognizer = new CloudRecognizer(mCloudServices);
                        startRecognitionButton.setEnabled(true);
                        initCloudRecognition();
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

                Global.ambiguityList.clear();
                ambiguityListAdapter.notifyDataSetChanged();

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
//                        mCloudRecognizer.processResult();
                        stopRecording();
                    }
                });
//
//                // Start recording and recognition
                _recorder.startRecording();
                _speexPipe.connectAudioSource(_recorder);
                _endpointerPipe.connectAudioSource(_speexPipe);


//                mCloudRecognizer.startRecognition(createCustomSpec(), _endpointerPipe, new CloudRecognizer.Listener() {
//                    private int resultCount = 0;
//
//                    @Override
//                    public void onResult(CloudRecognitionResult result) {
////                        Logger.debug(this, "NLU Cloud Recognition succeeded!");
////                        stopRecording();
//                        resultCount++;
////                        startRecognitionButton.setEnabled(true);
////                        stopRecognitionButton.setEnabled(false);
////                        cancelButton.setEnabled(false);
//
//                        if (resultCount == 1) {
//                            ///////// 1st result is the transcription
//
//                        } else {
//                            ///////// 2nd result is the ADK/NLU action
//                            String readableResult = "Error";
//                            try {
//                                readableResult = result.getDictionary().toJSON().toString(4);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
////                            Log.d("sss", readableResult);
////                            sendJsonToEmail(readableResult);
//                            onGetDataResult(result.getDictionary().toJSON());
//
//                        }
//                    }
//
//                    @Override
//                    public void onError(CloudRecognitionError error)
//                    {
//                        Logger.debug(this, "NLU Cloud Recognition failed!");
//                        stopRecording();
//
//                        startRecognitionButton.setEnabled(true);
//                        stopRecognitionButton.setEnabled(false);
//                        cancelButton.setEnabled(false);
//
//                        resultEditText.setText("ERROR");
//                    }
//
//                    @Override
//                    public void onTransactionIdGenerated(String transactionId)
//                    {
//                    }
//                });

                mMixCloudRecognizer.startRecognition(createMixRecogSpec(DEFAULT_DICTATION_TYPE,
                        getLanguage(), MIX_CONTEXT, null),
                        _endpointerPipe, new CloudRecognizer.Listener() {
                    private int resultCount = 0;
                    @Override
                    public void onResult(CloudRecognitionResult result) {
                        Log.d("sss", "mix NLU Cloud Recognition succeeded!");
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
                            Log.d("sss", readableResult);
                            sendJsonToEmail(readableResult);
//                            onGetDataResult(result.getDictionary().toJSON());
                        }
                    }

                    @Override
                    public void onError(CloudRecognitionError error)
                    {
                        Logger.debug(this, "NLU Cloud Recognition failed!");
                        Log.d("sss", error.toString());
                        stopRecording();

                        startRecognitionButton.setEnabled(true);
                        stopRecognitionButton.setEnabled(false);
                        cancelButton.setEnabled(false);

                        resultEditText.setText("ERROR");
                    }

                    @Override
                    public void onTransactionIdGenerated(String transactionId) {
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

                mCloudRecognizer.processResult();
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
                mCloudRecognizer.cancel();
                stopRecording();
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (_recorder != null)
            _recorder.stopRecording();
        _recorder = null;
        releaseCloudRecognition();
        releaseCloudServices();
    }

    private void sendJsonToEmail(String result){
        Intent data=new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse("mailto:rpbloom@gmail.com"));
        data.putExtra(Intent.EXTRA_SUBJECT, "这是标题");
        data.putExtra(Intent.EXTRA_TEXT, result);
        startActivity(data);
    }

    private void showResults(TextView textbox, String result)
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

    /**
     * Start adk subdialog.
     *
     * @param mAdkSubdialogListener the listener
     */
    public void startAdkSubdialog(Transaction.Listener mAdkSubdialogListener) {
//        initCloudServices();
        try {
            Log.e(TAG, "Inside startAdkSubdialog()");
            Dictionary settings = this.createSubdialogCommandSettings(DEFAULT_APPSERVER_COMMAND, "nma_dm_main", getLanguage());
            Dictionary root = new Dictionary();
            Dictionary appserver_data = new Dictionary();

            appserver_data.put("hour_format", "LOCALE");
            appserver_data.put("timezone", getTimeZone());
            appserver_data.put("time", getCurrentTime());
            //appserver_data.put("return_nlu_results", 1);
            appserver_data.put("action_mode", "default");
            appserver_data.put("application", getNluProfile());
            //appserver_data.put("dialog_seq_id", 2);

            //Ji's code
            String choose = "SLOTS:GENERIC_ORDER:"+Global.ambiguityListChosenID.toString();
            appserver_data.put("message", choose);

            root.put("appserver_data", appserver_data);
            root.put("nlsml_results", 1);
            root.put("dg_result_as_json", 1);
            root.put("use_dg_domain", 0);
            root.put("type", "conversation");

            root.put("start", 0);
            root.put("end", 0);
            root.put("text", "");

//            Log.d(TAG, "ADK Subdialog Request Data: " + root.toString());
            DictionaryParam RequestInfo = new DictionaryParam("REQUEST_INFO", root);

            Log.d(TAG, "Creating Start ADK Subdialog Transaction...");

            Transaction dut;

            if( mAdkSubdialogListener != null )
                dut = new Transaction(DEFAULT_APPSERVER_COMMAND, settings, mAdkSubdialogListener, 3000, true);
            else
                dut = new Transaction(DEFAULT_APPSERVER_COMMAND, settings, new Transaction.Listener() {

                    @Override
                    public void onTransactionStarted(Transaction arg0) {
                        Log.d(TAG, "Transaction Started...");
                    }

                    @Override
                    public void onTransactionProcessingStarted(Transaction transaction) {
                    }

                    @Override
                    public void onTransactionResult(Transaction arg0, TransactionResult arg1,
                                                    boolean arg2) {
                        Log.d(TAG, "Transaction Completed...");
                        JSONObject results = arg1.getContents().toJSON();
                        onGetDataResult(results);
                    }

                    @Override
                    public void onTransactionError(Transaction arg0, TransactionError arg1) {
                        Log.d(TAG, "Transaction Error...");
//                        onGetDataError(arg0, arg1.toJSON());
                    }

                    @Override
                    public void onTransactionIdGenerated(String s) {
                    }

                }, 3000, true);

//            Log.d(TAG, "settings: " + settings.toString());
//            Log.d(TAG, "requestInfo: " + RequestInfo.toString());

//            this.mCloudServices.addTransaction(dut, 1);
            mCloudServices.addTransaction(dut, 1);
            dut.addParam(RequestInfo);
            dut.finish();

        } catch( Exception e) {
            //Log.e(TAG, e.getMessage());
            Log.e(TAG, "Exception thrown in RecognizerCloudActivity.startAdkSubdialog()");
            e.printStackTrace();
        }
    }

    /**
     * Creates the command settings.
     *
     * @param commandName the command name
     * @param type the dictation type
     * @param language the dictation language
     * @return the command settings dictionary
     */
    private Dictionary createSubdialogCommandSettings(String commandName, String type, String language) {
        Data.Dictionary settings = new Data.Dictionary();

        settings.put("command", commandName);
        settings.put("nmaid", AppInfo.AppId);
        settings.put("dictation_language", language);
        settings.put("carrier", "ATT");
//        settings.put("dictation_type", type);
        settings.put("application_name",  this.getString(R.string.app_name));
        settings.put("application_session_id", AppInfo.applicationSessionID);
        settings.put("application_state_id", "45");
//        settings.put("location", getLastKnownLocation());
        settings.put("utterance_number", "5");
        settings.put("audio_source", "SpeakerAndMicrophone");

        //Ji Li's code
        settings.put("uid", AppInfo.IMEInumber);
        settings.put("nlps_use_adk", 1);
        settings.put("application", "TCL");
        settings.put("dictation_type", "gens_dm_main");

        return settings;
    }


    private String getNluProfile() {
        if( mNluProfile != null )
            return mNluProfile;

        return DEFAULT_NLU_PROFILE;
    }

    /**
     * Process the response json according to its domain
     *
     * @param result the result
     */
    private void onGetDataResult(JSONObject result) {
//        try {
//            sendJsonToEmail(result.toString(4));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        String feedback = "";
        String phoneNumber = "";
        jsonParser = new JsonParser(result);
        feedback = jsonParser.getTtsText();
        ttsService.performTTS(getApplicationContext(), feedback);
        showResults(resultEditText, feedback);

        //calling domain process
        if(jsonParser.getDomain().equals("calling")){
            CallingDomainProc callingDomain
                    = new CallingDomainProc(getApplicationContext(), jsonParser.getActions(), jsonParser.getTtsText());
            callingDomain.process();
            phoneNumber = callingDomain.phoneNumber;
            Log.d("sss", phoneNumber);

            //if there is ambiguty
            if(jsonParser.getDialogPhase().equals("disambiguation")){
                ambiguityListAdapter.notifyDataSetChanged();
            }

            //if it is ready to call
            if (jsonParser.getIntent().equals("call") && !phoneNumber.equals("") && ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            }
        }

        //message domain process
        if(jsonParser.getDomain().equals("messaging")) {
            MessageDomainProc messageDomainProc
                    = new MessageDomainProc(getApplicationContext(), jsonParser.getActions(), jsonParser.getTtsText());
            messageDomainProc.process();
            phoneNumber = messageDomainProc.getPhoneNumber();
            Log.d("sss", phoneNumber);
            Log.d("sss", messageDomainProc.getMessageContent());

            //if there is ambiguity
            if (jsonParser.getDialogPhase().equals("disambiguation")) {
                ambiguityListAdapter.notifyDataSetChanged();
            }

            if (jsonParser.getIntent().equals("display")) {
                Global.ambiguityList.clear();
                Log.d("sss", "message: "+messageDomainProc.getMessageContent());
                Global.ambiguityList.add(messageDomainProc.getMessageContent());
                ambiguityListAdapter.notifyDataSetChanged();
            }

            //if it is ready to send the message
            if (jsonParser.getIntent().equals("send") &&
                    !phoneNumber.equals("") &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, messageDomainProc.getMessageContent(), null, null);
            }
        }
    }


    void releaseCloudServices() {

        if (mMixCloudServices != null) {
            mMixCloudServices.release();
            mMixCloudServices = null;
        }
        if(mCloudServices != null){
            mCloudServices.release();
            mCloudServices = null;
        }
    }

    void releaseCloudRecognition() {
        if (mMixCloudRecognizer!= null) {
            mMixCloudRecognizer.cancel();
            mMixCloudRecognizer = null;
        }
        if(mCloudRecognizer != null){
            mCloudRecognizer.cancel();
            mCloudRecognizer = null;
        }
    }

    boolean initCloudServices() {
        releaseCloudServices();

        mCloudServices = CloudServices.createCloudServices(NLUCloudASRActivity.this,
                new CloudConfig(AppInfo.Host, AppInfo.Port, AppInfo.AppId, AppInfo.AppKey,
                        _uniqueId, AudioType.SPEEX_WB, AudioType.SPEEX_WB));

        CloudConfig mixConfig = null;
        mixConfig = new CloudConfig(
                    AppInfo.mixHost,
                    AppInfo.mixPort,
                    AppInfo.mixAppId,
                    AppInfo.mixAppKey,
                    AudioType.SPEEX_WB,
                    AudioType.SPEEX_WB);

        if( mixConfig != null ) {
            Log.d(TAG, "Mix Config: " + mixConfig.toString());
            mMixCloudServices = CloudServices.createCloudServices(this, mixConfig);
            return true;
        }
        return false;
    }

    void initCloudRecognition(){
        releaseCloudRecognition();
        initCloudServices();
        if(mMixCloudServices != null) {
            mMixCloudRecognizer = new CloudRecognizer(mMixCloudServices);
        }
        if(mCloudServices != null){
            mCloudRecognizer = new CloudRecognizer(mCloudServices);
        }
    }

    /**
     * Creates the recog spec.
     *
     * @param type the dictation type (example: nma_dm_main)
     * @param language the dictation language
     * @param mixContext the mix context
     * @param message the text to pass for NLU interpretation (null if using voice)
     * @return the recognition spec
     */
    private RecogSpec createMixRecogSpec(String type, String language, String mixContext, String message) {

        String command;
        Data.Dictionary settings = new Data.Dictionary();
        Data.Dictionary appDataDict = new Data.Dictionary();
        Data.Dictionary requestInfo = new Data.Dictionary();

        // Set core settings regardless of command type (ASR vs. NLU)
        //settings.put("dictation_type", type);
        settings.put("dictation_language", language);
        settings.put("application_name",   this.getString(R.string.app_name));
        settings.put("application_session_id", AppInfo.applicationSessionID);
        settings.put("application_state_id", "45");
        settings.put("location", getLastKnownLocation());
        settings.put("utterance_number", "5");

        settings.put("context_tag", mixContext);

        // Determine if we're passing in audio or text for recognition
        if (message == null) {
            command = getMixAsrCommand(); // "...ASR_CMD";
            settings.put("audio_source", "SpeakerAndMicrophone");
        }
        else {
            command = getMixAppserverCommand(); // "...APP_CMD";
            appDataDict.put("message", message);
        }

        RecogSpec recogSpec = new RecogSpec(command, settings, "AUDIO_INFO");

        // Start creating REQUEST_INFO dictionary
        requestInfo.put("start", 0);
        requestInfo.put("end", 0);
        requestInfo.put("text", "");

        recogSpec.addParam(new DictionaryParam("REQUEST_INFO", requestInfo));

        Log.d(TAG, "command: " + command);
        Log.d(TAG, "settings: " + settings.toString());
        Log.d(TAG, "requestInfo: " + requestInfo.toString());

        return recogSpec;
    }

    private String getMixAsrCommand(){
        return "NDSP_ASR_APP_CMD";
    }

    private String getMixAppserverCommand() {
        return "NDSP_APP_CMD";
    }

    private String getLastKnownLocation() {
        Log.d(TAG, "Retrieving last known location...");

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String provider = LocationManager.GPS_PROVIDER;
        return locationToString(locationManager.getLastKnownLocation(provider));
    }

    private String locationToString(Location location) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(">");

            if (location.hasAccuracy()) {
                sb.append(" +/- ");
                sb.append(location.getAccuracy());
                sb.append("m");
            }

            return sb.toString();
        }
        catch (Exception e) {
            Log.d(TAG, "Failed to create location string: " + e.getLocalizedMessage());
            return "";
        }
    }

    /** The recognizer text listener. */
    private final CloudTextRecognizer.Listener recognizerTextListener = new CloudTextRecognizer.Listener() {

        @Override
        public void onResult(CloudRecognitionResult result) {
//            onRecognitionResult(result.toJSON());
            JSONObject jsonResult = result.toJSON();
            String resultToShow = "";
            try {
                resultToShow = jsonResult.toString(4);
            }catch (JSONException e){
                e.printStackTrace();
            }
            Log.d("sss", resultToShow);
            sendJsonToEmail(resultToShow);
//            onGetDataResult(jsonResult);
        }

        @Override
        public void onError(CloudRecognitionError error) {
//            onRecognitionError(error.toJSON());
            Log.d("sss", intToString(error.getType()));
            Log.d("sss", intToString(error.getTransactionError().getType()));
        }
    };

    String intToString(int x){
        Integer X = x;
        return X.toString();
    }

    private String getDictationType(){
        return DEFAULT_DICTATION_TYPE;
    }

    /**
     * Creates the recog spec.
     *
     * @param type the dictation type (example: nma_dm_main)
     * @param language the dictation language
     * @param nluProfile the nlu profile
     * @param message the text to pass for NLU interpretation (null if using voice)
     * @param serverSpecifiedSettings the server specified settings
     * @return the recognition spec
     */
    private RecogSpec createRecogSpec(String type, String language, String nluProfile, String message, Data.Dictionary serverSpecifiedSettings) {

        String command;
        Data.Dictionary settings = new Data.Dictionary();
        Data.Dictionary appDataDict = new Data.Dictionary();
        Data.Dictionary requestInfo = new Data.Dictionary();

        // Set core settings regardless of command type (ASR vs. NLU)
        settings.put("dictation_type", type);
        settings.put("dictation_language", language);
        settings.put("application_name",   this.getString(R.string.app_name));
        settings.put("application_session_id", AppInfo.applicationSessionID);
        settings.put("application_state_id", "45");
        settings.put("location", getLastKnownLocation());
        settings.put("utterance_number", "5");

        // Determine if we're passing in audio or text for recognition
        if (message == null) {
            command = getAsrCommand(); // "...ASR_CMD";
            settings.put("audio_source", "SpeakerAndMicrophone");
        }
        else {
            command = getAppserverCommand(); // "...APPSERVER_CMD";
            appDataDict.put("message", message);
        }

        RecogSpec recogSpec = new RecogSpec(command, settings, "AUDIO_INFO");

        // Start creating REQUEST_INFO dictionary
        requestInfo.put("start", 0);
        requestInfo.put("end", 0);
        if(message == null) requestInfo.put("text", "");
        else requestInfo.put("text", message);

        // If this request is a DRAGON_NLU... request, create the appserver_data dictionary...
        if (isNluEnabled()) {
            settings.put("application",   nluProfile);
            appDataDict.put("application", nluProfile);
            appDataDict.put("timezone", getTimeZone());
            appDataDict.put("time", getCurrentTime());
            appDataDict.put("nlps_use_adk", 1);
            requestInfo.put("appserver_data", appDataDict);
            requestInfo.put("nlsml_results", 1);
        }

        recogSpec.addParam(new DictionaryParam("REQUEST_INFO", requestInfo));

        Log.d(TAG, "command: " + command);
        Log.d(TAG, "settings: " + settings.toString());
        Log.d(TAG, "requestInfo: " + requestInfo.toString());

        return recogSpec;
    }

    private String getAsrCommand() {
        if( isNluEnabled() )
            return DEFAULT_NLU_ASR_COMMAND;
        else
            return DEFAULT_ASR_COMMAND;
    }

    boolean isNluEnabled() {
        return mNluEnabled;
    }

    String getAppserverCommand() {
        return DEFAULT_APPSERVER_COMMAND;
    }

    protected String getTimeZone() {
        TimeZone tz = TimeZone.getDefault();

        Log.d(TAG, "Device timezone is: " + tz.getID());
        return tz.getID();	//tz.getDisplayName();
    }

    protected String getCurrentTime() {
        String format = "yyyy-MM-dd'T'HH:mm:ssZ";

        TimeZone tz = TimeZone.getDefault();	//.getTimeZone("UTC");
        Log.d(TAG, "Device timezone is: " + tz.getID());	//tz.getDisplayName());

        DateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        dateFormat.setTimeZone(tz);

        Date date = new Date();
        Log.d(TAG, "Device timestamp is: " + dateFormat.format(date));

        return dateFormat.format(date);
    }

    protected String getLanguage() {
        if( mLanguage != null )
            return mLanguage;

        return DEFAULT_LANGUAGE;
    }

    void setLanguage(String language) {
        mLanguage = language;
    }



}
