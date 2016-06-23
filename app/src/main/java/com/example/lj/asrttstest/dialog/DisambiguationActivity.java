package com.example.lj.asrttstest.dialog;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

import com.example.lj.asrttstest.R;
import com.example.lj.asrttstest.TTSService;
import com.example.lj.asrttstest.info.AppInfo;
import com.example.lj.asrttstest.info.Global;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class DisambiguationActivity extends BaseCloudActivity {

    private static final String TAG = "DisambiguationActivity";

    /** The default NCS NLU App Server command. Default is DRAGON_NLU_APPSERVER_CMD. */
    private static final String DEFAULT_APPSERVER_COMMAND = "DRAGON_NLU_APPSERVER_CMD";

    /** The default dictation type. Default value is nma_dm_main. */
    private static final String DEFAULT_DICTATION_TYPE = "nma_dm_main";

    /** The default NCS ASR command. Default value is NMDP_ASR_CMD. */
    private static final String DEFAULT_ASR_COMMAND = "NMDP_ASR_CMD";

    /** The default NCS ASR+NLU command. Default value is DRAGON_NLU_ASR_CMD. */
    private static final String DEFAULT_NLU_ASR_COMMAND = "DRAGON_NLU_ASR_CMD";

    /** The default nlu profile name. Default value is REFERENCE_NCS. */
    private static final String DEFAULT_NLU_PROFILE = "REFERENCE_NCS";

    /** The rdomains. */
    private final HashMap<String, IRdomain> mRdomains = new HashMap<String, IRdomain>();

    /** An intance of an rdomain. */
    private IRdomain r;

    /**
     * Gets the rdoman.
     *
     * @return the rdomain
     */
    public IRdomain getRdoman() {
        return r;
    }

    /**
     * Sets the rdomain.
     *
     * @param r the rdomain to set
     */
    public void setRdomain(IRdomain r) {
        this.r = r;
    }

    /** A flag identifying if nlu is enabled for a transaction. */
    private boolean mNluEnabled = true;

    /** The dictation type. */
    private String mDictationType = null;

    /** The nlu profile. */
    private String mNluProfile = null;

    /** The server specified settings - returned to the client by the server in the NLU response. */
    private JSONObject mServerSpecifiedSettings = null;

    private ArrayList<String> ambigutyList;

    private Integer choosenID = 1;

    public DisambiguationActivity(Context _context, ArrayList<String> _ambiguty){
        super(_context);
        ambigutyList = _ambiguty;
        initCloudServices();
    }

    public DisambiguationActivity(Context _context, ArrayList<String> _ambiguty, Integer _chosenID){
        super(_context);
        ambigutyList = _ambiguty;
        choosenID = _chosenID;
        initCloudServices();
    }

    public DisambiguationActivity(Context _context, Integer _chosenID){
        super(_context);
        choosenID = _chosenID;
        initCloudServices();
    }

    /**
     * Start adk subdialog.
     *
     * @param mAdkSubdialogListener the listener
     */
    public void startAdkSubdialog(JSONObject jsonData, Transaction.Listener mAdkSubdialogListener) {
        try {
            Log.e(TAG, "Inside startAdkSubdialog()");
//            Log.e(TAG, "ADK Subdialog json data: " + jsonData.toString(4));
            Dictionary settings = this.createCommandSettings(DEFAULT_APPSERVER_COMMAND, "nma_dm_main", getLanguage());

            Dictionary root = new Dictionary();
            Dictionary appserver_data = new Dictionary();

            appserver_data.put("hour_format", "LOCALE");
            appserver_data.put("timezone", getTimeZone());
            appserver_data.put("time", getCurrentTime());
            //appserver_data.put("return_nlu_results", 1);
            appserver_data.put("action_mode", "default");
            appserver_data.put("application", getNluProfile());
            //appserver_data.put("dialog_seq_id", 2);

            //Ji's code
            String choose = "SLOTS:GENERIC_ORDER:"+choosenID.toString();
            appserver_data.put("message", choose);
//            appserver_data.put("message", "SLOTS:GENERIC_ORDER:1");

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

//            Log.d(TAG, "settings: " + settings.toString());
//            Log.d(TAG, "requestInfo: " + RequestInfo.toString());

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
        settings.put("nmaid", AppInfo.AppId);
        settings.put("dictation_language", language);
        settings.put("carrier", "ATT");
//        settings.put("dictation_type", type);
        settings.put("application_name",  mContext.getString(R.string.app_name));
        settings.put("application_session_id", AppInfo.applicationSessionID);
        settings.put("application_state_id", "45");
//        settings.put("location", getLastKnownLocation());
        settings.put("utterance_number", "5");
        settings.put("audio_source", "SpeakerAndMicrophone");

        //Ji Li's code
        settings.put("uid", AppInfo.IMEInumber);
        settings.put("nlps_use_adk", 1);
        settings.put("application", "TCL");
        settings.put("dictation_type", "gens_dm_main");

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
//        try {
//            Log.d("sss", result.toString(4));
//            Intent data=new Intent(Intent.ACTION_SENDTO);
//            data.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            data.setData(Uri.parse("mailto:rpbloom@gmail.com"));
//            data.putExtra(Intent.EXTRA_SUBJECT, "这是标题");
//            data.putExtra(Intent.EXTRA_TEXT, result.toString(4));
//            mContext.startActivity(data);
//        }catch (JSONException e){
//            e.printStackTrace();
//        }

        String feedback = "";
        String phoneNumber = "";
        JsonParser jsonParser = new JsonParser(result);
        TTSService ttsService = new TTSService(mContext);
        feedback = jsonParser.getTtsText();
        ttsService.performTTS(mContext, feedback);
        if(jsonParser.getDomain().equals("calling")){
            CallingDomainProc callingDomain
                    = new CallingDomainProc(mContext, jsonParser.getActions(), jsonParser.getTtsText());
            callingDomain.process();
            phoneNumber = callingDomain.phoneNumber;
            Log.d("sss", phoneNumber);
            if(jsonParser.getDialogPhase().equals("disambiguation")){
//                Log.d("sss", callingDomain.ambiguityList.toString());
//                JSONObject data = new JSONObject();
//                try {
//                    data.putOpt("message", "SLOTS:GENERIC_ORDER:1");
//                    DisambiguationActivity disambiguation = new DisambiguationActivity(mContext,callingDomain.ambiguityList);
////                                        disambiguation.doDataExchange(data, null);
//                    disambiguation.startAdkSubdialog(data, null);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                Global.ambiguityListChosenID = -1;
                Intent localIntent = new Intent(mContext, AmbiguityActivity.class);
                localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(localIntent);
            }
            if (jsonParser.getIntent().equals("call") && !phoneNumber.equals("") && ActivityCompat.checkSelfPermission(mContext,
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                mContext.startActivity(callIntent);
            }
        }
        if(jsonParser.getDomain().equals("messaging")){
            MessageDomainProc messageDomainProc
                    = new MessageDomainProc(mContext, jsonParser.getActions(), jsonParser.getTtsText());
            messageDomainProc.process();
            phoneNumber = messageDomainProc.getPhoneNumber();
            Log.d("sss", phoneNumber);
            Log.d("sss", messageDomainProc.getMessageContent());
            if (jsonParser.getIntent().equals("send") &&
                    !phoneNumber.equals("") &&
                    ActivityCompat.checkSelfPermission(mContext,
                            Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, messageDomainProc.getMessageContent(), null, null);
            }
        }
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
