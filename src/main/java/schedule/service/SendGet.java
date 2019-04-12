package schedule.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author white matter
 */
public class SendGet {
    public static String sendGet(String student_id) {
        //url
        String url = "http://jwzx.cqu.pt/kebiao/kb_stu.php?xh=" + student_id + "#kbStuTabs-list";
        //get请求数据
        StringBuffer sb = new StringBuffer();
        PrintWriter out = null;
        BufferedReader in = null;
        String result = null;
        try {
            URL realURL = new URL(url);
            // 打开和URL之间的连接，并 转成 HttpURLConnection 对象
            HttpURLConnection connection = (HttpURLConnection) realURL.openConnection();
            //设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0");
            //发送 get请求必须设置如下两行
            connection.setDoOutput(true);
            connection.setDoInput(true);
            //调用connect方法连接远程资源,且此方法应于设置后
            connection.connect();
            // 获取HttpURLConnection对象对应的输出流
            out = new PrintWriter(connection.getOutputStream());
            out.flush();
            //定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line = null;
            //获取自己想要的 源代码段
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }

            String R = "\\s*|\\t|\\r|\\n";
            Matcher m = Pattern.compile(R).matcher(sb);
            result = m.replaceAll("");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //去除所有的 空格 和 回车符
        //貌似只要去掉 空格就可以了
        return String.valueOf(result);
    }

    public static void main(String[] args) {
        System.out.println(SendGet.sendGet("2018211314"));
    }
}
