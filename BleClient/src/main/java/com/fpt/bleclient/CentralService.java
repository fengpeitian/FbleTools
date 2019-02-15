package com.fpt.bleclient;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.fpt.ble.AbstractBleClient;
import com.fpt.ble.OnProgressListener;

/**
 * <pre>
 *   @author  : fpt
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2018/12/11 17:42
 *   desc    :
 *   version :
 * </pre>
 */
public class CentralService extends Service {
    private Central bleClient;
    private LocalReceiver localReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                String json = (String) msg.obj;
                Toast.makeText(getApplicationContext(),json, Toast.LENGTH_SHORT).show();
            }else if (msg.what == 2){
                bleClient.disconnect();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        registerBroadcast();

        bleClient = new Central(this){
            @Override
            protected void receive(String json) {
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = json;
                handler.sendMessage(msg);
            }

            @Override
            protected void disconnectBle() {
                handler.sendEmptyMessage(2);
            }
        };

        bleClient.setOnSendProgressListener(new OnProgressListener() {
            @Override
            public void onProgress(int done_byte, int all_byte) {
                Log.d("Central_send","done_byte: "+done_byte+" all_byte: "+all_byte);
            }
        });

        bleClient.setOnReceiveProgressListener(new OnProgressListener() {
            @Override
            public void onProgress(int done_byte, int all_byte) {
                Log.d("Central_receive","done_byte: "+done_byte+" all_byte: "+all_byte);
            }
        });

        bleClient.setOnScanStateListener(new AbstractBleClient.OnScanStateListener() {
            @Override
            public void onStart() {
                Log.d("Central_receive","扫描开始");
            }

            @Override
            public void onStop() {
                Log.d("Central_receive","扫描停止");
            }

            @Override
            public void onCompleted() {
                Log.d("Central_receive","扫描结束");
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerBroadcast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.fpt.bleclient.DisconnectBroadcast");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);
    }

    private class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int event = intent.getIntExtra("event",0);
            if (event == 0){
                bleClient.disconnect();
            }else if (event == 1){
                bleClient.scanBleDevice(true,Integer.MAX_VALUE);
            }
        }
    }

}
