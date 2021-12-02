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

## 自定义Decoder
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

### 配置Decoder
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
            //如果该Decoder无法解码该类型，则调用下一个解码器
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