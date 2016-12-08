package ffmpegenc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

//执行FFMPEG程序的线程
class FfmpegRunnable implements Runnable{
    //命令行
    private List<String> command;
    //传入参数
    private Properties info;
    //回调函数
    private ICallback callback;
    //keyinfo文件位置
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
        //合并stdout和stderr
        builder.redirectErrorStream(true);
        try {
            //运行ffmpeg
            Process ffmpegProcess = builder.start();
            //读取控制台输出并保留最后一行（因为最后一行常常是错误信息）
            BufferedReader reader = new BufferedReader(new InputStreamReader(ffmpegProcess.getInputStream()));
            String lastStr = null;
            int result = -1;
            while (true) {
                try {
                    //获取ffmpeg进程的返回值
                    result = ffmpegProcess.exitValue();
                    break;
                } catch (IllegalThreadStateException e) {
                    //尚未执行完毕
                    String currStr;
                    while((currStr = reader.readLine()) != null){
                        lastStr = currStr;
                    }
                    Thread.sleep(1000);
                }
            }
            //删除临时生成的keyinfo文件
            File keyinfo = new File(keyinfo_path);
            if(keyinfo.exists()) keyinfo.delete();
            if(result == 0){
                //ffmpeg运行成功
                callback.onSuccess(info);
            }
            else{
                //运行失败，key文件也就无效，可以删除
                File keyfile = new File(keyinfo_path.substring(0, keyinfo_path.lastIndexOf(".keyinfo")));
                if(keyfile.exists()) keyfile.delete();
                //将最后一行输出作为错误信息
                info.setProperty("error", lastStr);
                callback.onError(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //找不到ffmpeg可执行文件
            info.setProperty("error", e.getMessage());
            callback.onError(info);
        }
    }
}
