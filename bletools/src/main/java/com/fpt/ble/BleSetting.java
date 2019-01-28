package com.fpt.ble;

import java.util.UUID;

/**
 * <pre>
 *   @author  : fpt
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2018/12/05 16:34
 *   desc    : 公共参数池
 *   version : 1.0.0
 * </pre>
 */
public class BleSetting {

    /**
     * service uuid
     */
    public static UUID UUID_SERVICE =
            UUID.fromString("00001000-0000-1000-8000-00805f9b34fb");

    /**
     * characteristic uuid
     */
    public static UUID UUID_CHARACTERISTIC =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    public static int MAX_RECONNECT_COUNT = Integer.MAX_VALUE;

    /**
     * 修改默认值
     * @param uuidService
     * @param uuidCharacteristic
     */
    public static void init(UUID uuidService, UUID uuidCharacteristic) {
        setUuidService(uuidService);
        setUuidCharacteristic(uuidCharacteristic);
    }

    /**
     * 修改默认值
     * @param uuidService
     */
    public static void setUuidService(UUID uuidService) {
        BleSetting.UUID_SERVICE = uuidService;
    }

    /**
     * 修改默认值
     * @param uuidCharacteristic
     */
    public static void setUuidCharacteristic(UUID uuidCharacteristic) {
        BleSetting.UUID_CHARACTERISTIC = uuidCharacteristic;
    }

    /**
     * 修改中心设备重连次数
     * @param count
     */
    public static void setCentralReconnectCount(int count){
        BleSetting.MAX_RECONNECT_COUNT = count;
    }

}
