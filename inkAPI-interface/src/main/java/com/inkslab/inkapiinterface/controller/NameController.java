package com.inkslab.inkapiinterface.controller;



import com.inkslab.inkapiclientsdk.model.User;
import com.inkslab.inkapiclientsdk.util.SignUtil;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/get")
    public String getName(String name){
        return "GET 你的名字是:"+name;
    }
    @PostMapping("/post")
    public String getNameByPost(@RequestParam("name")String name){
        return "POST 你的名字是:"+name;
    }

    @PostMapping("/post/body")
    public String getNameByBody(@RequestBody User user, HttpServletRequest request){
        String accessKey = request.getHeader("accessKey");
        String nonce = request.getHeader("nonce");
        String timestamp = request.getHeader("timestamp");
        String sign = request.getHeader("sign");
        String body = request.getHeader("body");

        //todo 去数据库里查询accesskey，是否存在
        if(!accessKey.equals("accessKey")){
            throw  new RuntimeException("无权限");
        }
        //服务端的secretKey是分发给用户的，是知道的
        //todo secretKey也是从服务器里查的
        String serverSign = SignUtil.getSign(body, "secretKey");
        if(!sign.equals(serverSign)){
            throw new RuntimeException("无权限");
        }
        return "POST BODY 你的名字是:"+user.getName();
    }
}
