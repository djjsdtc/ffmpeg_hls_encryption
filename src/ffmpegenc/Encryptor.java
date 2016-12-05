/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ffmpegenc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
public class Encryptor {
    private String ffmpeg_path;
    private ICallback callback;
    private final String VCODEC = "libx264";
    private final String ACODEC = "aac";    //on some version might be "libfaac"?

    public Encryptor(ICallback callback) {
        this(null, callback);
    }

    public Encryptor(String ffmpeg_path, ICallback callback) {
        this.ffmpeg_path = (ffmpeg_path == null) ? "" : ffmpeg_path;
        if(!this.ffmpeg_path.equals("")){
            //user defined where the ffmpeg's path is
            if(!this.ffmpeg_path.endsWith(File.separator)) this.ffmpeg_path += File.separator;
        }
        this.callback = callback;
    }
    
    public void runEncrypt(Properties param) throws IOException{
        if(!param.containsKey("input") || !param.containsKey("output")){
            throw new IllegalArgumentException("Param 'input' or 'output' not specified.");
        }
        else{
            /**************
             * FFMPEG Command Line Sample:
             * ffmpeg -i <input> -vcodec libx264 -acodec aac -f hls -hls_key_info_file <keyinfo> -hls_list_size 0 <output>
             */
            ArrayList<String> command = new ArrayList<String>();
            String output = param.getProperty("output").toString();
            String keyinfo = generateKeyFile(output);
            command.add(ffmpeg_path + "ffmpeg");
            command.add("-i");
            command.add("\"" + param.getProperty("input").toString() + "\"");
            command.add("-vcodec");
            command.add(VCODEC);
            command.add("-acodec");
            command.add(ACODEC);
            command.add("-f");
            command.add("hls");
            command.add("-hls_key_info_file");
            command.add("\"" + keyinfo + "\"");     //key file name
            command.add("-hls_list_size");
            command.add("0");
            command.add("\"" + output + "\"");
            new Thread(new FfmpegRunnable(command, keyinfo, param, callback)).start();
            //key full path and filename
        }
    }
    
    private String generateKeyFile(String fileFullName) throws IOException{
        //generate a 16-bytes(128-bits) key
        byte[] key = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        //write the key into a key file
        String filepath = fileFullName.substring(0, fileFullName.lastIndexOf(File.separatorChar) + 1);
        String filename = fileFullName.substring(fileFullName.lastIndexOf(File.separatorChar) + 1, fileFullName.lastIndexOf('.'));
        filename += ".key";
        FileOutputStream fos = new FileOutputStream(filepath + filename, false);
        fos.write(key);
        fos.close();
        //write the keyinfo file
        fos = new FileOutputStream(filepath + filename + ".keyinfo", false);
        PrintStream bos = new PrintStream(fos);
        bos.println(filename);
        bos.println(filepath + filename);
        bos.close();
        fos.close();
        //return the keyinfo path and filename
        return (filepath + filename + ".keyinfo");
    }
}
