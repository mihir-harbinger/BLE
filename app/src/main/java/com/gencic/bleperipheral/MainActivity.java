package com.gencic.bleperipheral;

import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnClickListener, ILogger {

    private TextView mTextViewLog;
    private EditText mEditTextMsg;
    private BluetoothManager mBluetoothManager;
    private BleAdvertiser mAdvertiser;
    private BleScanner mBleScanner;
    private Button responseIndicator_1;
    private Button responseIndicator_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextViewLog = (TextView) findViewById(R.id.text_view_log);
        mEditTextMsg = (EditText) findViewById(R.id.edit_text_msg);
        findViewById(R.id.button_advertise).setOnClickListener(this);
        findViewById(R.id.button_scan).setOnClickListener(this);
        findViewById(R.id.button_send).setOnClickListener(this);
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        responseIndicator_1 = (Button) findViewById(R.id.responseIndicator_1);
        findViewById(R.id.responseIndicator_1).setOnClickListener(this);
        responseIndicator_2 = (Button) findViewById(R.id.responseIndicator_2);
        findViewById(R.id.responseIndicator_2).setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTextViewLog.setText("");
        if (mAdvertiser != null) {
            mAdvertiser.destroy();
        }
        if (mBleScanner != null) {
            mBleScanner.destroy();
        }
    }

    private void startAdvertising() {
        if (mAdvertiser == null){
            mAdvertiser = new BleAdvertiser(this, mBluetoothManager);
            mAdvertiser.setLogger(this);
        }
        mAdvertiser.startAdvertising();
    }

    private void startScanning() {
        if (mBleScanner == null){
            mBleScanner = new BleScanner(this, mBluetoothManager);
            mBleScanner.setLogger(this);
        }
        mBleScanner.startScanning();
    }

    public void log(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextViewLog.setText(msg + "\n" + mTextViewLog.getText());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_advertise:
                startAdvertising();
                break;
            case R.id.button_scan:
                startScanning();
                break;
            case R.id.button_send:
                if (mEditTextMsg.getText() != null) {
                    if (mBleScanner != null) {
                        mBleScanner.sendMessage(mEditTextMsg.getText().toString());
                    } else if (mAdvertiser != null) {
                        mAdvertiser.sendMessage(mEditTextMsg.getText().toString());
                    }
                }
                break;
            case R.id.responseIndicator_1:
                Toast.makeText(this, "HI", Toast.LENGTH_SHORT).show();
                if(mBleScanner != null){
                    mBleScanner.sendMessage("HI");
                }
                break;
            case R.id.responseIndicator_2:
                Toast.makeText(this, "HELLO", Toast.LENGTH_SHORT).show();
                if(mBleScanner != null){
                    mBleScanner.sendMessage("HELLO");
                }
                break;
        }
    }

    public void changeButtonColor(final String str){
        runOnUiThread(new Runnable() {
            public void run() {
                switch(str){
                    case "HI"       :   responseIndicator_1.setBackgroundColor(Color.parseColor("#3b5988"));
                                        responseIndicator_1.setTextColor(Color.parseColor("#ffffff"));
                                        responseIndicator_1.setText("HI Tapped!");
                                        break;
                    case "HELLO"    :   responseIndicator_2.setBackgroundColor(Color.parseColor("#883b59"));
                                        responseIndicator_2.setTextColor(Color.parseColor("#ffffff"));
                                        responseIndicator_2.setText("HELLO Tapped!");
                                        break;
                }
            }
        });
    }
}
