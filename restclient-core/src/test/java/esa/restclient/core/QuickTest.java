package esa.restclient.core;


import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;

public class QuickTest {

    public static void main(String[] args){
        Cookie cookie = ClientCookieDecoder.STRICT.decode("aaa=aaa");
        System.out.println(cookie);
    }

}
