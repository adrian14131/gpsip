package com.app.adrian.gpsip;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;

public class ChooseLine extends AppCompatActivity implements View.OnClickListener {
    private ArrayList<Track> lines;
    private ArrayList<String> registry = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Button okButton, cancelButton;
    private FloatingActionButton editLineButton;
    private TextView stopsLineView, lineInfoView;
    private ListView listView;
    private Track currentLine;
    private Track originalLine;
    private LinearLayout mainLinearLayout;
    private int firstNumberStop, lastNumberStop;
    private android.support.v7.app.ActionBar actionBar;
    public static final int GET_VALUE_CODE = 234;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_line);
        Bundle extra = getIntent().getBundleExtra("linesBundle");
        if(extra != null){
            if(extra.getSerializable("lines") != null){
                lines = (ArrayList<Track>) extra.getSerializable("lines");
            }
            else{
                lines = new ArrayList<>();
            }
        }

        okButton = findViewById(R.id.okButton);
        okButton.setEnabled(false);
        cancelButton = findViewById(R.id.cancelButton);
        listView = findViewById(R.id.listView);
        stopsLineView = findViewById(R.id.stopsLineView);
        lineInfoView = findViewById(R.id.lineInfoView);
        editLineButton = findViewById(R.id.editLineButton);
        mainLinearLayout = findViewById(R.id.chooseLineMainLinearLayout);
        actionBar = getSupportActionBar();
        changeActionBar();
        Iterator<Track> iterator = lines.iterator();
        while(iterator.hasNext()) {
            Track element = iterator.next();
            String reg = element.getLine()+" -> "+element.getDirection();
            registry.add(reg);
        }
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,registry);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentLine = getLine(i);
                originalLine = currentLine;
                /*lineInfoView.setText(registry.get(i));
                String stopTexts = "";
                ArrayList<Stop> stops = currentLine.getStops();
                Iterator <Stop> stopIterator = stops.iterator();
                while(stopIterator.hasNext()){
                    Stop stop = stopIterator.next();
                    stopTexts += stop.getName();
                    if(stopIterator.hasNext()){
                        stopTexts +="\n";
                    }
                }*/
                generateLinePreview();
                firstNumberStop=0;
                lastNumberStop=originalLine.getStops().size()-1;
                editLineButton.setEnabled(true);
                editLineButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

                //stopsLineView.setText(stopTexts);
                okButton.setEnabled(true);
            }
        });
        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        editLineButton.setOnClickListener(this);
        editLineButton.setEnabled(false);
        editLineButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorDisable)));
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extra;
        if(requestCode==GET_VALUE_CODE){
            if(resultCode == Activity.RESULT_OK&&data!=null){
                extra = data.getBundleExtra("lineEditBundle");
                currentLine = (Track) extra.getSerializable("lineEdit");
                firstNumberStop = data.getIntExtra("firstNumberStop", 0);
                lastNumberStop = data.getIntExtra("lastNumberStop", originalLine.getStops().size()-1);
                generateLinePreview();
            }
        }
    }
    private void generateLinePreview(){
        ArrayList<Stop> stops = currentLine.getStops();
        Iterator <Stop> stopIterator = stops.iterator();
        String stopTexts = "";
        while(stopIterator.hasNext()){
            Stop stop = stopIterator.next();
            stopTexts += stop.getName();
            if(stopIterator.hasNext()){
                stopTexts +="\n";
            }
        }
        stopsLineView.setText(stopTexts);
        lineInfoView.setText(currentLine.getLine().toString()+" -> "+currentLine.getDirection());
    }
    Track getLine(int index){
        return lines.get(index);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeActionBar();

    }
    private void changeActionBar(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){

            actionBar.hide();
            mainLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        }
        else{
            actionBar.show();
            actionBar.setSubtitle("Wybór linii");
            mainLinearLayout.setOrientation(LinearLayout.VERTICAL);
        }
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.okButton:
                Bundle extra = new Bundle();
                extra.putSerializable("lines", currentLine);
                Intent intent = new Intent();
                intent.putExtra("linesBundle", extra);
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
            case R.id.cancelButton:
                Intent intent2 = new Intent();
                setResult(Activity.RESULT_CANCELED,intent2);
                finish();
                break;
            case R.id.editLineButton:
                Bundle extraEdit = new Bundle();
                extraEdit.putSerializable("lineEdit", currentLine);
                extraEdit.putSerializable("originalLine", originalLine);
                Intent intentEdit = new Intent(v.getContext(),EditLine.class);
                intentEdit.putExtra("lineEditBundle", extraEdit);
                intentEdit.putExtra("firstNumberStop", firstNumberStop);
                intentEdit.putExtra("lastNumberStop",  lastNumberStop);
                startActivityForResult(intentEdit, GET_VALUE_CODE);
        }
    }
}