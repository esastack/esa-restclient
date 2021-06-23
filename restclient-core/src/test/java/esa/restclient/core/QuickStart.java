package esa.restclient.core;

import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;
import esa.commons.netty.http.CookieImpl;
import esa.restclient.core.codec.Decoder;
import esa.restclient.core.codec.Encoder;

import java.lang.reflect.Type;

public class QuickStart {

    private static final String url = "http://localhost:8080/hello";

    public static void main(String[] args) {
        RestClient restClient = createClient();
        sendRestRequest(restClient);
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
                .addDecoder(new Decoder<String>() {
                    @Override
                    public boolean canDecode(Class<?> type, Type genericType, MediaType mediaType) {
                        //TODO implement the method!
                        throw new UnsupportedOperationException("The method need to be implemented!");
                    }

                    @Override
                    public String decode(Class<String> type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders, Buffer buffer) {
                        //TODO implement the method!
                        throw new UnsupportedOperationException("The method need to be implemented!");
                    }
                })
                .addEncoder(new Encoder<String>() {
                    @Override
                    public boolean canEncode(Class<?> type, Type genericType, MediaType mediaType) {
                        //TODO implement the method!
                        throw new UnsupportedOperationException("The method need to be implemented!");
                    }

                    @Override
                    public Buffer encode(String entity, Type genericType, MediaType mediaType, HttpHeaders httpHeaders) {
                        //TODO implement the method!
                        throw new UnsupportedOperationException("The method need to be implemented!");
                    }
                })
                .addInterceptor(((request, next) -> next.proceed(request)))
                .build();
    }
}
