---
sort: 1
---

# 为什么Codec这样设计

## Encoder
`Encoder`将用户设置的请求`Entity``Encode`成```RequestContent```。```RequestContent```负责将`RestClient``Encode`后的对象传递给`HttpClient`(底层使用`Netty`)，其当前可以接受```byte[]```、```MultipartBody```、```File```类型。
- Encode to ```byte[]```：底层的`HttpClient`将```byte[]```直接当做请求体发送。
- Encode to ```MultipartBody```：底层的`HttpClient`将`MultipartBody` 以Multipart的形式发送。
- Encode to ```File```：底层的`HttpClient`将使用`NIO`的`FileChannel`来进行0拷贝传输，传输更加快速的同时避免文件过大造成的OOM。

#### EncodeContext
`RestClient`将`Encode`过程所需要的数据均封装到```EncodeContext``` 接口中，这样更符合**依赖倒置**原则 ，在未来要对`Encode`的上下文进行扩展时，也不会影响到用户的业务逻辑。
调用`EncodeContext.next()`，意味着当前`Encoder`无法`Encode`该请求的`Entity`，使用下一个`Encoder`继续进行`Encode`。具体接口内容如下:
```java
public interface EncodeContext<V> extends EncodeChain {

    /**
     * @return The headers of request
     */
    HttpHeaders headers();

    @Override
    RequestContent<V> next() throws Exception;
}

public interface EncodeChain {

    /**
     * @return The contentType of request
     */
    MediaType contentType();

    /**
     * @return The entity of request
     */
    Object entity();

    /**
     * @return The type of entity
     */
    Class<?> entityType();

    /**
     * @return The generics of entity
     */
    Type entityGenerics();

    /**
     * Proceed to the next member in the chain.
     *
     * @return encoded requestContent
     * @throws Exception error
     */
    RequestContent<?> next() throws Exception;
}
```
#### EncodeAdviceContext
`RestClient`将`aroundEncode`过程所需要的数据均封装到```EncodeAdviceContext``` 接口中，这样更符合**依赖倒置**原则 ，在未来要对`EncodeAdvice`的上下文进行扩展时，也不会影响到用户的业务逻辑。
调用`EncodeAdviceContext.next()`，该方法的返回值为`Encode`后的结果，调用该方法通常意味着使用下一个`EncodeAdvice`，最后一个`EncodeAdvice`将会开始执行`Encode`流程。具体接口内容如下:
```java
public interface EncodeAdviceContext extends EncodeChain {

    RestRequest request();

    /**
     * set entity,this method is not safe for use by multiple threads
     *
     * @param entity entity
     */
    void entity(Object entity);

    /**
     * set entity and generics,this method is not safe for use by multiple threads
     *
     * @param entity      entity
     * @param generics generics
     */
    void entity(Object entity, Type generics);
}

public interface EncodeChain {

    /**
     * @return The contentType of request
     */
    MediaType contentType();

    /**
     * @return The entity of request
     */
    Object entity();

    /**
     * @return The type of entity
     */
    Class<?> entityType();

    /**
     * @return The generics of entity
     */
    Type entityGenerics();

    /**
     * Proceed to the next member in the chain.
     *
     * @return encoded requestContent
     * @throws Exception error
     */
    RequestContent<?> next() throws Exception;
}
```


## Decoder
`Decoder`将`ResponseContent`解码成用户期望的`Entity`类型。```ResponseContent```负责将`HttpClient`(底层使用`Netty`)接收到的数据传递给`RestClient`，其当前仅传递`byte[]`，直接对应了响应体的字节流。未来会支持更多类型，便于用户处理各种复杂场景(如:下载大文件)。
#### DecodeContext
`RestClient`将`Decode`过程所需要的数据均封装到```DecodeContext``` 接口中，这样更符合**依赖倒置**原则 ，在未来要对`Decode`的上下文进行扩展时，也不会影响到用户的业务逻辑。
调用`DecodeContext.next()`，意味着当前`Decoder`无法`Decode`该响应，使用下一个`Decoder`继续进行`Decode`。具体接口内容如下:
```java
public interface DecodeContext<V> extends DecodeChain {

    /**
     * @return The headers of response
     */
    HttpHeaders headers();

    @Override
    ResponseContent<V> content();
}

public interface DecodeChain {

    /**
     * @return The contentType of response
     */
    MediaType contentType();

    /**
     * @return The content of response
     */
    ResponseContent<?> content();

    /**
     * @return The type of target
     */
    Class<?> targetType();

    /**
     * @return The generics of target
     */
    Type targetGenerics();

    /**
     * Proceed to the next member in the chain.
     *
     * @return decoded object
     * @throws Exception error
     */
    Object next() throws Exception;
}
```

#### DecodeAdviceContext
`RestClient`将`aroundDecode`过程所需要的数据均封装到`DecodeAdviceContext` 接口中，这样更符合**依赖倒置**原则 ，在未来要对`DecodeAdvice`的上下文进行扩展时，也不会影响到用户的业务逻辑。
调用`DecodeAdviceContext.next()`，该方法的返回值为`Decode`后的结果，调用该方法通常意味着使用下一个`DecodeAdvice`，最后一个`DecodeAdvice`将会开始执行`Decode`流程。具体接口内容如下:
```java
public interface DecodeAdviceContext extends DecodeChain {

    RestRequest request();

    RestResponse response();

    /**
     * set responseContent,this method is not safe for use by multiple threads
     *
     * @param responseContent responseContent
     */
    void content(ResponseContent<?> responseContent);
}

public interface DecodeChain {

    /**
     * @return The contentType of response
     */
    MediaType contentType();

    /**
     * @return The content of response
     */
    ResponseContent<?> content();

    /**
     * @return The type of target
     */
    Class<?> targetType();

    /**
     * @return The generics of target
     */
    Type targetGenerics();

    /**
     * Proceed to the next member in the chain.
     *
     * @return decoded object
     * @throws Exception error
     */
    Object next() throws Exception;
}
```