package ffmpegenc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Properties;

/**
 * FFMPEG一键加密程序主入口。
 */
public class Encryptor {
    //FFMPEG路径
    private String ffmpeg_path;
    //回调函数
    private ICallback callback;
    //视频编码器，一定要是x264的编码器
    private final String VCODEC = "libx264";
    //音频编码器，一定要是aac的编码器。
    //aac是ffmpeg自带的编码器，某些机器上使用的可能是libfdkaac或者libfaac。
    private final String ACODEC = "aac"; 

    /**
     * 使用系统的PATH环境变量中指定的FFMPEG可执行文件路径和给定的回调函数实现初始化一键加密程序。<br>
     * 等同于Encryptor(null, callback)。
     * @param callback 回调函数。
     */
    public Encryptor(ICallback callback) {
        this(null, callback);
    }

    /**
     * 使用用户指定的FFMPEG可执行文件路径和给定的回调函数实现初始化一键加密程序。
     * @param ffmpeg_path FFMPEG可执行文件所在路径。如果为null或空字符串则将在PATH环境变量中搜索FFMPEG可执行文件。
     * @param callback 回调函数。
     */
    public Encryptor(String ffmpeg_path, ICallback callback) {
        this.ffmpeg_path = (ffmpeg_path == null) ? "" : ffmpeg_path;
        if(!this.ffmpeg_path.equals("")){
            //用户定义了FFMPEG可执行文件路径。如果最后没有“\”或“/”则自动补全
            if(!this.ffmpeg_path.endsWith(File.separator)) this.ffmpeg_path += File.separator;
        }
        this.callback = callback;
    }
    
    /**
     * 执行一键加密过程。该过程是异步的，因此执行时不会使程序阻塞。在执行完毕后，根据执行结果，对应的回调函数将会被调用。<br>
     * 传入参数param中可以包含任何信息，但必须包含以下两个条目：<br>
     * (1)键为“input”的项指定输入文件的带全路径的文件名；<br>
     * (2)键为“output”的项指定输出文件的带全路径的文件名，后缀名必须为.m3u8，且输出文件的所在目录必须已经存在。<br>
     * 在给定的目录下将会生成以下这些文件：<br>
     * (1)[文件名].m3u8：HLS播放列表文件；<br>
     * (2)[文件名].key：播放器解密用的密钥文件；<br>
     * (3)[文件名][编号].ts：加密后的视频片段。
     * @param param 带有输入和输出文件名的参数。
     * @throws IOException 如果生成密钥文件过程中发生错误则抛出。
     * @throws IllegalArgumentException 如果给定的param不包含键input和output则抛出。
     */
    public void runEncrypt(Properties param) throws IOException{
        if(!param.containsKey("input") || !param.containsKey("output")){
            //没有指定输入和输出
            throw new IllegalArgumentException("Param 'input' or 'output' not specified.");
        }
        else{
            /* FFMPEG Command Line Sample:
             * ffmpeg -i <input> -vcodec libx264 -acodec aac -f hls -hls_key_info_file <keyinfo> -hls_list_size 0 <output>
             */
            ArrayList<String> command = new ArrayList<String>();
            String output = param.getProperty("output");
            //生成密钥
            String keyinfo = generateKeyFile(output);
            //构造ffmpeg执行命令
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
            //开新线程执行加密过程
            new Thread(new FfmpegRunnable(command, keyinfo, param, callback)).start();
        }
    }
    
    private String generateKeyFile(String fileFullName) throws IOException{
        //生成一个128位（16字节）的密钥
        byte[] key = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        //将密钥写入文件
        String filepath = fileFullName.substring(0, fileFullName.lastIndexOf(File.separatorChar) + 1);
        String filename = fileFullName.substring(fileFullName.lastIndexOf(File.separatorChar) + 1, fileFullName.lastIndexOf('.'));
        filename += ".key";
        FileOutputStream fos = new FileOutputStream(filepath + filename, false);
        fos.write(key);
        fos.close();
        //生成临时keyinfo文件供ffmpeg使用
        fos = new FileOutputStream(filepath + filename + ".keyinfo", false);
        PrintStream bos = new PrintStream(fos);
        //第一行是#EXT-X-KEY:METHOD=AES-128,URI="<...>"中的<...>
        bos.println(filename);
        //第二行是密钥文件的完整路径，供加密使用
        bos.println(filepath + filename);
        bos.close();
        fos.close();
        //返回keyinfo文件的完整路径供ffmpeg调用
        return (filepath + filename + ".keyinfo");
    }
}
