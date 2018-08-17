package io.poyarzun.transactions

import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.ClassPathResource
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.web.client.RestTemplate

@SpringBootApplication(
    exclude = [DataSourceAutoConfiguration::class]
)
class Main {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder.build()
    }
}

fun main(args: Array<String>) {
    val ctx = AnnotationConfigApplicationContext {
        register(Main::class.java)
        val propertySourcesPlaceholderConfigurer = PropertySourcesPlaceholderConfigurer()
        propertySourcesPlaceholderConfigurer.setLocation(ClassPathResource("application.properties"))
        addBeanFactoryPostProcessor(propertySourcesPlaceholderConfigurer)
    }
    ctx.refresh()

    val transactionRepository: TransactionRepository = ctx.getBean()

    val es: ElasticsearchOperations = ctx.getBean()
    if (es.indexExists(Transaction::class.java)) {
        es.deleteIndex(Transaction::class.java)
    }
    es.createIndex(Transaction::class.java)

    val mint: Mint = ctx.getBean()
    mint.use {
        it.login()
        val csv = it.getTransactionsCSV()
        val transactions = Csv.parse<Transaction>(csv)
        println("Loaded ${transactions.size} transactions")
        println(transactions[0])
        println(transactions[1])

        transactionRepository.saveAll(transactions)
    }
}
