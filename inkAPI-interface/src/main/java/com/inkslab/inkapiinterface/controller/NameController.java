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
        String result = "POST BODY 你的名字是:"+user.getName();
        return result;
    }
}
