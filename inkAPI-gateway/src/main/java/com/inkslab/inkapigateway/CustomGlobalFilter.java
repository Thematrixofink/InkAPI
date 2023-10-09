package com.inkslab.inkapigateway;

import com.inkslab.inkapiclientsdk.util.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
        if(System.currentTimeMillis() / 1000 - Long.parseLong(timestamp) >= FIVE_MINUTES){
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

        //5.相应日志

        //6.调用次数+1，有效次数-1

        //7.调用失败的话返回一个错误吗
        if(response.getStatusCode() == HttpStatus.OK){

        }else{
            return handleErrorInvoke(response);
        }




        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response){
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }
    public Mono<Void> handleErrorInvoke(ServerHttpResponse response) {

        return response.setComplete();
    }
}
