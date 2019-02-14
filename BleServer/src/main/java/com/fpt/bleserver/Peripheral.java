package com.fpt.bleserver;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fpt.ble.AbstractBleServer;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * <pre>
 *   @author  : fpt
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2018/12/11 17:42
 *   desc    :
 *   version :
 * </pre>
 */
public class Peripheral extends AbstractBleServer {
    private static final String TAG = "Peripheral";

    public Peripheral(@NonNull Context context) {
        super(context);
    }

    @Override
    protected byte[] getAdvertiseDataBytes(int length) {
        byte[] bytes = new byte[length];
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < length; i++) {
            bb.put((byte) i);
        }
        return bytes;
    }

    @Override
    protected void onReceive(byte[] receive) {
        String json = new String(receive);
        InfoBean info = new Gson().fromJson(json,InfoBean.class);
        Log.d(TAG,info.toString());
    }

    @Override
    protected void onConnectionChange(BluetoothDevice device, int connectionState) {
        switch (connectionState){
            case BluetoothProfile.STATE_CONNECTED:
                Log.d(TAG,"onConnectionChange->address: "+device.getAddress()+",status:已连接");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                Thread.sleep(2000);

                                String json = "{\"ip\": \"192.168.2.4\", \"wifi\": \"Fis98\", \"battery\": \"100\"}";
                                List<BluetoothDevice> devices = getBondBluetoothDevices();
                                bindBluetoothDevice(devices.get(devices.size()-1));
                                send(json.getBytes());
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                Log.d(TAG,"onConnectionChange->address: "+device.getAddress()+",status:连接断开");
                break;
            default:
                break;
        }
    }

}
