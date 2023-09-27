package com.inkslab.inkapiinterface;

import com.inkslab.inkapiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.inkslab.inkapiclientsdk.client.inkClient;
import javax.annotation.Resource;

@SpringBootTest
class InkApiInterfaceApplicationTests {

    @Resource
    private inkClient inkClient;
    @Test
    void contextLoads() {
        User user = new User();
        user.setName("111");
        String nameByPostBody = inkClient.getNameByPostBody(user);
        System.out.println(nameByPostBody);
    }

}
