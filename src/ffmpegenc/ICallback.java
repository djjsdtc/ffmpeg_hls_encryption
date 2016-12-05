/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ffmpegenc;

import java.util.Properties;

/**
 *
 * @author Administrator
 */
public interface ICallback {
    public void onSuccess(Properties info);
    public void onError(Properties info);
}
