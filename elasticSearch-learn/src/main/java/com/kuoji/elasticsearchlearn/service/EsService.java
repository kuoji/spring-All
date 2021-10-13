package com.kuoji.elasticsearchlearn.service;

public interface EsService {
    void indexDocuments(String indexName, String document);

    void getIndexDocuments(String indexName, String document);

    void getIndexDocumentsAsync(String indexName, String document);

    void checkExistIndexDocuments(String indexName, String document);

    void deleteIndexDocuments(String indexName, String document);

    void updateIndexDocuments(String indexName, String document);

    void exucateTermVectorsRequest (String indexName, String document, String field);
}
