package esa.restclient.core;

import esa.commons.http.HttpHeaders;
import esa.commons.netty.http.CookieImpl;
import esa.commons.netty.http.Http1HeadersAdaptor;

public class QuickStart {

    private static final String url = "http://localhost:8080/hello";

    public static void main(String[] args) {
        HttpHeaders httpHeaders = new Http1HeadersAdaptor();
        httpHeaders.add("aasd", "asd");
        System.out.println(httpHeaders);
//        RestClient restClient = createClient();
//        sendRestRequest(restClient);
    }

    private static void sendRestRequest(RestClient restClient) {
        restClient.get(url)
                .accept(MediaType.APPLICATION_JSON)
                .cookie(new CookieImpl("aaa", "aaa"))
                .cookie("aaa", "aaa")
                .maxRetries(3)
                .readTimeout(100)
                .execute()
                .thenAccept(response -> System.out.println(response.bodyToEntity(QuickStart.class)));

        restClient.post(url)
                .accept(MediaType.APPLICATION_JSON)
                .cookie(new CookieImpl("aaa", "aaa"))
                .bodyEntity("Test")
                .maxRetries(3)
                .readTimeout(100)
                .execute()
                .thenAccept(response -> System.out.println(response.bodyToEntity(QuickStart.class)));
    }

    private static RestClient createClient() {
        return RestClient.create()
                .build();
    }
}
