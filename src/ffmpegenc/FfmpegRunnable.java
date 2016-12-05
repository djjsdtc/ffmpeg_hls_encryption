/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ffmpegenc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
class FfmpegRunnable implements Runnable{
    private List<String> command;
    private Properties info;
    private ICallback callback;
    private String keyinfo_path;

    public FfmpegRunnable(List<String> command, String keyinfo_path, Properties info, ICallback callback) {
        this.command = command;
        this.keyinfo_path = keyinfo_path;
        this.info = info;
        this.callback = callback;
    }
    
    @Override
    public void run() {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        //builder.inheritIO();
        try {
            Process ffmpegProcess = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ffmpegProcess.getInputStream()));
            String lastStr = null;
            int result = -1;
            while (true) {
                try {
                    result = ffmpegProcess.exitValue();
                    break;
                } catch (IllegalThreadStateException e) {
                    String currStr;
                    while((currStr = reader.readLine()) != null){
                        lastStr = currStr;
                    }
                    Thread.sleep(1000);
                }
            }
            File keyinfo = new File(keyinfo_path);
            if(keyinfo.exists()) keyinfo.delete();
            if(result == 0){
                callback.onSuccess(info);
            }
            else{
                File keyfile = new File(keyinfo_path.substring(0, keyinfo_path.lastIndexOf(".keyinfo")));
                if(keyfile.exists()) keyfile.delete();
                info.setProperty("error", lastStr);
                callback.onError(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
            info.setProperty("error", e.getMessage());
            callback.onError(info);
        }
    }
}
