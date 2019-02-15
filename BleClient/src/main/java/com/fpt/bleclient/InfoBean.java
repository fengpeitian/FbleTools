package com.fpt.bleclient;

/**
 * <pre>
 *   @author  : lucien.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2019/01/25 16:55
 *   desc    :
 * </pre>
 */
public class InfoBean {
    /**
     * ip地址
     */
    private String ip;
    /**
     * 电池电量
     */
    private String battery;
    /**
     * wifi名称
     */
    private String wifi;
    /**
     * wifi密码
     */
    private String password;
    /**
     * 手机助手端推送来的url，本地选择浏览器打开
     */
    private String url;
    /**
     * 请求内容type
     */
    private int request = REQUEST_NONE;

    public static final int REQUEST_NONE = 0;

    public static final int REQUEST_HMD_STATE = 1;

    public static final int REQUEST_DISCONNECT_BLE = 2;

    public int getRequest() {
        return request;
    }

    @Override
    public String toString() {
        return "InfoBean{" +
                "ip='" + ip + '\'' +
                ", battery='" + battery + '\'' +
                ", wifi='" + wifi + '\'' +
                ", password='" + password + '\'' +
                ", url='" + url + '\'' +
                ", request=" + request +
                '}';
    }
}
