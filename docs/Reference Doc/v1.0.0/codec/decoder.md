---
sort: 2
---

# Decoder
`RestClient`会自动根据用户的 `Headers` 与 期望`Entity`类型 等选择合适的`Decoder`进行解码。`RestClient`内置了下面这些`Decoder`：
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
## 使用Json Decoder
当Response的`contentType`为`MediaTypeUtil.APPLICATION_JSON`，将自动使用`Json Decoder`来来进行`Decode`。
```java
final RestClient client = RestClient.ofDefault();
RestResponseBase response  = client.get("localhost:8080/aaa")
        .execute()
        .toCompletableFuture()
        .get();

//当 response.contentType() == MediaTypeUtil.APPLICATION_JSON 时将自动使用Json Decoder
Person person = response.bodyToEntity(Person.class);

```

## 使用ProtoBuf Decoder
当Response的`contentType`为`ProtoBufCodec.PROTO_BUF`，且`response.bodyToEntity()`传入的类型为`com.google.protobuf.Message`的子类时，将自动使用`ProtoBuf Decoder`来进行`Decode`。
```java
final RestClient client = RestClient.ofDefault();
RestResponseBase response = client.get("localhost:8080/aaa")
        .execute()
        .toCompletableFuture()
        .get();

//当 response.contentType() == ProtoBufCodec.PROTO_BUF，且 Person 为 Message 的子类时，将自动使用ProtoBuf Decoder
Person person = response.bodyToEntity(Person.class);

```
## 使用String Encoder
当`response.bodyToEntity()`传入的类型为`String.class`，且Response的`contentType`不为`MediaTypeUtil.APPLICATION_JSON`时，将自动使用`String Decoder`来进行`Decode`。
```java
final RestClient client = RestClient.ofDefault();
RestResponseBase response = client.get("localhost:8080/aaa")
        .execute()
        .toCompletableFuture()
        .get();

//当 response.contentType() != MediaTypeUtil.APPLICATION_JSON 时，自动使用String Decoder来进行Decode
String result = response.bodyToEntity(String.class);

```
## 使用byte[] Encoder
当`response.bodyToEntity()`传入的类型为`byte[].class`，且Response的`contentType`不为`MediaTypeUtil.APPLICATION_JSON`时，将自动使用`byte[] Decoder`来进行`Decode`。
```java
final RestClient client = RestClient.ofDefault();
RestResponseBase response = client.get("localhost:8080/aaa")
        .execute()
        .toCompletableFuture()
        .get();

//当 response.contentType() != MediaTypeUtil.APPLICATION_JSON 时，自动使用byte[] Decoder来进行Decode
byte[] result = response.bodyToEntity(byte[].class);
```

## 自定义Decoder
当`RestClient`内置的`Decoder`无法满足用户需求时，用户可以自定义`Decoder`，示例如下：
```java
public class StringDecoder implements ByteDecoder {

    @Override
    public Object doDecode(DecodeContext<byte[]> ctx) {
        if (String.class.isAssignableFrom(ctx.targetType())) {
            return new String(ctx.content().value());
        }
        return ctx.next();
    }
}
```
```tip
在`RestClient`中，同时存在多个`Decoder`，它们之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高。
针对当前`Decoder`，分为两种情况：
- 当前`Decoder`可以`Decode`该响应:
    直接返回`Decode`后的结果。
- 当前`Decoder`不可以`Decode`该响应：
    调用`ctx.next()`，将`Decode`工作交给下一个`Decoder`，如果`RestClient`中的所有`Decoder`都无法编码该请求，则`RestClient`将抛出`CodecException`异常。
```
### 配置Decoder
#### Builder
在构造`RestClient`时传入自定义的`Decoder`实例，如：
```java
final RestClient client = RestClient.create()
        .addDecoder(ctx -> {
            //decode...
            return ctx.next();
        })
        .build();
```
#### SPI
##### 普通SPI
`RestClient`支持通过SPI的方式加载`Decoder`接口的实现类，使用时只需要按照SPI的加载规则将自定义的`Decoder`放入指定的目录下即可。
##### DecoderFactory
如果用户自定义的`Decoder`对于不同`RestClient`的配置有不同的实现，则用户可以实现`DecoderFactory`接口，并按照SPI的加载规则将自定义的`DecoderFactory`放入指定的目录下即可。
```java
public interface DecoderFactory {
    Collection<Decoder> decoders(RestClientOptions clientOptions);
}
```
在`RestClient`构建时将调用`DecoderFactory.decoders(RestClientOptions clientOptions)`，该方法返回的所有`Decoder`都将加入到构建好的`RestClient`中。
#### 直接绑定Request
`Decoder`可以直接绑定`Request`，使用方式如下:
```java
final RestResponseBase response = client.post(url)
        .entity(new File("aaa"))
        .decoder(ctx -> {
            //decode...
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

## DecodeAdvice
用户可以通过`DecodeAdvice`在`Decode`前后进行来插入业务逻辑，来对要解码的 `ResponseContent` 或者 `Decode`后的对象 进行修改替换等操作。
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
```tip
- 多个`DecodeAdvice`之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高。
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

#### SPI
##### 普通SPI
`RestClient`支持通过SPI的方式加载`DecodeAdvice`接口的实现类，使用时只需要按照SPI的加载规则将自定义的`DecodeAdvice`放入指定的目录下即可。
##### DecodeAdviceFactory
如果用户自定义的`DecodeAdvice`对于不同`RestClient`的配置有不同的实现，则用户可以实现`DecodeAdviceFactory`接口，并按照SPI的加载规则将自定义的`DecodeAdviceFactory`放入指定的目录下即可。
```java
public interface DecodeAdviceFactory {
    Collection<DecodeAdvice> decodeAdvices(RestClientOptions clientOptions);
}
```
在`RestClient`构建时将调用`DecodeAdviceFactory.decodeAdvices(RestClientOptions clientOptions)`，该方法返回的所有`DecodeAdvice`都将加入到构建好的`RestClient`中。

### 执行时机
见[请求处理完整流程](../process_of_restclient/)中的`DecodeAdvice`。