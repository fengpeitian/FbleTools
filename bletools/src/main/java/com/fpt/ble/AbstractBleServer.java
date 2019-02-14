package com.fpt.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


/**
 * <pre>
 *   @author  : fpt
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2018/12/05 16:41
 *   desc    : ble服务端父类,支持数据收发、监听连接状态
 *   version : 2.0.3
 * </pre>
 */
public abstract class AbstractBleServer {
    private Context mContext;
    private BluetoothAdapter mBlueToothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattServer mGattServer;
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseCallback mAdvertiseCallback;
    private DataBuffer dataBuffer = new DataBuffer(4096);
    private OnProgressListener onSendProgressListener;
    private OnProgressListener onReceiveProgressListener;

    /**
     * 绑定的设备
     */
    private List<BluetoothDevice> mBondBluetoothDevices = new ArrayList<>();
    private BluetoothDevice mBondBluetoothDevice;

    public AbstractBleServer(@NonNull Context context) {
        this.mContext = context;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //不支持低功耗蓝牙
            throw new NullPointerException("this device's BluetoothAdapter is null");
        } else {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(android.content.Context.BLUETOOTH_SERVICE);
            mBlueToothAdapter = mBluetoothManager.getAdapter();
        }
        initServices();
        startBleAdvertise();
    }

    /**
     * 初始化BLE蓝牙广播Advertiser，配置指定UUID的服务
     */
    public void startBleAdvertise() {
        if (mBluetoothLeAdvertiser == null) {
            mBluetoothLeAdvertiser = mBlueToothAdapter.getBluetoothLeAdvertiser();
        }
        int length;
        if (TextUtils.isEmpty(mBlueToothAdapter.getName())){
            length = 18;
        }else {
            int temp = 18 - mBlueToothAdapter.getName().length();
            length = temp < 0 ? 0:temp;
        }
        AdvertiseSettings settings = buildAdvertiseSettings();

        AdvertiseData advertiseData = buildAdvertiseData(new ParcelUuid(BleSetting.UUID_SERVICE),getAdvertiseDataBytes(length));

        //广播创建成功之后的回调
        mAdvertiseCallback = new AdvertiseCallback() {
            private String TAG = "AdvertiseCallback";

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {

            }

            @Override
            public void onStartFailure(int errorCode) {
                switch (errorCode){
                    case ADVERTISE_FAILED_DATA_TOO_LARGE:
                        Log.e(TAG, "Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.");
                        break;
                    case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        Log.e(TAG, "Failed to start advertising because no advertising instance is available.");
                        break;
                    case ADVERTISE_FAILED_ALREADY_STARTED:
                        Log.e(TAG, "Failed to start advertising as the advertising is already started");
                        break;
                    case ADVERTISE_FAILED_INTERNAL_ERROR:
                        Log.e(TAG, "Operation failed due to an internal error");
                        break;
                    case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        Log.e(TAG, "This feature is not supported on this platform");
                        break;
                    default:
                        break;
                }
            }
        };
        mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, mAdvertiseCallback);
    }

    /**
     * 停止广播服务
     */
    public void stopBleAdvertise(){
        if (mBluetoothLeAdvertiser != null && mAdvertiseCallback != null){
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mBluetoothLeAdvertiser = null;
        }
    }

    /**
     * 获得已连接的设备
     * @return
     */
    public List<BluetoothDevice> getBondBluetoothDevices(){
        return mBondBluetoothDevices;
    }

    /**
     * 断开与指定设备的连接
     * @param device
     */
    public void disconnect(BluetoothDevice device){
        mGattServer.cancelConnection(device);
    }

    /**
     * 从绑定列表中选择一个设备绑定，其他设备将会被断开连接
     * @param device
     * @return
     */
    public boolean bindBluetoothDevice(BluetoothDevice device){
        if (mBondBluetoothDevices.contains(device)){
            mBondBluetoothDevice = device;

            stopBleAdvertise();

            for (int i = 0; i < mBondBluetoothDevices.size(); i++) {
                if (i != mBondBluetoothDevices.indexOf(device)){
                    disconnect(mBondBluetoothDevices.get(i));
                }
            }

            return true;
        }else {
            return false;
        }
    }

    /**
     * 设置广播频率
     * @return
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        return new AdvertiseSettings.Builder()
                /*
                ADVERTISE_MODE_LOW_LATENCY 100ms
                ADVERTISE_MODE_LOW_POWER 1s(默认)
                ADVERTISE_MODE_BALANCED  250ms
                */
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                /*
                ADVERTISE_TX_POWER_ULTRA_LOW
                ADVERTISE_TX_POWER_LOW
                ADVERTISE_TX_POWER_MEDIUM(默认)
                ADVERTISE_TX_POWER_HIGH
                */
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();
    }

    /**
     * 构建广播数据
     * @param Service_UUID
     * @param data
     * @return
     */
    private AdvertiseData buildAdvertiseData(ParcelUuid Service_UUID, byte[] data) {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        //设置 serviceUuid
        dataBuilder.addServiceUuid(Service_UUID);
        // 是否包含设备名称
        dataBuilder.setIncludeDeviceName(true);
        //广播包符合长度
        if (data != null && data.length > 0) {
            dataBuilder.addServiceData(Service_UUID, data);
        }
        return dataBuilder.build();
    }

    /**
     * 初始化服务，mGattServer添加服务
     */
    private void initServices() {
        //创建GattServer服务器
        mGattServer = mBluetoothManager.openGattServer(mContext, bluetoothGattServerCallback);

        //这个指定的创建指定UUID的服务
        BluetoothGattService service = new BluetoothGattService(BleSetting.UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //添加指定UUID的可读可写characteristic
        mCharacteristic = new BluetoothGattCharacteristic(
                BleSetting.UUID_CHARACTERISTIC,
                BluetoothGattCharacteristic.PROPERTY_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(mCharacteristic);

        mGattServer.addService(service);
    }

    /**
     * 服务事件的回调
     */
    private BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {

        /**
         * 连接状态发生变化时
         * @param device
         * @param status        表示相应的连接或断开操作是否完成，而不是指连接状态
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED){
                if (!mBondBluetoothDevices.contains(device)){
                    mBondBluetoothDevices.add(device);

                    onConnectionChange(device, newState);
                }
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                if (mBondBluetoothDevices.contains(device)){
                    mBondBluetoothDevices.remove(device);

                    onConnectionChange(device, newState);

                    if (mBondBluetoothDevice.equals(device)){
                        mBondBluetoothDevice = null;
                        startBleAdvertise();
                    }
                }
            }
            super.onConnectionStateChange(device, status, newState);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {

            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }

        /**
         * onCharacteristicWriteRequest,接收具体的字节
         * @param device
         * @param requestId
         * @param characteristic
         * @param preparedWrite
         * @param responseNeeded
         * @param offset
         * @param requestBytes
         */
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes) {

            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, requestBytes);
            //处理响应内容
            onResponseToClient(requestBytes, device, requestId, characteristic);
        }

        /**
         * 描述被写入时，在这里执行 bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS...  收，触发 onCharacteristicWriteRequest
         * @param device
         * @param requestId
         * @param descriptor
         * @param preparedWrite
         * @param responseNeeded
         * @param offset
         * @param value
         */
        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {

            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }

        /**
         * 特征被读取。当回复响应成功后，客户端会读取然后触发本方法
         * @param device
         * @param requestId
         * @param offset
         * @param descriptor
         */
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {

            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
        }

    };

    /**
     * 处理响应内容
     *
     * @param requestBytes
     * @param device
     * @param requestId
     * @param characteristic
     */
    private void onResponseToClient(byte[] requestBytes, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic) {
        onReceiveBytes(requestBytes);
    }

    /**
     * 发送字节方法
     * @param bytes 要发送的字节
     * @return 是否发送成功
     */
    private boolean sendBytes(byte[] bytes){
        if (mBondBluetoothDevice != null) {
            mCharacteristic.setValue(bytes);
            boolean result = mGattServer.notifyCharacteristicChanged(mBondBluetoothDevice, mCharacteristic, false);
            return result;
        }else {
            return false;
        }
    }

    /**
     * 设置发送进度监听
     * @param onSendProgressListener
     */
    public void setOnSendProgressListener(OnProgressListener onSendProgressListener) {
        this.onSendProgressListener = onSendProgressListener;
    }

    /**
     * 设置接收监听
     * @param onReceiveProgressListener
     */
    public void setOnReceiveProgressListener(OnProgressListener onReceiveProgressListener) {
        this.onReceiveProgressListener = onReceiveProgressListener;
    }

    /**
     * 发送字符串
     * 注意: 1.不能连接上后马上发送,最好第一次发送做个延迟
     *      2.一定要在子线程里调用
     *      3.注意字节数不能超过4096，理论此发送速率 800 Bytes/S
     * @param _bytes
     */
    public boolean send(byte[] _bytes){
        try {
            byte[] bytes = DataUtils.getData(_bytes);
            int all_length = bytes.length;
            DataBuffer dataBuffer = new DataBuffer(all_length);
            dataBuffer.enqueue(bytes,all_length);
            boolean result = true;
            for (int i = 0; i < all_length/20; i++) {
                byte[] sends = new byte[20];
                dataBuffer.dequeue(sends,20);
                //兼容IOS的情况下20ms间隔，安卓为7.5ms间隔
                Thread.sleep(20);
                boolean isSend = sendBytes(sends);
                if (isSend){
                    if (onSendProgressListener != null) {
                        onSendProgressListener.onProgress((i + 1) * 20, all_length);
                    }
                }else {
                    result = false;
                    break;
                }
            }
            return result;
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 收到的字节数组
     * @param value
     */
    private void onReceiveBytes(byte[] value) {
        if (value.length == 20){
            int bytes_sum = DataUtils.checkCode(value,1,value.length);
            if (bytes_sum == value[0]) {
                int package_count = value[1];
                int package_current = value[2];
                int valid_data = value[3];

                if (package_current == package_count) {
                    byte[] bytes = new byte[valid_data];
                    System.arraycopy(value, 4, bytes, 0, valid_data);
                    dataBuffer.enqueue(bytes, bytes.length);

                    byte[] all = new byte[(package_count - 1) * 16 + valid_data];
                    dataBuffer.dequeue(all, all.length);
                    if (onReceiveProgressListener != null){
                        onReceiveProgressListener.onProgress(package_current*20,package_count*20);
                    }
                    onReceive(all);
                } else {
                    byte[] bytes = new byte[16];
                    System.arraycopy(value, 4, bytes, 0, 16);
                    dataBuffer.enqueue(bytes, bytes.length);
                    if (onReceiveProgressListener != null){
                        onReceiveProgressListener.onProgress(package_current*20,package_count*20);
                    }
                }
            }else {
                throw new IllegalArgumentException("Parameter checksum error");
            }
        }else {
            throw new IllegalArgumentException("Parameter byte length error");
        }
    }

    /**
     * 设置广播的字节数据
     * @param length  广播字节数组最大长度
     * @return
     */
    protected abstract byte[] getAdvertiseDataBytes(int length);

    /**
     * 接收
     * @param receive 接收到的字节
     */
    protected abstract void onReceive(byte[] receive);

    /**
     * 连接状态发生改变
     * @param device
     * @param connectionState
     */
    protected abstract void onConnectionChange(BluetoothDevice device, int connectionState);

}
