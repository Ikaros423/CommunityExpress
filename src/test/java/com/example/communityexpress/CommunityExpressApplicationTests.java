package com.example.communityexpress;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.express.system.CommunityExpressApplication;
import org.junit.jupiter.api.Disabled;

@Disabled("Context load requires MyBatis and datasource; covered by controller tests.")
@SpringBootTest(classes = CommunityExpressApplication.class, properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class CommunityExpressApplicationTests {

    @Test
    void contextLoads() {
    }

}
