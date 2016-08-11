package com.example.lj.asrttstest;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import android.widget.TextView;
import android.widget.Toast;
/////////test git
import com.example.lj.asrttstest.info.AppInfo;
import com.nuance.dragon.toolkit.audio.AudioChunk;
import com.nuance.dragon.toolkit.audio.AudioType;
import com.nuance.dragon.toolkit.audio.SpeechDetectionListener;
import com.nuance.dragon.toolkit.audio.pipes.ConverterPipe;
import com.nuance.dragon.toolkit.audio.pipes.EndPointerPipe;
import com.nuance.dragon.toolkit.audio.pipes.OpusEncoderPipe;
import com.nuance.dragon.toolkit.audio.pipes.SpeexEncoderPipe;
import com.nuance.dragon.toolkit.audio.sources.MicrophoneRecorderSource;
import com.nuance.dragon.toolkit.audio.sources.RecorderSource;
import com.nuance.dragon.toolkit.calllog.CalllogManager;
import com.nuance.dragon.toolkit.calllog.CalllogManager.CalllogDataListener;
import com.nuance.dragon.toolkit.calllog.CalllogSender;
import com.nuance.dragon.toolkit.calllog.CalllogSender.SenderListener;
import com.nuance.dragon.toolkit.calllog.SessionEvent;
import com.nuance.dragon.toolkit.calllog.SessionEventBuilder;
import com.nuance.dragon.toolkit.cloudservices.CloudConfig;
import com.nuance.dragon.toolkit.cloudservices.CloudServices;
import com.nuance.dragon.toolkit.cloudservices.DictionaryParam;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognitionError;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognitionResult;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognizer;
import com.nuance.dragon.toolkit.cloudservices.recognizer.RecogSpec;
import com.nuance.dragon.toolkit.data.Data;
import com.nuance.dragon.toolkit.util.Logger;


public class CloudASRActivity extends AppCompatActivity
{
    private static  final int VOICE_RECOGNITION_REQUEST_CODE = 1001;

    private CloudServices      _cloudServices;
    private CloudRecognizer    _cloudRecognizer;
    private RecorderSource<AudioChunk>     recorder;
    private ConverterPipe<AudioChunk, AudioChunk> _encoder;
    private SpeexEncoderPipe speexPipe;
    private EndPointerPipe endpointerPipe;
    // call log feature
    private SessionEvent       _appSessionLeadEvent;
    private String             _appLeadSessionId;
    private CalllogSender      _calllogSender;
    private AudioType          _audioType;
    private TTSService         _ttsService;

    private TextView resultTextView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cloud_asr);

        // UI initialization
        resultTextView = (TextView)findViewById(R.id.cloudResultEditText);
        final Button startRecognitionButton = (Button) findViewById(R.id.startCloudRecognitionButton);
//        final Button stopRecognitionButton = (Button) findViewById(R.id.stopCloudRecognitionButton);
        startRecognitionButton.setEnabled(true);
//        stopRecognitionButton.setEnabled(false);

        _audioType = AudioType.SPEEX_WB;

        reCreateCloudRecognizer();

        startRecognitionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startRecognitionButton.setEnabled(false);
//                stopRecognitionButton.setEnabled(true);
                resultTextView.setText("");

                String resultmodeName = "No Partial Results";

                // Set-up audio chaining
                recorder = new MicrophoneRecorderSource(AudioType.PCM_16k);
                speexPipe = new SpeexEncoderPipe();
                endpointerPipe = new EndPointerPipe(new SpeechDetectionListener() {
                    @Override
                    public void onStartOfSpeech() {
                        resultTextView.setText("Start of Speech...");
                    }

                    @Override
                    public void onEndOfSpeech() {
                        resultTextView.setText("End of Speech...");
                        _cloudRecognizer.processResult();
                        startRecognitionButton.setEnabled(true);
                        stopRecording();
                    }
                });

                // Start recording and recognition
                recorder.startRecording();
                speexPipe.connectAudioSource(recorder);
                endpointerPipe.connectAudioSource(speexPipe);
                _cloudRecognizer.startRecognition(createRecogSpec(resultmodeName),
                        endpointerPipe,
                        new CloudRecognizer.Listener()
                        {
                            @Override
                            public void onResult(CloudRecognitionResult result) {
                                //get the original jason
//                                Data.Dictionary appServerResults = result.getDictionary().getDictionary("appserver_results");
//                                Log.d("ssss", appServerResultc就可以去掉高亮啦.toJSON().toString());
                                java.lang.String topResult = parseResults(result);

                                if(topResult != null) {
                                    resultTextView.setText(topResult);
                                }
                            }

                            @Override
                            public void onError(CloudRecognitionError error) {
//                                resultTextView.setText(error.toString());
                                String err = error.toJSON().toString();
                                resultTextView.setText("speech not recognized");
                                Log.d("sss", err);
                            }

                            @Override
                            public void onTransactionIdGenerated(String transactionId)
                            {
                                // TODO Auto-generated method stub
                            }
                        });

                if (_appSessionLeadEvent != null) {
                    SessionEventBuilder eventBuilder = _appSessionLeadEvent.createChildEventBuilder("cloud recognition");
                    eventBuilder.putString("start", "recognition started");
                    eventBuilder.commit();
                }
            }
        });

//        stopRecognitionButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                startRecognitionButton.setEnabled(true);
//                stopRecognitionButton.setEnabled(false);
//                _cloudRecognizer.processResult();
//                stopRecording();
//
//                if (_appSessionLeadEvent != null) {
//                    SessionEventBuilder eventBuilder = _appSessionLeadEvent.createChildEventBuilder("cloud recognition");
//                    eventBuilder.putString("stop", "recognition stopped");
//                    eventBuilder.commit();
//                }
//                CalllogManager.flushCallLogData();
//            }
//        });

    }

    private void stopRecording() {
        if (endpointerPipe != null) {
            endpointerPipe.disconnectAudioSource();
            endpointerPipe = null;
        }

        if (speexPipe != null) {
            speexPipe.disconnectAudioSource();
            speexPipe = null;
        }

        if (recorder != null) {
            recorder.stopRecording();
            recorder = null;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (recorder != null)
            recorder.stopRecording();
        recorder = null;

        if (_encoder != null) {
            _encoder.disconnectAudioSource();
            _encoder.release();
        }
        _encoder = null;

        if (_cloudRecognizer != null)
            _cloudRecognizer.cancel();
        _cloudRecognizer = null;

        if (_cloudServices != null)
            _cloudServices.release();
        _cloudServices = null;
    }

    private RecogSpec createRecogSpec(String resultModeName) {
        // Create a sample recognition spec. based on the "NVC_ASR_CMD"
        Data.Dictionary settings = new Data.Dictionary();
        settings.put("dictation_type", "dictation");
        settings.put("dictation_language", "eng-USA");
        if (_appLeadSessionId != null)
            settings.put(CalllogManager.CALLLOG_APP_TRANSACTION_REF_EVENT, _appLeadSessionId);
        RecogSpec retRecogSpec = new RecogSpec("NVC_ASR_CMD", settings, "AUDIO_INFO");

        // Also, add necessary "REQUEST_INFO" parameter
        Data.Dictionary requestInfo = new Data.Dictionary();
        requestInfo.put("start", 0);
        requestInfo.put("end", 0);
        requestInfo.put("text", "");

        if (resultModeName.equals("Utterance Detection Default"))
            requestInfo.put("intermediate_response_mode", "UtteranceDetectionWithCompleteRecognition");
        else if (resultModeName.equals("Utterance Detection Very Aggressive")) {
            requestInfo.put("intermediate_response_mode", "UtteranceDetectionWithCompleteRecognition");
            requestInfo.put("utterance_detection_silence_duration", "VeryAggressive");
        }
        else if (resultModeName.equals("Utterance Detection Aggressive")) {
            requestInfo.put("intermediate_response_mode", "UtteranceDetectionWithCompleteRecognition");
            requestInfo.put("utterance_detection_silence_duration", "Aggressive");
        }
        else if (resultModeName.equals("Utterance Detection Average")) {
            requestInfo.put("intermediate_response_mode", "UtteranceDetectionWithCompleteRecognition");
            requestInfo.put("utterance_detection_silence_duration", "Average");
        }
        else if (resultModeName.equals("Utterance Detection Conservative")) {
            requestInfo.put("intermediate_response_mode", "UtteranceDetectionWithCompleteRecognition");
            requestInfo.put("utterance_detection_silence_duration", "Conservative");
        }
        else if (resultModeName.equals("Streaming Results")) {
            requestInfo.put("intermediate_response_mode", "NoUtteranceDetectionWithPartialRecognition");
            _cloudRecognizer.setResultCadence(500);
        }

        retRecogSpec.addParam(new DictionaryParam("REQUEST_INFO", requestInfo));

        return retRecogSpec;
    }

    private java.lang.String parseResults(CloudRecognitionResult cloudResult)
    {
        Data.Dictionary processedResult = cloudResult.getDictionary();

        // Parse results based on the "NVC_ASR_CMD"
        //java.lang.String prompt = ((Data.String)results.get("prompt")).Value;
        //Data.Sequence transcriptions = (Data.Sequence) results.get("transcriptions");
        //Data.Sequence confidences = (Data.Sequence) results.get("confidences");

        // The processed result has "prompt" == "warning", "transcriptions" == "choices"
        if (processedResult == null || processedResult.getString("prompt") == null )
            return null;

        java.lang.String prompt = processedResult.getString("prompt").value;
        Data.Sequence transcriptions = processedResult.getSequence("transcriptions");
        //Data.Sequence scores = processedResult.getSequence("confidences");

        int len = transcriptions.size();
        //len = (confidences.size() == len) ? len : 0;

        ArrayList<String> sentences = new ArrayList<java.lang.String>();

        for (int idx = 0; idx < len; idx++) {
            Data.String text = transcriptions.getString(idx);
            //Data.Integer score = scores.getInt(idx);

            sentences.add(text.value);
        }

        return (len == 0 ? prompt : sentences.get(0));
    }

    private void reCreateCloudRecognizer()
    {
        if (_cloudRecognizer != null)
            _cloudRecognizer.cancel();
        _cloudRecognizer = null;

        if (_cloudServices != null)
            _cloudServices.release();
        _cloudServices = null;

        // Cloud services initialization
        _cloudServices = CloudServices.createCloudServices(CloudASRActivity.this,
                new CloudConfig(AppInfo.Host, AppInfo.Port, AppInfo.AppId, AppInfo.AppKey, _audioType, _audioType, false));

        _calllogSender = CalllogManager.createCalllogSender(new SenderListener() {

            @Override
            public void succeeded(byte[] data) {
                Logger.debug(this, "call log data: " + data + " is sent");
            }

            @Override
            public void failed(short errorCode, byte[] data) {
                Logger.debug(this, "call log data: " + data + " failed on sending error code: " + errorCode + ". Save me! ");
            }
        });

        CalllogManager.registerCalllogDataListener(new CalllogDataListener() {

            @Override
            public void callLogDataGenerated(byte[] callLogData, List<String> sessionEventIds) {
                Logger.debug(this, "call log data: " + callLogData + " is generated");
                _calllogSender.send(callLogData);
            }
        });

        SessionEventBuilder appSessionEventBuilder = CalllogManager.logAppEvent("SampleAppEvent", "2.0");

        if (appSessionEventBuilder != null) {

            _appSessionLeadEvent = appSessionEventBuilder.commit();
            _appLeadSessionId = _appSessionLeadEvent.getId();
            Logger.debug(this, "application session event lead id is: " + _appLeadSessionId);
        }

        // Cloud Recognizer initialization
        _cloudRecognizer = new CloudRecognizer(_cloudServices);
    }


    public void startGoogleASR(View view){
        resultTextView.setText("");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        startActivityForResult(intent,VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if (resultCode == RESULT_OK){
            ArrayList<String> textMatchlist = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (!textMatchlist.isEmpty()){
                resultTextView.setText(textMatchlist.get(0));
//                if (textMatchlist.get(0).contains("search")){
//                    String searchQuery = textMatchlist.get(0).replace("search"," ");
//                    Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
//                    search.putExtra(SearchManager.QUERY,searchQuery);
//                    startActivity(search);
//                }
//                else {
//                    mlvTextMatches.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,textMatchlist));
//                }
            }
        }
        else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
            showToastMessage("Audio Error");

        }
        else if ((resultCode == RecognizerIntent.RESULT_CLIENT_ERROR)){
            showToastMessage("Client Error");

        }
        else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
            showToastMessage("Network Error");
        }
        else if (resultCode == RecognizerIntent.RESULT_NO_MATCH){
            showToastMessage("No Match");
        }
        else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
            showToastMessage("Server Error");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void  showToastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }
}