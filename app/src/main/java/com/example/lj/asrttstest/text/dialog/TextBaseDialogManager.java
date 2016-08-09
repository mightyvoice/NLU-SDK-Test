package com.example.lj.asrttstest.text.dialog;

/**
 * Created by lj on 16/7/13.
 */

import com.example.lj.asrttstest.dialog.DialogResult;
import com.example.lj.asrttstest.dialog.IDialogManager;
import com.example.lj.asrttstest.dialog.IDialogResult;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lj on 16/7/13.
 */
public class TextBaseDialogManager implements ITextDialogManager {
    /** Dialog Phase is returned in the NCS Ref NLU JSON Response. topLevel let's the client know that the dialog is complete and it should reset dialog management after processing the response. */
    private static final String DIALOG_PHASE_TOP_LEVEL				= "topLevel";

    /** Dialog Phase is returned in the NCS Ref NLU JSON Response. informationRequest informs the client that the dialog is not complete and the user needs to be prompted for more information. */
    protected static final String DIALOG_PHASE_INFORMATION_REQUEST		= "informationRequest";

    /** Dialog Phase is returned in the NCS Ref NLU JSON Response. disambiguation informs the client that the response contains multiple items, such as contact data, requiring the user to select the desired option. */
    protected static final String DIALOG_PHASE_DISAMBIGUATION			= "disambiguation";

    /** Dialog Phase is returned in the NCS Ref NLU JSON Response. confirmation informs the client that the user needs to be prompted for yes/no confirmation. */
    protected static final String DIALOG_PHASE_CONFIRMATION				= "confirmation";

    /** Action type is returned in the NCS Ref NLU JSON Response. dmState contains details about the dialog phase and request status. */
    private static final String ACTION_TYPE_DMSTATE					= "dmState";

    /** Action type is returned in the NCS Ref NLU JSON Response. conversation contains UI prompt details. */
    private static final String ACTION_TYPE_CONVERSATION				= "conversation";

    /** Action type is returned in the NCS Ref NLU JSON Response. tts contains TTS prompt details. */
    private static final String ACTION_TYPE_TTS						= "tts";

    /** Action type is returned in the NCS Ref NLU JSON Response. domain contains NLU domain details. */
    private static final String ACTION_TYPE_DOMAIN					= "domain";

    /** Action type is returned in the NCS Ref NLU JSON Response. application contains details about the target application for the selected domain */
    private static final String ACTION_TYPE_APPLICATION				= "application";

    /** Action type is returned in the NCS Ref NLU JSON Response. reset informs the client to send a reset request to the server. */
    private static final String ACTION_TYPE_RESET						= "reset";

    /** Action type is returned in the NCS Ref NLU JSON Response. get_data informs the client that a data exchange between client and server is required, and specifies the parameters involved. */
    private static final String ACTION_TYPE_GETDATA					= "get_data";

    /** The NCS server response represented as a String. */
    protected String serverResponse = null;

    /** The NCS Server response represented as a JSON object. */
    private JSONObject mJsonResponse = null;

    /** The status value returned in the NCS response. */
    private String mStatus 				= null;

    /** The value of final_response returned in the NCS response. */
    private int mFinalResponse 			= 0;

    /** The NLU domain returned in the NCS response. */
    private String mDomain 				= null;

    /** The dialog phase returned in the NCS response. */
    private String mDialogPhase 			= null;

    /** The system text returned in the NCS response to be displayed in the UI. */
    private String mSystemText			= null;

    /** The tts text returned in the NCS response to be played back to the user. */
    private String mTtsText				= null;

    /** A flag to track if the server has requested the dialog be reset. */
    private boolean mResetDialog			= false;

    /** The NLU intent returned in the NCS response. */
    private String mIntent				= null;

    /** An instance of the get_data JSON object returned in the NCS response. */
    private JSONObject mGetData			= null;

    /** The version of the NLPS server that handled the request. */
    private String mNlpsVersion			= null;

    /** An instance of the server_specified_settings returned in the NCS response. The client must use these in the follow-up transaction. */
    private JSONObject mServerSpecifiedSettings	= null;


    private JSONArray mActions = null;

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#processServerResponse(org.json.JSONObject)
     */
    @Override
    public void processServerResponse(JSONObject response) {
        mJsonResponse = response;
        mStatus = parseStatus();
        mFinalResponse = parseFinalResponse();
        mDomain = parseDomain();
        mDialogPhase = parseDialogPhase();
        mSystemText = parseSystemText();
        mTtsText = parseTtsText();
        mResetDialog = parseResetDialog();
        mIntent = parseIntent();
        mGetData = parseGetData();
        mNlpsVersion = parseNlpsVersion();
        mServerSpecifiedSettings = parseServerSpecifiedSettings();
    }

    /**
     * Gets the app server results.
     *
     * @return the app server results
     */
    private JSONObject getAppServerResults() {
        if( mJsonResponse != null )
            return mJsonResponse.optJSONObject("appserver_results");

        return null;
    }

    /**
     * Gets the payload.
     *
     * @return the payload
     */
    private JSONObject getPayload() {
        JSONObject payload = null;

        JSONObject appserverResults = getAppServerResults();
        if (appserverResults != null)
            payload = appserverResults.optJSONObject("payload");

        return payload;
    }

    /**
     * Gets the actions.
     *
     * @return the actions
     */
    public JSONArray getActions() {
        if(mActions != null) return mActions;
        JSONObject payload = getPayload();
        if (payload != null)
            mActions = payload.optJSONArray("actions");
        return mActions;
    }

    /**
     * Find action by type.
     *
     * @param t the action type name
     * @return the action as a JSON object
     */
    private JSONObject findActionByType(String t) {
        JSONArray actions = getActions();
        if (actions == null) return null;

        for(int i = 0; i < actions.length(); i++){
            JSONObject o = actions.optJSONObject(i);
            String oType = o.optString("type");

            if( oType.equalsIgnoreCase(t)) return o;
        }

        return null;
    }

    /**
     * Find action by type.
     *
     * @param searchFromBottom this flag specifies whether or not to search for the action in the server response from bottom up or top down
     * @return the JSON object
     */
    private JSONObject findActionByType(boolean searchFromBottom) {
        if( !searchFromBottom )
            return findActionByType(ACTION_TYPE_DMSTATE);

        JSONArray actions = getActions();
        if (actions == null) return null;

        for(int i = actions.length(); i > 0; i--){
            JSONObject o = actions.optJSONObject(i-1);
            String oType = o.optString("type");

            if( oType.equalsIgnoreCase(ACTION_TYPE_DMSTATE)) return o;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#parseStatus()
     */
    @Override
    public String parseStatus() {
        mStatus = "";
        JSONObject appserverResults = getAppServerResults();
        if (appserverResults != null)
            mStatus = appserverResults.optString("status");

        return mStatus;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#getStatus()
     */
    @Override
    public String getStatus() {
        return mStatus;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#parseFinalResponse()
     */
    @Override
    public int parseFinalResponse() {
        mFinalResponse = 0;
        if (mJsonResponse != null)
            mFinalResponse = mJsonResponse.optInt("final_response");

        return mFinalResponse;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#isFinalResponse()
     */
    @Override
    public boolean isFinalResponse() {
        return (mFinalResponse != 0);
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#parseDomain()
     */
    @Override
    public String parseDomain() {
        mDomain = null;
        JSONObject action = findActionByType(ACTION_TYPE_DOMAIN);
        if (action != null)
            mDomain = action.optString("app");

        return mDomain;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#getDomain()
     */
    @Override
    public String getDomain() {
        return mDomain;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#parseDialogPhase()
     */
    @Override
    public String parseDialogPhase() {
        mDialogPhase = "";
        JSONObject action = findActionByType(true);
        if (action != null)
            mDialogPhase = action.optString("dialogPhase");

        return mDialogPhase;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#getDialogPhase()
     */
    @Override
    public String getDialogPhase() {
        return mDialogPhase;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#parseSystemText()
     */
    @Override
    public String parseSystemText() {
        mSystemText = "";
        JSONObject action = findActionByType(ACTION_TYPE_CONVERSATION);
        if (action != null)
            mSystemText = action.optString("text");

        return mSystemText;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#getSystemText()
     */
    @Override
    public String getSystemText() {
        return mSystemText;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#parseIntent()
     */
    @Override
    public String parseIntent() {
        mIntent = "";
        JSONObject action = findActionByType(ACTION_TYPE_APPLICATION);
        if (action != null)
            mIntent = action.optString("action");

        return mIntent;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#getIntent()
     */
    @Override
    public String getIntent() {
        return mIntent;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#parseTtsText()
     */
    @Override
    public String parseTtsText() {
        mTtsText= "";
        JSONObject action = findActionByType(ACTION_TYPE_TTS);
        if (action != null)
            mTtsText = action.optString("text");

        return mTtsText;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#getTtsText()
     */
    @Override
    public String getTtsText() {
        return mTtsText;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#parseResetDialog()
     */
    @Override
    public boolean parseResetDialog() {
        mResetDialog = false;
        JSONObject action = findActionByType(ACTION_TYPE_RESET);
        if (action != null)
            mResetDialog = true;

        return mResetDialog;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#resetDialog()
     */
    @Override
    public boolean resetDialog() {
        return mResetDialog;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#continueDialog()
     */
    @Override
    public boolean continueDialog() {
        return mDialogPhase != null && !mDialogPhase.equalsIgnoreCase(DIALOG_PHASE_TOP_LEVEL);
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#parseGetData()
     */
    @Override
    public JSONObject parseGetData() {
        mGetData = null;
        JSONObject action = findActionByType(ACTION_TYPE_GETDATA);
        if (action != null)
            mGetData = action;	//action.optJSONObject("payload");

        return mGetData;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#getGetData()
     */
    @Override
    public JSONObject getGetData() {
        return mGetData;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#parseNlpsVersion()
     */
    @Override
    public String parseNlpsVersion() {
        mNlpsVersion = "";

        JSONObject payload = getPayload();
        if (payload != null)
            mNlpsVersion = payload.optString("nlps_version");

        return mNlpsVersion;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#getNlpsVersion()
     */
    @Override
    public String getNlpsVersion() {
        return mNlpsVersion;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#parseServerSpecifiedSettings()
     */
    @Override
    public JSONObject parseServerSpecifiedSettings() {
        mServerSpecifiedSettings = null;

        JSONObject payload = getPayload();
        if (payload != null)
            mServerSpecifiedSettings = payload.optJSONObject("server_specified_settings");

        return mServerSpecifiedSettings;
    }

    /* (non-Javadoc)
     * @see com.nuance.dragon.toolkit.SampleCode.IDialogManager#getServerSpecifiedSettings()
     */
    @Override
    public JSONObject getServerSpecifiedSettings() {
        return mServerSpecifiedSettings;
    }
}


