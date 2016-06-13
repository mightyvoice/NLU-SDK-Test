package com.example.lj.asrttstest.upload;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import com.example.lj.asrttstest.R;
import com.example.lj.asrttstest.info.AllContactInfo;
import com.example.lj.asrttstest.info.ContactInfo;

import 	android.content.ContentResolver;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContactsActivity extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_asr);

        getAllContactList();
        try {
            getAllContactJsonArray();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        final EditText resultEditText = (EditText)findViewById(R.id.cloudResultEditText);
        String allContacts = "";
        for(int i = 0; i < AllContactInfo.allContactList.size(); i++){
            allContacts += AllContactInfo.allContactList.get(i).toString();
        }

        try {
            resultEditText.setText(AllContactInfo.allContactJsonArray.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getAllContactList(){

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        ContactInfo contact;
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                contact = new ContactInfo();

                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                String[] nameList = name.split(" ");
                contact.setFirstName(nameList[0]);
                contact.setLastName(nameList[1]);
                contact.setMobilePhone("0");

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
//                        Toast.makeText(ContactsActivity.this, "Name: " + name
//                                + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();
                        contact.setMobilePhone(phoneNo);
                    }
                    pCur.close();
                }
                AllContactInfo.allContactList.add(contact);
            }
        }
    }

    private void getAllContactJsonArray() throws JSONException {
        int curID = -1;
        AllContactInfo.allContactJsonArray = new JSONArray();
        for (ContactInfo contact: AllContactInfo.allContactList){
            curID++;
            JSONObject tmp = new JSONObject();
            tmp.put("fn", contact.getFirstName());
            tmp.put("ln", contact.getLastName());
            JSONArray phoneTypeArray = new JSONArray();
            phoneTypeArray.put("mobile");
            tmp.put("ph", phoneTypeArray);
            JSONArray phoneNumArray = new JSONArray();
            String phId = new Integer(curID).toString()+"_0";
            phoneNumArray.put(phId);
            tmp.put("phId", phoneNumArray);
            tmp.put("content_id", curID);
            AllContactInfo.allPhoneIDtoPhoneNum.put(phId, contact.getMobilePhone());
            AllContactInfo.allContactJsonArray.put(new JSONObject().put("content",tmp));
        }
    }
}
