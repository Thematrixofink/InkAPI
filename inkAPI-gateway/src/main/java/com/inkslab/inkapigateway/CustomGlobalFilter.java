package com.inkslab.inkapigateway;

import com.inkslab.inkapiclientsdk.util.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    //请求地址白名单
    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.请求日志
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        log.info("请求唯一标识:"+request.getId());
        log.info("请求路径为:"+request.getPath());
        log.info("请求方法:"+request.getMethod());
        log.info("请求参数为:"+request.getQueryParams());
        log.info("请求来源地址:"+request.getRemoteAddress());
        //2.黑白名单
        if (!IP_WHITE_LIST.contains(request.getRemoteAddress().getHostString())){
            log.info("来者无权限");
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }


        //3.用户鉴权
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");
        //todo 去数据库里查询accesskey，是否存在
        if(!accessKey.equals("accessKey")){
            return handleNoAuth(response);
        }
        if(Long.parseLong(nonce) > 10000L){
            return handleNoAuth(response);
        }
        //如果超时了
        Long FIVE_MINUTES = 60 * 5L;
        long nowTime = System.currentTimeMillis() / 1000;
        if( nowTime - Long.parseLong(timestamp) >= FIVE_MINUTES){
            return handleNoAuth(response);
        }
        //服务端的secretKey是分发给用户的，是知道的
        //todo secretKey也是从服务器里查的
        String serverSign = SignUtil.getSign(body, "secretKey");
        if(!sign.equals(serverSign)){
            return handleNoAuth(response);
        }
        //3.判断调用的接口是否存在
        //todo 从数据库查询接口是否存在，请求方法是否匹配(参数什么的不要放到网关)
        //4.请求转发

        return handleResponse(exchange,chain);
//        //5.相应日志
//
//        //6.调用次数+1，有效次数-1
//
//        //7.调用失败的话返回一个错误码
//        if(response.getStatusCode() == HttpStatus.OK){
//
//        }else{
//            return handleErrorInvoke(response);
//        }
//
//
//
//
//        return chain.filter(exchange);
    }

    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // TODO: 2023/10/10   7. 调用成功，接口调用次数 + 1 invokeCount
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);//释放掉内存
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        List<Object> rspArgs = new ArrayList<>();
                                        rspArgs.add(originalResponse.getStatusCode());
                                        String data = new String(content, StandardCharsets.UTF_8); //data
                                        sb2.append(data);
                                        // 打印日志
                                        log.info("响应结果：" + data);
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange); // 降级处理返回数据
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }


    @Override
    public int getOrder() {
        return -2;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response){
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }
    public Mono<Void> handleErrorInvoke(ServerHttpResponse response) {

        return response.setComplete();
    }
}
