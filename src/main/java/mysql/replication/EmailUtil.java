package mysql.replication;

/**
 * Created by Administrator on 2017/12/11.
 */
import org.apache.commons.mail.*;


/**
 * @author liny
 *    邮件发送工具类
 */
public class EmailUtil {


    public static void sendMail( String subject, String content){
        ImageHtmlEmail email = new ImageHtmlEmail();
        email.setHostName("smtp.zy.com");
        email.setSmtpPort(25);
        email.setAuthenticator(new DefaultAuthenticator("yuyou_app@zy.com", "Yuyou100"));
        email.setSSLOnConnect(true);//commons-mail-1.1支持的方法，1.4中使用setSSLOnConnect(true)代替
        try {
            email.setFrom("yuyou_app@zy.com");
            email.setSubject(subject);
            email.setMsg(content);
            email.addTo("wenzuojing1@zy.com");
            email.send();

        } catch (EmailException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        sendMail("【MySQL数据复制服务】","发送失败");
    }



}