package com.example.lj.asrttstest.dialog;

import android.content.Context;
import android.util.Log;

import com.example.lj.asrttstest.info.AllContactInfo;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lj on 16/6/16.
 */
public class MessageDomainProc extends DomainProc{
   private JsonParser jsonParser;
   private static final String TAG = "MessageDomainProc";
   private String phoneNumber = "";
   private String messageContent = "";

   public MessageDomainProc(Context _context, JSONArray _actionArray, String _ttsText) {
      super(_context, _actionArray, _ttsText);
   }

   @Override
   public void process() {
      JSONArray curArray = actionArray;
      for(int i = 0; i < curArray.length(); i++) {
         JSONObject curObject = curArray.optJSONObject(i);
         curObject = curObject.optJSONObject("value");
         if (curObject.has("recipients")) {
            curObject = curObject.optJSONObject("recipients");
            JSONArray tmpArray = curObject.optJSONArray("value");
            for (int j = 0; j < tmpArray.length(); j++) {
               curObject = tmpArray.optJSONObject(i);
               curObject = curObject.optJSONObject("value");
               JSONObject tmpObject = curObject.optJSONObject("phoneNumberId");
               if (tmpObject != null) {
                  phoneNumber = tmpObject.optString("value");
                  if (AllContactInfo.allPhoneIDtoPhoneNum.containsKey(phoneNumber)) {
                     phoneNumber = AllContactInfo.allPhoneIDtoPhoneNum.get(phoneNumber);
                     break;
                  }
               }
               tmpObject = curObject.optJSONObject("phoneNumber");
               if (tmpObject != null) {
                  phoneNumber = tmpObject.optString("value");
                  break;
               }
            }
         }
          curObject = curArray.optJSONObject(i);
          curObject = curObject.optJSONObject("value");
          if(curObject.has("body")){
              curObject = curObject.optJSONObject("body");
              messageContent = curObject.optString("value");
              break;
          }
      }
   }

   public String getPhoneNumber(){
      return phoneNumber;
   }

   public String getMessageContent(){
      return messageContent;
   }
}
