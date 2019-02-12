package com.fpt.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;


/**
 * <pre>
 *   @author  : fpt
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2018/12/06 12:39
 *   desc    : ble客户端父类,支持数据收发、监听连接状态
 *   version : 2.0.3
 * </pre>
 */
public abstract class AbstractBleClient {
    /**
     * 扫描设备开始时发送的消息
     */
    private static final int DEVICE_SCAN_STARTED = 1;
    /**
     * 扫描终止时发送的消息
     */
    private static final int DEVICE_SCAN_STOPPED = 2;
    /**
     * 扫描设备完成时发送的消息
     */
    private static final int DEVICE_SCAN_COMPLETED = 3;

    private Context mContext;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothAdapter mBlueToothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private OnScanStateListener listener;
    private DataBuffer dataBuffer = new DataBuffer(4096);
    private OnProgressListener onSendProgressListener;
    private OnProgressListener onReceiveProgressListener;
    private BluetoothDevice mBondBluetoothDevice;
    private int reconnect_count = 0;
    private boolean isScanning = false;

    public void setOnScanStateListener(OnScanStateListener listener) {
        this.listener = listener;
    }

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (listener != null) {
                switch (msg.what) {
                    case DEVICE_SCAN_STARTED:
                        listener.onStart();
                        break;
                    case DEVICE_SCAN_STOPPED:
                        listener.onStop();
                        break;
                    case DEVICE_SCAN_COMPLETED:
                        listener.onCompleted();
                        break;
                    default:
                        break;
                }
            }
        }
    };

    public AbstractBleClient(@NonNull Context context) {
        this.mContext = context;
        android.bluetooth.BluetoothManager mBluetoothManager = (BluetoothManager) mContext.getSystemService(android.content.Context.BLUETOOTH_SERVICE);
        mBlueToothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeScanner = mBlueToothAdapter.getBluetoothLeScanner();
    }

    /**
     * 扫描设备的方法，扫描按钮点击后调用
     * @param enable 扫描方法的使能标志
     * @param scanTime 单次扫描时间(毫秒单位)
     */
    public void scanBleDevice(boolean enable,int scanTime) {
        if (enable) {
            if (!isScanning) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isScanning) {
                            mBluetoothLeScanner.stopScan(mScanCallback);
                            isScanning = false;
                            Message message = new Message();
                            message.what = DEVICE_SCAN_COMPLETED;
                            handler.sendMessage(message);
                        }
                    }
                }, scanTime);

                mBluetoothLeScanner.startScan(mScanCallback);
                isScanning = true;
                Message message = new Message();
                message.what = DEVICE_SCAN_STARTED;
                handler.sendMessage(message);
            }
        } else {
            if (isScanning) {
                mBluetoothLeScanner.stopScan(mScanCallback);
                isScanning = false;
                Message message = new Message();
                message.what = DEVICE_SCAN_STOPPED;
                handler.sendMessage(message);
            }
        }

    }

    /**
     * 扫描设备的方法，扫描按钮点击后调用，只扫描出指定服务的结果，扫描持续5秒
     *
     * @param enable 扫描方法的使能标志
     */
    public void scanBleDevice(boolean enable){
        scanBleDevice(enable,5*1000);
    }

    /**
     * 蓝牙扫描时的回调
     */
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            onScan(callbackType,result);
        }
    };

    /**
     * 连接
     * @param device 蓝牙设备
     */
    public void connect(BluetoothDevice device){
        this.mBondBluetoothDevice = device;
        mBluetoothGatt = mBondBluetoothDevice.connectGatt(mContext, false, mGattCallback);
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            mBondBluetoothDevice = null;
        }
    }

    /**
     * 发送字节方法
     * @param data 要发送的字节
     * @return      是否发送成功
     */
    private boolean sendBytes(byte[] data) {
        if (mBluetoothGatt == null || mCharacteristic == null || data == null){
            return false;
        }
        if (!mBluetoothGatt.connect()){
            return false;
        }

        mCharacteristic.setValue(data);
        boolean result = mBluetoothGatt.writeCharacteristic(mCharacteristic);
        return result;
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
     *      3.注意字符串长度字节数不能超过4096，理论此发送速率 80 Bytes/S
     * @param _bytes
     */
    public void send(byte[] _bytes){
        try {
            byte[] bytes = DataUtils.getData(_bytes);
            int all_length = bytes.length;
            DataBuffer dataBuffer = new DataBuffer(all_length);
            dataBuffer.enqueue(bytes,all_length);
            for (int i = 0; i < all_length/20; i++) {
                byte[] sends = new byte[20];
                dataBuffer.dequeue(sends,20);
                //此处需要200ms间隔，降低丢包几率
                Thread.sleep(200);
                boolean isSend = sendBytes(sends);
                if (onSendProgressListener != null && isSend){
                    onSendProgressListener.onProgress((i+1)*20,all_length);
                }
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * 连接Gatt之后的回调
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                reconnect_count = 0;
                //此处连接优先级设置是为了分包之间设置为200ms不丢包
                gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                gatt.discoverServices();
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
                if (mBondBluetoothDevice != null) {
                    if (reconnect_count <= BleSetting.MAX_RECONNECT_COUNT) {
                        connect(mBondBluetoothDevice);
                        reconnect_count++;
                    }
                }
            }
            onConnectionChange(newState);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic != null) {
                try {
                    onReceiveBytes(characteristic.getValue());
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(BleSetting.UUID_SERVICE);
                mCharacteristic = service.getCharacteristic(BleSetting.UUID_CHARACTERISTIC);
                //开启通知
                mBluetoothGatt.setCharacteristicNotification(mCharacteristic, true);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

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
     * 蓝牙扫描期间返回结果的回调
     * @param callbackType
     * @param result
     */
    protected abstract void onScan(int callbackType, ScanResult result);

    /**
     * 接收
     * @param receive 接收到的字节
     */
    protected abstract void onReceive(byte[] receive);

    /**
     * 连接状态发生改变
     * @param connectionState
     */
    protected abstract void onConnectionChange(int connectionState);

    public interface OnScanStateListener{

        /**
         * 开始扫描
         */
        void onStart();

        /**
         * 停止扫描
         */
        void onStop();

        /**
         * 完成扫描
         */
        void onCompleted();

    }

}
