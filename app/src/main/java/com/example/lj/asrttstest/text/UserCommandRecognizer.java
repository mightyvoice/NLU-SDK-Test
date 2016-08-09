package com.example.lj.asrttstest.text;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.example.lj.asrttstest.R;
import com.example.lj.asrttstest.info.AppInfo;
import com.example.lj.asrttstest.info.Global;
import com.example.lj.asrttstest.text.dialog.TextDialogManager;
import com.example.lj.asrttstest.text.dialog.TextServerResponse;
import com.nuance.dragon.toolkit.audio.AudioType;
import com.nuance.dragon.toolkit.cloudservices.CloudConfig;
import com.nuance.dragon.toolkit.cloudservices.CloudServices;
import com.nuance.dragon.toolkit.cloudservices.DictionaryParam;
import com.nuance.dragon.toolkit.cloudservices.Transaction;
import com.nuance.dragon.toolkit.cloudservices.TransactionError;
import com.nuance.dragon.toolkit.cloudservices.TransactionResult;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognizer;
import com.nuance.dragon.toolkit.data.Data;
import com.nuance.dragon.toolkit.util.WorkerThread;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lj on 16/8/8.
 */
public class UserCommandRecognizer {

    private static final String Host = "mtldev02.nuance.com";
    private static final int Port = 443;
    private static final String AppId = "NMT_EVAL_TCL_20150814";
    public static final byte[] AppKey = { (byte) 0x89, (byte) 0xe9,
            (byte) 0xb1, (byte) 0xb6, (byte) 0x19, (byte) 0xdf, (byte) 0xc7,
            (byte) 0xd6, (byte) 0x82, (byte) 0x23, (byte) 0x7e, (byte) 0x70,
            (byte) 0x1d, (byte) 0xa7, (byte) 0xad, (byte) 0xa4, (byte) 0x83,
            (byte) 0x16, (byte) 0xf6, (byte) 0x75, (byte) 0xf7, (byte) 0x3c,
            (byte) 0x5e, (byte) 0xcd, (byte) 0x23, (byte) 0xa4, (byte) 0x1f,
            (byte) 0xc4, (byte) 0x07, (byte) 0x82, (byte) 0xbc, (byte) 0x21,
            (byte) 0x2e, (byte) 0xd3, (byte) 0x56, (byte) 0x20, (byte) 0x22,
            (byte) 0xc2, (byte) 0x3e, (byte) 0x75, (byte) 0x21, (byte) 0x4d,
            (byte) 0xcb, (byte) 0x90, (byte) 0x10, (byte) 0x28, (byte) 0x6c,
            (byte) 0x23, (byte) 0xaf, (byte) 0xe1, (byte) 0x00, (byte) 0xe0,
            (byte) 0x0d, (byte) 0x44, (byte) 0x64, (byte) 0x87, (byte) 0x3e,
            (byte) 0x00, (byte) 0x4d, (byte) 0x1f, (byte) 0x4c, (byte) 0x8a,
            (byte) 0x58, (byte) 0x83 };
    private final static boolean UseTLS = true;
    private final static String Topic = "nma_dm_main";
    private final static String LangCode = "eng-USA";

    private static final String DEFAULT_APPSERVER_COMMAND = "DRAGON_NLU_APPSERVER_CMD";
    private String userCommand = null;
    private String dataUploadReturnUniqueID;
    private String applicationSessionID;
    private CommandRecognizerListener commandRecognizerListener = null;
    private TextServerResponse textServerResponse = null;
    private Context context;
    private CloudServices cloudServices;
    private WorkerThread workerThread;

    public interface CommandRecognizerListener{
        void onGetTextRecognitionResult(TextServerResponse response);
    }

    public UserCommandRecognizer(String _dataUploadReturnUniqueID,
                                 String _applicationSessionID,
                                 String _userCommand,
                                 CommandRecognizerListener _commandRecognizerListener,
                                 Context _context){
        userCommand = _userCommand;
        dataUploadReturnUniqueID = _dataUploadReturnUniqueID;
        applicationSessionID = _applicationSessionID;
        commandRecognizerListener = _commandRecognizerListener;
        context = _context;
//        new CommandRecognitionAsyncTask().execute();
        cloudServices = CloudServices.createCloudServices(context,
                new CloudConfig(Host, Port, AppId, AppKey,
                        AudioType.SPEEX_WB, AudioType.SPEEX_WB));
        startAdkSubdialog();

    }

    private class CommandRecognitionAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
                            // Cloud services initialization
            cloudServices = CloudServices.createCloudServices(context,
                    new CloudConfig(Host, Port, AppId, AppKey,
                            AudioType.SPEEX_WB, AudioType.SPEEX_WB));
            startAdkSubdialog();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    /**
     * Upload data to server by choosing the ID for disambiguation
     */
    private void startAdkSubdialog() {
//        initCloudServices();
        try {
            Data.Dictionary settings = this.createCommandSettings(DEFAULT_APPSERVER_COMMAND, "nma_dm_main", LangCode);
            Data.Dictionary root = new Data.Dictionary();
            Data.Dictionary appserver_data = new Data.Dictionary();

            appserver_data.put("hour_format", "LOCALE");
//            appserver_data.put("timezone", getTimeZone());
//            appserver_data.put("time", getCurrentTime());
            //appserver_data.put("return_nlu_results", 1);
            appserver_data.put("action_mode", "default");
            appserver_data.put("application", "TCL");
            //appserver_data.put("dialog_seq_id", 2);

            //Ji's code
//            String choose = "SLOTS:GENERIC_ORDER:" + Global.ambiguityListChosenID.toString();
            appserver_data.put("message", userCommand);

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

            Transaction dut = new Transaction(DEFAULT_APPSERVER_COMMAND, settings, new Transaction.Listener() {

                @Override
                public void onTransactionStarted(Transaction arg0) {
//                        Log.d(TAG, "Transaction Started...");
                }

                @Override
                public void onTransactionProcessingStarted(Transaction transaction) {
                }

                @Override
                public void onTransactionResult(Transaction arg0, TransactionResult arg1,
                                                boolean arg2) {
//                        Log.d(TAG, "Transaction Completed...");
                    JSONObject results = null;
                    String resultString = arg1.getContents().toString();
                    resultString = resultString.replaceAll("(,)(\\s+)(\\})", "$2$3");
//                    resultString = resultString.replaceAll("\\;,", ",");
//                    resultString = resultString.replaceAll("\\;", ",");
                    resultString = resultString.replaceAll("( : )(\\w)", "$1\"$2");
                    resultString = resultString.replaceAll("(,\\n)", "\"$1");
                    Log.d("sss", resultString);
                    try {
                        results = new JSONObject(resultString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(results != null) {
                        textServerResponse = new TextDialogManager(results).getTextServerResponse();
                        commandRecognizerListener.onGetTextRecognitionResult(textServerResponse);
                    }
                }

                @Override
                public void onTransactionError(Transaction arg0, TransactionError arg1) {
                    Log.d("sss", "Command Upload Transaction Error...");
//                        onGetDataError(arg0, arg1.toJSON());
                }

                @Override
                public void onTransactionIdGenerated(String s) {
                }

            }, 3000, true);

            //            this.mCloudServices.addTransaction(dut, 1);
            cloudServices.addTransaction(dut, 1);
            dut.addParam(RequestInfo);
            dut.finish();

        } catch (Exception e) {
            //Log.e(TAG, e.getMessage());
//            Log.e(TAG, "Exception thrown in RecognizerCloudActivity.startAdkSubdialog()");
            e.printStackTrace();
        }
    }

    /**
     * Creates the command settings.
     *
     * @param commandName the command name
     * @param type        the dictation type
     * @param language    the dictation language
     * @return the command settings dictionary
     */
    private Data.Dictionary createCommandSettings(String commandName, String type, String language) {
        Data.Dictionary settings = new Data.Dictionary();

        settings.put("command", commandName);
        settings.put("nmaid", AppId);
        settings.put("dictation_language", language);
        settings.put("carrier", "ATT");
//        settings.put("dictation_type", type);
        settings.put("application_name", R.string.app_name);
        settings.put("application_session_id", applicationSessionID);
        settings.put("application_state_id", "45");
//        settings.put("location", getLastKnownLocation());
        settings.put("utterance_number", "5");
//        settings.put("audio_source", "SpeakerAndMicrophone");

        //Ji Li's code
        settings.put("uid", dataUploadReturnUniqueID);
        settings.put("nlps_use_adk", 1);
        settings.put("application", "TCL");
        settings.put("dictation_type", "gens_dm_main");

        return settings;
    }

}
