package com.example.lj.asrttstest.text.dialog;

import java.util.ArrayList;

/**
 * Created by lj on 16/8/1.
 */
public class TextServerResponse {

    private String domain = null;
    private String intent = null;
    private String dialogPhase = null;
    private String dialogPhaseDetail = null;
    private String status = null;
    private String ttsText = null;
    private String systemText = null;
    private ArrayList<String> ambiguityList = null;
    private String phoneID = null;
    private String phoneNumber = null;
    private String contactID = null;
    private String messageContent = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public String getTtsText() {
        return ttsText;
    }

    public void setTtsText(String ttsText) {
        this.ttsText = ttsText;
    }

    public String getSystemText() {
        return systemText;
    }

    public void setSystemText(String systemText) {
        this.systemText = systemText;
    }

    public ArrayList<String> getAmbiguityList() {
        return ambiguityList;
    }

    public void setAmbiguityList(ArrayList<String> ambiguityList) {
        this.ambiguityList = ambiguityList;
    }

    public String getContactID() {
        return contactID;
    }

    public void setContactID(String contactID) {
        this.contactID = contactID;
    }

    public String getDialogPhase() {
        return dialogPhase;
    }

    public void setDialogPhase(String dialogPhase) {
        this.dialogPhase = dialogPhase;
    }

    public String getDialogPhaseDetail() {
        return dialogPhaseDetail;
    }

    public void setDialogPhaseDetail(String dialogPhaseDetail) {
        this.dialogPhaseDetail = dialogPhaseDetail;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getPhoneID() {
        return phoneID;
    }

    public void setPhoneID(String phoneID) {
        this.phoneID = phoneID;
    }
}

