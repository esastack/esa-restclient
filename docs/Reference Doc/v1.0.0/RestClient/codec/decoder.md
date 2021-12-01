---
sort: 2
---

# 解码器
`RestClient`会自动根据用户的 `Headers` 与 期望`Entity`类型 等选择合适的解码器进行解码。`RestClient`内置了下面这些解码器：
- Json
    - jackson(默认)
    - fastjson
    - gson
- ProtoBuf
- String
- byte[]

除此之外`RestClient`也支持用户自定义解码器。
```note
其中Json相关的序列化方式默认配置了日期格式为`yyyy-MM-dd HH:mm:ss`
```

## 自定义解码器
### Decoder
`Decoder`将`ResponseContent`解码成用户期望的`Entity`类型。```ResponseContent```负责将`ESA-HttpClient`(底层使用`Netty`)接收到的数据传递给`RestClient`，其当前仅传递`byte[]`，直接对应了响应体的字节流。未来会支持更多类型，便于用户处理各种复杂场景(如:下载大文件)。

#### 示例
为了便于用户使用，`RestClient`抽象出继承自```Decoder```的```ByteDecoder```，用户无需再判断`ResponseContent`内部数据的具体类型。下面为`ByteDecoder`的示例：
```java
public class StringDecoder implements ByteDecoder {

    @Override
    public Object doDecode(DecodeContext<byte[]> ctx) {
        if (String.class.isAssignableFrom(ctx.targetType())) {
            MediaType contentType = ctx.contentType();
            Charset charset = null;
            if (contentType != null) {
                charset = contentType.charset();
            }
            if (charset == null) {
                return new String(ctx.content().value(), StandardCharsets.UTF_8);
            } else {
                return new String(ctx.content().value(), charset);
            }
        }
        return ctx.next();
    }
}
```

#### DecodeContext
`RestClient`将解码过程所需要的数据均封装到```DecodeContext``` 接口中，这样更符合**依赖倒置**原则 ，在未来要对解码的上下文进行扩展时，也不会影响到用户的业务逻辑。
调用`DecodeContext.next()`，意味着当前解码器无法解码该响应，使用下一个解码器继续进行解码。具体接口内容如下:
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

### 配置解码器
#### Builder
在构造`RestClient`时传入自定义的`Decoder`实例，如：
```java
final RestClient client = RestClient.create()
                .addDecoder(ctx -> {
                    //解码...
                    return ctx.next();
                })
                .build();
```
```tip
- 多个`Decoder`之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高。
```
#### SPI
`RestClient`支持通过SPI的方式加载`Decoder`接口的实现类，使用时只需要按照SPI的加载规则将自定义的`Decoder`放入指定的目录下即可。

#### 直接绑定Request
`Decoder`可以直接绑定`Request`，使用方式如下:
```java
final RestResponseBase response = client.post(url)
                        .entity(new File("aaa"))
                        .decoder(ctx -> {
                            //解码...
                            //如果该解码器无法解码该类型，则调用下一个解码器
                            return ctx.next();
                        })
                        .execute()
                        .toCompletableFuture()
                        .get();
```
```tip
- 当`Request`绑定了`Decoder`，该Client中设置的所有`Decoder`将对该请求失效。即：如果当前`Decoder`无法解码该响应，则`RestClient`将会抛出CodecException异常。
```

### 执行时机
见[请求处理完整流程](../process_of_restclient/)中的`Decoder`。

## 解码器埋点
用户可以通过`DecodeAdvice`在解码前后进行埋点来插入业务逻辑，来对要解码的 `ResponseContent` 或者 解码后的对象 进行修改替换等操作。
### 示例
```java
public class DecodeAdviceImpl implements DecodeAdvice {
    @Override
    public Object aroundDecode(DecodeAdviceContext ctx) throws Exception {
        //...before decode
        Object decoded = ctx.next();
        //...after decode
        return decoded;
    }
}
```
#### DecodeAdviceContext
`RestClient`将解码埋点过程所需要的数据均封装到`DecodeAdviceContext` 接口中，这样更符合**依赖倒置**原则 ，在未来要对解码埋点的上下文进行扩展时，也不会影响到用户的业务逻辑。
调用`DecodeAdviceContext.next()`，该方法的返回值为解码后的结果，调用该方法通常意味着使用下一个`DecodeAdvice`，最后一个`DecodeAdvice`将会开始执行解码流程。具体接口内容如下:
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

### 配置方式
#### Builder
在构造`RestClient`时传入自定义的`DecodeAdvice`实例，如：
```java
final RestClient client = RestClient.create()
                      .addDecodeAdvice(ctx ->{
                            //...before decode
                            Object decoded = ctx.next();
                            //...after decode
                            return decoded;
                      })
                      .build();
```
```tip
- 多个`DecodeAdvice`之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高。
```
#### SPI
`RestClient`支持通过SPI的方式加载`DecodeAdvice`接口的实现类，使用时只需要按照SPI的加载规则将自定义的`DecodeAdvice`放入指定的目录下即可。

### 执行时机
见[请求处理完整流程](../process_of_restclient/)中的`DecodeAdvice`。