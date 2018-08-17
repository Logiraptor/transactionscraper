package io.poyarzun.transactions

import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.ClassPathResource
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.math.roundToInt

@SpringBootApplication(
    exclude = [DataSourceAutoConfiguration::class]
)
class Process {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder.build()
    }
}

fun main(args: Array<String>) {
    val ctx = AnnotationConfigApplicationContext {
        register(Process::class.java)
        val propertySourcesPlaceholderConfigurer = PropertySourcesPlaceholderConfigurer()
        propertySourcesPlaceholderConfigurer.setLocation(ClassPathResource("application.properties"))
        addBeanFactoryPostProcessor(propertySourcesPlaceholderConfigurer)
    }
    ctx.refresh()

    val transactionRepository: TransactionRepository = ctx.getBean()

    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -365)

    val transactions = transactionRepository.findAll().toList().filter {
        it.dateTs.after(cal.time)
    }

    val groupedTransactions = transactions.groupBy { it.description }

    val descriptionToCount = groupedTransactions.mapValues { (_, value) ->
        value.distinctBy { it.dateTs }.size
    }

    val descriptionToDistinctDays = groupedTransactions.mapValues { (_, value) ->
        value.map { it.dateTs.dayOfMonth() }.distinct()
    }

    val descriptionToBillFactor = groupedTransactions.mapValues { (key, _) ->
        val repeated: Double = descriptionToCount[key]?.toDouble() ?: 0.0
        val recurring: Double = descriptionToDistinctDays[key]?.size?.toDouble() ?: 0.0
        repeated / recurring
    }

    val descriptionToAmount = groupedTransactions.mapValues { (_, value) ->
        val amounts = value.map { it.absoluteAmount }.sorted()
        amounts.first()..amounts.last()
    }

    groupedTransactions.asIterable().sortedBy { descriptionToBillFactor[it.key] }.forEach {
        val desc = it.key
        println(desc)
        println(" - Bill factor: ${descriptionToBillFactor[desc]}")
        println(" - Amount: ${descriptionToAmount[desc]}")
        println(" - Occurred ${descriptionToCount[desc]} times")
        println(" - Occurs on ${descriptionToDistinctDays[desc]?.size} distinct days: ${descriptionToDistinctDays[desc]}")
    }
}

fun showRecurringTransactions(transactionRepository: TransactionRepository) {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -365)

    println("Filtering to transactions after ${cal.time}")

    val transactions = transactionRepository.findAll().toList().filter {
        it.dateTs.after(cal.time)
    }

    val transactionsByDate = transactions.groupBy {
        val date = it.dateTs
        val day = date.dayOfMonth()
        day to it.description
    }

    println("Found ${transactions.size} transactions")

    val blackList = setOf("Uber.com", "Lyft", "Whole Foods", "Payment", "Pivotal Soft Osv", "ATM Fee")

    val recurringTransactions = transactionsByDate.entries.filter {
        !blackList.contains(it.key.second)
    }.sortedBy { entry -> entry.value.distinctBy { it.dateTs }.size }

    recurringTransactions.forEach { (key, transactions) ->
        val (day, desc) = key
        val averageCost = transactions.map { it.absoluteAmount }.average().roundToInt()
        val size = transactions.distinctBy { it.dateTs }.size
        println("$desc on day $day -> $size times averaging $$averageCost")
    }
}

fun Date.dayOfMonth(): Int {
    return LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault()).dayOfMonth
}
