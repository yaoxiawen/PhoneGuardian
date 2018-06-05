package utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 流工具
 */
public class StreamUtils {
    /**
     * 将字节输入流获取的字节数据转换为字符串
     * @param in 字节输入流
     * @return 字符串
     * @throws IOException
     */
    public static String readFromStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = 0;
        byte[] buf = new byte[1024];
        while((len = in.read(buf))!=-1){
            out.write(buf,0,len);
        }
        String result = out.toString();
        in.close();
        out.close();
        return result;
    }
}
