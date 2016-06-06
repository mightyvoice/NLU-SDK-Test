package com.example.lj.asrttstest;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.telephony.TelephonyManager;

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
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognitionError;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognitionResult;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognizer;
import com.nuance.dragon.toolkit.cloudservices.recognizer.RecogSpec;
import com.nuance.dragon.toolkit.data.Data;
import com.nuance.dragon.toolkit.util.Logger;
import com.nuance.dragon.toolkit.util.WorkerThread;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NLUCloudASRActivity extends AppCompatActivity {

    private int DMA_GRAMMAR_VERSION = 1;
    private final String TAG = "NLUCloud";

    private CloudServices               _cloudServices;
    private CloudRecognizer             _cloudRecognizer;
    private RecorderSource<AudioChunk> _recorder;
    private SpeexEncoderPipe            _speexPipe;
    private EndPointerPipe              _endpointerPipe;

    private String _uniqueId;
    private Data.Sequence _grammarList, _checksumList;
    private WorkerThread _workerThread;
    private JsonParser jsonParser;
    private TTSService _ttsService;
    private CloudDataUpload mCloudDataUploadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_asr);
        final EditText resultEditText = (EditText)findViewById(R.id.cloudResultEditText);
        final Button startRecognitionButton = (Button) findViewById(R.id.startCloudRecognitionButton);
        final Button stopRecognitionButton = (Button) findViewById(R.id.stopCloudRecognitionButton);
        final Button cancelButton = (Button) findViewById(R.id.cancelCloudRecognitionButton);
        final Spinner resultModeSpinner = (Spinner) findViewById(R.id.resultModeSpinner);

        startRecognitionButton.setEnabled(false);
        stopRecognitionButton.setEnabled(false);
        cancelButton.setEnabled(false);
        resultModeSpinner.setEnabled(false);

        //my code here for jason parser initialization
        jsonParser = new JsonParser();
        _ttsService = new TTSService(getApplicationContext());

        //get IMEI number which is the value of uid
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        AppInfo.IMEInumber = telephonyManager.getDeviceId();
        Log.d(TAG, "IMEI: "+AppInfo.IMEInumber );

        //start to upload contact information
        mCloudDataUploadService = new CloudDataUpload(NLUCloudASRActivity.this, resultEditText);
        mCloudDataUploadService.startDataUpload();

        // First, grammar set-up
        final Handler uiHandler = new Handler();

        _workerThread = new WorkerThread();
        _workerThread.start();
        _workerThread.getHandler().post(new Runnable()
        {
            @Override
            public void run()
            {
                readDragonData();
                uiHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
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

        startRecognitionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startRecognitionButton.setEnabled(false);
                stopRecognitionButton.setEnabled(true);
                cancelButton.setEnabled(true);

                _recorder = new MicrophoneRecorderSource(AudioType.PCM_16k);
                _speexPipe = new SpeexEncoderPipe();
                _endpointerPipe = new EndPointerPipe(new SpeechDetectionListener()
                {
                    @Override
                    public void onStartOfSpeech()
                    {
                        resultEditText.setText("Start of Speech...");
                    }

                    @Override
                    public void onEndOfSpeech()
                    {
                        resultEditText.setText("End of Speech...");

                        _cloudRecognizer.processResult();
                        stopRecording();
                    }
                });

                // Start recording and recognition
                _recorder.startRecording();
                _speexPipe.connectAudioSource(_recorder);
                _endpointerPipe.connectAudioSource(_speexPipe);

                _cloudRecognizer.startRecognition(createCustomSpec(), _endpointerPipe, new CloudRecognizer.Listener()
                {
                    private int resultCount = 0;

                    @Override
                    public void onResult(CloudRecognitionResult result)
                    {
                        Logger.debug(this, "NLU Cloud Recognition succeeded!");
                        stopRecording();
                        resultCount++;

                        startRecognitionButton.setEnabled(true);
                        stopRecognitionButton.setEnabled(false);
                        cancelButton.setEnabled(false);

                        if (resultCount == 1)
                        {
                            ///////// 1st result is the transcription

//                            Log.d("sss", "1");
//                            try {
//                                Log.d("sss", result.getDictionary().toJSON().toString(4));
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            showResults(resultEditText, getTranscription(result));
                        }
                        else
                        {
                            ///////// 2nd result is the ADK/NLU action
                            String readableResult = "Error";
                            try {
                                readableResult = result.getDictionary().toJSON().toString(4);
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                            Log.d("sss", "2");
                            Log.d("sss", readableResult);
                            sendJsonToEmail(readableResult);
                            String feedback = "I don't know what to do";
                            String phoneNumber = "Error";
                            try {
//                                feedback = jsonParser.getCallingFeedBack(readableResult);
                                feedback = jsonParser.getConverstationFeedback(readableResult);
                                phoneNumber = jsonParser.getPhoneNumberFromActionObject();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }finally {
                                showResults(resultEditText, feedback);
                                _ttsService.performTTS(getApplicationContext(), feedback);
//                                _ttsService.performTTS(getApplicationContext(), "The phone number is "+phoneNumber);
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
        //user id for data upload
        customSettings.put("uid", AppInfo.IMEInumber);
        customSettings.put("application_session_id", String.valueOf(UUID.randomUUID()));
        customSettings.put("dictation_language", "eng-USA");
        //original one from sample app
//        customSettings.put("dictation_type", "searchormessaging");
        customSettings.put("dictation_type", "nma_dm_main");

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
}
