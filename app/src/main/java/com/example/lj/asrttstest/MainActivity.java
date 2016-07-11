package com.example.lj.asrttstest;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;

import com.example.lj.asrttstest.info.AllContactInfo;
import com.example.lj.asrttstest.info.AppInfo;
import com.example.lj.asrttstest.info.ContactInfo;
import com.example.lj.asrttstest.upload.ContactsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        final Button cloudRecognizerButton = (Button) findViewById(R.id.cloudRecognizerButton);
        cloudRecognizerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent localIntent = new Intent(MainActivity.this, CloudASRActivity.class);
                MainActivity.this.startActivity(localIntent);
            }
        });

        final Button nluCloudRecognizerButton = (Button) findViewById(R.id.textNLUCloudRecognizerButton);
        nluCloudRecognizerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent localIntent = new Intent(MainActivity.this, NLUCloudASRActivity.class);
                MainActivity.this.startActivity(localIntent);
            }
        });

        final Button cloudVocalizerButton = (Button) findViewById(R.id.cloudVocalizerButton);
        cloudVocalizerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent localIntent = new Intent(MainActivity.this, TTSCloudActivity.class);
                MainActivity.this.startActivity(localIntent);
            }
        });

        final Button getContactButton = (Button) findViewById(R.id.uploadContactButton);
        getContactButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent localIntent = new Intent(MainActivity.this, ContactsActivity.class);
                MainActivity.this.startActivity(localIntent);
            }
        });

    }

    private void init(){
        //init the hashtable of the phoneId to phoneNumber
        getAllContactList();
        try{
            getAllContactJsonArrayAndObject();
        }catch (JSONException e){
            e.printStackTrace();
        }

        //get imei number for uid
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        AppInfo.IMEInumber = telephonyManager.getDeviceId();
    }

    private void getAllContactList(){
        AllContactInfo.allContactList = new ArrayList<ContactInfo>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        ContactInfo contact = new ContactInfo();
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                contact = new ContactInfo();

                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                String[] nameList = null;
                if(name != null && !name.equals("")){
                    nameList = name.split(" ");
                    contact.setFirstName(nameList[0]);
                    if(nameList.length > 1) contact.setLastName(nameList[1]);
                }

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String phoneType = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.TYPE));
                        phoneType = contact.phoneTypeTable.get(phoneType);
                        contact.phoneNumberTable.put(phoneType, phoneNo);
                    }
                    pCur.close();
                }
                AllContactInfo.allContactList.add(contact);
            }
        }
    }

    private void getAllContactJsonArrayAndObject() throws JSONException {
        int curID = -1;
        AllContactInfo.allContactJsonArray = new JSONArray();
        AllContactInfo.allPhoneIDtoPhoneNum = new Hashtable<String, String>();
        AllContactInfo.allContactJsonObject = new JSONObject();
        for (ContactInfo contact: AllContactInfo.allContactList){
            curID++;
            JSONObject tmp = new JSONObject();
            JSONObject all = new JSONObject();
            tmp.put("fn", contact.getFirstName());
            tmp.put("ln", contact.getLastName());
            JSONArray phoneTypeArray = new JSONArray();
            JSONArray phoneNumArray = new JSONArray();
            Set<String> types = contact.phoneNumberTable.keySet();
            int phoneID = -1; //starts from 0
            for(String phoneType: types){
                phoneTypeArray.put(phoneType);
                phoneID++;
                String phId = new Integer(curID).toString()+"_"+new Integer(phoneID).toString();
                phoneNumArray.put(phId);
                AllContactInfo.allPhoneIDtoPhoneNum.put(phId, contact.phoneNumberTable.get(phoneType));
            }
            tmp.put("phId", phoneNumArray);
            tmp.put("ph", phoneTypeArray);
            all.put("content", tmp);
            all.put("content_id", curID);
            AllContactInfo.allContactJsonArray.put(all);
        }
        AllContactInfo.allContactJsonObject.put("list", AllContactInfo.allContactJsonArray);
    }
}
