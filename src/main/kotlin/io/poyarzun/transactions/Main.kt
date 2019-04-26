package io.poyarzun.transactions

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.ClassPathResource
import org.springframework.integration.annotation.InboundChannelAdapter
import org.springframework.integration.annotation.Poller
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.core.MessageSource
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.messaging.MessageChannel
import org.springframework.web.client.RestTemplate

@SpringBootApplication(
    exclude = [DataSourceAutoConfiguration::class, ElasticsearchAutoConfiguration::class]
)
class Main {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder.build()
    }

    @Bean
    fun transactionChannel(): MessageChannel {
        return DirectChannel()
    }

    @Bean
    @InboundChannelAdapter(
        value = "transactionChannel",
        poller = [Poller(fixedDelay = (1000 * 60 * 60 * 12).toString())]
    )
    fun transactionMessageSource(mint: MintService): MessageSource<List<Transaction>> {
        return TransactionMessageSource(mint)
    }

    @Bean
    fun integrationFlow(@Qualifier("transactionChannel") transactionChannel: MessageChannel): IntegrationFlow {
        return IntegrationFlows.from(transactionChannel)
            .log<List<Transaction>> { "${it.payload.size} transactions received" }
            .get()
    }
}

fun main(args: Array<String>) {
    runApplication<Main>(*args)
}
