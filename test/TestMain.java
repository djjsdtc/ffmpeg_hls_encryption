
import ffmpegenc.Encryptor;
import ffmpegenc.ICallback;
import java.io.IOException;
import java.util.Properties;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Administrator
 */
public class TestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        ICallback callback = new ICallback() {
            @Override
            public void onSuccess(Properties info) {
                System.out.println("success");
                System.exit(0);
            }

            @Override
            public void onError(Properties info) {
                String error = info.getProperty("error");
                System.out.println(error);
                System.exit(0);
            }
        };
        Encryptor enc = new Encryptor("d:\\ffmpeg\\bin\\", callback);
        Properties param = new Properties();
        param.setProperty("input", "d:\\desktop\\afath16.mp4");
        param.setProperty("output", "c:\\output\\mylist.m3u8");
        enc.runEncrypt(param);
        while (true) {
            Thread.sleep(3000);
        }
    }

}
