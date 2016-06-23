package com.example.lj.asrttstest.dialog;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.lj.asrttstest.R;
import com.example.lj.asrttstest.info.Global;

import java.util.ArrayList;

public class AmbiguityActivity extends AppCompatActivity {

    private ListView ambiguityListView;
    private String[] ambiguityArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambiguity);
        resizeDisplay();

        //only array works, arrayList does not work here
        ambiguityArray = new String[Global.ambiguityList.size()];
        for(int i = 0; i < Global.ambiguityList.size(); i++){
            ambiguityArray[i] = Global.ambiguityList.get(i);
        }

        ambiguityListView = (ListView) findViewById(R.id.AmbiguityListView);
        ArrayAdapter<String> listAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, ambiguityArray);
        ambiguityListView.setAdapter(listAdapter);

        ambiguityListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Global.ambiguityListChosenID = (int)id;
                finish();
            }
        });

    }

    private void resizeDisplay(){
        DisplayMetrics ds = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(ds);

        int width = ds.widthPixels;
        int height = ds.heightPixels;

        getWindow().setLayout(
                (int) (width * 0.6),
                (int) (height * 0.5));
    }
}
