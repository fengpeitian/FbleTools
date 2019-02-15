package com.fpt.bleclient;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 99;
    private static final int REQUEST_ENABLE_LO = 98;
    private static final int REQUEST_PERMISSION_LOCATION = 97;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean b = BleUtils.checkBlePermission(this,REQUEST_ENABLE_LO,REQUEST_PERMISSION_LOCATION);
        if (b){
            boolean c = BleUtils.checkAndEnableBluetooth(this,REQUEST_ENABLE_BT);
            if (c){
                startService();
            }
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        findViewById(R.id.bt_disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.fpt.bleclient.DisconnectBroadcast");
                intent.putExtra("event",0);
                localBroadcastManager.sendBroadcast(intent);
            }
        });
        findViewById(R.id.bt_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.fpt.bleclient.DisconnectBroadcast");
                intent.putExtra("event",1);
                localBroadcastManager.sendBroadcast(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    startService();
                }
                break;
            case REQUEST_ENABLE_LO:
                if (resultCode == RESULT_OK) {
                    boolean c = BleUtils.checkAndEnableBluetooth(this,REQUEST_ENABLE_BT);
                    if (c){
                        startService();
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (BleUtils.checkAndEnableLocation(this,REQUEST_ENABLE_LO)){
                    startService();
                }
            }
        }
    }

    private void startService(){
        Intent intent = new Intent(MainActivity.this,CentralService.class);
        startService(intent);
    }

}
