package io.poyarzun.transactions

import org.apache.http.HttpHost
import org.elasticsearch.client.Client
import org.elasticsearch.client.RestClientBuilder
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import java.net.InetAddress


@Configuration
@EnableElasticsearchRepositories(basePackages = arrayOf("io.poyarzun.transactions"))
class EsConfig {

    @Value("\${elasticsearch.host:127.0.0.1}")
    private val EsHost: String? = null

    @Value("\${elasticsearch.port:9300}")
    private val EsPort: Int = 0


    @Bean
    @Throws(Exception::class)
    fun client(): Client {

        val esSettings = Settings.builder()
            .put("cluster.name", "elasticsearch_patrickoyarzun")
            .build()

        //https://www.elastic.co/guide/en/elasticsearch/guide/current/_transport_client_versus_node_client.html
        return PreBuiltTransportClient(esSettings).addTransportAddress(
            InetSocketTransportAddress(InetAddress.getByName(EsHost), EsPort)
        )
    }

    @Bean
    @Throws(Exception::class)
    fun elasticsearchTemplate(): ElasticsearchOperations {
        return ElasticsearchTemplate(client())
    }

    //Embedded Elasticsearch Server
    /*@Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchTemplate(nodeBuilder().local(true).node().client());
    }*/

}
