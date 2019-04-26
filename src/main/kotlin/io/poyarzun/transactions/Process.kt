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

//    val transactionRepository: TransactionRepository = ctx.getBean()
//
//    val cal = Calendar.getInstance()
//    cal.add(Calendar.DAY_OF_YEAR, -365)
//
//    val transactions = transactionRepository.findAll().toList().filter {
//        it.dateTs.after(cal.time)
//    }
//
//    val bills = transactions.groupBy { it.description to it.accountName }.map { Bill(it.value) }
//
//    bills.sortedBy { it.billFactor }.forEach {
//        val desc = it.description
//        println()
//        println(desc)
//        println(it.accountName)
//        println(" - Bill factor: ${it.billFactor}")
//        println(" - Amount: ${it.amount}")
//        println(" - Occurred ${it.count} times")
//        println(" - Occurs on ${it.distinctDays} distinct days")
//        println(" - Intercharge Duration Avg: ${it.interchargeDurationAverage}")
//        println(" - Intercharge Duration Rng: ${it.interchargeDurationRange}")
////        it.printDayDist()
//    }
}
//
//fun showRecurringTransactions(transactionRepository: TransactionRepository) {
//    val cal = Calendar.getInstance()
//    cal.add(Calendar.DAY_OF_YEAR, -365)
//
//    println("Filtering to transactions after ${cal.time}")
//
//    val transactions = transactionRepository.findAll().toList().filter {
//        it.dateTs.after(cal.time)
//    }
//
//    val transactionsByDate = transactions.groupBy {
//        val date = it.dateTs
//        val day = date.dayOfMonth()
//        day to it.description
//    }
//
//    println("Found ${transactions.size} transactions")
//
//    val blackList = setOf("Uber.com", "Lyft", "Whole Foods", "Payment", "Pivotal Soft Osv", "ATM Fee")
//
//    val recurringTransactions = transactionsByDate.entries.filter {
//        !blackList.contains(it.key.second)
//    }.sortedBy { entry -> entry.value.distinctBy { it.dateTs }.size }
//
//    recurringTransactions.forEach { (key, transactions) ->
//        val (day, desc) = key
//        val averageCost = transactions.map { it.absoluteAmount }.average().roundToInt()
//        val size = transactions.distinctBy { it.dateTs }.size
//        println("$desc on day $day -> $size times averaging $$averageCost")
//    }
//}
//
fun Date.dayOfMonth(): Int {
    return LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault()).dayOfMonth
}
