package esa.restclient;

import com.google.gson.reflect.TypeToken;
import esa.commons.netty.http.CookieImpl;
import esa.restclient.codec.*;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuickStart {

    private static final String url = "http://localhost:8080/hello";

    public static void main(String[] args) {
        RestClient restClient = createClient();
        sendRestRequest(restClient);
    }

    private static void sendRestRequest(RestClient restClient) {
//        restClient.get(url)
//                .accept(MediaType.APPLICATION_JSON)
//                .cookie(new CookieImpl("aaa", "aaa"))
//                .cookie("aaa", "aaa")
//                .maxRetries(3)
//                .readTimeout(100)
//                .execute()
//                .thenAccept(response -> System.out.println(response.bodyToEntity(QuickStart.class)));

        restClient.post(url)
                .accept(MediaType.TEXT_HTML)
                .cookie(new CookieImpl("aaa", "aaa"))
                .bodyEntity(new GenericEntity<List<String>>(Arrays.asList(new String[]{"aaa", "aaa"})) {
                })
                .maxRetries(3)
                .readTimeout(100)
                .execute()
                .thenAccept(response -> System.out.println(response.bodyToEntity(String.class)));
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static RestClient createClient() {
        return RestClient.create()
                .addInterceptor((request, next) -> {
                    System.out.println("----------Intercept1 begin----------");
                    System.out.println(request.headers());
                    return next.proceed(request).thenApply((response) -> {
                                System.out.println("----------Intercept1 end----------");
                                System.out.println(response.headers());
                                return response;
                            }

                    );
                })
                .addInterceptor((request, next) -> {
                    System.out.println("----------Intercept2 begin----------");
                    System.out.println(request.headers());
                    return next.proceed(request).thenApply((response) -> {
                                System.out.println("----------Intercept2 end----------");
                                System.out.println(response.headers());
                                return response;
                            }

                    );
                })
                .addInterceptor((request, next) -> {
                    System.out.println("----------Intercept3 begin----------");
                    System.out.println(request.headers());
                    return next.proceed(request).thenApply((response) -> {
                                System.out.println("----------Intercept3 end----------");
                                System.out.println(response.headers());
                                return response;
                            }

                    );
                })
                .addBodyReader(new JsonBodyReader())
                .addBodyWriter(new JsonBodyWriter())
                .addBodyReader(new StringBodyReader())
                .addBodyWriter(new StringBodyWriter())
                .build();
    }
}
