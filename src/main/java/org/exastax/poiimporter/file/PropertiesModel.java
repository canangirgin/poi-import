package org.exastax.poiimporter.file;

import org.springframework.core.env.Environment;

// Data Model for user defined properties
public class PropertiesModel {

    //Parameters
    private String  filePathPoi;
    private String  filePathCity;
    private String  filePathTown;
    private String  charSet;
    private String  charSeparatorPoi;
    private String  charSeparatorCity;
    private String  charSeparatorTown;
    private String  elasticsearchHostName;
    private String  elasticSearchClusterName;
    private String  elasticSearchIndexName;
    private String  elasticSearchDocumentName;
    private String  elasticSearchUserName;
    private String  elasticSearchUserPassw;
    private Integer  elasticSearchPort;
    private Integer bulkConcurrentRequests;
    private Integer bulkSize;

    //Constructor with Environments
    public PropertiesModel(Environment env) {
        filePathPoi = env.getProperty ( "csv.path.poi" );
        filePathCity = env.getProperty ( "csv.path.city" );
        filePathTown = env.getProperty ( "csv.path.town" );
        charSeparatorPoi = env.getProperty ( "csv.char.separator.poi" );
        charSeparatorCity = env.getProperty ( "csv.char.separator.city" );
        charSeparatorTown = env.getProperty ( "csv.char.separator.town" );
        charSet=env.getProperty ( "csv.charset" );
        bulkConcurrentRequests = Integer.valueOf ( env.getProperty ( "elasticsearch.bulk.concurrent.requests" ) );
        bulkSize = Integer.valueOf ( env.getProperty ( "elasticsearch.bulk.size" ) );
        elasticSearchClusterName = env.getProperty ( "elasticsearch.cluster.name" );
        elasticsearchHostName = env.getProperty ( "elasticsearch.host" );
        elasticSearchIndexName = env.getProperty ( "elasticsearch.index.name" );
        elasticSearchDocumentName = env.getProperty ( "elasticsearch.document.name" );
        elasticSearchUserName = env.getProperty ( "elasticsearch.user.name" );
        elasticSearchUserPassw = env.getProperty ( "elasticsearch.user.passw" );
        elasticSearchPort = Integer.valueOf (env.getProperty ( "elasticsearch.port" ));

    }

    // Getter Setter Methods

    public String getFilePathPoi() {
        return filePathPoi;
    }

    public String getFilePathCity() {
        return filePathCity;
    }

    public String getFilePathTown() {
        return filePathTown;
    }

    public String getCharSeparatorPoi() {
        return charSeparatorPoi;
    }

    public String getCharSeparatorCity() {
        return charSeparatorCity;
    }

    public String getCharSeparatorTown() {
        return charSeparatorTown;
    }

    public String getElasticsearchHostName() {
        return elasticsearchHostName;
    }

    public String getElasticSearchClusterName() {
        return elasticSearchClusterName;
    }

    public String getElasticSearchIndexName() {
        return elasticSearchIndexName;
    }

    public String getElasticSearchDocumentName() {
        return elasticSearchDocumentName;
    }

    public Integer getBulkSize() {
        return bulkSize;
    }

    public Integer getBulkConcurrentRequests() {
        return bulkConcurrentRequests;
    }

    public String getCharSet() {
        return charSet;
    }

    public String getElasticSearchUserName() {
        return elasticSearchUserName;
    }

    public String getElasticSearchUserPassw() {
        return elasticSearchUserPassw;
    }

    public Integer getElasticSearchPort() {
        return elasticSearchPort;
    }
}
