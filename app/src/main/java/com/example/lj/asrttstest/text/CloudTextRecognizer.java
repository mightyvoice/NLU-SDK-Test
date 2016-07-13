package com.example.lj.asrttstest.text;

/**
 * Created by lj on 16/6/29.
 */

import com.example.lj.asrttstest.text.http.HttpAsrClient;
import com.example.lj.asrttstest.info.AppInfo;
import org.json.JSONObject;


/**
 * A cloud text recognizer performs a simple network text recognition using a cloud
 * services transaction.
 * Must be used not in the main thread
 */
public class CloudTextRecognizer {

    private HttpAsrClient asrClient = null;
    private static CloudTextRecognizer textRecognizer = null;
    private final static String appKey = "89e9b1b619dfc7d682237e701da7ada48316f675f73c5ecd23a41fc40782bc212ed3562022c23e75214dcb9010286c23afe100e00d4464873e004d1f4c8a5883";
    private final static boolean useTLS = true;
    private final static boolean requireTrustedRootCert = false;
    private final static String topic = "nma_dm_main";
    private final static String langCode = "eng-USA";
    private final static boolean enableProfanityFiltering = false;
    private final static boolean enableNLU = true;
    private final static boolean batchMode = false;
    private final static boolean resetUserProfile = false;
    private final static String application = AppInfo.Application;

    public CloudTextRecognizer(){
        if(asrClient == null){
            asrClient = new HttpAsrClient(
                    AppInfo.TextHost,
                    AppInfo.Port,
                    useTLS,
                    AppInfo.AppId,
                    appKey,
                    topic,
                    langCode );
        }
    }

    public static CloudTextRecognizer getTextRecognizer(){
        if(textRecognizer == null){
            textRecognizer = new CloudTextRecognizer();
        }
        return textRecognizer;
    }

    public JSONObject startTextRecognition(String _text) {

        String nluTextString = _text;
        if(asrClient == null) {
            HttpAsrClient asrClient = new HttpAsrClient(
                    AppInfo.TextHost,
                    AppInfo.Port,
                    useTLS,
                    AppInfo.AppId,
                    appKey,
                    topic,
                    langCode);
        }

        if( nluTextString != null && !nluTextString.equals("")) {
            asrClient.enableTextNLU();
            asrClient.sendNluTextRequest(nluTextString);
        }

        return asrClient.serverResponseJSON;
    }
}
