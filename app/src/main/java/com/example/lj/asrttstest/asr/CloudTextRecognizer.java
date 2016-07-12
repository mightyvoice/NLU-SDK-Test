package com.example.lj.asrttstest.asr;

/**
 * Created by lj on 16/6/29.
 */
import android.util.Log;

import com.example.lj.asrttstest.asr.text.HttpAsrClient;
import com.example.lj.asrttstest.info.AppInfo;
import org.json.JSONObject;


/**
 * A cloud text recognizer performs a simple network text recognition using a cloud
 * services transaction.
 *
 * Copyright (c) 2015 Nuance Communications. All rights reserved.
 */
public class CloudTextRecognizer {

    public JSONObject startTextRecognition(String _text) {
        String appKey = "89e9b1b619dfc7d682237e701da7ada48316f675f73c5ecd23a41fc40782bc212ed3562022c23e75214dcb9010286c23afe100e00d4464873e004d1f4c8a5883";
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

        HttpAsrClient asrClient = new HttpAsrClient(
                AppInfo.TextHost,
                AppInfo.Port,
                useTLS,
                AppInfo.AppId,
                appKey,
                topic,
                langCode );

        if( !requireTrustedRootCert )
            asrClient.disableTrustedRootCert();

        // Reset User Profile requests take precedence over any other conflicting command-line args
        if( resetUserProfile ) {
            asrClient.resetUserProfile();
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
        }

        return asrClient.serverResponseJSON;
    }
}
