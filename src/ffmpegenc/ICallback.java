package ffmpegenc;

import java.util.Properties;

/**
 * 回调函数规范接口。包含执行成功时的回调和执行失败时的回调。用户需要实现该接口供加密程序进行回调。
 */
public interface ICallback {
    /**
     * ffmpeg执行成功时的回调函数。
     * @param info 传入的信息结构。
     */
    public void onSuccess(Properties info);
    /**
     * ffmpeg执行失败时的回调函数。
     * @param info 传入的信息结构。可以通过info.getProperty("error")来获取详细的错误信息。
     */
    public void onError(Properties info);
}
