package com.example.syslog.ling;

import com.alibaba.fastjson.JSONObject;
import org.apache.tomcat.jni.Socket;
import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogConstants;
import org.graylog2.syslog4j.SyslogIF;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Blackdragon9527
 * @date 2023/5/24 - 15:24
 */
public class SysLogJob  {
    private static ArrayBlockingQueue queue = new ArrayBlockingQueue(5000);
    private static int SOCKET_POOL_SIZE = 100;
    private static Random r = new Random();
    private volatile static Map<String, Socket> tcpMap = new HashMap<>();
    private static List<SyslogIF> UdpSyslogPool = new ArrayList<>(SOCKET_POOL_SIZE);
    static {
        while (true) {

            if (UdpSyslogPool.size() == 100) {
                break;
            }
            try {
                UdpSyslogPool.add(Syslog.getInstance(SyslogConstants.UDP));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    //放消息
    public static void putMsg(String msg) {
        queue.add(msg);
        new SysLogJob().execute("jkjkjl");
    }

    @Scheduled(fixedDelay = 5000)
    public static void execute(String msg){

        //消费信息
        while((msg=(String)queue.poll())!=null){
            System.out.println("sb21");
            //发送syslog到服务端
            SyslogIF syslog =  UdpSyslogPool.get(r.nextInt(SOCKET_POOL_SIZE));

            // 确定目标服务器的ip和端口
            syslog.getConfig().setHost("127.0.0.1");//端口
            syslog.getConfig().setPort(Integer.parseInt("32376"));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sysLog", msg);
            System.out.println("sbsbsbs");
            try {
                //扫描队列 有消息就发服务端
                syslog.log(0, URLDecoder.decode(jsonObject.toJSONString(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                System.out.println("generate log get exception " + e);
            }
        }
        System.out.println("sb2");
    }

    public static void main(String[] args) {
//
        new SysLogJob().putMsg("jkjkjl");
//        new SysLogJob().execute("127.0.0.1","32376","jkjkjl");
    }
}

