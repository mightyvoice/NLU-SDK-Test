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
import com.example.lj.asrttstest.asr.text.HttpAsrClient;
import com.example.lj.asrttstest.asr.text.IHttpAsrClient;
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

    /** for text recognizer **/
    private CloudTextRecognizer mCloudTextRecognizer = null;

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
                        startRecognitionButton.setEnabled(true);
//                        initCloudRecognition();
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

//                startTextRecognition("Make a phone call");
                startTextRecognition("Call Teddy");
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
        if (mCloudTextRecognizer != null) {
            mCloudTextRecognizer.cancel();
            mCloudTextRecognizer = null;
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

        return false;
    }

    void initCloudRecognition(){
        releaseCloudRecognition();
        initCloudServices();
        if(mCloudServices != null){
            mCloudTextRecognizer = new CloudTextRecognizer(mCloudServices);
        }
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

//    private void startTextRecognition(String text) {
//        Log.d(TAG, "startTextRecognition: " + text);
////        RecogSpec recogSpec = createRecogSpec(getDictationType(), getLanguage(), "TCL", text, null);
////        mCloudTextRecognizer.startRecognition(recogSpec, recognizerTextListener);
////        textRecognition(text);
//    }

    private void startTextRecognition(String _text) {
        Log.d(TAG, "startTextRecognition: " + _text);
        String host = "mtldev08.nuance.com";
        String nmaid = "NMT_EVAL_TCL_20150814";
        String appKey = "89e9b1b619dfc7d682237e701da7ada48316f675f73c5ecd23a41fc40782bc212ed3562022c23e75214dcb9010286c23afe100e00d4464873e004d1f4c8a5883";

        /** DEFAULTS */
        int port = 443;
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

        IHttpAsrClient asrClient = new HttpAsrClient(
                host,
                port,
                useTLS,
                nmaid,
                appKey,
                topic,
                langCode );

        if( !requireTrustedRootCert )
            asrClient.disableTrustedRootCert();

        // Reset User Profile requests take precedence over any other conflicting command-line args
        if( resetUserProfile ) {
            asrClient.resetUserProfile();
            System.exit(0);
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

        //Ji Li's code
        settings.put("uid", AppInfo.IMEInumber);
        settings.put("nlps_use_adk", 1);
        settings.put("application", "TCL");
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

            //this line is Ji's code
            appDataDict.put("nlps_return_abstract_nlu", 1);

            requestInfo.put("appserver_data", appDataDict);
            requestInfo.put("nlsml_results", 1);

            //this line is Ji's code
            requestInfo.put("nbest_text_results", 1);
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

}
