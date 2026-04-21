package com.newscloud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "newscloud.feeds[0].name=None",
        "newscloud.feeds[0].url=http://127.0.0.1:1/missing"
})
class NewsCloudApplicationTests {

    @Test
    void contextLoads() {
    }
}
