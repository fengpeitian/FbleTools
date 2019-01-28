package com.fpt.bleserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

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
public class PeripheralService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        Peripheral bleServer = new Peripheral(this);
        bleServer.setOnSendProgressListener(new OnProgressListener() {
            @Override
            public void onProgress(int done_byte, int all_byte) {
                Log.d("Peripheral_send","done_byte: "+done_byte+" all_byte: "+all_byte);
            }
        });
        bleServer.setOnReceiveProgressListener(new OnProgressListener() {
            @Override
            public void onProgress(int done_byte, int all_byte) {
                Log.d("Peripheral_receive","done_byte: "+done_byte+" all_byte: "+all_byte);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
