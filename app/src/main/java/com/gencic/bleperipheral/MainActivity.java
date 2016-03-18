package com.gencic.bleperipheral;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
                Toast.makeText(this, "Call request sent", Toast.LENGTH_SHORT).show();
                if (mBleScanner != null) {
                    mBleScanner.sendMessage("CALL");
                } else if (mAdvertiser != null) {
                    mAdvertiser.sendMessage("CALL");
                }
                break;
            case R.id.responseIndicator_2:
                if (mBleScanner != null) {
                    mBleScanner.sendMessage("TEST");
                } else if (mAdvertiser != null) {
                    mAdvertiser.sendMessage("TEST");
                }
                break;
        }
    }

    public void serveIncomingRequest(final String str){
        runOnUiThread(new Runnable() {
            public void run() {
                switch(str){
                    case "TEST" :   _indicateTestTouch();
                                    break;
                    case "CALL" :   //responseIndicator_2.setBackgroundColor(Color.parseColor("#883b59"));
                                    //responseIndicator_2.setTextColor(Color.parseColor("#ffffff"));
                                    //responseIndicator_2.setText("HELLO Tapped!");
                                    _call("123456789");
                                    break;
                }
            }
        });
    }

    public void _call(String number){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        Toast.makeText(this, "INSIDE _CALL", Toast.LENGTH_SHORT).show();
        callIntent.setData(Uri.parse("tel:" + number));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
            startActivity(callIntent);
        }
    }

    public void _indicateTestTouch(){
        Toast.makeText(this, "TEST was tapped", Toast.LENGTH_SHORT).show();
    }
}
