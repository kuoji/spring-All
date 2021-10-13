package com.kuoji.elasticsearchlearn.service.impl;

import com.kuoji.elasticsearchlearn.service.EsService;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MultiTermVectorsRequest;
import org.elasticsearch.client.core.MultiTermVectorsResponse;
import org.elasticsearch.client.core.TermVectorsRequest;
import org.elasticsearch.client.core.TermVectorsResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.*;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsServiceImpl implements EsService {

    private static final Logger log = LoggerFactory.getLogger(EsServiceImpl.class);

    @Autowired
    private RestHighLevelClient restClient;

    // 基于String构建IndexRequest
    public void buildIndexRequestWithString(String indexName, String document){
        // 索引名称
        IndexRequest request = new IndexRequest(indexName);
        // 文档id
        request.id(document);
        // String 类型的文档
        String jsonString = "{" + "\"user\":\"kuoji\"," + "\"postDate\":\"2021-08-09\","
                                + "\"message\":\"Hello es\"" + "}";
        request.source(jsonString, XContentType.JSON);
    }

    // 基于Map构建IndexRequest
    public void buildIndexRequestWithMap(String indexName, String document){
        Map<String,Object> jsonMap = new HashMap<>();
        jsonMap.put("user", "kuoji");
        jsonMap.put("postDate", new Date());
        jsonMap.put("message", "Hello es!");

        // 以Map形式提供文档源， es会自动将Map形式转换为JSON格式
        IndexRequest indexRequest = new IndexRequest(indexName).id(document).source(jsonMap);
    }

    // 基于XContentBuilder构建IndexRequest
    public void buildIndexRequestWithXContentBuilder(String indexName, String document){
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            {
                builder.field("user","kuoji");
                builder.timeField("postDate", new Date());
                builder.field("message", "Hello es!");
            }
            builder.endObject();
            //以XContentBuilder对象提供文档源，Elasticsearch内置的帮助器自动将其生成
            IndexRequest indexRequest = new IndexRequest(indexName).id(document).source(builder);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 基于键值对构建IndexRequest
    public void buildIndexRequestWithKV(String indexName, String document){
        //以键值对提供文档源，Elasticsearch自动将其转换为JSON格式
        IndexRequest indexRequest = new IndexRequest(indexName).id(document)
                                    .source("user","kuoji","postDate",new Date(),"message","Hello es!");
    }

    // 配置IndexRequest的其他参数
    public void buildIndexRequestWithParam(String indexName, String document){
        //以键值对提供文档源其转换为JSON格式
        IndexRequest indexRequest = new IndexRequest(indexName).id(document)
                .source("user","kuoji", "postDate",new Date(), "message","Hello es!");
       // 设置路由值
        indexRequest.routing("routing");

        //设置超时时间
        indexRequest.timeout(TimeValue.timeValueSeconds(1));
        indexRequest.timeout("1s");

        //设置超时策略
        indexRequest.setRefreshPolicy(WriteRequest. RefreshPolicy.WAIT_UNTIL) ;
        indexRequest.setRefreshPolicy("wait_for") ;

        //设置版本
        indexRequest.version(2);

        //设置版本类型
        indexRequest.versionType(VersionType.EXTERNAL);

        //设置操作类型
        indexRequest.opType(DocWriteRequest.OpType.CREATE);
        indexRequest.opType("create");

        //在索引文档之前要执行的接收管道的名称
        indexRequest.setPipeline("pipeline");
    }


    // 索引文档
    public void indexDocuments(String indexName, String document){
        //以键值对提供文档源其转换为JSON格式
        IndexRequest indexRequest = new IndexRequest(indexName).id(document)
                                         .source("user","kuoji", "postDate",new Date(),
                                                 "message","Hello es! 北京时间8月1日凌晨2点，美联储公布7月议息会议结果" +
                                                         "如市场预期，美联储本次降息25个基点，将联邦基金利率的目标范围调至2.00%~2.25%");
        try{
            IndexResponse indexResponse = restClient.index(indexRequest, RequestOptions.DEFAULT);
            // 解析索引结果
            processIndexResponse(indexResponse);
        }catch (Exception e){
            e.printStackTrace();
        }

        // 关闭es 连接
        closeEs();
    }

    // 解析索引结果
    private void processIndexResponse(IndexResponse indexResponse) {
        String index = indexResponse.getIndex();
        String id = indexResponse.getId();
        log.info("index is " + index + ", id is " + id);
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED){
            // 文件创建时
            log.info("Document is created!");
        }else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED){
            // 文件更新时
            log.info("Document has updated!");
        }

        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()){
            // 处理成功，shards 小于总 shards的情况
            log.info("Successed shard are not enough!");
        }

        if (shardInfo.getFailed() > 0){
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()){
                String reason = failure.reason();
                log.info("Fail reason is " + reason);
            }
        }
    }

    // 以异步方式索引文档
    public void indexDocumentsAsync(String indexName, String document){
        //以键值对提供文档源其转换为JSON格式
        IndexRequest indexRequest = new IndexRequest(indexName).id(document)
                .source("user","kuoji", "postDate",new Date(), "message","Hello es!");
        ActionListener<IndexResponse> listener = new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
            }
            @Override
            public void onFailure(Exception e) {
            }
        };

        try {
            restClient.indexAsync(indexRequest,RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    // 构建GetRequest
    public void buildGetRequest(String indexName, String document){
        GetRequest getRequest = new GetRequest(indexName, document);

        // 可选配置参数
        // 禁用源检查，在默认情况下启动
        getRequest.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);

        // 为特定字段配置源包含
        String[] includes = new String[]{"message","*Date"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
        getRequest.fetchSourceContext(fetchSourceContext);

        // 为特定字段配置源排除
        includes = Strings.EMPTY_ARRAY;
        excludes = new String[]{"message"};
        fetchSourceContext = new FetchSourceContext(true, includes, excludes);
        getRequest.fetchSourceContext(fetchSourceContext);

        // 为特定存储字段配置检索
        getRequest.storedFields("message");

        // 要求在映射中单独存储字段
        try {
            GetResponse getResponse = restClient.get(getRequest,RequestOptions.DEFAULT);
            String message = getResponse.getField("message").getValue();
            // 检索消息存储字段(要求该字段单独存储在映射中)
            log.info("message is " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 路由值
        getRequest.routing("routing");

        // 偏好值
        getRequest.preference("preference");

        // 将实时标志设置为假(默认为 true)
        getRequest.realtime(false);

        // 在检索文档之前执行刷新(默认为 false)
        getRequest.refresh(true);

        // 配置版本号
        getRequest.version(2);

        // 配置版本类型
        getRequest.versionType(VersionType.EXTERNAL);
    }

    // 同步方式执行GetRequest
    public void getIndexDocuments(String indexName, String document){
        GetRequest getRequest = new GetRequest(indexName, document);
        try {
            GetResponse getResponse = restClient.get(getRequest, RequestOptions.DEFAULT);
            // 处理 GetResponse
            processGetResponse(getResponse);
        }catch (Exception e){
            e.printStackTrace();
        }

        // 关闭es
        closeEs();
    }

    // 处理 GetResponse
    private void processGetResponse(GetResponse getResponse) {
        String index = getResponse.getIndex();
        String id = getResponse.getId();
        log.info("id is " + id + ", index is " + index);
        if (getResponse.isExists()){
            long version = getResponse.getVersion();
            // String形式检索文档
            String sourceAsString = getResponse.getSourceAsString();
            // Map<String,Object>形式检索文档
            Map<String,Object> sourceAsMap = getResponse.getSourceAsMap();
            // byte[]形式检索文档
            byte[] sourceAsBytes = getResponse.getSourceAsBytes();
            log.info("version is " + version + ", sourceAsString is " + sourceAsString);
        }else {
            //当找不到文档时在此处处理。注意，尽管返回的响应具有404状态码，但返回的是有效的
            // getResponse, 而不是引发异常。这样的响应不包含任何源文档，并且其isexists方法返
            //回false
        }
    }

    // 异步方式执行GetRequest
    public void getIndexDocumentsAsync(String indexName, String document){
        GetRequest getRequest = new GetRequest(indexName,document);
        ActionListener<GetResponse> listener = new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse getResponse) {
                String id = getResponse.getId();
                String index = getResponse.getIndex();
                log.info("id is " + id + ", index is "+ index);
            }
            @Override
            public void onFailure(Exception e) {
            }
        };
        try {
            restClient.getAsync(getRequest,RequestOptions.DEFAULT, listener);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 同步方式检验索引文档是否存在
    public void checkExistIndexDocuments(String indexName, String document){
        GetRequest getRequest = new GetRequest(indexName, document);
        // 禁用提取源
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        // 禁用提取存储字段
        getRequest.storedFields("_none_");
        try {
            boolean exists = restClient.exists(getRequest, RequestOptions.DEFAULT);
            log.info("索引" + indexName + "下的" + document + "文档的存在性是 " + exists);
        } catch (Exception e){
            e.printStackTrace();
        }

        // 关闭es
        closeEs();
    }

    // 异步方式检验索引文档是否存在
    public void checkExistIndexDocumentsAsync(String indexName, String document){
        GetRequest getRequest = new GetRequest(indexName, document);
        // 禁用提取源
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        // 禁用提取存储字段
        getRequest.storedFields("_none_");
        // 定义监听器
        ActionListener<Boolean> listener = new ActionListener<Boolean>() {
            @Override
            public void onResponse(Boolean exists) {
                log.info("索引" + indexName + "下的 " + document + "文档的存在性是 " + exists);
            }
            @Override
            public void onFailure(Exception e) {
            }
        };
        try {
            restClient.existsAsync(getRequest,RequestOptions.DEFAULT,listener);
        } catch (Exception e){
            e.printStackTrace();
        }

        // 关闭es
        closeEs();
    }

    // 构建 DeleteRequest
    public void buildDeleteRequestIndexDocuments(String indexName, String document){
        DeleteRequest deleteRequest = new DeleteRequest(indexName, document);
    }

    // 构建 DeleteRequest及可选参数
    public void buildDeleteRequestIndexDocumentsWithParam(String indexName, String document){
        DeleteRequest deleteRequest = new DeleteRequest(indexName, document);

        //设置路由
        deleteRequest.routing("routing");
        //设置超时
        deleteRequest.timeout(TimeValue.timeValueMinutes(2));
        deleteRequest.timeout("2m") ;
        //设置刷新策略
        deleteRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL) ;
        deleteRequest.setRefreshPolicy("wait_ for") ;
        //设置版本
        deleteRequest.version(2) ;
        //设置版本类型
        deleteRequest.versionType (VersionType.EXTERNAL) ;
    }

    // 同步方式删除文档索引请求
    public void deleteIndexDocuments(String indexName, String document){
        DeleteRequest deleteRequest = new DeleteRequest(indexName,document);
        try {
            DeleteResponse deleteResponse = restClient.delete(deleteRequest,RequestOptions.DEFAULT);
            // 处理DeleteResponse
            processDeleteRequest(deleteResponse);
        }catch (Exception e){
            e.printStackTrace();
        }

        // 关闭es连接
        closeEs();
    }

    // 解析删除结果
    private void processDeleteRequest(DeleteResponse deleteResponse) {
        String index = deleteResponse.getIndex();
        String id = deleteResponse.getId();
        long version = deleteResponse.getVersion();
        log.info("delete id is " + id + " , index is " + index + ", version is " + version);

        ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()){
            log.info("Success shards are not enought");
        }

        if (shardInfo.getFailed() > 0){
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()){
                String reason = failure.reason();
                log.info("Fail reason is " + reason);
            }
        }

    }

    // 异步方式删除文档索引请求
    public void deleteIndexDocumentsAsync(String indexName, String document){
        DeleteRequest deleteRequest = new DeleteRequest(indexName,document);
        ActionListener<DeleteResponse> listener = new ActionListener<DeleteResponse>() {
            @Override
            public void onResponse(DeleteResponse deleteResponse) {
                String id = deleteResponse.getId();
                String index = deleteResponse.getIndex();
                long version = deleteResponse.getVersion();
                log.info("delete id is " + id + ", index is " + index + " , version is " + version);
            }
            @Override
            public void onFailure(Exception e) {
            }
        };
        try {
            restClient.deleteAsync(deleteRequest,RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }

        // 关闭es连接
        closeEs();
    }

    // 构建UpdateRequest
    public void buildUpdateRequestIndex(String indexName, String document){
        UpdateRequest request = new UpdateRequest(indexName,document);

        /**
         * 用其他方法构建UpdateRequest
         */
        // 2. 以JSON格式构建文档
        request = new UpdateRequest(indexName,document);
        String jsonString =
                "{" + "\"updated\": \"2021-08-10\"," +
                        "\"reason\": \"Year update!\"" + "}";
        request.doc(jsonString, XContentType.JSON) ;

        //方式3:以Map形式提供文档源，Elasticsearch自动将其转换为JSON格式
        Map<String, Object> jsonMap = new HashMap<>() ;
        jsonMap.put ("updated", new Date());
        jsonMap.put ("reason", "Year update!") ;
        request = new UpdateRequest (indexName, document).doc(jsonMap);

        //方式4:以XContentBuilder对象形式提供文档源，Elasticsearch内置的帮助器自动将
        //其生成为JSON格式的内容
        try {
            XContentBuilder builder = XContentFactory. jsonBuilder() ;
            builder.startObject() ;
            builder.timeField ("updated", new Date()) ;
            builder.field("reason", "Year update!") ;
            builder.endObject() ;
            request = new UpdateRequest (indexName, document) . doc (builder) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }

        //方式5:以键值对形式提供文档源，Elasticsearch自动将其转换为JSON格式
        request =
        new UpdateRequest (indexName, document).doc("updated", new Date(),
                "reason", "Year update! ") ;

        // 如果被更新的文档不存在，则可以使用upsert方法将某些内容定义为新文档
        jsonString = "{ \"created\": \"2021-08-10\"}";
        request.upsert(jsonString, XContentType.JSON) ;

    }

    // 构建UpdateRequest (需要配置一些可选参数)
    public void buildUpdateRequestIndexWithParam(String indexName, String document){
        UpdateRequest request = new UpdateRequest(indexName,document);

        //设置路由
        request.routing("routing");

        //设置超时
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s") ;

        //设置刷新策略
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL) ;
        request.setRefreshPolicy("wait_for") ;

        //设置:如果更新的文档在更新时被另一个操作更改，则重试更新操作的次数
        request.retryOnConflict(3);

        //启用源检索，在默认情况下禁用
        request.fetchSource(true);

        // 为特定 字段配置源包含关系
        String[] includes = new String[] {"updated", "r*"};
        String[] excludes = Strings.EMPTY_ARRAY;
        request.fetchSource(new FetchSourceContext (true, includes, excludes));

        //为特定字段配置源排除关系
        includes = Strings.EMPTY_ARRAY;
        excludes = new String[] {"updated"};
        request. fetchSource (new FetchSourceContext(true,includes, excludes));
    }

    // 同步方式更新文档索引请求
    public void updateIndexDocuments(String indexName, String document){
        UpdateRequest request = new UpdateRequest(indexName, document);
        Map<String,Object> jsonMap = new HashMap<>();
        jsonMap.put("updated", new Date());
        jsonMap.put("reason", "Year update!");
        jsonMap.put("content","中央红军准备长征时，中革军委仅有这一台手摇发电机和一台发报机，数量稀少的设备尤显珍贵，组织专门委派一个128人的加强连保护这些设备。长征路途遥远而艰辛，" +
                                    "但红军战士始终没有抛下那些珍贵的设备，用生命守护着手摇发电机和发报机抵达延安，保证了部队信息的传递。");
        request = new UpdateRequest(indexName, document).doc(jsonMap);
        try {
            UpdateResponse updateResponse = restClient.update(request, RequestOptions.DEFAULT);
            // 处理UpdateResponse
            processUpdateRequest(updateResponse);
        }catch (Exception e){
            e.printStackTrace();
        }

        // 关闭es
        closeEs();
    }

    // 处理UpdateResponse
    private void processUpdateRequest(UpdateResponse updateResponse) {
        String id = updateResponse.getId();
        String index = updateResponse.getIndex();
        long version = updateResponse.getVersion();
        log.info("update id is " + id + ", index is " + index + " , version is " + version);

        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            //创建文档成功
        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED){
            // 更新文档成功
            // 查看更新的数据
            log.info (updateResponse.getResult().toString());
        } else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED){
            // 删除文档成功
        } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP){
            // 无文档操作
        }
    }

    // 异步方式更新文档索引请求
    public void updateIndexDocumentsAsync(String indexName, String document){
        UpdateRequest request = new UpdateRequest(indexName, document);
        ActionListener<UpdateResponse> listener = new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        };
        try {
            restClient.updateAsync(request,RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }
        // 关闭es
    }

    //构建 TermVectorsRequest
    public void buildTermVectorsRequest (String indexName, String document,String field) {
        // 方式 1: 索引中存在的文档
        TermVectorsRequest request = new TermVectorsRequest (indexName,document);
        request.setFields(field);

        /**
         * 可选参数
         */
        //当把Fieldstatistics设置为false (默认为true)时，可忽略文档计数、文档频率总和
        //及总术语频率总和
        request.setFieldStatistics(false);

        //将TermStatistics设置为true (默认为false)，以显示术语总频率和文档频率
        request.setTermStatistics(true);

        //将“位置”设置为“假”(默认为“真”)，忽略位置的输出
        request.setPositions (false);

        //将“偏移”设置为“假”(默认为“真”)，忽略偏移的输出
        request.setOffsets(false);

        //将“有效载荷”设置为“假”(默认为“真”)，忽略有效载荷的输出
        request.setPayloads(false);
        Map<String,Integer> filterSettings = new HashMap<>();
        filterSettings.put("max_num_terms", 3);
        filterSettings.put("min_term_freq", 1);
        filterSettings.put("max_term_freq", 10);
        filterSettings.put("min_doc_freq", 1);
        filterSettings.put("max_doc_freq", 100);
        filterSettings.put("min_word_length", 1);
        filterSettings.put("max_word_length", 10);

        //设置FilterSettings, 根据TF-IDF分数筛选可返回的词条
        request.setFilterSettings (filterSettings) ;
        Map<String,String> perFieldAnalyzer = new HashMap<>() ;
        perFieldAnalyzer.put("user", "keyword");

        //设置PerFieldAnalyzer,指定与字段已有的分析器不同的分析器
        request.setPerFieldAnalyzer(perFieldAnalyzer);

        //将Realtime设置为false (默认为true)，以便在Realtime附近检索术语向量
        request.setRealtime(false);
        //设置路由
        request.setRouting("routing");


        // 方式 2: 索引中不存在的文档，可以人工为文档生成词向量
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject().field("user","kuoji").endObject();
            request = new TermVectorsRequest(indexName,builder);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 以同步方式执行获取文档词向量请求
    public void exucateTermVectorsRequest (String indexName, String document, String field){
        TermVectorsRequest request = new TermVectorsRequest(indexName, document);
        request.setFields(field);
        try {
            TermVectorsResponse response = restClient.termvectors(request,RequestOptions.DEFAULT);
            // 处理TermVectorsResponse
            processTermVectorsResponse(response);
        }catch (Exception e){
            e.printStackTrace();
        }

        // 关闭es
        closeEs();
    }

    // 处理VectorsResponse
    private void processTermVectorsResponse(TermVectorsResponse response) {
        String index = response .getIndex() ;
        String type = response.getType();
        String id = response.getId();

        //指示是否找到文档
        boolean found = response.getFound();
        log.info("index is" + index + ",id is " + id + " , type is "+ type + ", found is "+ found);
        List<TermVectorsResponse.TermVector> list = response.getTermVectorsList () ;
        log.info("list is" + list.size());
        for (TermVectorsResponse.TermVector tv : list) {
            processTermVector(tv);
        }
    }

    // 处理TermVector
    private void processTermVector(TermVectorsResponse.TermVector tv) {
        String fieldname = tv.getFieldName();
        int docCount = tv.getFieldStatistics().getDocCount();
        long sumTotalTermFreq = tv.getFieldStatistics().getSumTotalTermFreq();
        long sumDocFreq = tv.getFieldStatistics().getSumDocFreq();
        log.info("fieldname is " + fieldname + ", docCount is " + docCount + ", sumTotalTermFreq is"
                + sumTotalTermFreq + ", sumDocFreq is " + sumDocFreq);
        if (tv.getTerms() == null) {
            return;
        }
        List<TermVectorsResponse.TermVector.Term> terms = tv.getTerms();
        for (TermVectorsResponse.TermVector.Term term : terms) {
            String termStr = term.getTerm();
            int termFreq = term.getTermFreq();
            int docFreq = term.getDocFreq() == null ? 0 : term.getDocFreq();
            long totalTermFreq = term.getTotalTermFreq() == null ? 0 : term.getTotalTermFreq();
            float score = term.getScore() == null ? 0 : term.getScore();
            log.info("termStr is " + termStr + "; tremFreq is " + termFreq + " ; docFreq is" + docFreq
                        + " ; totalTermFreq is " + totalTermFreq + " ; score is " + score);
            if (term.getTokens() != null){
                List<TermVectorsResponse.TermVector.Token> tokens = term.getTokens();
                for (TermVectorsResponse.TermVector.Token token : tokens){
                    int position = token.getPosition() == null ? 0 : token.getPosition();
                    int startOffset = token.getStartOffset() == null ? 0 : token.getStartOffset();
                    int endOffset = token.getEndOffset() == null ? 0 : token.getEndOffset();
                    String payload = token.getPayload();
                    log.info("position is " + position + " , startOffset is " + startOffset
                                + ", endOffset is " + endOffset + " , payload is " + payload);
                }
            }
        }
    }

    // 以异步方式执行获取文档词向量请求
    public void exucateTermVectorsRequestAsync (String indexName, String document, String field){
        TermVectorsRequest request = new TermVectorsRequest(indexName, document);
        request.setFields(field);
        ActionListener<TermVectorsResponse> listener = new ActionListener<TermVectorsResponse>() {
            @Override
            public void onResponse(TermVectorsResponse termVectorsResponse) {
            }
            @Override
            public void onFailure(Exception e) {
            }
        };
        try {
            restClient.termvectorsAsync(request,RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }

        // 关闭es
        closeEs();
    }

    // 构建BulkRequest
    public void buildBulkRequest(String indexName, String field){
        /**
         * 方式 1: 添加同型请求
         */
        BulkRequest request = new BulkRequest();
        // 添加第一个 IndexRequest
        request.add(new IndexRequest(indexName).id("1")
                .source(XContentType.JSON,field,"111在使用批量请求前，需要构建批量请求，即BulkRequest"));
        // 添加第二个 IndexRequest
        request.add(new IndexRequest(indexName).id("2")
                .source(XContentType.JSON,field,"222在使用批量请求前，需要构建批量请求，即BulkRequest"));
        // 添加第三个 IndexRequest
        request.add(new IndexRequest(indexName).id("3")
                .source(XContentType.JSON,field,"333在使用批量请求前，需要构建批量请求，即BulkRequest"));

        /**
         * 方式 2: 添加异型请求
         */
        // 添加一个 DeleteRequest
        request.add(new DeleteRequest(indexName,"3"));
        // 添加一个 UpdateRequest
        request.add(new UpdateRequest(indexName,"2")
                    .doc(XContentType.JSON,field,"异型请求 UpdateRequest..."));
        // 添加一个IndexRequest
        request.add(new IndexRequest(indexName).id("4")
                    .source(XContentType.JSON,field,"异型请求 IndexRequest.."));

        /**
         * 以下是可选参数的配置
         */
        // 设置超时时间
        request.timeout(TimeValue.timeValueMinutes(2));
        request.timeout("2m");

        //设置数据刷新策略
        request.setRefreshPolicy (WriteRequest.RefreshPolicy.WAIT_UNTIL) ;
        request.setRefreshPolicy("wait_for");

        //设置在继续执行索引/更新/删除操作之前必须处于活动状态的分片副本数
        request.waitForActiveShards(2);
        request.waitForActiveShards(ActiveShardCount.ALL) ;

        //用于所有子请求的全局pipelineid,即全局管道标识
        request.pipeline("pipelineId");

        //用于所有子请求的全局路由ID
        request.routing("routingId");
    }

    // 以同步方式执行BulkRequest
    public void executeBulkRequest(String indexName, String field){
        BulkRequest request = new BulkRequest();
        // 添加第一个 IndexRequest
        request.add(new IndexRequest(indexName).id("1")
                .source(XContentType.JSON,field,"111在使用批量请求前，需要构建批量请求，即BulkRequest"));
        // 添加第二个 IndexRequest
        request.add(new IndexRequest(indexName).id("2")
                .source(XContentType.JSON,field,"222在使用批量请求前，需要构建批量请求，即BulkRequest"));
        // 添加第三个 IndexRequest
        request.add(new IndexRequest(indexName).id("3")
                .source(XContentType.JSON,field,"333在使用批量请求前，需要构建批量请求，即BulkRequest"));

        try {
            BulkResponse bulkResponse = restClient.bulk(request,RequestOptions.DEFAULT);

            // 解析BulkResponse
            processBulkResponse(bulkResponse);
        }catch (Exception e){
            e.printStackTrace();
        }

        // 关闭es连接
        closeEs();
    }

    // 解析BulkResponse
    private void processBulkResponse(BulkResponse bulkResponse) {
        if (bulkResponse == null) return;
        for (BulkItemResponse bulkItemResponse : bulkResponse){
            DocWriteResponse itemResponse = bulkItemResponse.getResponse();
            switch (bulkItemResponse.getOpType()){
                // 索引状态
                case INDEX:
                // 索引生成
                case CREATE:
                    IndexResponse indexResponse = (IndexResponse) itemResponse;
                    String index = indexResponse.getIndex();
                    String id = indexResponse.getId();
                    long version = indexResponse.getVersion();
                    log.info("create id is " + id + " , index is " + index + " ,version is " + version);
                    break;
                // 索引更新
                case UPDATE:
                    UpdateResponse updateResponse = (UpdateResponse) itemResponse;
                    break;
                // 索引删除
                case DELETE:
                    DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
            }
        }
    }

    // 以异步方式执行BulkRequest
    public void executeBulkRequestAsync(String indexName, String field){
        BulkRequest request = new BulkRequest();
        // 添加第一个 IndexRequest
        request.add(new IndexRequest(indexName).id("1")
                .source(XContentType.JSON,field,"111在使用批量请求前，需要构建批量请求，即BulkRequest"));
        // 添加第二个 IndexRequest
        request.add(new IndexRequest(indexName).id("2")
                .source(XContentType.JSON,field,"222在使用批量请求前，需要构建批量请求，即BulkRequest"));
        // 添加第三个 IndexRequest
        request.add(new IndexRequest(indexName).id("3")
                .source(XContentType.JSON,field,"333在使用批量请求前，需要构建批量请求，即BulkRequest"));

        // 构建监听器
        ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        };

        try {
            restClient.bulkAsync(request,RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }

        // 关闭es
        closeEs();
    }

    // 构建BulkProcessor
    public void buildBulkRequestWithBulkProcessor(String indexName, String field){
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long l, BulkRequest bulkRequest) {
                // 批量处理前的动作
            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                // 批量处理后的动作
            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
                // 批量处理后的动作
            }
        };

        BulkProcessor bulkProcessor = BulkProcessor.builder((request,bulkListener) ->
                                                restClient.bulkAsync(request,RequestOptions.DEFAULT,bulkListener),
                                                listener).build();

        /**
         * BulkProcessor的配置
         */
        BulkProcessor.Builder builder = BulkProcessor.builder((request, bulkListener) ->
                                                    restClient.bulkAsync(request,RequestOptions.DEFAULT,bulkListener),
                                                    listener);
        //根据当前添加的操作数，设置刷新批量请求的时间(默认值为1000，使用-1表示禁用)
        builder.setBulkActions(500);

        //根据当前添加的操作大小，设置刷新批量请求的时间(默认为5MB，使用-1表示禁用)
        builder.setBulkSize(new ByteSizeValue(1L,ByteSizeUnit.MB));

        //设置允许执行的并发请求数(默认为1，当使用0时表示仅允许执行单个请求)
        builder.setConcurrentRequests(0);

        //设置刷新间隔 (默认为未设置 )
        builder.setFlushInterval(TimeValue.timeValueSeconds(10L));

        //设置一个恒定的后退策略，该策略最初等待1s,最多重试3次
        builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L),3));


        /**
         * 添加索引请求
         */
        IndexRequest one = new IndexRequest(indexName).id("6").source(XContentType.JSON,"title","1111 BulkProcessor");
        IndexRequest two = new IndexRequest(indexName).id("7").source(XContentType.JSON,"title","2222 BulkProcessor");
        IndexRequest three = new IndexRequest(indexName).id("8 ").source(XContentType.JSON,"title","3333 BulkProcessor");

        bulkProcessor.add(one);
        bulkProcessor.add(two);
        bulkProcessor.add(three);

    }

    // 同步方式执行 BulkRequest
    public void executeBulkRequestWithBulkProcessor(String indexName, String field){
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long l, BulkRequest bulkRequest) {
                // 批量处理前的操作
                int numberOfActions = bulkRequest.numberOfActions();
                log.info("Executing bulk " + l + " with " + numberOfActions);
            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                // 批量处理后的操作
                if (bulkResponse.hasFailures()){
                    log.info("Bulk " + l + " executed with failures");
                }else {
                    log.info("Bulk " + l + " completed in " + bulkResponse.getTook().getMillis() + " milliseconds");
                }
            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
                // 批量处理后的操作
                log.error("Failed to execute bulk ", throwable);
            }
        };

        BulkProcessor bulkProcessor = BulkProcessor.builder((request,bulkListener) ->
                        restClient.bulkAsync(request,RequestOptions.DEFAULT,bulkListener),
                listener).build();

        /**
         * 添加索引请求
         */
        IndexRequest one = new IndexRequest(indexName).id("6").source(XContentType.JSON,"title","1111 BulkProcessor");
        IndexRequest two = new IndexRequest(indexName).id("7").source(XContentType.JSON,"title","2222 BulkProcessor");
        IndexRequest three = new IndexRequest(indexName).id("8 ").source(XContentType.JSON,"title","3333 BulkProcessor");

        bulkProcessor.add(one);
        bulkProcessor.add(two);
        bulkProcessor.add(three);
    }

    // 构建MultiGetRequest
    public void buildMultiGetRequest(String indexName, String[] documentIds){
        if (documentIds == null || documentIds.length <= 0){
            return;
        }

        MultiGetRequest request = new MultiGetRequest();

        for (String documentId : documentIds){
            // 添加请求
            request.add(new MultiGetRequest.Item(indexName, documentId));
        }

        /**
         * 可选参数使用介绍
         */

        //禁用源检索，在默认情况下启用
        request.add(new MultiGetRequest.Item(indexName, documentIds[0])
                . fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE));

        //为特定字段配置源包含关系
        String[] excludes = Strings.EMPTY_ARRAY;
        String[] includes = {"title", "content"};
        FetchSourceContext fetchSourceContext = new FetchSourceContext (true,
                includes, excludes);
        request.add(
                new MultiGetRequest.Item(indexName, documentIds[0]).fetchSourceContext
                        (fetchSourceContext)) ;
        //为特定字段配置源排除关系
        fetchSourceContext = new FetchSourceContext(true, includes, excludes) ;
        request.add (
                new MultiGetRequest.Item(indexName, documentIds[0]).
                        fetchSourceContext(fetchSourceContext));

        //为特定存储字段配置检索(要求字段在索引中单独存储字段)
        try {
            request. add (new MultiGetRequest. Item (indexName, documentIds
                    [0]) .storedFields ("title")) ;
            MultiGetResponse response = restClient.mget(request,RequestOptions.DEFAULT);
            MultiGetItemResponse item = response.getResponses()[0];

            // 检索title存储字段(要求该字段单独存储在索引中)
            String value = item.getResponse().getField("title").getValue() ;
            log.info("value is " + value);
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }

        //配置路由
        request.add(new MultiGetRequest.Item(indexName, documentIds[0]).routing("routing"));
        //配置版本和版本类型
        request.add(new MultiGetRequest.Item(indexName, documentIds[0])
                .versionType(VersionType.EXTERNAL).version (10123L));
        // 配置偏好值
        request.preference("title");
        //将实时标志设置为假(默认为真)
        request.realtime (false);
        //在检索文档之前执行刷新(默认为false)
        request.refresh(true);
    }

    // 同步方式执行 MultiGetRequest
    public void executeMultiGetRequest(String indexName, String[] documentIds){
        if (documentIds == null || documentIds.length <= 0){
            return;
        }

        MultiGetRequest request = new MultiGetRequest();

        for (String documentId : documentIds){
            // 添加请求
            request.add(new MultiGetRequest.Item(indexName, documentId));
        }

        try {
            MultiGetResponse response = restClient.mget(request,RequestOptions.DEFAULT);
            // 解析MultiGetResponse
            processMultiGetResponse(response);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es连接
            closeEs();
        }
    }

    // 异步方式执行 MultiGetRequest
    public void executeMultiGetRequestAsync(String indexName, String[] documentIds){
        if (documentIds == null || documentIds.length <= 0){
            return;
        }

        MultiGetRequest request = new MultiGetRequest();

        for (String documentId : documentIds){
            // 添加请求
            request.add(new MultiGetRequest.Item(indexName, documentId));
        }

        // 添加 ActionListener
        ActionListener<MultiGetResponse> listener = new ActionListener<MultiGetResponse>() {
            @Override
            public void onResponse(MultiGetResponse multiGetItemResponses) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        };

        // 执行批量获取
        try {
            restClient.mgetAsync(request,RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es连接
            closeEs();
        }
    }

    // 解析MultiGetResponse
    private void processMultiGetResponse(MultiGetResponse multiResponse){
        if (multiResponse == null){
            return;
        }
        MultiGetItemResponse[] responses = multiResponse.getResponses () ;
        log.info("responses is"+ responses.length);
        for (MultiGetItemResponse response : responses) {
            GetResponse getResponse = response.getResponse();
            String index = response.getIndex();
            String id = response.getId() ;
            log.info("index is " + index + ";id is " + id);
            if (getResponse. isExists()) {
                long version = getResponse .getVersion() ;
                //按字符串方式获取内容
                String sourceAsString = getResponse.getSourceAsString();
                //按Map方式获取内容
                Map<String, Object> sourceAsMap = getResponse. getSourceAsMap() ;
                //按字节数组方式获取内容
                byte[] sourceAsBytes = getResponse .getSourceAsBytes () ;
                log.info("version is " + version + "; sourceAsString is " + sourceAsString) ;
            }
        }
    }

    // 构建 ReindexRequest
    public void buildReindexRequest(String fromIndex, String toIndex){
        ReindexRequest request = new ReindexRequest();
        // 添加要从中复制的源的列表
        request.setSourceIndices("source1","source2",fromIndex);
        // 添加目标索引
        request.setDestIndex(toIndex);

        /**
         *  ReindexRequest 的参数配置
         */
        //设置目标索引的版本类型
        request.setDestVersionType(VersionType.EXTERNAL);
        //设置目标索引的操作类型为创建类型
        request.setDestOpType("create");
        //在默认情况下，版本冲突会中止重新索引进程，我们可以用以下方法计算它们
        request.setConflicts("proceed");
        //通过添加查询限制文档。下面仅复制用户字段设置为kimchy的文档
        request . setSourceQuery (new TermQueryBuilder("user", "kimchy"));
        //通过设置大小限制已处理文档的数量
        request.setSize(10) ;
        //在默认情况下，ReIndex使用1000个批次。可以使用sourceBatchSize更改批大小
        request.setSourceBatchSize(100) ;
        //指定管道模式
        request.setDestPipeline("my_pipeline") ;
        //如果需要用到源索引中的一组特定文档，则需要使用sort。建议最好选择更具选择性的查询，
        //而不是进行大小和排序
        request.addSortField("field1", SortOrder.DESC);
        request.addSortField("field2", SortOrder.ASC);
        //使用切片滚动对uid进行切片。使用setslices指定要使用的切片数
        request.setSlices (2) ;
        //使用scroll参数控制"search context", 保持活动的时间
        request.setScroll(TimeValue.timeValueMinutes(10));
        //设置超时时间
        request.setTimeout (TimeValue.timeValueMinutes(2));
        //调用reindex后刷新索引
        request.setRefresh(true);
    }

    // 同步方式执行 ReindexRequest
    public void executeReindexRequest(String fromIndex, String toIndex){
        ReindexRequest request = new ReindexRequest();
        // 添加要从中复制的源的列表
        request.setSourceIndices(fromIndex);
        // 添加目标索引
        request.setDestIndex(toIndex);
        try {
            BulkByScrollResponse bulkResponse = restClient.reindex(request, RequestOptions.DEFAULT);
            // 解析 BulkByScrollResponse
            processBulkByScrollResponse(bulkResponse);
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 异步方式执行 ReindexRequest
    public void executeReindexRequestAsync(String fromIndex, String toIndex){
        ReindexRequest request = new ReindexRequest();
        // 添加要从中复制的源的列表
        request.setSourceIndices(fromIndex);
        // 添加目标索引
        request.setDestIndex(toIndex);
        // 构建监听器
        ActionListener<BulkByScrollResponse> listener = new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        };
        try {
            restClient.reindexAsync(request, RequestOptions.DEFAULT,listener);
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 解析 BulkByScrollResponse
    private void processBulkByScrollResponse(BulkByScrollResponse bulkResponse) {
        if (bulkResponse == null){
            return;
        }

        //获取总耗时
        TimeValue timeTaken = bulkResponse.getTook();
        log.info("time is "+ timeTaken. getMillis());
        //检查请求是否超时
        boolean timedOut = bulkResponse.isTimedOut();
        log. info("timedOut is " + timedOut);
        //获取已处理的文档总数
        long totalDocs = bulkResponse.getTotal();
        log.info("totalDocs is " + totalDocs);
        //已更新的文档数
        long updatedDocs = bulkResponse.getUpdated();
        log.info("updatedDocs is " + updatedDocs);
        //已创建的文档数
        long createdDocs = bulkResponse.getCreated();
        log.info("createdDocs is " + createdDocs);
        //已删除的文档数
        long deletedDocs = bulkResponse.getDeleted();
        log.info ("deletedDocs is " + deletedDocs);
        //已执行的批次数
        long batches = bulkResponse.getBatches();
        log.info("batches is " + batches);
        //跳过的文档数
        long noops = bulkResponse.getNoops();
        log.info("noops is " + noops);
        //版本冲突数
        long versionConflicts = bulkResponse.getVersionConflicts();
        log. info ("versionConflicts is "+ versionConflicts);
        //重试批量索引操作的次数
        long bulkRetries = bulkResponse.getBulkRetries();
        log. info ("bulkRetries is "+ bulkRetries);
        //重试搜索操作的次数
        long searchRetries = bulkResponse.getSearchRetries();
        log. info ("searchRetries is "+ searchRetries);
        //请求阻塞的总时间，不包括当前处于休眠状态的限制时间
        TimeValue throttledMillis = bulkResponse.getStatus().getThrottled();
        log.info("throttledMillis is "+ throttledMillis.getMillis());
        //查询失败数量
        List<ScrollableHitSource.SearchFailure> searchFailures = bulkResponse.getSearchFailures();
        log.info ("searchFailures is "+ searchFailures.size());
        //批量操作失败数量
        List<BulkItemResponse.Failure> bulkFailures = bulkResponse.getBulkFailures() ;
        log.info("bulkFailures is "+ bulkFailures.size());
    }

    // 构建UpdateByQueryRequest
    public void UpdateByQueryRequest(String indexName){
        UpdateByQueryRequest request = new UpdateByQueryRequest(indexName);

        /**
         * 配置 UpdateByQueryRequest
         */

        //在默认情况下，版本冲突将中止UpdateByQueryRequest进程，但我们可以使用以下方法来计算它们
        request.setConflicts ("proceed");
        // 通过添加查询条件来限制。下面仅更新字段设置为 niudong 的文档
        request.setQuery(new TermQueryBuilder("user", "niudong"));
        //设置大小来限制已处理文档的数量
        request.setSize(10);
        //在默认情况下，UpdateByQueryRequest使用的批数为1000。可以使用setBatchSize更改批大小
        request.setBatchSize(100) ;
        //指定管道模式
        request.setPipeline("my_pipeline");
        //设置分片滚动来并行化
        request.setSlices(2);
        //使用滚动参数控制 “搜索上下文”, 保持连接的时间
        request.setScroll(TimeValue.timeValueMinutes(10));
        //如果提供路由，那么路由将被复制到滚动查询，从而限制与该路由值匹配的分片处理
        request.setRouting("=cat");
        //设置等待请求的超时时间
        request.setTimeout(TimeValue.timeValueMinutes(2));
        //调用update by query 后刷新索引
        request.setRefresh(true);
        //设置索引选项
        request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
    }

    // 同步方式执行UpdateByQueryRequest
    public void executeUpdateByQueryRequest(String indexName){
        UpdateByQueryRequest request = new UpdateByQueryRequest(indexName);
        try {
            BulkByScrollResponse bulkResponse = restClient.updateByQuery(request,RequestOptions.DEFAULT);
            // 处理 BulkByScrollResponse
            processBulkByScrollResponse(bulkResponse);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 异步方式执行UpdateByQueryRequest
    public void executeUpdateByQueryRequestAsync(String indexName){
        UpdateByQueryRequest request = new UpdateByQueryRequest(indexName);
        // 添加监听器
        ActionListener<BulkByScrollResponse> listener = new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        };
        try {
            restClient.updateByQueryAsync(request,RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 构建 DeleteByQueryRequest
    public void buildDeleteByQueryRequest(String indexName){
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);

        /**
         *  配置 DeleteByQueryRequest
         */

        //在默认情况下，版本冲突将中止 DeleteByQueryRequest 进程，但我们可以使用以下方法来计算它们
        request.setConflicts("proceed");
        // 通过添加查询条件来限制。下面仅删除用户字段设置为 niudong 的文档
        request.setQuery(new TermQueryBuilder("user", "niudong"));
        //设置大小，限制已处理文档的数量
        request.setSize(10);
        //在默认情况下，DeleteByQueryRequest 使用的批数为1000。可以使用setBatchSize更改批大小
        request.setBatchSize(100) ;
        //设置分片滚动来并行化
        request.setSlices(2);
        //使用滚动参数控制 “搜索上下文”, 保持连接的时间
        request.setScroll(TimeValue.timeValueMinutes(10));
        //如果提供路由，那么路由将被复制到滚动查询，从而限制与该路由值匹配的分片处理
        request.setRouting("=cat");
        //设置等待请求的超时时间
        request.setTimeout(TimeValue.timeValueMinutes(2));
        //调用delete by query 后刷新索引
        request.setRefresh(true);
        //设置索引选项
        request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
    }

    // 同步方式执行 DeleteByQueryRequest
    public void executeDeleteByQueryRequest(String indexName) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
        try {
            BulkByScrollResponse bulkResponse = restClient.deleteByQuery(request,RequestOptions.DEFAULT);
            // 处理BulkByScrollResponse
            processBulkByScrollResponse(bulkResponse);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 异步方式执行 DeleteByQueryRequest
    public void executeDeleteByQueryRequestAsync(String indexName) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
        // 构建监听器
        ActionListener<BulkByScrollResponse> listener = new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        };

        try {
            restClient.deleteByQueryAsync(request,RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 构建MultiTermVectorsRequest
    public void buildMultiTermVectorsRequest(String indexName, String[] documentIds, String field){
        // 方法1: 创建一个空的 MultiTermVectorsRequest, 向其添加单个 Term Vectors请求
        MultiTermVectorsRequest request = new MultiTermVectorsRequest();
        for (String documentId : documentIds){
            TermVectorsRequest tvrequest = new TermVectorsRequest(indexName, documentId);
            tvrequest.setFields(field);
            request.add(tvrequest);
        }

        //方法2:所有词向量请求共享相同参数(如索引和其他设置)
        TermVectorsRequest tvrequestTemplate = new TermVectorsRequest (indexName, "1");
        tvrequestTemplate.setFields(field);
        String[] ids = {"1","2"};
        request = new MultiTermVectorsRequest (ids, tvrequestTemplate);
    }

    // 同步方式执行 MultiTermVectorsRequest
    public void executeMultiTermVectorsRequest(String indexName, String[] documentIds, String field) {
        // 方法1: 创建一个空的 MultiTermVectorsRequest, 向其添加单个 Term Vectors请求
        MultiTermVectorsRequest request = new MultiTermVectorsRequest();
        for (String documentId : documentIds) {
            TermVectorsRequest tvrequest = new TermVectorsRequest(indexName, documentId);
            tvrequest.setFields(field);
            request.add(tvrequest);
        }
        try {
            MultiTermVectorsResponse response = restClient.mtermvectors(request, RequestOptions.DEFAULT);
            // 解析 MultiTermVectorsResponse
            processMultiTermVectorsResponse(response);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 异步方式执行 MultiTermVectorsRequest
    public void executeMultiTermVectorsRequestAsync(String indexName, String[] documentIds, String field) {
        // 方法1: 创建一个空的 MultiTermVectorsRequest, 向其添加单个 Term Vectors请求
        MultiTermVectorsRequest request = new MultiTermVectorsRequest();
        for (String documentId : documentIds) {
            TermVectorsRequest tvrequest = new TermVectorsRequest(indexName, documentId);
            tvrequest.setFields(field);
            request.add(tvrequest);
        }
        // 构建监听器
        ActionListener<MultiTermVectorsResponse> listener = new ActionListener<MultiTermVectorsResponse>() {
            @Override
            public void onResponse(MultiTermVectorsResponse multiTermVectorsResponse) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        };
        try {
            restClient.mtermvectorsAsync(request, RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 解析 MultiTermVectorsResponse
    private void processMultiTermVectorsResponse(MultiTermVectorsResponse response) {
        if (response == null) {
            return;
        }
        List<TermVectorsResponse> tvresponseList = response.getTermVectorsResponses();
        log.info("tvresponseList size is " + tvresponseList.size());
        for (TermVectorsResponse tvresponse : tvresponseList) {
            String id = tvresponse.getId();
            String index = tvresponse.getIndex();
            log.info("id size is " + id + "; index is " + index);
        }
    }


        // 关闭es连接
    public void closeEs(){
        try {
            restClient.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }





}
