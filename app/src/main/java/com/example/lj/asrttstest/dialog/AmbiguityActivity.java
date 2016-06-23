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

import java.util.ArrayList;

public class AmbiguityActivity extends AppCompatActivity {

    private ListView ambiguityListView;
    private String[] ambiguityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambiguity);
        resizeDisplay();

        Intent intent=getIntent();

        ArrayList<String> tmp = (ArrayList<String>) intent.getStringArrayListExtra("list");
        ambiguityList = new String[tmp.size()];
        for(int i = 0; i < tmp.size(); i++){
            ambiguityList[i] = tmp.get(i);
        }

        Log.d("ssss", ambiguityList.toString());

        ambiguityListView = (ListView) findViewById(R.id.AmbiguityListView);
        ArrayAdapter<String> listAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, ambiguityList);
        ambiguityListView.setAdapter(listAdapter);

        ambiguityListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent=new Intent();
                intent.putExtra("chosenID", id);
                setResult(1, intent);
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
