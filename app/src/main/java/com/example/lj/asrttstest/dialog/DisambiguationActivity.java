package com.example.lj.asrttstest.dialog;

import android.content.Context;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.lj.asrttstest.R;
import com.example.lj.asrttstest.info.AppInfo;
import com.example.lj.asrttstest.upload.BaseCloudActivity;
import com.nuance.dragon.toolkit.audio.AudioType;
import com.nuance.dragon.toolkit.cloudservices.CloudConfig;
import com.nuance.dragon.toolkit.cloudservices.CloudServices;
import com.nuance.dragon.toolkit.cloudservices.DictionaryParam;
import com.nuance.dragon.toolkit.cloudservices.Transaction;
import com.nuance.dragon.toolkit.cloudservices.TransactionError;
import com.nuance.dragon.toolkit.cloudservices.TransactionResult;
import com.nuance.dragon.toolkit.data.Data;
import com.nuance.dragon.toolkit.data.Data.Dictionary;
import com.nuance.dragon.toolkit.data.Data.Sequence;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class DisambiguationActivity extends BaseCloudActivity {

    private static final String TAG = "DisambiguationActivity";

    /** The default NCS NLU App Server command. Default is DRAGON_NLU_APPSERVER_CMD. */
    private static final String DEFAULT_APPSERVER_COMMAND = "DRAGON_NLU_APPSERVER_CMD";

    private ArrayList<String> ambigutyList;

    public DisambiguationActivity(Context _context, ArrayList<String> _ambiguty){
        super(_context);
        ambigutyList = _ambiguty;
    }

    /**
     * Start adk subdialog.
     *
     * @param mAdkSubdialogListener the listener
     */
    void startAdkSubdialog(JSONObject jsonData, Transaction.Listener mAdkSubdialogListener) {
        try {
            Log.e(TAG, "Inside startAdkSubdialog()");
            Log.e(TAG, "ADK Subdialog json data: " + jsonData.toString(4));
            Dictionary settings = this.createCommandSettings(DEFAULT_APPSERVER_COMMAND, "nma_dm_main", getLanguage());

            Dictionary root = new Dictionary();
            Dictionary appserver_data = new Dictionary();
            Dictionary useredit = new Dictionary();

            Iterator<?> keys = jsonData.keys();
            while( keys.hasNext() ) {
                String key = (String)keys.next();
                Object obj = jsonData.get(key);
                if( obj instanceof String )
                    useredit.put(key, (String)obj);
                else if( obj instanceof JSONArray) {
                    JSONArray arr = (JSONArray)obj;
                    Sequence seq = new Sequence();
                    for(int i = 0; i < arr.length(); i++) {
                        Dictionary row = new Dictionary();
                        JSONObject r = arr.getJSONObject(i);
                        Iterator<?> rkeys = r.keys();
                        while( rkeys.hasNext() ) {
                            String rkey = (String)rkeys.next();
                            row.put(rkey,r.getString(rkey));
                        }
                        seq.add(row);
                    }
                    useredit.put(key, seq);
                }
            }

            appserver_data.put("useredit", useredit);

            appserver_data.put("hour_format", "LOCALE");
            appserver_data.put("timezone", getTimeZone());
            appserver_data.put("time", getCurrentTime());
            //appserver_data.put("return_nlu_results", 1);
            appserver_data.put("action_mode", "default");
            appserver_data.put("application", getNluProfile());
            //appserver_data.put("dialog_seq_id", 2);
            root.put("appserver_data", appserver_data);
            root.put("nlsml_results", 1);
            root.put("dg_result_as_json", 1);
            root.put("use_dg_domain", 0);
            root.put("type", "conversation");

            root.put("start", 0);
            root.put("end", 0);
            root.put("text", "");

            Log.d(TAG, "ADK Subdialog Request Data: " + root.toString());
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
                        onGetDataStarted(arg0);
                    }

                    @Override
                    public void onTransactionProcessingStarted(Transaction transaction) {

                    }

                    @Override
                    public void onTransactionResult(Transaction arg0, TransactionResult arg1,
                                                    boolean arg2) {
                        Log.d(TAG, "Transaction Completed...");
                        JSONObject results = arg1.getContents().toJSON();

                        if( r != null )
                            r.processNluResults(results);

                        onGetDataResult(arg0, results);

                    }

                    @Override
                    public void onTransactionError(Transaction arg0, TransactionError arg1) {
                        Log.d(TAG, "Transaction Error...");
                        onGetDataError(arg0, arg1.toJSON());
                    }

                    @Override
                    public void onTransactionIdGenerated(String s) {

                    }
                }, 3000, true);

            Log.d(TAG, "settings: " + settings.toString());
            Log.d(TAG, "requestInfo: " + RequestInfo.toString());

            this.mCloudServices.addTransaction(dut, 1);
            dut.addParam(RequestInfo);
            dut.finish();

        } catch( Exception e) {
            //Log.e(TAG, e.getMessage());
            Log.e(TAG, "Exception thrown in RecognizerCloudActivity.startAdkSubdialog()");
            e.printStackTrace();
        }
    }
    /**
     * Do data exchange.
     *
     * @param jsonData the json data
     * @param listener the listener
     */
    void doDataExchange(JSONObject jsonData, Transaction.Listener listener) {
        try {
            Data.Dictionary settings = this.createCommandSettings(DEFAULT_APPSERVER_COMMAND, getDictationType(), getLanguage());

            Dictionary root = new Dictionary();
            Dictionary appserver_data = new Dictionary();
            Sequence actions = new Sequence();
            Dictionary entry = new Dictionary();
            Dictionary payload = new Dictionary();

            entry.put("type", "get_data");
            entry.put("req_id", jsonData.getString("req_id"));
            entry.put("dpv", jsonData.getString("dpv"));

            // call rdomain-specific handler to build payload....
            String rdomain = jsonData.getJSONObject("payload").getString("rdomain");
            r = mRdomains.get(rdomain);
            if( r != null ) {
                JSONArray attributes = jsonData.getJSONObject("payload").getJSONArray("attributes");
                for(int i = 0; i < attributes.length(); i++) {
                    // payload.put(attributes.getString(i), "0");
                    String attrName = attributes.getString(i);
                    Object value = r.getValue(attrName);
                    if( value == null ) continue;
                    if( value instanceof String ) payload.put(attrName, (String) value);
                    else if( value instanceof Integer ) payload.put(attrName, (Integer) value);
                    else if( value instanceof Data ) payload.put(attrName, (Data) value);
                    else Log.e(TAG, "get_data attribute {"+ attrName +"} value of unknown type {"+ value.getClass().getSimpleName() +"}");
                }
            }

            if( payload.getEntries().size() > 0 ) entry.put("payload", payload);

            actions.add(entry);
            appserver_data.put("actions", actions);

            // this should really be passed in as part of the jsonData...
            Dictionary server_specified_settings = new Dictionary();
            server_specified_settings.put("fieldID", "dm_main");
            appserver_data.put("server_specified_settings", server_specified_settings);


            appserver_data.put("hour_format", "LOCALE");
            appserver_data.put("timezone", getTimeZone());
            appserver_data.put("time", getCurrentTime());
            //appserver_data.put("return_nlu_results", 1);
            appserver_data.put("action_mode", "default");
            appserver_data.put("application", getNluProfile());
            //appserver_data.put("dialog_seq_id", 2);
            root.put("appserver_data", appserver_data);
            root.put("nlsml_results", 1);
            root.put("dg_result_as_json", 1);
            root.put("use_dg_domain", 0);
            root.put("type", "conversation");

            root.put("start", 0);
            root.put("end", 0);
            root.put("text", "");

            DictionaryParam RequestInfo = new DictionaryParam("REQUEST_INFO", root);

            Log.d(TAG, "Creating GetData Transaction...");

            Transaction dut;

            if( listener != null )
                dut = new Transaction(DEFAULT_APPSERVER_COMMAND, settings, listener, 3000, true);
            else
                dut = new Transaction(DEFAULT_APPSERVER_COMMAND, settings, new Transaction.Listener() {

                    @Override
                    public void onTransactionStarted(Transaction arg0) {
                        Log.d(TAG, "Transaction Started...");
                        onGetDataStarted(arg0);
                    }

                    @Override
                    public void onTransactionProcessingStarted(Transaction transaction) {
                        Log.d(TAG, "Transaction Processing Started...");
                    }

                    @Override
                    public void onTransactionResult(Transaction arg0, TransactionResult arg1,
                                                    boolean arg2) {
                        Log.d(TAG, "Transaction Completed...");
                        JSONObject results = arg1.getContents().toJSON();

                        if( r != null )
                            r.processNluResults(results);

                        onGetDataResult(arg0, results);

                    }

                    @Override
                    public void onTransactionError(Transaction arg0, TransactionError arg1) {
                        Log.d(TAG, "Transaction Error...");
                        onGetDataError(arg0, arg1.toJSON());
                    }

                    @Override
                    public void onTransactionIdGenerated(String s) {
                        Log.d(TAG, "Transaction Id Generated: " + s);

                    }
                }, 3000, true);

            Log.d(TAG, "settings: " + settings.toString());
            Log.d(TAG, "requestInfo: " + RequestInfo.toString());

            this.mCloudServices.addTransaction(dut, 1);
            dut.addParam(RequestInfo);
            dut.finish();

        } catch( Exception e) {
            Log.e(TAG, e.getMessage());
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
    private Dictionary createCommandSettings(String commandName, String type, String language) {

        Data.Dictionary settings = new Data.Dictionary();

        settings.put("command", commandName);
        settings.put("nmaid", this.mCredentials.getAppID());
        settings.put("dictation_language", language);
        settings.put("carrier", "ATT");
        settings.put("dictation_type", type);
        settings.put("application_name",  mContext.getString(R.string.app_name));
        settings.put("application_session_id", ((this.mAppSessionId == null) ? UUID.randomUUID().toString() : this.mAppSessionId) );
        settings.put("application_state_id", "45");
        settings.put("location", getLastKnownLocation());
        settings.put("utterance_number", "5");
        settings.put("audio_source", "SpeakerAndMicrophone");

        return settings;
    }

    /**
     * Checks if is nlu enabled.
     *
     * @return true, if is nlu enabled
     */
    boolean isNluEnabled() {
        return mNluEnabled;
    }

    /* (non-Javadoc)
	 * @see com.nuance.dragon.toolkit.sample.ICloudActivity#setNluEnabled(boolean)
	 */
    /**
     * Sets the nlu enabled flag.
     *
     * @param value the new nlu enabled
     */
    public void setNluEnabled( boolean value ) {
        mNluEnabled = value;
    }

    /**
     * Gets the time zone.
     *
     * @return the time zone
     */
    protected String getTimeZone() {
        TimeZone tz = TimeZone.getDefault();

        Log.d(TAG, "Device timezone is: " + tz.getID());
        return tz.getID();
    }

    /**
     * Gets the current time.
     *
     * @return the current time
     */
    protected String getCurrentTime() {
        String format = "yyyy-MM-dd'T'HH:mm:ssZ";

        TimeZone tz = TimeZone.getDefault();
        Log.d(TAG, "Device timezone is: " + tz.getID());

        DateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        dateFormat.setTimeZone(tz);

        Date date = new Date();
        Log.d(TAG, "Device timestamp is: " + dateFormat.format(date));

        return dateFormat.format(date);
    }

    /**
     * Gets the dictation type.
     *
     * @return the dictation type
     */
    private String getDictationType() {
        if( mDictationType != null )
            return mDictationType;

        return DEFAULT_DICTATION_TYPE;
    }

    /**
     * Sets the adk subdialog.
     *
     * @param value the new adk subdialog
     */
    void setAdkSubdialog(String value) {
    	/* The m adk subdialog. */
        String mAdkSubdialog = value;
    }

    /**
     * Sets the dictation type.
     *
     * @param value the new dictation type
     */
    void setDictationType(String value) {
        mDictationType = value;
    }

    /**
     * Gets the asr command.
     *
     * @return the asr command
     */
    private String getAsrCommand() {
        if( isNluEnabled() )
            return DEFAULT_NLU_ASR_COMMAND;
        else
            return DEFAULT_ASR_COMMAND;
    }

    /**
     * Gets the appserver command.
     *
     * @return the appserver command
     */
    String getAppserverCommand() {
        return DEFAULT_APPSERVER_COMMAND;
    }

    /**
     * Gets the nlu profile.
     *
     * @return the nlu profile
     */
    private String getNluProfile() {
        if( mNluProfile != null )
            return mNluProfile;

        return DEFAULT_NLU_PROFILE;
    }

    /**
     * Sets the nlu profile.
     *
     * @param value the new nlu profile
     */
    void setNluProfile(String value) {
        mNluProfile = value;
    }
    /**
     * Gets the server specified settings.
     *
     * @return the server specified settings
     */
    private JSONObject getServerSpecifiedSettings() {
        return mServerSpecifiedSettings;
    }

    /**
     * On recognition result.
     *
     * @param results the results
     */
    private void onRecognitionResult(JSONObject results) {
        /* Do nothing */
    }

    /**
     * On recognition error.
     *
     * @param error the error
     */
    private void onRecognitionError(JSONObject error) {
        /* Do nothing */
    }

    /**
     * On get data started.
     *
     * @param t the t
     */
    private void onGetDataStarted(Transaction t) {

    }

    /**
     * On get data result.
     *
     * @param t the t
     * @param result the result
     */
    private void onGetDataResult(Transaction t, JSONObject result) {

    }

    /**
     * On get data error.
     *
     * @param t the t
     * @param error the error
     */
    private void onGetDataError(Transaction t, JSONObject error) {

    }

}
