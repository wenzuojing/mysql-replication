package mysql.replication;

/**
 * Created by Administrator on 2017/12/11.
 */
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * @author liny
 *    邮件发送工具类
 */
public class EmailUtil {

    private final static OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(true).connectTimeout(30 , TimeUnit.SECONDS ).readTimeout(60 ,TimeUnit.SECONDS).build();

    public static void sendMail( String subject, String content){
        String to = "wenzuojing1@zy.com" ;
        FormBody formBody = new FormBody.Builder()
                .add("token", DigestUtils.md5Hex(to + "UEZJQGHAwpTHssfGV"))
                .add("toAddress", to)
                .add("subject", subject)
                .add("content", content)
                .build();

        Request request = new Request.Builder().url("http://10.135.57.159:9099/api/message/sendEmail").post(formBody).build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        sendMail("【MySQL数据复制服务】","发送失败");
    }



}