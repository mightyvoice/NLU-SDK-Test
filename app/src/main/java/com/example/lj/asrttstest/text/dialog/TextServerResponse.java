package com.example.lj.asrttstest.text.dialog;

import java.util.ArrayList;

/**
 * Created by lj on 16/8/8.
 */
public class TextServerResponse {

    private String domain = "";
    private String intent = "";
    private String dialogPhase = "";
    private String dialogPhaseDetail = "";
    private String status = "";
    private String ttsText = "";
    private String systemText = "";
    private ArrayList<String> ambiguityList = new ArrayList<>();
    private String phoneID = "";
    private String phoneNumber = "";
    private String contactID = "";
    private String messageContent = "";
    private ArrayList<String> userClickCommands = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("=== TextServerResponse ===")
                .append("\ndomain : ").append(domain)
                .append("\nintent : ").append(intent)
                .append("\ndialogPhase : ").append(dialogPhase)
                .append("\ndialogPhaseDetail : ").append(dialogPhaseDetail)
                .append("\nstatus : ").append(status)
                .append("\nttsText : ").append(ttsText)
                .append("\nsystemText : ").append(systemText)
                .append("\nphoneID : ").append(phoneID)
                .append("\nphoneNumber : ").append(phoneNumber)
                .append("\ncontactID : ").append(contactID)
                .append("\nmessageContent : ").append(messageContent)
                .append("\nambiguityList : ").append(ambiguityList);
        return builder.toString();
    }

    public ArrayList<String> getUserClickCommands() {
        return userClickCommands;
    }

    public void setUserClickCommands(ArrayList<String> userClickCommands) {
        this.userClickCommands = userClickCommands;
    }

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

