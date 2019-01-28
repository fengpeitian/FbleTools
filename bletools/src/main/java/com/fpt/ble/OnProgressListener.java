package com.fpt.ble;

/**
 * <pre>
 *   @author  : fpt
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2019/01/25 09:03
 *   desc    : 进度监听
 * </pre>
 */
public interface OnProgressListener {

    /**
     * 当前进度
     * @param done_byte  已完成字节
     * @param all_byte   所有字节
     */
    void onProgress(int done_byte,int all_byte);

}
