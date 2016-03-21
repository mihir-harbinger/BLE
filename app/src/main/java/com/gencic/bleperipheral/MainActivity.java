package com.gencic.bleperipheral;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnClickListener, ILogger {

    private TextView mTextViewLog;
    private EditText mEditTextMsg;
    private BluetoothManager mBluetoothManager;
    private BleAdvertiser mAdvertiser;
    private BleScanner mBleScanner;
    private Button responseIndicator_1;
    private Button responseIndicator_2;
    private TextView mDeviceStatus;
    private TextView mConnectionStatus;
    private TextView mMessageLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextViewLog = (TextView) findViewById(R.id.text_view_log);
        mEditTextMsg = (EditText) findViewById(R.id.edit_text_msg);
        mDeviceStatus = (TextView) findViewById(R.id.device_status);
        mConnectionStatus = (TextView) findViewById(R.id.conn_status);
        mMessageLog = (TextView) findViewById(R.id.message_log);
        findViewById(R.id.button_advertise).setOnClickListener(this);
        findViewById(R.id.button_scan).setOnClickListener(this);
        findViewById(R.id.button_send).setOnClickListener(this);
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        responseIndicator_1 = (Button) findViewById(R.id.responseIndicator_1);
        findViewById(R.id.responseIndicator_1).setOnClickListener(this);
        responseIndicator_2 = (Button) findViewById(R.id.responseIndicator_2);
        findViewById(R.id.responseIndicator_2).setOnClickListener(this);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        BluetoothAdapter mBluetoothAdapter
                = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            mDeviceStatus.setText("Your device does not support Bluetooth.");
            // Device does not support Bluetooth
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                mDeviceStatus.setTextColor(Color.parseColor("#009d00"));
                mDeviceStatus.setText("Device ready");
                // Bluetooth is not enable :)
            }
            else{
                mDeviceStatus.setTextColor(Color.parseColor("#ff9999"));
                mDeviceStatus.setText("Bluetooth disabled");
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    /* ... */

        // Unregister broadcast listeners
        unregisterReceiver(mReceiver);
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
                    mEditTextMsg.setText("");
                }
                break;
            case R.id.responseIndicator_1:
                //Toast.makeText(this, "Call request sent", Toast.LENGTH_SHORT).show();
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
                                    Random rnd = new Random();
                                    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                                    responseIndicator_1.setBackgroundColor(color);
                                    break;
                }
            }
        });
    }

//    public void _randomizeColor(String number){
//        Intent callIntent = new Intent(Intent.ACTION_CALL);
//        Toast.makeText(this, "INSIDE _CALL", Toast.LENGTH_SHORT).show();
//        callIntent.setData(Uri.parse("tel:" + number));
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
//            startActivity(callIntent);
//        }
//    }

    public void _indicateTestTouch(){
        Toast.makeText(this, "TEST was tapped", Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        setDeviceStatus("Bluetooth disabled", false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        setDeviceStatus("Turning Bluetooth off...", false);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        setDeviceStatus("Device ready", true);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        setDeviceStatus("Turning Bluetooth on...", true);
                        break;
                }
            }
        }
    };

    public void setDeviceStatus(final String message, final boolean code) {
        runOnUiThread(new Runnable() {
            public void run() {

                if (code) {
                    mDeviceStatus.setTextColor(Color.parseColor("#009d00"));
                } else {
                    mDeviceStatus.setTextColor(Color.parseColor("#ff9999"));
                }
                mDeviceStatus.setText(message);
            }
        });
    }

    public void setConnectionStatus(final String message, final boolean code) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (code) {
                    mConnectionStatus.setTextColor(Color.parseColor("#009d00"));
                } else {
                    mConnectionStatus.setTextColor(Color.parseColor("#cccccc"));
                }
                mConnectionStatus.setText(message);
            }
        });
    }

    public void setMessageText(final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                mMessageLog.setText(message);
            }
        });
    }
}


