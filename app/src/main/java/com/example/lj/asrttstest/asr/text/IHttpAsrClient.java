package com.example.lj.asrttstest.asr.text;

/**
 * Created by lj on 16/6/30.
 */

public interface IHttpAsrClient {

    public abstract void start(String audioFilename);

    public abstract void start(String audioFile, String codec, boolean streamingResults);

    public abstract void enableBatchMode();

    public abstract void disableBatchMode();

    public abstract void setSavedAudioPath(String savedAudioPath);

    public abstract void enableStartOfSpeechDetection();

    public abstract void disableStartOfSpeechDetection();

    public abstract boolean isStartOfSpeechDetectionEnabled();

    public abstract void enableProfanityFiltering();

    public abstract void disableProfanityFiltering();

    public abstract boolean isProfanityFilteringEnabled();

    public abstract void enableNLU();

    public abstract void disableNLU();

    public abstract boolean isNluEnabled();

    public abstract void setApplication(String application);

    public abstract String getApplication();

    public abstract void resetUserProfile();

    public abstract void enableVerbose();

    public abstract void disableVerbose();

    public abstract boolean isVerbose();

    public abstract void enableTextNLU();

    public abstract void batchModeText(String nluTextStrings);

    public abstract void sendNluTextRequest(String nluTextString);

    public abstract void enableTrustedRootCert();

    public abstract void disableTrustedRootCert();

}
