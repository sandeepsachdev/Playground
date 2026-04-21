package com.newscloud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "newscloud.feeds[0].name=None",
        "newscloud.feeds[0].url=http://127.0.0.1:1/missing",
        "spring.datasource.url=jdbc:h2:mem:newscloud-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class NewsCloudApplicationTests {

    @Test
    void contextLoads() {
    }
}
