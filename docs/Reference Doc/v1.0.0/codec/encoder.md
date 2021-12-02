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
- String
- byte[]

除此之外`RestClient`也支持用户自定义`Encoder`。
```note
其中Json相关的序列化方式默认配置了日期格式为`yyyy-MM-dd HH:mm:ss`
```

## 自定义Encoder
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

### 配置Encoder
#### Builder
在构造`RestClient`时传入自定义的`Encoder`实例，如：
```java
final RestClient client = RestClient.create()
        .addEncoder(ctx -> {
            //`Encode`...
            //如果该`Encoder`无法`Encode`该类型，则调用下一个`Encoder`
            return ctx.next();
        })
        .build();
```

```tip
- 多个Encoder之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高。
```

#### SPI
`RestClient`支持通过SPI的方式加载`Encoder`接口的实现类，使用时只需要按照SPI的加载规则将自定义的`Encoder`放入指定的目录下即可。

#### 直接绑定Request
`Encoder`可以直接绑定`Request`，使用方式如下:
```java
final RestResponseBase response = client.post(url)
        .entity(new File("aaa"))
        .encoder(ctx -> {
            //`Encode`...
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
#### SPI
`RestClient`支持通过SPI的方式加载`EncodeAdvice`接口的实现类，使用时只需要按照SPI的加载规则将自定义的`EncodeAdvice`放入指定的目录下即可。

```tip
- 多个EncodeAdvice之间通过`getOrder()`方法返回值区分执行顺序，值越小，优先级越高。
```

### 执行时机
见[请求处理完整流程](../process_of_restclient/)中的`EncodeAdvice`。