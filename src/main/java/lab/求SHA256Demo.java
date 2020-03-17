package lab;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class 求SHA256Demo {
//MD5
    //SHA-256
    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        MessageDigest messageDigest=MessageDigest.getInstance("SHA-256");
                      //信息摘要                     //实例
        String s="你好世界";
        byte [] bytes=s.getBytes("UTF-8");
        messageDigest.update(bytes);//把要处理的字节传进去
        byte[] result=messageDigest.digest();// 吸收 处理完后结果放在result
        System.out.println(result.length);
        for(byte b:result) {//打印信息摘要的每条信息
            System.out.print(String.format("%02x", b));
        }
        System.out.println();

    }
}
