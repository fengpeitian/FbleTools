package com.fpt.bleclient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

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

    @Override
    public void onCreate() {
        super.onCreate();

        Central bleClient = new Central(this);
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

        bleClient.scanBleDevice(true,Integer.MAX_VALUE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
