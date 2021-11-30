---
sort: 1
---

# 编码器
`RestClient`内置了下面这些编码器：
- Json
    - jackson(默认)
    - fastjson
    - gson
- ProtoBuf
- String

除此之外`RestClient`也支持用户自定义编码器。
```note
其中Json相关的序列化方式默认配置了日期格式为`yyyy-MM-dd HH:mm:ss`
```

## 自定义编解码器
### `Encoder`
`Encoder`将用户设置的请求`Entity`编码成```RequestContent```。```RequestContent```负责将`RestClient`编码后的对象传递给`ESA-HttpClient`(底层使用`Netty`)，其当前可以接受```byte[]```、```MultipartBody```、```File```类型。
- 编码成```byte[]```：底层的`HttpClient`将```byte[]```直接当做请求体发送。
- 编码成```MultipartBody```：底层的`HttpClient`将`MultipartBody`编码成Multipart的形式发送。
- 编码成```File```：底层的`HttpClient`将使用`NIO`的`FileChannel`来进行0拷贝传输，传输更加快速的同时避免文件过大造成的OOM。

之所以这样设计，是由于`RestClient`的底层`HttpClient`使用的是`Netty`框架，而`Netty`本身可以接受请求体多种数据类型，同时对不同的类型做了不同的优化。因此这样设计的好处就是未来`RestClient`未来可以更好地利用`Netty`的很多特性。如：
- `File`使用`NIO`的`FileChannel`来进行0拷贝传输，传输更加快速的同时避免文件过大造成的OOM。(将对象编码成`File`将会利用该特性)
- 使用`ChunkedInput`来分块发送请求体过大的请求(未来`RestClient`层面将利用`Netty`的该特性支持分块写请求体)。
- ...

由于大多数用户最常用的是将请求体编码成```byte[]```，因此下面仅以编码成```byte[]```作为示例。
#### 编码成```byte[]```
编码成```byte[]```是用户最常用的一种编码方式，因此`RestClient`抽象出继承自```Encoder```的```ByteEncoder```。下面为```ByteEncoder```的示例：
```java
public class StringEncoder implements ByteEncoder {

    @Override
    public RequestContent<byte[]> doEncode(EncodeContext<byte[]> ctx)  {
        if (String.class.isAssignableFrom(ctx.entityType())) {
            MediaType contentType = ctx.contentType();
            Charset charset = null;
            if (contentType != null) {
                charset = contentType.charset();
            }
            if (charset == null) {
                return RequestContent.of(((String) ctx.entity()).getBytes(StandardCharsets.UTF_8));
            } else {
                return RequestContent.of(((String) ctx.entity()).getBytes(charset));
            }
        }
        //该编码器无法编码这种类型，将编码工作交给下一个编码器
        return ctx.next();
    }
}
```


#### EncodeContext
`RestClient`将编码过程所需要的数据均封装到```EncodeContext``` 接口中，这样更符合**依赖倒置**原则 ，在未来要对编码的上下文进行扩展时，也不会影响到用户的业务逻辑。
调用`EncodeContext.next()`，意味着当前编码器无法编码该请求的`Entity`，使用下一个编码器继续进行编码。具体接口内容如下:
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

### 配置编码器
#### Builder
在构造`RestClient`时传入自定义的`Encoder`实例，如：
```java
final RestClient client = RestClient.create()
                .addEncoder(ctx -> {
                    //编码...
                    //如果该编码器无法编码该类型，则调用下一个编码器
                    return ctx.next();
                })
                .build();
```
#### SPI
`RestClient`支持通过Spi的方式加载`Encoder`接口的实现类，使用时只需要按照SPI的加载规则将自定义的`Encoder`放入指定的目录下即可。

```tip
- 多个Encoder之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高。
```
#### 直接绑定Request
`Encoder`可以直接绑定`Request`，使用方式如下:
```java
final RestResponseBase response = client.post(url)
                        .entity(new File("aaa"))
                        .encoder(ctx -> {
                            //编码...
                            //如果该编码器无法编码该类型，则调用下一个编码器
                            return ctx.next();
                        })
                        .execute()
                        .toCompletableFuture()
                        .get();
```
```tip
- 当`Request`绑定了`Encoder`，该Client中设置的所有`Encoder`将对该请求失效。即：如果当前`Encoder`无法编码该请求的Entity，则`RestClient`将会抛出CodecExceptiony异常。
```

### 执行时机
见[请求处理完整流程](../process_of_restclient/)中的`Encoder`。