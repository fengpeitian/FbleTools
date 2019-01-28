package com.fpt.bleserver;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * <pre>
 *   @author  : fpt
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2018/12/06 15:32
 *   desc    : 蓝牙相关的权限
 *   version : 1.0.0
 * </pre>
 */
public class BleUtils {

    /**
     * 检查是否有ble蓝牙，检查是否是启用状态，若未启用，则申请启用
     * @param activity
     * @param requestCode    申请启用蓝牙的requestCode
     * @return 是否启用
     */
    public static boolean checkAndEnableBluetooth(Activity activity, int requestCode) {
        boolean result = false;
        BluetoothManager mBluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBlueToothAdapter = mBluetoothManager.getAdapter();
        if (mBlueToothAdapter != null && activity.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                if (!mBlueToothAdapter.isEnabled()) {
                    Intent enableBle = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activity.startActivityForResult(enableBle,requestCode);
                }else {
                    result = true;
                }
        } else {
            Toast.makeText(activity,"该设备不支持BLE蓝牙",Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    /**
     * 是否需要申请运行时权限
     * @return
     */
    private static boolean isNeedRequestPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? true : false;
    }

    /**
     * 定位是否打开
     * @param context
     * @return
     */
    private static boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (networkProvider || gpsProvider) {
            return true;
        }
        return false;
    }

    /**
     * 检查定位权限，若无权限则申请，若有权限则检查是否定位已开启，若未开启，就申请开启
     * @param activity
     * @param requestCode   申请启用定位的requestCode
     * @param requestPermissionCode 申请定位权限
     * @return
     */
    public static boolean checkBlePermission(Activity activity,int requestCode,int requestPermissionCode) {
        boolean result = false;
        if (isNeedRequestPermission()) {
            boolean hasLocationPermission =
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (!hasLocationPermission) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestPermissionCode);
            } else {
                result = checkAndEnableLocation(activity,requestCode);
            }
        }else {
            result = true;
        }
        return result;
    }

    /**
     * 检查定位是否是启用状态，若未启用，则申请启用
     * @param activity
     * @param requestCode 申请启用定位的requestCode
     * @return 是否启用
     */
    public static boolean checkAndEnableLocation(Activity activity, int requestCode) {
        boolean result = false;
        if (!isLocationEnable(activity)) {
            Toast.makeText(activity,"由于系统问题，请开启定位服务",Toast.LENGTH_SHORT).show();
            Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivityForResult(locationIntent, requestCode);
        }else {
            result = true;
        }
        return result;
    }

}
