---
sort: 1
---

# Encoder
`RestClient`会自动根据用户的 `Headers` 与 `Entity` 等选择合适的`Encoder`进行`Encode`。其内置了下面这些`Encoder`：
- Json
    - jackson(默认)
    - fastjson
    - gson
- ProtoBuf
- File
- String
- byte[]

除此之外`RestClient`也支持用户自定义`Encoder`。
```note
其中Json相关的序列化方式默认配置了日期格式为`yyyy-MM-dd HH:mm:ss`
```
## 使用Json Encoder
指定`contentType`为`MediaTypeUtil.APPLICATION_JSON`，将自动使用`Json Encoder`来对`Entity`来进行`Encode`。示例如下：
```java
final RestClient client = RestClient.ofDefault();
RestResponseBase response  = client.post("localhost:8080/aaa")
        .contentType(MediaTypeUtil.APPLICATION_JSON)
        .entity(new Person("Bob","male"))
        .execute()
        .toCompletableFuture()
        .get();
```

## 使用ProtoBuf Encoder
指定`contentType`为`ProtoBufCodec.PROTO_BUF`，且`Entity`类型为`com.google.protobuf.Message`的子类时，将自动使用`ProtoBuf Encoder`来对`Entity`来进行`Encode`。示例如下：
```java
final RestClient client = RestClient.ofDefault();
RestResponseBase response  = client.post("localhost:8080/aaa")
        .contentType(ProtoBufCodec.PROTO_BUF)
        .entity(message)
        .execute()
        .toCompletableFuture()
        .get();
```
## 使用File Encoder
当`Entity`类型为`File`时，将自动使用`File Encoder`来对`Entity`来进行`Encode`。示例如下：
```java
final RestClient client = RestClient.ofDefault();
RestResponseBase response  = client.post("localhost:8080/aaa")
        .entity(new File("tem"))
        .execute()
        .toCompletableFuture()
        .get();
```
## 使用String Encoder
当`Entity`类型为`String`时，将自动使用`String Encoder`来对`Entity`来进行`Encode`。示例如下：
```java
final RestClient client = RestClient.ofDefault();
RestResponseBase response  = client.post("localhost:8080/aaa")
        .entity("string")
        .execute()
        .toCompletableFuture()
        .get();
```
## 使用byte[] Encoder
当`Entity`类型为`byte[]`时，将自动使用`byte[] Encoder`来对`Entity`来进行`Encode`。示例如下：
```java
final RestClient client = RestClient.ofDefault();
RestResponseBase response = client.post("localhost:8080/aaa")
        .entity("bytes".getBytes())
        .execute()
        .toCompletableFuture()
        .get();
```

## 自定义Encoder
当`RestClient`内置的`Encoder`无法满足用户需求时，用户可以自定义`Encoder`，示例如下：
```java
public class StringEncoder implements ByteEncoder {

    @Override
    public RequestContent<byte[]> doEncode(EncodeContext<byte[]> ctx) {
        if (ctx.contentType() == MediaTypeUtil.TEXT_PLAIN) {
            if (ctx.entity() != null) {
                return RequestContent.of(ctx.entity().toString());
            } else {
                return RequestContent.of("null");
            }
        }
        //该Encoder无法Encode这种类型，将Encode工作交给下一个Encoder
        return ctx.next();
    }
}
```
```tip
在`RestClient`中，同时存在多个`Encoder`，它们之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高。
针对当前`Encoder`，分为两种情况：
- 当前`Encoder`可以`Encode`该请求:
    直接返回`Encode`后的结果。
- 当前`Encoder`不可以`Encode`该请求：
    调用`ctx.next()`，将`Encode`工作交给下一个`Encoder`，如果`RestClient`中的所有`Encoder`都无法编码该请求，则`RestClient`将抛出`CodecException`异常。
```

### 配置Encoder
#### Builder
在构造`RestClient`时传入自定义的`Encoder`实例，如：
```java
final RestClient client = RestClient.create()
        .addEncoder(ctx -> {
            //encode...
            return ctx.next();
        })
        .build();
```

#### SPI
##### 普通SPI
`RestClient`支持通过SPI的方式加载`Encoder`接口的实现类，使用时只需要按照SPI的加载规则将自定义的`Encoder`放入指定的目录下即可。
##### EncoderFactory
如果用户自定义的`Encoder`对于不同`RestClient`的配置有不同的实现，则用户可以实现`EncoderFactory`接口，并按照SPI的加载规则将自定义的`EncoderFactory`放入指定的目录下即可。
```java
public interface EncoderFactory {
    Collection<Encoder> encoders(RestClientOptions clientOptions);
}
```
在`RestClient`构建时将调用`EncoderFactory.encoders(RestClientOptions clientOptions)`，该方法返回的所有`Encoder`都将加入到构建好的`RestClient`中。
#### 直接绑定Request
`Encoder`可以直接绑定`Request`，使用方式如下:
```java
final RestResponseBase response = client.post(url)
        .entity(new File("aaa"))
        .encoder(ctx -> {
            //encode...
            return ctx.next();
        })
        .execute()
        .toCompletableFuture()
        .get();
```
```tip
- 当`Request`绑定了`Encoder`，该Client中设置的所有`Encoder`将对该请求失效。即：如果当前`Encoder`无法`Encode`该请求的Entity，则`RestClient`将会抛出CodecException异常。
```

### 执行时机
见[请求处理完整流程](../process_of_restclient/)中的`Encoder`。

## EncodeAdvice
用户可以通过```EncodeAdvice```在`Encode`前后插入业务逻辑，来对要`Encode`的 `Entity` 或者 `Encode`后的`RequestContent` 进行修改替换等操作。
### 示例
```java
public class EncodeAdviceImpl implements EncodeAdvice {
    @Override
    public RequestContent<?> aroundEncode(EncodeAdviceContext ctx) throws Exception {
        //...before encode
        RequestContent<?> requestContent = ctx.next();
        //...after encode
        return requestContent;
    }
}
```

### 配置方式
#### Builder
在构造`RestClient`时传入自定义的`EncodeAdvice`实例，如：
```java
final RestClient client = RestClient.create()
        .addEncodeAdvice(ctx -> {
            //...before encode
            RequestContent<?> requestContent = ctx.next();
            //...after encode
            return requestContent;
        })
        .build();
```
```tip
- 多个EncodeAdvice之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高。
```
#### SPI
##### 普通SPI
`RestClient`支持通过SPI的方式加载`EncodeAdvice`接口的实现类，使用时只需要按照SPI的加载规则将自定义的`EncodeAdvice`放入指定的目录下即可。
##### EncodeAdviceFactory
如果用户自定义的`EncodeAdvice`对于不同`RestClient`的配置有不同的实现，则用户可以实现`EncodeAdviceFactory`接口，并按照SPI的加载规则将自定义的`EncodeAdviceFactory`放入指定的目录下即可。
```java
public interface EncodeAdviceFactory {
    Collection<EncodeAdvice> encodeAdvices(RestClientOptions clientOptions);
}
```
在`RestClient`构建时将调用`EncodeAdviceFactory.encodeAdvices(RestClientOptions clientOptions)`，该方法返回的所有`EncodeAdvice`都将加入到构建好的`RestClient`中。

### 执行时机
见[请求处理完整流程](../process_of_restclient/)中的`EncodeAdvice`。