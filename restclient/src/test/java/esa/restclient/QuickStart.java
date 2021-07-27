package esa.restclient;

import esa.commons.netty.http.CookieImpl;

public class QuickStart {

    private static final String url = "http://localhost:8080/hello";

    public static void main(String[] args) {
        RestClient restClient = createClient();
        sendRestRequest(restClient);
    }

    private static void sendRestRequest(RestClient restClient) {
        restClient.post(url)
                .cookie(new CookieImpl("aaa", "aaa"))
                .entity("aaa")
                .maxRetries(3)
                .readTimeout(100)
                .contentType(ContentType.TEXT_PLAIN)
                .accept(ContentType.TEXT_PLAIN)
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
        RestClientBuilder builder = RestClient.create();
        addInterceptors(builder);
        addEncodeAdvices(builder);
        addDecodeAdvices(builder);
        return builder.build();
    }

    private static void addInterceptors(RestClientBuilder builder) {
        builder.addInterceptor((request, next) -> {
            System.out.println("----------Intercept1 begin----------");
            System.out.println(request.headers());
            return next.proceed(request).thenApply((response) -> {
                        System.out.println("----------Intercept1 end----------");
                        System.out.println(response.headers());
                        return response;
                    }
            );
        }).addInterceptor((request, next) -> {
            System.out.println("----------Intercept2 begin----------");
            System.out.println(request.headers());
            return next.proceed(request).thenApply((response) -> {
                        System.out.println("----------Intercept2 end----------");
                        System.out.println(response.headers());
                        return response;
                    }
            );
        });
    }

    private static void addEncodeAdvices(RestClientBuilder builder) {
        builder.addEncodeAdvice(encodeContext -> {
            System.out.println("--------aroundEncode1 begin---------");
            RequestBodyContent<?> content = encodeContext.proceed();
            System.out.println("--------aroundEncode1 end---------");
            return content;
        }).addEncodeAdvice(encodeContext -> {
            System.out.println("--------aroundEncode2 begin---------");
            RequestBodyContent<?> content = encodeContext.proceed();
            System.out.println("--------aroundEncode2 end---------");
            return content;
        });
    }

    private static void addDecodeAdvices(RestClientBuilder builder) {
        builder.addDecodeAdvice(decodeContext -> {
            System.out.println("--------aroundDecode1 begin---------");
            Object result = decodeContext.proceed();
            System.out.println("--------aroundDecode1 end---------");
            return result;
        }).addDecodeAdvice(decodeContext -> {
            System.out.println("--------aroundDecode2 begin---------");
            Object result = decodeContext.proceed();
            System.out.println("--------aroundDecode2 end---------");
            return result;
        });
    }

}
