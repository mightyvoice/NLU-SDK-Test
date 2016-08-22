package com.example.lj.asrttstest.text;

/**
 * Created by lj on 16/6/29.
 */

import android.os.AsyncTask;

import com.example.lj.asrttstest.text.dialog.TextDialogManager;
import com.example.lj.asrttstest.text.dialog.TextServerResponse;
import com.example.lj.asrttstest.text.http.HttpAsrClient;
import org.json.JSONObject;


/**
 * A cloud text recognizer performs a simple network text recognition using a cloud
 * services transaction.
 * Must be used not in the main thread
 */
public class CloudTextRecognizer {

    //past settings
//    private static final String TextHost = "mtldev08.nuance.com";
//    private static final String TextAppId = "NMT_EVAL_TCL_20150814";
//    private final static String TextAppKey = "89e9b1b619dfc7d682237e701da7ada48316f675f73c5ecd23a41fc40782bc212ed3562022c23e75214dcb9010286c23afe100e00d4464873e004d1f4c8a5883";

    //////new settings
    private static final String TextHost = "mtldev04.nuance.com";
    private static final String TextAppId = "TCL_TESTING_20160307";
    private final static String TextAppKey = "601c876b563065fb5e1b39f7b15dda6a8052c3c63b602bec22a7aec1ead93ea3434d596d761234e9a9771763b14412ea6551ed2c17a4f71839af9aac959e1566";


    private static final int TextPort = 443;
    private final static boolean UseTLS = true;
    private final static String Topic = "nma_dm_main";
    private final static String LangCode = "eng-USA";

    private HttpAsrClient asrClient = null;
    private JSONObject serverResponseJSON;
    private String dataUploadReturnUniqueID;
    private String applicationSessionID;

    public String textForNLU;
    private TextServerResponse serverResponse;
    private TextRecognizerListener textRecognizerListener;

    public interface TextRecognizerListener{
        void onGetTextRecognitionResult(TextServerResponse response);
    }

    public CloudTextRecognizer(String _dataUploadReturnUniqueID,
                               String _applicationSessionID,
                               String _textForNLU,
                               TextRecognizerListener _textRecognizerListener){
        dataUploadReturnUniqueID = _dataUploadReturnUniqueID;
        applicationSessionID = _applicationSessionID;
        textForNLU = _textForNLU;
        textRecognizerListener = _textRecognizerListener;
        if(asrClient == null){
            asrClient = new HttpAsrClient(
                    TextHost,
                    TextPort,
                    UseTLS,
                    TextAppId,
                    TextAppKey,
                    dataUploadReturnUniqueID,
                    applicationSessionID,
                    Topic,
                    LangCode);
        }
        serverResponseJSON = null;
        new TextRecognitionAsyncTask().execute();
    }

    public TextServerResponse getServerResponse() {
        return serverResponse;
    }

    private void startTextRecognition() {

        if( textForNLU != null && !textForNLU.equals("")) {
            asrClient.enableTextNLU();
            asrClient.sendNluTextRequest(textForNLU);
        }

    }

    private class TextRecognitionAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            serverResponse = null;
        }

        @Override
        protected String doInBackground(String... params) {
            if(textForNLU != null && !textForNLU.equals("")){
                startTextRecognition();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            TextDialogManager textDialogManager = new TextDialogManager(asrClient.serverResponseJSON);
            serverResponse = textDialogManager.getTextServerResponse();
            textRecognizerListener.onGetTextRecognitionResult(serverResponse);
        }
    }

}
