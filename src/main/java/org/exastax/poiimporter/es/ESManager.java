package org.exastax.poiimporter.es;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.exastax.poiimporter.conf.AppConfig;
import org.exastax.poiimporter.file.PropertiesModel;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

//Manager for Elastic Search Operations.
public class ESManager implements IESManager {

    private final Log log = LogFactory.getLog ( ESManager.class );
    private BulkProcessor   bulk;
    private TransportClient client;

    public ESManager(Environment env) {
        AppConfig.properties = new PropertiesModel ( env );
    }

    @SuppressWarnings("resource")
    @PostConstruct
    private void init() throws Exception {
        log.info ( String.format ( "Connecting to cluster: %s via %s using ssl:%s",
                AppConfig.properties.getElasticSearchClusterName ( ), AppConfig.properties.getElasticsearchHostName ( )
                , AppConfig.properties.getElasticSearchPort (), true ) );
        Settings settings = Settings.builder ( )
                .put ( "client.transport.nodes_sampler_interval", "5s" )
                .put ( "client.transport.sniff", false )
                .put ( "transport.tcp.compress", true )
                .put ( "cluster.name", AppConfig.properties.getElasticSearchClusterName ( ) )
                .put ( "xpack.security.transport.ssl.enabled", true )
                .put ( "request.headers.X-Found-Cluster", "${cluster.name}" )
                .put ( "xpack.security.user", AppConfig.properties.getElasticSearchUserName ()
                        +":"+AppConfig.properties.getElasticSearchUserPassw () )
                .build ( );
        InetAddress address = InetAddress.getByName ( AppConfig.properties.getElasticsearchHostName ( ) );
        client = new PreBuiltXPackTransportClient ( settings );
        try {
            client.addTransportAddress ( new InetSocketTransportAddress ( address, AppConfig.properties.getElasticSearchPort () ) );
        } catch (Exception e) {
            log.error ( String.format ( "Unable to connect the elastic search host", e.getMessage ( ) ) );
        }
        bulk = prepareBulk ( );
    }

    @PreDestroy
    public void shutdown() {
        try {
            bulk.awaitClose ( 10, TimeUnit.MINUTES );
        } catch (InterruptedException e) {
            e.printStackTrace ( );
        }
        client.close ( );
    }

    // Create elastic search index and put mappings
    @Override
    public void createIndex() throws IOException {
        final IndicesExistsResponse res = client.admin ( ).indices ( ).prepareExists
                ( AppConfig.properties.getElasticSearchIndexName ( ) ).execute ( ).actionGet ( );
        if (!res.isExists ( )) {
            client.admin ( ).indices ( )
                    .prepareCreate ( AppConfig.properties.getElasticSearchIndexName ( ) )
                    .get ( );
            client.admin ( ).cluster ( ).prepareHealth ( AppConfig.properties.getElasticSearchIndexName ( ) ).setWaitForYellowStatus ( );
        }
        client.admin ( ).indices ( ).preparePutMapping ( AppConfig.properties.getElasticSearchIndexName ( ) )
                .setType ( AppConfig.properties.getElasticSearchDocumentName ( ) )
                .setSource ( "{\n" +
                        "    \"" + AppConfig.properties.getElasticSearchDocumentName ( ) + "\":{\n" +
                        "  \"properties\": {\n" +
                        "    \"name\": {\n      \"type\": \"string\"\n    },\n" +
                        "    \"category\": {\n      \"type\": \"string\"\n    },\n" +
                        "    \"topcategory\": {\n      \"type\": \"string\"\n    },\n" +
                        "    \"cityid\": {\n       \"type\": \"string\"\n    },\n" +
                        "    \"cityname\": {\n       \"type\": \"string\"\n    },\n" +
                        "    \"townid\": {\n       \"type\": \"string\"\n    },\n" +
                        "    \"townname\": {\n       \"type\": \"string\"\n    },\n" +
                        "    \"oldtowns\": {\n       \"type\": \"string\"\n    },\n" +
                        "    \"oldquarters\": {\n       \"type\": \"string\"\n    },\n" +
                        "    \"quarterid\": {\n       \"type\": \"string\"\n    },\n" +
                        "    \"streetid\": {\n       \"type\": \"string\"\n    },\n" +
                        "    \"zipcode\": {\n       \"type\": \"string\"\n    },\n" +
                        "    \"location\": {\n       \"type\": \"geo_shape\"\n }" +
                        "  }\n" +
                        "  }\n" +
                        "}" )
                .get ( );
    }

    @Override
    //Add poi properties to elastic search bulk index.
    public void addBulk(List <String> headers, List <String> values) throws Exception {
        XContentBuilder json     = jsonBuilder ( ).startObject ( );
        GeoPoint        geoPoint = new GeoPoint ( );
        String          id       = "";
        for (int i = 0; i < headers.size ( ); i++) {
            String header = headers.get ( i ).toLowerCase ( );
            try {
                if (header.equals ( "latitude" ) && !values.get ( i ).isEmpty ( ))
                    geoPoint.resetLat ( Double.parseDouble ( values.get ( i ) ) );
                else if (header.equals ( "longitude" ) && !values.get ( i ).isEmpty ( ))
                    geoPoint.resetLon ( Double.parseDouble ( values.get ( i ) ) );
                else if (header.equals ( "id" ))
                    id = values.get ( i );
                else {
                    String value = values.get ( i );
                    json.field ( header, value );
                }
            } catch (Exception ex) {
                log.warn ( String.format ( "Invalid Location Value: %s. ID:%s, Exception:%s", values.get ( i ), id, ex.getMessage ( ) ) );
            }
        }
        json.startObject ( "location" )
                .field ( "type", "point" )
                .startArray ( "coordinates" ).value ( geoPoint.getLon ( ) ).value ( geoPoint.getLat ( ) ).endArray ( )
                .endObject ( );

        json.endObject ( );
        bulk.add ( client.prepareIndex ( AppConfig.properties.getElasticSearchIndexName ( ), AppConfig.properties.getElasticSearchDocumentName ( ), id ).setSource ( json ).request ( ) );

    }

    //Prepeare and configure elastic search bulk processor for bulk indexing
    private BulkProcessor prepareBulk() throws Exception {
        return BulkProcessor.builder ( client,
                new BulkProcessor.Listener ( ) {
                    @Override
                    public void beforeBulk(long executionId, BulkRequest request) {
                        log.info ( String.format ( "Bulk %d process prepared for %d bytes", executionId,
                                request.estimatedSizeInBytes ( ) ) );
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          BulkResponse response) {
                        log.info ( String.format ( "Bulk %d process completed in %d milliseconds", executionId,
                                response.getTookInMillis ( ) ) );
                        if (response.hasFailures ( )) {
                            log.error ( String.format ( "Bulk %d process failed in %d milliseconds. Error : %s", executionId,
                                    response.getTookInMillis ( ), response.buildFailureMessage ( ) ) );
                        }
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          Throwable failure) {
                        log.error ( String.format ( "Bulk %d process has error: %s", executionId, failure.getMessage ( ) ) );
                    }
                } )
                .setBulkActions ( AppConfig.properties.getBulkSize ( ) )
                .setBulkSize ( new ByteSizeValue ( 5, ByteSizeUnit.MB ) )
                .setFlushInterval ( TimeValue.timeValueSeconds ( 5 ) )
                .setConcurrentRequests ( AppConfig.properties.getBulkConcurrentRequests ( ) )
                .setBackoffPolicy (
                        BackoffPolicy.exponentialBackoff ( TimeValue.timeValueMillis ( 100 ), 3 ) )
                .build ( );

    }

}