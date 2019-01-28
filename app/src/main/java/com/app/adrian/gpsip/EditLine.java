package com.app.adrian.gpsip;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

public class EditLine extends AppCompatActivity implements View.OnClickListener {
    Track originalLine;
    private Track currentLine;
    private Spinner firstStopSpinner, lastStopSpinner;
    private EditText lineEditText, directionEditText;
    private Button okButton, cancelButton, lastStopButton;
    private TextView stopsLineView, lineInfoView;
    private ArrayAdapter<String> firstAdapter;
    private ArrayAdapter<String> lastAdapter;
    private ArrayList<String> registry;
    private ArrayList<String> firstStopList;
    private ArrayList<String> lastStopList;
    private LinearLayout mainLinearLayout;
    int firstNumberStop;
    int lastNumberStop;
    private android.support.v7.app.ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_line);
        Bundle extra = getIntent().getBundleExtra("lineEditBundle");
        currentLine = (Track) extra.getSerializable("lineEdit");
        originalLine = (Track) extra.getSerializable("originalLine");
        firstNumberStop = getIntent().getIntExtra("firstNumberStop", 0);
        lastNumberStop = getIntent().getIntExtra("lastNumberStop", originalLine.getStops().size()-1);
        firstStopSpinner = findViewById(R.id.firstStopSpinner);
        lastStopSpinner = findViewById(R.id.lastStopSpinner);
        lineEditText = findViewById(R.id.lineEditText);
        directionEditText = findViewById(R.id.directionEditText);
        okButton = findViewById(R.id.okEditButton);
        cancelButton = findViewById(R.id.cancelEditButton);
        lastStopButton = findViewById(R.id.setEndStopButton);
        stopsLineView = findViewById(R.id.editStopsLineView);
        lineInfoView = findViewById(R.id.editLineInfoView);
        okButton.setEnabled(false);
        mainLinearLayout = findViewById(R.id.editLineMainLinearLayout);
        actionBar = getSupportActionBar();
        changeActionBar();
        lineEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        lineEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String line = lineEditText.getText().toString();
                Track temp = currentLine;
                if(isNumeric(line)){
                    currentLine = new Track<Integer>(Integer.parseInt(line), temp.getDirection(), temp.getStops(), temp.getSayDirection());
                }
                else{
                    currentLine = new Track<String>(line, temp.getDirection(), temp.getStops(), temp.getSayDirection());
                }
                okButton.setEnabled(true);
                generateLinePreview();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        lineEditText.setText(currentLine.getLine().toString());
        directionEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        directionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                currentLine.setDirection(directionEditText.getText().toString());
                currentLine.setSayDirection(directionEditText.getText().toString());
                okButton.setEnabled(true);
                generateLinePreview();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        directionEditText.setText(currentLine.getDirection());
        firstStopList = new ArrayList<>();
        lastStopList = new ArrayList<>();
        ArrayList<Stop> stops = originalLine.getStops();
        Iterator <Stop> stopIterator = stops.iterator();
        String stopTexts = "";
        while(stopIterator.hasNext()){
            Stop stop = stopIterator.next();
            firstStopList.add(stop.getName());
            lastStopList.add(stop.getName());
            stopTexts += stop.getName();
            if(stopIterator.hasNext()){
                stopTexts +="\n";
            }
        }
        firstStopSpinner.setSelection(firstNumberStop);
        stopsLineView.setText(stopTexts);
        firstStopList.remove(firstStopList.size()-1);
        lastStopList.remove(0);
        firstAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_dropdown_item, firstStopList);
        lastAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, lastStopList);
        firstStopSpinner.setAdapter(firstAdapter);
        firstStopSpinner.setSelection(firstNumberStop);
        firstStopSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(firstStopSpinner.getSelectedItemPosition()<lastNumberStop)
                {
                    firstNumberStop = firstStopSpinner.getSelectedItemPosition();

                    currentLine.setStops(generateStops());
                    okButton.setEnabled(true);
                    generateLinePreview();
                }
                else{
                    firstStopSpinner.setSelection(firstNumberStop);
                    Toast.makeText(getApplicationContext(), "Przystanek początkowy musi być przed końcowym", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        lastStopSpinner.setAdapter(lastAdapter);
        lastStopSpinner.setSelection(lastNumberStop-1);
        lastStopSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(lastStopSpinner.getSelectedItemPosition()+1>firstNumberStop){
                    lastNumberStop = lastStopSpinner.getSelectedItemPosition()+1;

                    currentLine.setStops(generateStops());
                    okButton.setEnabled(true);
                    generateLinePreview();
                }
                else{
                    lastStopSpinner.setSelection(lastNumberStop-1);
                    Toast.makeText(getApplicationContext(), "Przystanek końcowy musi być po początkowym", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        lineInfoView.setText(currentLine.getLine().toString()+" -> "+currentLine.getDirection());
        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        lastStopButton.setOnClickListener(this);


    }
    @VisibleForTesting
    ArrayList<Stop> generateStops(){
        ArrayList<Stop> temp = new ArrayList<>();
        for(int pos=firstNumberStop;pos<=lastNumberStop;pos++){
            temp.add((Stop) originalLine.getStops().get(pos));
        }
        return temp;
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
    private boolean isNumeric(String str){
        try{
            int number = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe){
            return false;
        }
        return true;
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
            actionBar.setSubtitle("Edycja linii");
            mainLinearLayout.setOrientation(LinearLayout.VERTICAL);
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.okEditButton:
                Bundle extra = new Bundle();
                extra.putSerializable("lineEdit", currentLine);
                Intent intent = new Intent();
                intent.putExtra("lineEditBundle", extra);
                intent.putExtra("firstNumberStop", firstNumberStop);
                intent.putExtra("lastNumberStop",  lastNumberStop);
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
            case R.id.cancelEditButton:
                Intent intent2 = new Intent();
                setResult(Activity.RESULT_CANCELED,intent2);
                finish();
                break;
            case R.id.setEndStopButton:

                ArrayList<Stop> stops = currentLine.getStops();
                int index = lastStopSpinner.getSelectedItemPosition()+1-firstNumberStop;
                if(index<stops.size()){
                    directionEditText.setText(stops.get(index).getName());
                    currentLine.setDirection(stops.get(index).getName());


                    currentLine.setSayDirection(stops.get(index).getSayName());
                    okButton.setEnabled(true);

                    generateLinePreview();

                }
                break;

        }
    }
}
