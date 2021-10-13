package com.kuoji.elasticsearchlearn.controller;

import com.kuoji.elasticsearchlearn.service.EsService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: kuoji
 * @Date: 2021/08/09/15:08
 * @Description:
 */

@RestController
@RequestMapping("/es")
public class EsController {

    @Autowired
    private EsService esService;

    @RequestMapping("/ccc")
    public String a(){

        return "ccc";
    }

    @RequestMapping("/index/put")
    public String putIndex(String indexName, String document) {
        if (Strings.isEmpty(indexName) || Strings.isEmpty(document)) {
            return "Parameters are error!";
        }

        esService.indexDocuments(indexName, document);

        return "index OK!";
    }

    // 同步方式执行GetRequest
    @RequestMapping("/index/get")
    public String getIndex(String indexName, String document){
        if (Strings.isEmpty(indexName) || Strings.isEmpty(document)) {
            return "Parameters are error!";
        }

        esService.getIndexDocuments(indexName, document);

        return "Get Index OK!";
    }

    // 同步方式执行GetRequest
    @RequestMapping("/index/check")
    public String checkIndex(String indexName, String document){
        if (Strings.isEmpty(indexName) || Strings.isEmpty(document)) {
            return "Parameters are error!";
        }

        esService.checkExistIndexDocuments(indexName, document);

        return "Check Index OK!";
    }

    // 同步方式删除文档索引请求
    @RequestMapping("/index/delete")
    public String deleteIndex(String indexName, String document){
        if (Strings.isEmpty(indexName) || Strings.isEmpty(document)) {
            return "Parameters are error!";
        }

        esService.deleteIndexDocuments(indexName, document);

        return "delete Index OK!";
    }

    // 同步方法更次文档索引请求
    @RequestMapping("/index/update")
    public String updateIndex(String indexName, String document){
        if (Strings.isEmpty(indexName) || Strings.isEmpty(document)) {
            return "Parameters are error!";
        }

        esService.updateIndexDocuments(indexName, document);

        return "update Index OK!";

    }

    // 解析词向量请求
    @RequestMapping("/index/term")
    public String termVectors(String indexName, String document, String field){
        if (Strings.isEmpty(indexName) || Strings.isEmpty(document)) {
            return "Parameters are error!";
        }

        esService.exucateTermVectorsRequest(indexName,document,field);

        return "term OK!";
    }





}
