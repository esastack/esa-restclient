package esa.restclient;

import esa.commons.netty.http.CookieImpl;
import esa.restclient.serializer.*;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class QuickStart {

    private static final String url = "http://localhost:8080/hello";

    public static void main(String[] args) {
        RestClient restClient = createClient();
        sendRestRequest(restClient);
    }

    private static void sendRestRequest(RestClient restClient) {
        restClient.post(url)
                .contentType(ContentType.APPLICATION_JSON_UTF8_JACKSON)
                .accept(ContentType.of(MediaType.TEXT_HTML, new RxSerializer() {
                    @Override
                    public String deSerialize(byte[] data, Type type) throws Exception {
                        return new String(data);
                    }

                    @Override
                    public String deSerialize(HttpInputStream inputStream, Type type) throws Exception {
                        //TODO implement the method!
                        throw new UnsupportedOperationException("The method need to be implemented!");
                    }
                }))
                .cookie(new CookieImpl("aaa", "aaa"))
                .entity("aaa")
                .maxRetries(3)
                .readTimeout(100)
                .execute()
                .thenAccept(response -> {
                    try {
                        System.out.println(response.bodyToEntity(String.class));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).whenComplete((result, e) -> e.printStackTrace());
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static RestClient createClient() {
        return RestClient.create()
                .addInterceptor((request, action, next) -> {
                    System.out.println("----------Intercept1 begin----------");
                    System.out.println(request.headers());
                    return next.proceed(request, action).thenApply((response) -> {
                                System.out.println("----------Intercept1 end----------");
                                System.out.println(response.headers());
                                return response;
                            }
                    );
                })
                .addInterceptor((request, action, next) -> {
                    System.out.println("----------Intercept2 begin----------");
                    System.out.println(request.headers());
                    return next.proceed(request, action).thenApply((response) -> {
                                System.out.println("----------Intercept2 end----------");
                                System.out.println(response.headers());
                                return response;
                            }

                    );
                })
                .addInterceptor((request, action, next) -> {
                    System.out.println("----------Intercept3 begin----------");
                    System.out.println(request.headers());
                    return next.proceed(request, action).thenApply((response) -> {
                                System.out.println("----------Intercept3 end----------");
                                System.out.println(response.headers());
                                return response;
                            }

                    );
                })
                .build();
    }
}
