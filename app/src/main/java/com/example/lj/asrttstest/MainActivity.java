package com.example.lj.asrttstest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;

import com.example.lj.asrttstest.info.AppInfo;
import com.example.lj.asrttstest.upload.ContactsActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        AppInfo.IMEInumber = telephonyManager.getDeviceId();

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

        final Button nluCloudRecognizerButton = (Button) findViewById(R.id.nluCloudRecognizerButton);
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

        final Button cloudDataUploadButton = (Button) findViewById(R.id.cloudDataUploaderButton);
        cloudDataUploadButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent localIntent = new Intent(MainActivity.this, CloudDataUploadActivity.class);
                MainActivity.this.startActivity(localIntent);
            }
        });

        final Button getContactButton = (Button) findViewById(R.id.getContactButton);
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
}
