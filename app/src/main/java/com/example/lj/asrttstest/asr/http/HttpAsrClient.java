package com.example.lj.asrttstest.asr.http;

/**
 * Created by lj on 16/6/30.
 */

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.example.lj.asrttstest.asr.http.LatencyMonitor.Marker;
import com.example.lj.asrttstest.info.AppInfo;
import com.example.lj.asrttstest.info.Global;

public class HttpAsrClient {

    // ********************* PRIVATE FIELDS *********************

    /** The hostname of the server hosting the HTTP Interface. */
    private String _host;

    /** The TCP port of the server hosting the HTTP Interface. */
    private int _port;

    /** The application's nmaid. */
    private String _appId;

    /** The 128-byte string app key. */
    private String _appKey;

    /** All connectivity to production Nuance Cloud Services must be over TLS. However, if you are connecting to one of our
     * Development environments you may need to connect over a non-secure transport */
    private boolean _useTLS;

    /** Nuance's Production data centers use trusted root certificates. Non-production run-times such as MTL-Dev do not. */
    private boolean _requireTrustedRootCert = false;

    /** A unique identifier that enables speaker-dependent acoustic and language model adaptation within Nuance Cloud Services */
    private String _userID;

    /** Streaming responses requires special server-side provisioning for your nmaid for this feature to take effect */
    private boolean _streamingResults = true;

    /** Flag to track whether or not to enable profanity filtering. Special server-side provisioning for your nmaid is required for this feature to take effect. */
    protected boolean _profanityFilteringEnabled = false;

    /** Flag to track whether or not to enable NLU */
    protected boolean _nluEnabled = true;

    /** Flag to track using text instead of audio for NLU interpretation */
    private boolean _useTextNlu = true;

    /** The text to pass in for NLU interpretation */
    private String _message = null;

    // ********************* PROTECTED FIELDS *********************

    /** Flag to track if batch mode is enabled. Default is false. */
    protected boolean batchMode = false;

    /** Enable/Disable console output logging level. Default is false. Enable for debugging. */
    protected boolean verbose = false;

    /** The socket connection to the HTTP service */
    private volatile Socket s = null;

    /** The socket reader */
    private volatile BufferedReader br = null;

    /** The HTTP socket output stream */
    protected volatile OutputStream out = null;

    /** The boundary string for chunked requests */
    protected volatile String boundary = null;

    /** Flag to track if query has failed */
    protected volatile boolean queryFailed = false;

    /** An instance of the LatencyMonitor class. */
    protected LatencyMonitor transactionLatency = new LatencyMonitor();

    /** A synchronization object to wait for asynchronous web socket responses and task completion. */
    private static Object waitLock = new Object();

    /** The client-side transaction timeout. Default is 5000ms. */
    protected final int txnTimeout = 10000;

    public JSONObject serverResponseJSON = null;
    /**
     * Request Data parameters are passed in with every NCS transaction request. Many of these parameters are used for
     * tuning language and acoustic models. Others are for reporting/analytics. Therefore, it's important to ensure your
     * production application is designed to set accurate and valid values using platform-level API's where applicable,
     * and not just copy/paste these hard-coded ones.
     *
     */
    protected class RequestData {
        static final String APP_NAME = "Nuance Java Sample HTTP App";	// Your application's name
        static final String APP_VERSION = "1.0";						// Your application's version number
        static final String ORGANIZATION_ID = "NUANCE";					// The name of your company or organization

        public String IN_CODEC = Codec.PCM_16_16K;						// Inbound (i.e. ASR) audio format. Please refer to documentation for complete list of supported values.
        public String OUT_CODEC = Codec.PCM_16_16K;						// Outbound (i.e. TTS) audio format. Ditto...
        static final String AUDIO_SOURCE = "SpeakerAndMicrophone";		// The audio source for a given transaction. Supported values are

        static final String COMMAND_NAME_ASR = "NMDP_ASR_CMD";				// NCS Command Name. NMDP_ASR_CMD = Dictation. NMDP_TTS_CMD = Text-to-Speech. The complete set of available command names are provided upon request.
        static final String COMMAND_NAME_NLU_ASR = "DRAGON_NLU_ASR_CMD";
        static final String COMMAND_NAME_NLU_TEXT = "DRAGON_NLU_APPSERVER_CMD";
        public String LANGUAGE = "eng-USA";								// Supported language codes can be found here: http://dragonmobile.nuancemobiledeveloper.com/public/index.php?task=supportedLanguages
        public String DICTATION_TYPE = "nma_dm_main";					// This is also sometimes referred to as Topic and Language Model. Supported values are: nma_dm_main (for NLU personal assistant), Dictation, Websearch, and DTV. Please reach out to Sales or PS for available language support for a given dictation type.
        static final String UI_LANGUAGE = "en";							// The keyboard language
        public String APPLICATION = AppInfo.Application;

        static final String CARRIER = "unknown";						// Name of your device's carrier, if applicable.
        static final String PHONE_NETWORK = "wifi";						// The device's network type
        final String PHONE_OS = System.getProperty("os.name");			// The OS of the device the application is running on
        final String DEVICE_MODEL = System.getProperty("os.arch");		// Name of your device's model
        final String PHONE_SUBMODEL = System.getProperty("os.version");	// The device's submodel, if applicable

        static final String LOCALE = "USA";								// Current locale of the device
        static final String LOCATION = "";								// If available, geo-location coordinates (e.g. <+45.5086699, -73.5539925> +/- 99.00m)

        static final String APPLICATION_STATE_ID = "0";					// Client-defined state id's, if applicable.

        private String _applicationSessionID = null;					// Track multiple transaction requests within a single application session
        private int _utteranceNumber = 1;								// Track the sequence of transaction requests within an application session

        public String initApplicationSessionID() {
            _applicationSessionID = AppInfo.applicationSessionID;

            return _applicationSessionID;
        }

        public String getApplicationSessionID() {
            return _applicationSessionID;
        }

        public void resetUtteranceNumber() {
            _utteranceNumber = 5;
        }

        public int getUtteranceNumber() {
            return _utteranceNumber;
        }
    }

    /** An instance of the RequestData class. */
    protected RequestData _requestData;



    // ********************* CONSTRUCTORS *********************

    /** Constructor */
    public HttpAsrClient(String host, int port, boolean useTLS, String appId,
                         String appKey, String topic, String langCode) {

        write("Host: " + host + ":" + port);
        _host = host;
        _port = port;
        _appId = appId;
        _appKey = appKey;
        _useTLS = useTLS;

        _requestData = new RequestData();
        _requestData.LANGUAGE = langCode;
        _requestData.DICTATION_TYPE = topic;

    }



    // ********************* GETTERS / SETTERS *********************

    public void enableBatchMode() {
        batchMode = true;
    }

    public void enableProfanityFiltering() {
        _profanityFilteringEnabled = true;
    }

    public void setApplication( String application ) {
        _requestData.APPLICATION = application;
    }

    public String getApplication() {
        return _requestData.APPLICATION;
    }

    public void enableNLU() {
        _nluEnabled = true;
    }

    public void disableNLU() {
        _nluEnabled = false;
    }

    public boolean isNluEnabled() {
        return _nluEnabled;
    }

    public void enableTextNLU() {
        _nluEnabled = true;
        _useTextNlu = true;
    }

    public boolean isNluTextEnabled() {
        return _useTextNlu;
    }

    public int getTxnTimeout() {
        return txnTimeout;
    }

    public void disableTrustedRootCert() {
        _requireTrustedRootCert = false;
    }

    public boolean isVerbose() {
        return verbose;
    }

    // ********************* CONNECTION AND NCS COMMAND HANDLERS *********************

    // Monitor socket timeout so app properly stops listening if no speech detected or recorder timeout fails...
    private volatile Thread connectionMonitorThread = null;
    private volatile static Object connectionWaitLock = new Object();
    protected int connectionTimeout = Global.CONNECTION_TIME_OUT;
    protected volatile boolean connectionTimedOut = false;
    protected volatile boolean headersSent = false;
    private volatile boolean reuseStatusCode = false;

    Runnable connectionTimeoutMonitor = new Runnable() {

        @Override
        public void run() {
            connectionTimedOut = false;

            synchronized(connectionWaitLock) {
                try {
                    write( "Connection timer started..." );
                    connectionWaitLock.wait(connectionTimeout);

                    write( "Connection timed out!" );

                    // reset some flags...
                    connectionTimedOut = true;
                    headersSent = false;
                    reuseStatusCode = false;
                    br.close();

                } catch( InterruptedException e) {
                    // Timer cancelled. Nothing to do...
//                    write( "Connection timer interrupted..." );
                    Log.d("sss", "Connection timer interrupted...");
                } catch( IOException e ) {
//                    write("Error closing socket reader: " + e.getMessage());
                    Log.d("sss", "Error closing socket reader: " + e.getMessage());
                }
            }

        }

    };

    protected Socket connectToServer() {
        try {

            // Create a socket connection to the server...
            if( s == null || s.isClosed() ) {
                write("Creating socket connection");
                s = SocketFactory.createSocket(_host, _port, _useTLS, _requireTrustedRootCert);
                br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                transactionLatency.setMarker(Marker.connected);
//                _logData.timeToConnect = transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.connected);
//                write("Time to establish socket connection: " + _logData.timeToConnect);

                connectionTimeout = s.getSoTimeout();
                write("Keep Alive Enabled: " + s.getKeepAlive());
                write("Socket Timeout: " + s.getSoTimeout());

                if( connectionMonitorThread != null ) {
                    connectionMonitorThread.interrupt();
                }

            }
            else {
                write("Socket connection already created");

                transactionLatency.setMarker(Marker.connected);
//                _logData.timeToConnect = transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.connected);
//                write("Time to establish socket connection: " + _logData.timeToConnect);

                // Interrupt any previously started connection monitor. We'll reset the timer below...
                connectionMonitorThread.interrupt();
            }

            // Send the headers to force a connection prior to audio capture to help minimize latency due to connection overhead
            if( !headersSent ) {
                /** Create a boundary for multi-part data upload */
                boundary = UUID.randomUUID().toString().replaceAll("-", "");
                out = s.getOutputStream();

                /** Send headers... */
                sendHeaders(out, _host, boundary);
                headersSent = true;
            }

            // Monitor the connection timeout
            connectionMonitorThread = new Thread(connectionTimeoutMonitor);
            connectionMonitorThread.start();

            return s;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected String sendNluTextQueryCommands() {

        /** Create a secure socket */
        Socket s = connectToServer();

        /** Listen and process the NCS response on a separate thread.
         * This is necessary for receiving word-by-word results while streaming audio to the server */
        class ResultListener implements Runnable
        {
            Socket _s;
            ResultListener(Socket s) {
                _s = s;
            }

            public void run() {
                try
                {
                    processResponse(_s);
                }
                catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
        Thread t = new Thread(new ResultListener(s));
        t.start();

		/*
		 * Refer to the "NCS HTTP Services 2.0 Users Guide" for more details on integrating via HTTP Services
		 */

        transactionLatency.setMarker(Marker.query_begin);

        /** Send the NCS Command request data */
        sendRequestData(out, _appId, _appKey, _userID, _requestData.getApplicationSessionID(), _requestData.getUtteranceNumber(), boundary);

        /** Send the Dictation parameters */
        transactionLatency.setMarker(Marker.audio_streaming_begin);	// re-using audio_streaming markers to simplify logging logic
        sendDictParameter(out, boundary);
        transactionLatency.setMarker(Marker.audio_streaming_end);

        transactionLatency.setMarker(Marker.query_complete);

        //Ji's code
//        wait4TerminateSignal(getTxnTimeout());

        return boundary;

    }

    /**
     * Refer to the "NCS HTTP Services 2.0 Users Guide" for more details on headers that need to be sent with a dictation request
     *
     * @param out
     * @param host
     * @param boundary
     */
    protected void sendHeaders(OutputStream out, String host, String boundary) {

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("POST /NmspServlet/ HTTP/1.1\r\n");
            sb.append("Host: " + host + "\r\n");
            sb.append("Connection: Keep-Alive\r\n");
            sb.append("Keep-Alive: timeout=100\r\n");
            sb.append("User-Agent: " + RequestData.APP_NAME + " " + RequestData.APP_VERSION + "\r\n");
            sb.append("Transfer-Encoding: chunked\r\n");
            sb.append("Content-Type: multipart/form-data; boundary=" + boundary + "\r\n");
            sb.append("\r\n");

            out.write(sb.toString().getBytes());
            out.flush();

            Chunk chunk = new Chunk();
            chunk.append(sb.toString());
//            this.printDataSent(chunk);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refer to the "NCS HTTP Services 2.0 Users Guide" for more details on Request Data that need to be sent with a dictation request
     *
     * @param out
     * @param appId
     * @param appKey
     * @param uId
     * @param appSessionId
     * @param uttNumber
     * @param boundary
     */
    protected void sendRequestData(OutputStream out, String appId, String appKey, String uId, String appSessionId, int uttNumber, String boundary) {

        try {

            JSONObject json = new JSONObject();
            JSONObject cmdDict = new JSONObject();

            json.put("appId", appId);
            json.put("appKey", appKey);
            json.put("uId", uId);
            json.put("inCodec", _requestData.IN_CODEC);
            json.put("outCodec", _requestData.OUT_CODEC);
            json.put("cmdName", ( isNluEnabled() ? ( (isNluTextEnabled()) ? RequestData.COMMAND_NAME_NLU_TEXT : RequestData.COMMAND_NAME_NLU_ASR ) : RequestData.COMMAND_NAME_ASR ) );
            json.put("appName", RequestData.APP_NAME);
            json.put("appVersion", RequestData.APP_VERSION);
            json.put("language", _requestData.LANGUAGE);
            //json.put("cmdTimeout", "10000");		// Optional. Reduce the value of cmdTimeout (in ms) if waiting for a long-running transaction is a concern...
            json.put("carrier", RequestData.CARRIER);
            json.put("deviceModel", _requestData.DEVICE_MODEL);

            cmdDict.put("dictation_type", _requestData.DICTATION_TYPE);
            cmdDict.put("dictation_language", _requestData.LANGUAGE);
            cmdDict.put("application", _requestData.APPLICATION);
            cmdDict.put("locale", RequestData.LOCALE);
            cmdDict.put("application_name", RequestData.APP_NAME);
            cmdDict.put("organization_id", RequestData.ORGANIZATION_ID);
            cmdDict.put("phone_OS", _requestData.PHONE_OS);
            cmdDict.put("phone_network", RequestData.PHONE_NETWORK);
            cmdDict.put("audio_source", RequestData.AUDIO_SOURCE);
            cmdDict.put("location", RequestData.LOCATION);
            cmdDict.put("application_session_id", appSessionId);
            cmdDict.put("utterance_number", uttNumber);
            cmdDict.put("ui_language", RequestData.UI_LANGUAGE);
            cmdDict.put("phone_submodel", _requestData.PHONE_SUBMODEL);
            cmdDict.put("application_state_id", RequestData.APPLICATION_STATE_ID);

            json.put("cmdDict", cmdDict);

            Chunk chunk = new Chunk();
            chunk.append("--" + boundary + "\r\n");
            chunk.append("Content-Disposition: form-data; name=\"RequestData\"\r\n");
            chunk.append("Content-Type: application/json; charset=utf-8\r\n");
            chunk.append("Content-Transfer-Encoding: 8bit\r\n");
            chunk.append("\r\n");
            chunk.append(json.toString(2));
            chunk.append("\r\n");
            chunk.writeTo(out);

//            this.printDataSent(chunk);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * The DictParameter contains an NCS REQUEST_INFO dictionary. This example illustrates the required start, end, and text
     * parameters. However, the REQUEST_INFO dictionary has many available options for managing the interaction with NCS Services.
     * For example, for a Dictation request, the format of the result can be specified. Features such as word-by-word streaming,
     * auto-punctuation, and profanity filtering can also be specified. And custom application language models can be requested.
     * In order to take advantage of some of these features, server-side provisioning unique to your App Id must first be implemented.
     * Please coordinate with Sales to engage with Professional Services if you'd like to explore these advanced features.
     *
     */
    protected void sendDictParameter(OutputStream out, String boundary) {
        try {
            JSONObject json = new JSONObject();
            json.put("start",0);
            json.put("end",0);
            json.put("text","");
            json.put("nbest_text_results", 1);

            if(_streamingResults)
                json.put("intermediate_response_mode", "NoUtteranceDetectionWithPartialRecognition");

            if(_profanityFilteringEnabled)
                json.put("enable_profanity_filtering", 1);

            //json.put("wake_up_phrase", "Hello Joe");


            //Ji's code to add grammar
            /*"grammar_list": [
            {
            "id" : "contacts",
            "type" : "structured_content",
            "structured_content_category" : "contacts",
            "checksum" : "<whatever the checksum value is from your last data upload>"
            }
            ]
            */
            JSONArray grammarArray = new JSONArray();
            JSONObject contactGrammar = new JSONObject();
            contactGrammar.put("id", "contacts");
            contactGrammar.put("type", "structured_content");
            contactGrammar.put("structured_content_category", "contacts");
            contactGrammar.put("checksum", AppInfo.dataUploadReturnedCheckSum);
            grammarArray.put(contactGrammar);
            json.put("grammar_list", grammarArray);


            if( isNluEnabled() ) {
                JSONObject appServerData = new JSONObject();

                appServerData.put("nlps_return_abstract_nlu", 1);
                appServerData.put("nlps_use_adk", 1);

                if( isNluTextEnabled() ) {
                    appServerData.put("message", _message);
                }

                json.put("appserver_data", appServerData);
            }

//            Log.d("sss", json.toString(4));

            Chunk chunk = new Chunk();
            chunk.append("--" + boundary + "\r\n");
            chunk.append("Content-Disposition: form-data; name=\"DictParameter\";paramName=\"REQUEST_INFO\"\r\n");
            chunk.append("Content-Type: application/json; charset=utf-8\r\n");
            chunk.append("Content-Transfer-Encoding: 8bit\r\n");
            chunk.append("\r\n");
            chunk.append(json.toString(2));
            chunk.append("\r\n");
            chunk.writeTo(out);

//            this.printDataSent(chunk);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The request dictionary data for resetting a user's acoustic and language model profiles on the server.
     *
     */
    protected void sendUserProfileResetRequestData(OutputStream out, String boundary) {
        try {

            JSONObject json = new JSONObject();
            JSONObject cmdDict = new JSONObject();

            json.put("appId", this._appId);
            json.put("appKey", this._appKey);
            json.put("uId", this._userID);
            json.put("inCodec", _requestData.IN_CODEC);
            json.put("outCodec", _requestData.OUT_CODEC);
            json.put("cmdName", "NVC_RESET_USER_PROFILE_CMD");
            json.put("appName", RequestData.APP_NAME);
            json.put("appVersion", RequestData.APP_VERSION);
            json.put("language", _requestData.LANGUAGE);
            //json.put("cmdTimeout", "10000");		// Optional. Reduce the value of cmdTimeout if waiting for a long-running transaction is a concern...
            json.put("carrier", RequestData.CARRIER);
            json.put("deviceModel", _requestData.DEVICE_MODEL);

            cmdDict.put("dictation_language", _requestData.LANGUAGE);
            cmdDict.put("locale", RequestData.LOCALE);
            cmdDict.put("application_name", RequestData.APP_NAME);
            cmdDict.put("organization_id", RequestData.ORGANIZATION_ID);
            cmdDict.put("phone_OS", _requestData.PHONE_OS);
            cmdDict.put("phone_network", RequestData.PHONE_NETWORK);
            cmdDict.put("audio_source", RequestData.AUDIO_SOURCE);
            cmdDict.put("location", RequestData.LOCATION);
            cmdDict.put("ui_language", RequestData.UI_LANGUAGE);
            cmdDict.put("phone_submodel", _requestData.PHONE_SUBMODEL);
            cmdDict.put("application_state_id", RequestData.APPLICATION_STATE_ID);

            json.put("cmdDict", cmdDict);

            Chunk chunk = new Chunk();
            chunk.append("--" + boundary + "\r\n");
            chunk.append("Content-Disposition: form-data; name=\"RequestData\"\r\n");
            chunk.append("Content-Type: application/json; charset=utf-8\r\n");
            chunk.append("Content-Transfer-Encoding: 8bit\r\n");
            chunk.append("\r\n");
            chunk.append(json.toString(2));
            chunk.append("\r\n");
            chunk.writeTo(out);

//            this.printDataSent(chunk);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Notify the NCS service that all request data and audio has been sent
     *
     * @param out
     * @param boundary
     */
    protected void sendTerminatingChunk(OutputStream out, String boundary) {

        try {
            Chunk chunk = new Chunk();
            chunk.append("--" + boundary + "--\r\n");
            chunk.writeTo(out);

            Chunk terminatingChunk = new Chunk();
            terminatingChunk.writeTo(out);

//            this.printDataSent(chunk);
            write("Sent terminating chunk.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *  Read and print out response from the server
     *
     *  Please refer to the HTTP 2.0 User Guide and NCS Command Specification, available from Professional Services,
     *  for additional details of NCS responses and how to parse out specific elements
     *
     * @param s
     * @throws IOException
     * @throws JSONException
     */
    protected void processResponse(Socket s) throws IOException, JSONException {

        ResponseParser rp = new ResponseParser();

        try {
            String t;
            boolean firstResponse = !reuseStatusCode;	// true;
            boolean headersParsed = false;
            boolean successfulStatusCode = true;
            Map<String, String> headers = new HashMap<String, String>();
            Map<String, String> bodyParts;
            String boundary = null;
            transactionLatency.removeMarker(Marker.initial_response);

            while((t = br.readLine()) != null) {

                if( firstResponse ) {

                    firstResponse = false;

                    if( !t.contains("200") && !t.contains("201") ) {
                        successfulStatusCode = false;
                        queryFailed = true;
                    }

                    write("Status Line: " + t);
                    reuseStatusCode = true;
                    continue;
                }
                if( !firstResponse && !headersParsed ) {
                    // parse headers...
                    headers = rp.parseHeaders(br);
                    headersParsed = true;

                    write("\r\nHeaders: ");
                    for( Map.Entry<String, String> entry : headers.entrySet() ) {
                        write( "\t" + entry.getKey() + ": " + entry.getValue() );
                    }
                    write();

                    // Save the session id. This is critical for debugging issues with the service
                    if( headers.containsKey("nuance-sessionid") )
//                        _logData.sessionId = headers.get("nuance-sessionid");

                    // Save the multi-part boundary if it exists. We'll need this to properly parse remaining body content
                    if( headers.containsKey("content-type") ) {
                        String contentType = headers.get("content-type");
                        if( contentType.contains("boundary=") ) {
                            String[] arr = contentType.split("=", 2);
                            if( arr != null && arr.length == 2 )
                                boundary = arr[1];
                        }
                    }

                    continue;
                }
                if( headersParsed && !successfulStatusCode ) {
                    // There won't be any structured body content, so just print out the response body as is
                    // For a real-world app, you'll need to inspect the status line in more detail and provide appropriate error handling
                    write(t);
                    // If the server has sent a value of "0", this indicates that the last multi-part response has been received and we can stop looping...
                    if( t.equals("0") ) {
                        if( transactionLatency.getMarker(Marker.initial_response) == -1 ) {
                            transactionLatency.setMarker(Marker.initial_response);
                            write();
                            if( _streamingResults ) {
                                write("Time from first audio packet to first reponse: " + transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_begin, Marker.initial_response) + " seconds");
//                                _logData.timeToFirstResponse = transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_begin, Marker.initial_response);
                            } else {
                                write("Time from last audio packet to first reponse: " + transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_end, Marker.initial_response) + " seconds");
//                                _logData.timeToFirstResponse = transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_end, Marker.initial_response);
                            }
                        }

                        if( transactionLatency.getMarker(Marker.final_response) == -1 ) {
                            transactionLatency.setMarker(Marker.final_response);
//                            _logData.timeToFinalResponse = transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_end, Marker.final_response);
                        }
                        break;
                    }

                    continue;
                }
                if( headersParsed && successfulStatusCode ) {

                    if( transactionLatency.getMarker(Marker.initial_response) == -1 ) {
                        transactionLatency.setMarker(Marker.initial_response);
                        write();
                        if( _streamingResults ) {
                            write("Time from first audio packet to first reponse: " + transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_begin, Marker.initial_response) + " seconds");
//                            _logData.timeToFirstResponse = transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_begin, Marker.initial_response);
                        } else {
                            write("Time from last audio packet to first reponse: " + transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_end, Marker.initial_response) + " seconds");
//                            _logData.timeToFirstResponse = transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_end, Marker.initial_response);
                        }
                    }

                    // If the response is not a chunked response containing boundaries, then the last buffer read should contain results...
                    if( boundary == null && headers.containsKey("content-disposition") ) {
                        bodyParts = new HashMap<String, String>();
                        bodyParts.putAll(headers);
                        bodyParts.put("json_1", t);
                    }
                    else	// hand off to body part parser...
                        bodyParts = rp.parseBodyPart(br, boundary);

                    // In case the server returns a header instead of a blank line between the last boundary and the new chunk...
                    if( t.length() > 2 ) {
                        String[] arr = t.split(":", 2);
                        if (arr != null & arr.length == 2) {
                            // Grab the entity header
                            bodyParts.put(arr[0].trim().toLowerCase(), arr[1].trim().toLowerCase());
                        }
                    }

                    if( bodyParts.containsKey("content-disposition") && bodyParts.get("content-disposition").contains("queryerror") ) {
                        queryFailed = true;
                        write("Server Response: ");
                        write( bodyParts.get("json_1") );
                    }
                    else if( bodyParts.containsKey("content-disposition") && bodyParts.get("content-disposition").contains("queryretry") ) {
                        queryFailed = true;
                        write("Server Response: ");
                        write( bodyParts.get("json_1") );
                    }
                    else if( bodyParts.containsKey("content-disposition") && bodyParts.get("content-disposition").contains("queryresult") ) {
                        String json_1 = (bodyParts.containsKey("json_1")) ? bodyParts.get("json_1") : null;
                        if( json_1 == null || !isJSON(json_1) )
                            continue;

                        JSONObject json = new JSONObject(json_1);

                        if(json.has("final_response") && json.getInt("final_response") == 0) {
                            if( json.has("transcriptions") )
                                write("Streaming Response: " + json.getJSONArray("transcriptions").getString(0));
                            else if( json.has("appserver_results") && json.getJSONObject("appserver_results").has("payload")
                                    && json.getJSONObject("appserver_results").getJSONObject("payload").has("actions")
                                    && json.getJSONObject("appserver_results").getJSONObject("payload").getJSONArray("actions").length() > 0
                                    && json.getJSONObject("appserver_results").getJSONObject("payload").getJSONArray("actions").getJSONObject(0).has("text"))
                                write("Streaming Response [text]: " + json.getJSONObject("appserver_results")
                                        .getJSONObject("payload")
                                        .getJSONArray("actions")
                                        .getJSONObject(0)
                                        .getString("text") );
                            else if( json.has("appserver_results") && json.getJSONObject("appserver_results").has("payload")
                                    && json.getJSONObject("appserver_results").getJSONObject("payload").has("actions")
                                    && json.getJSONObject("appserver_results").getJSONObject("payload").getJSONArray("actions").length() > 0
                                    && json.getJSONObject("appserver_results").getJSONObject("payload").getJSONArray("actions").getJSONObject(0).has("nbest_text"))
                                write("Streaming Response [nbest text]: " + json.getJSONObject("appserver_results")
                                        .getJSONObject("payload")
                                        .getJSONArray("actions")
                                        .getJSONObject(0)
                                        .getJSONObject("nbest_text")
                                        .getJSONArray("transcriptions")
                                        .getString(0) );
                            else
                                write(json.toString(4));

                        }
                        else if(json.has("final_response") && json.getInt("final_response") == 1) {
                            if( json.has("transcriptions") ) {
                                write("Final Response: " + json.getJSONArray("transcriptions").getString(0));
                                write("Final JSON Response: " + json.toString(4));

                            }
                            else if( json.has("appserver_results") && json.getJSONObject("appserver_results").has("payload")
                                    && json.getJSONObject("appserver_results").getJSONObject("payload").has("actions")
                                    && json.getJSONObject("appserver_results").getJSONObject("payload").getJSONArray("actions").length() > 0 ) {

                                //Ji's code
                                serverResponseJSON = json;
                                JSONArray actions = json.getJSONObject("appserver_results").getJSONObject("payload").getJSONArray("actions");
                                for( int i = 0; i < actions.length(); i++ ) {
                                    JSONObject action = actions.getJSONObject(i);
                                    if( action.has("type") && action.getString("type").equalsIgnoreCase("nlu_results")) {
                                        write("Final Response: " + action.getJSONObject("Input")
                                                .getJSONArray("Interpretations")
                                                .getString(0) );
                                    }
                                }
                                write("Final JSON Response: " + json.toString(4));

                                //Ji's code
//                                return;
                            } else
                                write("Unsupported Response Format: " + json.toString(4));
                        }
                        else
                            write("Unknown Response: " + json.toString(4));


                    }

                    // If the size of the body part is 0, the server has sent the last multi-part response and we can stop looping...
                    if( bodyParts.containsKey("size") && Integer.valueOf(bodyParts.get("size")) == 0 ) {
                        if( transactionLatency.getMarker(Marker.final_response) == -1 ) {
                            transactionLatency.setMarker(Marker.final_response);
//                            _logData.timeToFinalResponse = transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_end, Marker.final_response);
                        }
                        break;
                    }

                    continue;
                }
            }

        } catch(SocketTimeoutException e) {
            Log.d("sss", "Socket Exception: " + e.getMessage());

        } finally {
            if( transactionLatency.getMarker(Marker.final_response) == -1 ) {
                transactionLatency.setMarker(Marker.final_response);
            }

            printLineSeparator();
            write("Done reading response...");

            transactionLatency.setMarker(Marker.stop);

            this.printLineSeparator();
            showLatencyMarkers();

            synchronized(waitLock) {
                try {
                    waitLock.notifyAll();
                } catch( IllegalMonitorStateException e ) {
                    Log.d("sss", e.getMessage() );
                }
            }

        }

    }


    // ********************* MISCELLANEOUS *********************

    protected boolean isJSON(String str) {
        try
        {
            JSONObject json = new JSONObject(str);
        }
        catch (JSONException e) {
            return false;
        }
        return true;
    }

    /**
     * user (or device) id: Set the user id to a unique value that is persistent across usages of the app. Ideally this
     * 		value is unique based on the combination of user + device as this is used to tune user-specific acoustic
     * 		models. The acoustic model of each person is unique, and it's also unique across device types and network types
     * 		due to the variation in mic and network quality. With that said, this value may also be used for per-device
     * 		billing models, so the right algorithm needs to take both technical and business requirements into consideration.
     * 		User Id must be <= 40 chars and valid characters include [a-z,A-Z,0-9] and underscores.
     *
     * application session id: Use this to track a sequence of NCS Transactions that belong together. For example,
     * 		translation apps in which two users are conversing with one another, or a Personal Assistant with dialog
     * 		turns. Pass in the same application session id with each transaction (e.g. ASR and TTS) so they can be
     * 		linked together in our analytics. Create a new application session id to track a new or unrelated transaction
     * 		request.
     *
     * utterance number: Use this to help track the dialog turns for a given application session. For applications
     * 		where user input is typically one-and-done, this should be set to 1. However, if the application is designed
     * 		to have dialog turns with the user or recognition error corrections for a given input field, increment the
     * 		utterance number with each request.
     *
     */
    protected void initialize() {
//        _userID = UserIDManager.createUserIDManager().initUserID();
//        _requestData.initApplicationSessionID();
//        _requestData.resetUtteranceNumber();
        _userID = AppInfo.IMEInumber;
        _requestData.initApplicationSessionID();
        _requestData.resetUtteranceNumber();
    }

    /**
     * Write an empty line to console.
     */
    private void write() {
//        System.out.println();
        Log.d("sss", "\n");
    }

    /**
     * Write a message with end of line to console.
     *
     * @param msg the msg
     */
    private void write(String msg) {
//        System.out.println(msg);
        Log.d("sss", msg);
    }

    /** Help display some transaction timing markers */
    protected void showLatencyMarkers() {

        write( "marker\t\t\t\t\tDuration Since T0\tDuration Since Last Marker");
        write( "[t0] " + Marker.start + "\t\t\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.start) + "sec\t\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.start) + "sec");
        write( "[t1] " + Marker.connected + "\t\t\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.connected) + "sec\t\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.connected) + "sec");
        write( "[t2] " + Marker.query_begin + "\t\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.query_begin) + "sec\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.connected, Marker.query_begin) + "sec");
        write( "[t3] " + Marker.query_complete + "\t\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.query_complete) + "sec\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.query_begin, Marker.query_complete) + "sec");

        write( "[t4] " + Marker.audio_streaming_begin + "\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.audio_streaming_begin) + "sec\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.query_begin, Marker.audio_streaming_begin) + "sec");
        write( "[t5] " + Marker.audio_streaming_end + "\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.audio_streaming_end) + "sec\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_begin, Marker.audio_streaming_end) + "sec");

        if( _streamingResults )
            write( "[t6] " + Marker.initial_response + "\t\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.initial_response) + "sec\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_begin, Marker.initial_response) + "sec (from t4)");
        else
            write( "[t6] " + Marker.initial_response + "\t\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.initial_response) + "sec\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_end, Marker.initial_response) + "sec (from t5)");

        if( _streamingResults )
            write( "[t7] " + Marker.final_response + "\t\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.final_response) + "sec\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.audio_streaming_end, Marker.final_response) + "sec");
        else
            write( "[t7] " + Marker.final_response + "\t\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.final_response) + "sec\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.initial_response, Marker.final_response) + "sec");

        write( "[t8] " + Marker.stop + "\t\t\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.start, Marker.stop) + "sec\t\t" + transactionLatency.calculateDistanceBetweenMarkers(Marker.final_response, Marker.stop) + "sec");

    }

    protected void printLineSeparator() {
        write();
        write("---------------------------------");
        write();
    }

    /**
     * Wait to be signaled that the server has provided response.
     * <br><br>
     * The client will timeout waiting for a response. The default is 5000ms.
     *
     * @param timeout the timeout
     */
    private static void wait4TerminateSignal(int timeout) {
        synchronized(waitLock) {
            try {
                waitLock.wait(timeout);
            } catch( InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }


    // ********************* REQUEST HANDLERS *********************

    public void sendNluTextRequest(String message) {
        this.initialize();
        _message = message;
        serverResponseJSON = null;
        transactionLatency.reset();
        transactionLatency.setMarker(Marker.start);
        sendNluTextQueryCommands();
        sendTerminatingChunk(out, boundary);
        transactionLatency.setMarker(Marker.query_complete);
        wait4TerminateSignal(5000);
    }

    public void resetUserProfile() {
        this.initialize();

        transactionLatency.setMarker(Marker.start);

        /** Create a secure socket */
        Socket s = connectToServer();

        /** Send headers... */
        transactionLatency.setMarker(Marker.query_begin);

        /** Send the NCS Command request data */
        sendUserProfileResetRequestData(out, boundary);

        sendTerminatingChunk(out, boundary);
        transactionLatency.setMarker(Marker.query_complete);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String t;

            while((t = br.readLine()) != null) {
                write(t);

                if( t.equals("0") )
                    return;
            }
        } catch(Exception e) {
//            write(e.getMessage());
            e.printStackTrace();

        }
    }

}
