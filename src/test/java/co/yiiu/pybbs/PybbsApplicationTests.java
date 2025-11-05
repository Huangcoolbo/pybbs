package co.yiiu.pybbs;

import co.yiiu.pybbs.plugin.ElasticSearchService;
import co.yiiu.pybbs.service.impl.IndexedService;
import co.yiiu.pybbs.util.MyPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PybbsApplicationTests {

    @Resource
    ElasticSearchService elasticSearchService;
    @Resource
    IndexedService indexedService;

    @Test
    public void contextLoads() {
        // MyPage<Map<String, Object>> mapMyPage = elasticSearchService.searchDocument(0,1, 20, "问题", "title");
        indexedService.indexAllMessage(1);
        MyPage<Map<String, Object>> mapMyPage = elasticSearchService.searchDocument("message", 1, 1, 20, "让", "message");
        for (Map<String, Object> record : mapMyPage.getRecords()) {
            System.out.println(record.toString());
            System.out.println("-------------------");
        }
    }

}
