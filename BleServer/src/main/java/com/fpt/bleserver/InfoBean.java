package com.fpt.bleserver;

import android.text.TextUtils;

/**
 * <pre>
 *   @author  : lucien.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2019/01/25 16:55
 *   desc    :
 * </pre>
 */
public class InfoBean {
    private String ip;
    private String wifi;
    private String battery;
    private String url;
    private String password;

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("   ");
        if (!TextUtils.isEmpty(ip)){
            buffer.append("ip='" + ip + '\'');
            buffer.append("   ");
        }
        if (!TextUtils.isEmpty(wifi)){
            buffer.append("wifi='" + wifi + '\'');
            buffer.append("   ");
        }
        if (!TextUtils.isEmpty(battery)){
            buffer.append("battery='" + battery + '\'');
            buffer.append("   ");
        }
        if (!TextUtils.isEmpty(url)){
            buffer.append("url='" + url + '\'');
            buffer.append("   ");
        }
        if (!TextUtils.isEmpty(password)){
            buffer.append("password='" + password + '\'');
            buffer.append("   ");
        }
        buffer.append("}");
        return buffer.toString();
    }
}
