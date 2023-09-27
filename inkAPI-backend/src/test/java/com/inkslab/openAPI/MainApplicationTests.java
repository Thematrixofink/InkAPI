package com.inkslab.openAPI;

import com.inkslab.openAPI.config.WxOpenConfig;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 主类测试
 *

 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private WxOpenConfig wxOpenConfig;

    @Test
    void contextLoads() {
        System.out.println(wxOpenConfig);
    }

}
