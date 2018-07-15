package com.capsane.simplecamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener {

    private String artId;

    private String humanName;

    private EditText etArtId;
    private EditText etHumanName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        etArtId = findViewById(R.id.et_art_id);
        etHumanName = findViewById(R.id.et_human_name);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_finish:
                onFinish();
                break;
        }
    }

    private void onFinish() {
        artId = etArtId.getText().toString();
        humanName = etHumanName.getText().toString();
        saveRecord();

        //FIXME: 本次采集结束，记得将MainActivity中的GlobalDirName置为null
        Globals.initSaveDir(getExternalFilesDir(null));
    }

    private void saveRecord() {
        // TODO: Save to database

    }
}
