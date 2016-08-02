package com.example.lj.asrttstest.text;

/**
 * Created by lj on 16/6/29.
 */

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

    private static final String TextHost = "mtldev08.nuance.com";
    private static final int TextPort = 443;
    private static final String TextAppId = "NMT_EVAL_TCL_20150814";
    private final static String TextAppKey = "89e9b1b619dfc7d682237e701da7ada48316f675f73c5ecd23a41fc40782bc212ed3562022c23e75214dcb9010286c23afe100e00d4464873e004d1f4c8a5883";
    private final static boolean UseTLS = true;
    private final static String Topic = "nma_dm_main";
    private final static String LangCode = "eng-USA";

    private HttpAsrClient asrClient = null;
    private JSONObject serverResponseJSON;
    public String datauploadReturnUniqueID;
    public String textForNLU;
    private TextServerResponse serverResponse;

    public CloudTextRecognizer(String _datauploadReturnUniqueID,
                               String _applicationSessionID,
                               String _textForNLU){
        datauploadReturnUniqueID = _datauploadReturnUniqueID;
        textForNLU = _textForNLU;
        if(asrClient == null){
            asrClient = new HttpAsrClient(
                    TextHost,
                    TextPort,
                    UseTLS,
                    TextAppId,
                    TextAppKey,
                    _datauploadReturnUniqueID,
                    _applicationSessionID,
                    Topic,
                    LangCode);
        }
        serverResponseJSON = null;
    }

    public TextServerResponse startTextRecognition(String _text) {

        if( _text != null && !_text.equals("")) {
            asrClient.enableTextNLU();
            asrClient.sendNluTextRequest(_text);
        }

        serverResponse = null;
        TextDialogManager textDialogManager = new TextDialogManager(asrClient.serverResponseJSON);
        serverResponse = textDialogManager.getTextServerResponse();
        return serverResponse;
    }

}
