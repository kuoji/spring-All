package com.kuoji.elasticsearchlearn.service.impl;

import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.fieldcaps.FieldCapabilities;
import org.elasticsearch.action.fieldcaps.FieldCapabilitiesRequest;
import org.elasticsearch.action.fieldcaps.FieldCapabilitiesResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.rankeval.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.profile.ProfileShardResult;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: kuoji
 * @Date: 2021/08/20/19:29
 * @Description:
 */
@Service
public class EsSearchImpl {
    private static final Logger log = LoggerFactory.getLogger(EsServiceImpl.class);

    @Autowired
    private RestHighLevelClient restClient;

    // 构建SearchRequest
    public void buildSearchRequest(){
        SearchRequest searchRequest = new SearchRequest();

        // 大多数搜索参数都添加到 SearchSourceBuilder中，它为进入搜索请求主体的所有内容提供 setter
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 将 searchSourceBuilder中添加 “全部匹配” 查询
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        // 将 searchSourceBuilder 添加到 searchRequest 中
        searchRequest.source(searchSourceBuilder);

        /**
         *  可选参数配置
         */
        // 在索引上限制请求
        searchRequest = new SearchRequest("posts");

        // 设置路由参数
        searchRequest.routing("routing");

        // 设置IndicesOptions 控制方法
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());

        // 使用首选参数，例如，执行搜索以首选本地分片。 默认值是分片之间随机的
        searchRequest.preference("_local");

    }

    // 使用 SearchSourceBuilder
    public void useSearchSourceBuilder(){
        SearchRequest searchRequest = new SearchRequest();

        /**
         * 使用 SearchSourceBuilder
         */
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置查询条件
        searchSourceBuilder.query(QueryBuilders.termQuery("content","货币"));
        // 设置搜索结果索引的起始地址，默认为 0
        searchSourceBuilder.from(0);
        // 设置要返回的搜索命中数大小，默认为 10
        searchSourceBuilder.size(5);
        // 设置一个可选的超时时间，控制允许搜索的时间
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 将 searchSourceBuilder 添加到 searchRequest 中
        searchRequest.source(searchSourceBuilder);

        /**
         * 搜索查询 MatchQueryBuilder的使用
         */
        // 方法 1
        // 以content字段中的货币为查询对象
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("content","货币");
        // 创建 QueryBuilder,提供配置搜索查询选项的方法
        // 对匹配查询码启动模糊匹配
        matchQueryBuilder.fuzziness(Fuzziness.AUTO);
        // 在匹配查询上设置前缀长度
        matchQueryBuilder.prefixLength(3);
        // 设置最大扩展选项以控制查询的模糊过程
        matchQueryBuilder.maxExpansions(10);

        // 方法2
        matchQueryBuilder = QueryBuilders.matchQuery("content","货币").fuzziness(Fuzziness.AUTO)
                                            .prefixLength(3).maxExpansions(10);

        // 无论用于创建QueryBuilder的方法是什么，用户都必须将matchQueryBuilder添加到searchSourceBuilder中
        searchSourceBuilder.query(matchQueryBuilder);


        /**
         * 指定排序方法
         */
        // 按分数降序排序(默认)
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        // 按ID字段升序排序
        searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));

        /**
         * 源筛选的方法使用
         */
        searchSourceBuilder.fetchSource(false);
        //该方法还接收一个或多个通配符模式的数组，以更细的粒度控制哪些字段被包括或排除
        String[] includeFields = new String[]{"title","innerObject.*"};
        String[] excludeFields = new String[]{"user"};

        searchSourceBuilder.fetchSource(includeFields,excludeFields);


        /**
         * 配置请求高亮显示
         */
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 为 title 字段创建字段高亮
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("title");
        // 设置字段高亮类型
        highlightTitle.highlighterType("unified");
        // 将 highlightTitle 添加到 highlightBuilder中
        highlightBuilder.field(highlightTitle);

        // 添加第二个高亮显示字段
        HighlightBuilder.Field highlightUser = new HighlightBuilder.Field("user");
        highlightBuilder.field(highlightUser);

        searchSourceBuilder.highlighter(highlightBuilder);


        /**
         * 聚合请求的使用
         */
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("by_company").field("company.keyword");
        aggregationBuilder.subAggregation(AggregationBuilders.avg("average_age").field("age"));

        searchSourceBuilder.aggregation(aggregationBuilder);

        /**
         *  Suggestions 建议请求的使用
         */
        // 在TermSuggestionBuilder中为 content 字段添加货币的 Suggestions
        SuggestionBuilder suggestionBuilder = SuggestBuilders.termSuggestion("content").text("货币");
        SuggestBuilder suggestBuilder = new SuggestBuilder();

        // 添加建议生成器并命名
        suggestBuilder.addSuggestion("suggest_user",suggestionBuilder);

        // 将 suggestBuilder 添加到 searchSourceBuilder 中
        searchSourceBuilder.suggest(suggestBuilder);
    }

    // 参数化构建SearchRequest
    public SearchRequest buildSearchRequest(String filed, String text){
        SearchRequest searchRequest = new SearchRequest();
        // 大多数搜索参数都添加到 SearchSourceBuilder中，它为进入搜索请求主体的所有内容提供 setter
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 将 searchSourceBuilder中添加 “全部匹配” 查询
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        // 将 searchSourceBuilder 添加到 searchRequest 中
        searchRequest.source(searchSourceBuilder);


        /**
         *  使用 SearchSourceBuilder
         */
        // 设置查询条件
        searchSourceBuilder.query(QueryBuilders.termQuery(filed, text));
        // 设置搜索结果索引的起始地址，默认为 0
        searchSourceBuilder.from(0);
        // 设置要返回的搜索命中数大小，默认为 10
        searchSourceBuilder.size(5);
        // 设置一个可选的超时时间，控制允许搜索的时间
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 将 searchSourceBuilder 添加到 searchRequest 中
        searchRequest.source(searchSourceBuilder);


        /**
         * 配置请求高亮显示
         */
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 为 字段创建字段高亮
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field(filed);
        // 设置字段高亮类型
        highlightTitle.highlighterType("unified");
        // 将 highlightTitle 添加到 highlightBuilder中
        highlightBuilder.field(highlightTitle);

        searchSourceBuilder.highlighter(highlightBuilder);


        /**
         *  Suggestions 建议请求的使用
         */
        // 在TermSuggestionBuilder中为 content 字段添加货币的 Suggestions
        SuggestionBuilder suggestionBuilder = SuggestBuilders.termSuggestion(filed).text(text);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        // 添加建议生成器并命名
        suggestBuilder.addSuggestion("suggest_user",suggestionBuilder);
        // 将 suggestBuilder 添加到 searchSourceBuilder 中
        searchSourceBuilder.suggest(suggestBuilder);

        return searchRequest;
    }

    // 以同步方式执行 SearchRequest
    public void executeSearchRequest(){
        SearchRequest searchRequest = buildSearchRequest("context","货币");
        try {
            SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 以异步方式执行 SearchRequest
    public void executeSearchRequestAsync(){
        SearchRequest searchRequest = buildSearchRequest("context","货币");
        // 构建监听器
        ActionListener<SearchResponse> listener = new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        };
        try {
            restClient.searchAsync(searchRequest, RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 解析SearchResponse
    private void processSearchResponse(SearchResponse searchResponse) {
        if (searchResponse == null) {
            return;
        }

        // 获取HTTP状态代码
        RestStatus status = searchResponse.status();
        // 获取请求执行时间
        TimeValue took = searchResponse.getTook();
        // 获取请求是否提前终止
        Boolean terminatedEarly = searchResponse.isTerminatedEarly();
        // 获取请求是否超时
        boolean timedOut = searchResponse.isTimedOut();

        log.info("status is " + status + "; took is " + took + "; terminatedEarly is " + terminatedEarly
                + "; timeOut is " + timedOut);

        // 查看搜索影响的分片总数
        int totalShards = searchResponse.getTotalShards();
        // 搜索成功的分片的统计信息
        int successfulShards = searchResponse.getSuccessfulShards();
        // 搜索失败的分片的统计信息
        int failedShards = searchResponse.getFailedShards();
        log.info("totalShards is " + totalShards + "; successfulShards is " + successfulShards
                + "; failedShards is " + failedShards);
        for (ShardSearchFailure failure : searchResponse.getShardFailures()) {
            log.info("fail is " + failure.toString());
        }

        // 获取结果响应中包含的搜索结果SearchHits
        SearchHits hits = searchResponse.getHits();
        // SearchHits 提供了相关结果的全部信息，如点击总数或最高分数
        TotalHits totalHits = hits.getTotalHits();
        // 搜索结果的总量数
        long numHits = totalHits.value;
        // 最高分数
        float maxScore = hits.getMaxScore();
        log.info("numHits is " + numHits + "; maxScore is " + maxScore);

        // 对SearchHits的解析还可以通过遍历SearchHit数组实现
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            // 解析 SearchHit, SearchHits提供了对文档基本信息的访问，如索引名称、文档ID和每次搜索的得分
            // 获取索引名称
            String index = hit.getIndex();
            // 获取文档ID
            String id = hit.getId();
            // 获取搜索的得分
            float score = hit.getScore();
            log.info("docId is " + id + "; docIndex is " + index + "; docScore is " + score);

            //以JSON字符串形式返回文档源
            String sourceAsString = hit.getSourceAsString();
            //以键值对的形式返回文档源
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String documentTitle = (String) sourceAsMap.get("title");
            List<Object> users = (List<Object>) sourceAsMap.get("user");
            Map<String, Object> innerObject = (Map<String, Object>) sourceAsMap.get("innerObject");
            log.info("sourceAsString is " + sourceAsString + "; sourceAsMap size is " + sourceAsMap.size());

            // 高亮显示
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField highligh = highlightFields.get("content");
            // 获取包含高亮显示字段内容的一个或多个片段
            Text[] fragments = highligh.fragments();
            String fragmentString = fragments[0].string();
            log.info("fragmentsString is " + fragmentString);
        }

        // 聚合搜索
        Aggregations aggregations = searchResponse.getAggregations();
        if (aggregations == null) {
            return;
        }
        // 按content聚合
        Terms byCompanyAggregation = aggregations.get("by_content");
        // 获取以 Elastic为关键词的Bucket
        Terms.Bucket elasticBucket = byCompanyAggregation.getBucketByKey("Elastic");
        //获取平均年龄的子聚合
        Avg averageAge = elasticBucket.getAggregations().get("average_age");
        double avg = averageAge.getValue();
        log.info("avg is " + avg);

        // 搜索
        Suggest suggest = searchResponse.getSuggest();
        if (suggest == null) {
            return;
        }
        //按content搜索Suggest
        TermSuggestion termSuggestion = suggest.getSuggestion("content");
        for (TermSuggestion.Entry entry : termSuggestion.getEntries()) {
            for (TermSuggestion.Entry.Option option : entry) {
                String suggestText = option.getText().string();
                log.info("suggestText is " + suggestText);
            }
        }

        // 在搜索时分析结果
        Map<String, ProfileShardResult> profilingResults = searchResponse.
                getProfileResults();
        if (profilingResults == null) {
            return;
        }
        for (Map.Entry<String, ProfileShardResult> profilingResult :
                profilingResults.entrySet()) {
            String key = profilingResult.getKey();
            ProfileShardResult profileShardResult = profilingResult.getValue();
            log.info("key is "+ key + "; profileShardResult is "+ profileShardResult.toString());
        }
    }

    // 构建SearchRequest
    public void buildAndExecuteScrollSearchRequest(String indexName, int size){
        // 设置索引名称
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("title","es"));

        // 创建SearchRequest及相应的 SearchSourceBuilder
        // 还可以选择设置大小以控制一次检索多少结果
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);

        // 设置滚动间隔
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        try {
            SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
            // 读取返回的滚动ID，该ID指向保持活动状态的搜索上下文，并在后续搜索滚动调用中被需要
            String scrollId = searchResponse.getScrollId();
            // 检索第一批搜索结果
            SearchHits hits = searchResponse.getHits();
            while (hits != null && hits.getHits().length != 0){
                // 设置滚动标识符
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(TimeValue.timeValueSeconds(30));
                SearchResponse searchScrollResponse = restClient.scroll(scrollRequest,RequestOptions.DEFAULT);

                // 读取新的滚动ID，该ID指向保持活动状态的搜索上下文，并在后续搜索滚动调用中被需要
                scrollId = searchScrollResponse.getScrollId();
                // 检索另一批搜索结果
                hits = searchResponse.getHits();
                log.info("scrollId is " + scrollId);
                log.info("total hits is " + hits.getTotalHits().value + " ; now hits is " + hits.getHits().length);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 构建SearchRequest
    public void buildAndExecuteScrollSearchRequestAsync(String indexName, int size){
        // 设置索引名称
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("title","es"));

        // 创建SearchRequest及相应的 SearchSourceBuilder
        // 还可以选择设置大小以控制一次检索多少结果
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);

        // 设置滚动间隔
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        try {
            SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
            // 读取返回的滚动ID，该ID指向保持活动状态的搜索上下文，并在后续搜索滚动调用中被需要
            String scrollId = searchResponse.getScrollId();
            // 检索第一批搜索结果
            SearchHits hits = searchResponse.getHits();
            // 配置监听器
            ActionListener<SearchResponse> scrollListener = new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    // 读取新的ID，该ID指向保持活动状态的搜索上下文，并在后续搜索滚动调用中被需要
                    String scrollId = searchResponse.getScrollId();
                    // 检索另一批搜索结果
                    SearchHits hits = searchResponse.getHits();
                    log.info("scrollId is " + scrollId);
                    log.info("total hits is " + hits.getTotalHits().value + "; now hits is " + hits.getHits().length);
                }
                @Override
                public void onFailure(Exception e) {
                }
            };
            while (hits != null && hits.getHits().length != 0){
                // 设置滚动标识符
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(TimeValue.timeValueSeconds(30));
                // 异步执行
                restClient.scrollAsync(scrollRequest,RequestOptions.DEFAULT,scrollListener);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    //  构建ClearScrollRequest
    public void buildClearScrollRequest(String scrollId){
        ClearScrollRequest request = new ClearScrollRequest();
        // 添加单个滚动标识符
        request.addScrollId(scrollId);

        // 添加多个滚动标识符
        List<String> scrollIds = new ArrayList<>();
        scrollIds.add(scrollId);
        request.setScrollIds(scrollIds);
    }

    //以同步方式执行清除滚动搜索请求
    public void executeClearScrollRequest (String scrollId) {
        ClearScrollRequest request = new ClearScrollRequest();
        //添加单个滚动标识符
        request.addScrollId(scrollId);
        try {
            ClearScrollResponse response = restClient.clearScroll(request, RequestOptions.DEFAULT);
            /**
             * 解析清除滚动搜索请求的核心代码
             */
            // 如果请求成功，则会返回 true 的结果
            boolean success = response.isSucceeded();
            // 返回已释放的搜索上下文数
            int released = response.getNumFreed();
            log.info("success id " + success + "; released is " + released);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeEs();
        }
   }

    //以异步方式执行清除滚动搜索请求
    public void executeClearScrollRequestAsync (String scrollId) {
        ClearScrollRequest request = new ClearScrollRequest();
        //添加单个滚动标识符
        request.addScrollId(scrollId);
        //添加监听器
        ActionListener<ClearScrollResponse> listener = new ActionListener<ClearScrollResponse>() {
            @Override
            public void onResponse(ClearScrollResponse clearScrollResponse) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        };
        try {
            restClient.clearScrollAsync(request,RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 构建MultiSearchRequest
    public void buildMultiSearchRequest(){
        MultiSearchRequest request = new MultiSearchRequest();
        // 构建搜索请求对象1
        SearchRequest firstSearchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("user","kk"));
        firstSearchRequest.source(searchSourceBuilder);
        // 将搜索请求对象1添加到MultiSearchRequest
        request.add(firstSearchRequest);

        // 构建搜索请求对象2
        SearchRequest secondSearchRequest = new SearchRequest();
        searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("user","kk2"));
        firstSearchRequest.source(searchSourceBuilder);
        // 将搜索请求对象2添加到MultiSearchRequest
        request.add(secondSearchRequest);
    }

    // 构建MultiSearchRequest
    public MultiSearchRequest buildMultiSearchRequest(String field, String[] keywords){
        MultiSearchRequest request = new MultiSearchRequest();
        // 构建搜索请求对象1
        SearchRequest firstSearchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery(field,keywords[0]));
        firstSearchRequest.source(searchSourceBuilder);
        // 将搜索请求对象1添加到MultiSearchRequest
        request.add(firstSearchRequest);

        // 构建搜索请求对象2
        SearchRequest secondSearchRequest = new SearchRequest();
        searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery(field,keywords[1]));
        firstSearchRequest.source(searchSourceBuilder);
        // 将搜索请求对象2添加到MultiSearchRequest
        request.add(secondSearchRequest);

        return request;
    }

    //以同步方式执行MultiSearchRequest
    public void executeMultiSearchRequest(String field, String[] keywords) {
        //构建MultiSearchRequest
        MultiSearchRequest request = buildMultiSearchRequest(field, keywords);
        try {
            MultiSearchResponse response = restClient.msearch(request, RequestOptions.DEFAULT);
            // 解析返回结果 MultiSearchResponse
            processMultiSearchResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeEs();
        }
    }

    // 解析返回结果 MultiSearchResponse
    private void processMultiSearchResponse(MultiSearchResponse response) {
        // 获取返回结果集合
        MultiSearchResponse.Item[] items = response.getResponses();
        // 判断返回结果结合是否为空
        if (items == null || items.length < 0) {
            log.info("items is null.");
            return;
        }
        for (MultiSearchResponse.Item item : items) {
            Exception exception = item.getFailure();
            if (exception != null) {
                log.info("exception is " + exception.toString());
            }
            SearchResponse searchResponse = item.getResponse();
            SearchHits hits = searchResponse.getHits();
            if (hits.getTotalHits().value <= 0) {
                log.info("hits.getTotalHits() .value is 0.");
                return;
            }
            SearchHit[] hitArray = hits.getHits();
            for (SearchHit hit : hitArray) {
                log.info("id is " + hit.getId() + "; index is " + hit.getIndex() + "; source is"
                        + hit.getSourceAsString());
            }
        }
    }

    //以异步方式执行MultiSearchRequest
    public void executeMultiSearchRequestAsync(String field, String[] keywords) {
        //构建MultiSearchRequest
        MultiSearchRequest request = buildMultiSearchRequest(field, keywords);
        // 构建监听器
        ActionListener<MultiSearchResponse> listener = new ActionListener<MultiSearchResponse>() {
            @Override
            public void onResponse(MultiSearchResponse items) {
            }
            @Override
            public void onFailure(Exception e) {
            }
        };
        // 异步执行
        try {
            restClient.msearchAsync(request, RequestOptions.DEFAULT,listener);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeEs();
        }
    }

    // 构建FieldCapabilitiesRequest
    public FieldCapabilitiesRequest buildFieldCapabilitiesRequest(){
        FieldCapabilitiesRequest request = new FieldCapabilitiesRequest().fields("content")
                                                .indices("kk","kk1");

        //配置可选参数IndicesOptions: 解析不可用的索引及展开通配符表达式
        request.indicesOptions(IndicesOptions.lenientExpandOpen());

        return request;
    }

    // 构建FieldCapabilitiesRequest
    public FieldCapabilitiesRequest buildFieldCapabilitiesRequest(String field, String[] indices){
        FieldCapabilitiesRequest request = new FieldCapabilitiesRequest().fields(field)
                                                .indices(indices[0]).indices(indices[1]);

        //配置可选参数IndicesOptions: 解析不可用的索引及展开通配符表达式
        request.indicesOptions(IndicesOptions.lenientExpandOpen());

        return request;
    }

    // 同步方式执行跨索引字段搜索请求
    public void executeFieldCapabilitiesRequest(String field, String[] indices){
        // 构建FieldCapabilitiesRequest
        FieldCapabilitiesRequest request = buildFieldCapabilitiesRequest(field,indices);
        try {
            FieldCapabilitiesResponse response = restClient.fieldCaps(request, RequestOptions.DEFAULT);
            // 处理返回结果 FieldCapabilitiesResponse
            processFieldCapabilitiesResponse(response,field,indices);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 处理返回结果 FieldCapabilitiesResponse
    private void processFieldCapabilitiesResponse(FieldCapabilitiesResponse response, String field, String[] indices) {
        //获取字段中可能含有的类型的映射
        Map<String, FieldCapabilities> fieldResponse = response.getField(field) ;
        Set<String> set = fieldResponse.keySet();
        //获取文本宇段类型下的数据
        FieldCapabilities textCapabilities = fieldResponse.get("text");
        //数据能否被搜索到
        boolean isSearchable = textCapabilities.isSearchable();
        log.info("isSearchable is" + isSearchable);
        //数据能否聚合
        boolean isAggregatable = textCapabilities.isAggregatable();
        log.info("isAggregatable is " + isAggregatable);
        //获取特定字段类型下的索引
        String[] indicesArray = textCapabilities.indices();
        if (indicesArray != null){
            log.info("indicesArray is " + indicesArray.length) ;
        }
        // field 字段不能被搜索到的索引集合
        String[] nonSearchableIndices = textCapabilities.nonSearchableIndices();
        if (nonSearchableIndices != null) {
            log.info("nonSearchableIndices is" + nonSearchableIndices.length);
        }
        // field字段不能被聚合到的索引集合
        String[] nonAggregatableIndices = textCapabilities.nonAggregatableIndices();
        if (nonAggregatableIndices != null){
            log.info("nonAggregatableIndices is " + nonAggregatableIndices.length);
        }
    }

    // 异步方式执行跨索引字段搜索请求
    public void executeFieldCapabilitiesRequestAsync(String field, String[] indices){
        // 构建FieldCapabilitiesRequest
        FieldCapabilitiesRequest request = buildFieldCapabilitiesRequest(field,indices);
        // 配置监听器
        ActionListener<FieldCapabilitiesResponse> listener = new ActionListener<FieldCapabilitiesResponse>() {
            @Override
            public void onResponse(FieldCapabilitiesResponse fieldCapabilitiesResponse) {
            }
            @Override
            public void onFailure(Exception e) {
            }
        };
        // 执行异步请求
        try {
            restClient.fieldCapsAsync(request, RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 构建RankEvalRequest
    public RankEvalRequest buildRankEvalRequest(String index, String documentId, String field, String content){
        EvaluationMetric metric = new PrecisionAtK();
        List<RatedDocument> ratedDocs = new ArrayList<>();
        //添加按索引名称、ID和分级指定的分级文档
        ratedDocs.add(new RatedDocument(index, documentId, 1));
        SearchSourceBuilder searchQuery = new SearchSourceBuilder();
        //创建要评估的搜索查询
        searchQuery.query(QueryBuilders.matchQuery(field, content));
        //将前三部分合并为RatedRequest
        RatedRequest ratedRequest = new RatedRequest("content_query", ratedDocs,searchQuery);
        List<RatedRequest> ratedRequests = Arrays.asList(ratedRequest);
        //创建排序评估规范
        RankEvalSpec specification = new RankEvalSpec(ratedRequests,metric);
        //创建排序评估请求
        RankEvalRequest request = new RankEvalRequest(specification, new String[]{index});

        return request;
    }

    //以同步方式执行RankEvalRequest
    public void executeRankEvalRequest(String index, String documentId, String field, String content) {
        //构建RankEvalRequest
        RankEvalRequest request = buildRankEvalRequest(index, documentId, field, content);
        try {
            RankEvalResponse response = restClient.rankEval(request, RequestOptions.DEFAULT);
            // 处理RankEvalResponse
            processRankEvalResponse(response);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 处理RankEvalResponse
    private void processRankEvalResponse(RankEvalResponse response) {
        //总体评价结果
        double evaluationResult = response.getMetricScore();
        log.info("evaluationResult is " + evaluationResult);
        Map<String, EvalQueryQuality> partialResults = response.getPartialResults();
        //获取关键词content_query对应的评估结果
        EvalQueryQuality evalQuality = partialResults.get("content_query");
        log.info("content_query id is " + evalQuality.getId());
        //每部分结果的度量分数
        double qualityLevel = evalQuality.metricScore();
        log.info("qualityLevel is " + qualityLevel);
        List<RatedSearchHit> hitsAndRatings = evalQuality.getHitsAndRatings();
        RatedSearchHit ratedSearchHit = hitsAndRatings.get(2);
        //在分级搜索命中里包含完全成熟的搜索命中SearchHit
        log.info("SearchHit id is " + ratedSearchHit.getSearchHit().getId());
        //分级搜索命中还包含一个可选的<integer>分级optional<Integer> ，如果文档在请求中
        //未获得分级，则该分级不存在
        log.info("rate's isPresent is " + ratedSearchHit.getRating().isPresent());
        MetricDetail metricDetails = evalQuality.getMetricDetails();
        String metricName = metricDetails.getMetricName();
        //度量详细信息，以请求中使用的度量命名
        log.info("metricName is " + metricName);

        PrecisionAtK.Detail detail = (PrecisionAtK. Detail) metricDetails;
        //在转换到请求中使用的度量之后，度量详细信息提供了对度量计算部分的深入了解
        log.info("detail's relevantRetrieved is " + detail.getRelevantRetrieved());
        log.info("detail's retrieved is " + detail.getRetrieved());
    }

    //以异步方式执行RankEvalRequest
    public void executeRankEvalRequestAsync(String index, String documentId, String field, String content) {
        //构建RankEvalRequest
        RankEvalRequest request = buildRankEvalRequest(index, documentId, field, content);
        // 构建监听器
        ActionListener<RankEvalResponse> listener = new ActionListener<RankEvalResponse>() {
            @Override
            public void onResponse(RankEvalResponse rankEvalResponse) {
            }
            @Override
            public void onFailure(Exception e) {
            }
        };
        try {
            restClient.rankEvalAsync(request, RequestOptions.DEFAULT,listener);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭es
            closeEs();
        }
    }

    // 构建ExplainRequest
    public ExplainRequest buildExplainRequest(String indexName, String document, String field, String content){
        ExplainRequest request = new ExplainRequest(indexName, document);
        request.query(QueryBuilders.termQuery(field,content));

        /**
         * 配置可选参数
         */
        // 设置路由
        request.routing("routing");
        //使用首选参数，例如执行搜索以首选本地碎片。默认值是在分片之间随机进行的
        request.preference("_local");
        //设置为“真”，以检索解释的文档源。还可以通过使用“包含源代码”和“排除源代码”来检索
        //部分文档
        request.fetchSourceContext(new FetchSourceContext(true, new String[]{field}, null));
        //允许控制一部分的存储字段(要求在映射中单独存储该字段)，并将其返回作为说明文档
        request.storedFields(new String[] {field});

        return request;
    }

    // 同步方式执行ExplainRequest
    public void executeExplainRequest(String indexName, String document , String field, String content) {
        //构建ExplainRequest
        ExplainRequest request = buildExplainRequest(indexName, document, field, content);
        //执行请求，接收返回结果
        try {
            ExplainResponse response = restClient.explain(request, RequestOptions.DEFAULT);
            // 解析 ExplainResponse
            processExplainResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭es
            closeEs();
        }
    }

    // 解析 ExplainResponse
    private void processExplainResponse(ExplainResponse response) {
        // 解释文档的索引名称
        String index = response.getIndex();
        // 解释文档的ID
        String id = response.getId();
        // 查看解释的文档是否存在
        boolean exists = response.isExists();
        log.info("index is " + index + "; id is " + id + "; exists is " + exists);
        // 解释的文档与提供的查询之间是否匹配(匹配是从后台的Lucene解释中检索的)
        // Lucene解释建模匹配, 则返回 true, 否则返回 false
        boolean match = response.isMatch();
        // 查看是否存在此请求的Lucene解释
        boolean hasExplanation = response.hasExplanation();
        log.info("match is " + match + "; hasExplanation is " + hasExplanation);
        //荻取Lucene解释对象(如果存在)
        Explanation explanation = response.getExplanation();
        if (explanation != null) {
            log.info("explanation is " + explanation.toString());
        }
        //如果检索到源或存储宇段，则获取getresult对象
        GetResult getResult = response.getGetResult();
        if (getResult == null) {
            return;
        }
        // getresult内部包含两个映射，用于存储提取的源字段和存储的字段
        // 以Map形式检索源
        Map<String, Object> source = getResult.getSource();
        if (source == null) {
            return;
        }
        for (String str : source.keySet()) {
            log.info("str key is " + str);
        }
        //以映射形式检索指定的存储字段
        Map<String, DocumentField> fields = getResult.getFields();
        if (fields == null) {
            return;
        }
        for (String str : fields.keySet()){
            log.info("field str key is " + str);
        }
    }

    // 异步方式执行ExplainRequest
    public void executeExplainRequestAsync(String indexName, String document , String field, String content) {
        //构建ExplainRequest
        ExplainRequest request = buildExplainRequest(indexName, document, field, content);
        // 构建监听器
        ActionListener<ExplainResponse> listener = new ActionListener<ExplainResponse>() {
            @Override
            public void onResponse(ExplainResponse explainResponse) {
            }
            @Override
            public void onFailure(Exception e) {
            }
        };
        //执行请求，接收返回结果
        try {
            restClient.explainAsync(request, RequestOptions.DEFAULT,listener);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭es
            closeEs();
        }
    }

    // 构建CountRequest
    public CountRequest buildCountRequest(){
        //创建CountRequest。如果没有参数，则对所有索引运行
        CountRequest countRequest = new CountRequest();

        //大多数搜索参数都需要添加到SearchSourceBuilder中
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //向SearchSourceBuilder中添加 “全部匹配”查 询
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        //将SearchSourceBuilder添加到CountRequest中
        countRequest.source(searchSourceBuilder);

        return countRequest;
    }

    //构建CountRequest
    public CountRequest buildCountRequest(String indexName, String routeName,String field,String content) {
        //创建CountRequest。如果没有参数，则对所有索引运行
        //将请求限制为特定名称的索引
        CountRequest countRequest = new CountRequest(indexName)
                //设置路由参数
                .routing(routeName)
                //设置 IndicesOptions,控制如何解析不可用索引及如何展开通配符表达式
                .indicesOptions(IndicesOptions.lenientExpandOpen())
                //使用首选参数，例如执行搜索以首选本地分片。默认值是在分片之间随机选择的
                .preference("_local");
        //使用默认选项创建SearchSourceBuilder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //设置查询可以是任意类型的QueryBuilder
        sourceBuilder.query(QueryBuilders.termQuery(field, content));
        //将SearchSourceBuilder添加到CountRequest中
        countRequest.source(sourceBuilder);

        return countRequest;
    }

    //以同步方式执行CountRequest
    public void executeCountRequest() {
        CountRequest countRequest = buildCountRequest();
        try {
            CountResponse countResponse = restClient.count(countRequest, RequestOptions.DEFAULT);
            // 解析CountResponse
            processCountResponse(countResponse);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭es连接
            closeEs();
        }
    }

    // 解析CountResponse
    private void processCountResponse(CountResponse countResponse) {
        //统计请求对应的结果命中总数
        long count = countResponse.getCount();
        // HTTP状态代码
        RestStatus status = countResponse.status();
        //请求是否提前终止
        Boolean terminatedEarly = countResponse.isTerminatedEarly();
        log.info("count is " + count + ";status is " + status.getStatus() +"; terminatedEarly is"
                + terminatedEarly) ;
        //与统计请求对应的分片总数
        int totalShards = countResponse.getTotalShards();
        //执行统计请求跳过的分片数量
        int skippedShards = countResponse.getSkippedShards();
        //执行统计请求成功的分片数量
        int successfulShards = countResponse.getSuccessfulShards();
        //执行统计请求失败的分片数量
        int failedShards = countResponse.getFailedShards();
        log.info("totalShards is " + totalShards + ";skippedShards is " +
                skippedShards + ";successfulShards is "+ successfulShards + ";failedShards is " + failedShards);
        //通过遍历ShardSearchFailures数组来处理可能的失败信息
        if (countResponse. getShardFailures() == null) {
            return;
        }
        for (ShardSearchFailure failure : countResponse.getShardFailures()){
                log. info("fail index is "+ failure.index());
         }
    }

    //以异步方式执行CountRequest
    public void executeCountRequestAsync() {
        //创建CountRequest
        CountRequest countRequest = buildCountRequest();
        // 构建监听器
        ActionListener<CountResponse> listener = new ActionListener<CountResponse>() {
            @Override
            public void onResponse(CountResponse countResponse) {
            }
            @Override
            public void onFailure(Exception e) {
            }
        };
        try {
            restClient.countAsync(countRequest, RequestOptions.DEFAULT,listener);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        //关闭es连接
            closeEs();
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
