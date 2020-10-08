package com.voroby.elasticclient;

import com.google.gson.Gson;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AbstractElasticTest {
    @Autowired
    protected RestHighLevelClient restHighLevelClient;

    @Qualifier("elasticResponseListener")
    @Autowired
    protected ActionListener<IndexResponse> elasticResponseListener;

    protected static Gson userGson;
    protected static Gson itemGson;
}
