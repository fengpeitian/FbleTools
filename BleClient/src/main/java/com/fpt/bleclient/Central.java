package com.fpt.bleclient;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.fpt.ble.AbstractBleClient;
import com.fpt.ble.BleSetting;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 *   @author  : fpt
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2018/12/11 17:39
 *   desc    :
 *   version :
 * </pre>
 */
public class Central extends AbstractBleClient {
    private static final String TAG = "Central";
    private List<BluetoothDevice> scanBluetoothDevices  = new ArrayList<>();

    public Central(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onScan(int i, ScanResult scanResult) {
        BluetoothDevice device = scanResult.getDevice();
        //去重操作
        if (!scanBluetoothDevices.contains(device)){
            scanBluetoothDevices.add(device);

            byte[] data = scanResult.getScanRecord().getServiceData(new ParcelUuid(BleSetting.UUID_SERVICE));
            String name = TextUtils.isEmpty(device.getName()) ? "null":device.getName();
            Log.d(TAG,"device name: "+name);
            Log.d(TAG, Arrays.toString(data));

            String device_name = device.getName();
            if (!TextUtils.isEmpty(device_name) && device.getName().equals("mix3")){
                connect(device);
            }
        }

    }

    @Override
    protected void onReceive(byte[] receive) {
        String json = new String(receive);
        InfoBean info = new Gson().fromJson(json,InfoBean.class);
        Log.d(TAG,info.toString());
    }

    @Override
    protected void onConnectionChange(int connectionState) {
        switch (connectionState){
            case BluetoothProfile.STATE_CONNECTED:
                Log.d(TAG,"onConnectionChange: 已连接");
                scanBleDevice(false);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            Thread.sleep(2000);
                            String json = "{\"url\": \"http://www.baidu.com\", \"wifi\": \"Fis98\", \"password\": \"fpt54321\"}";
                            send(json.getBytes());

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                Log.d(TAG,"onConnectionChange: 连接断开");

                break;
            default:
                break;
        }
    }
    
}