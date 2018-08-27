package io.poyarzun.transactions

data class Bill(
    val previousTransactions: List<Transaction>
) {
    val mergedTransactions: List<Transaction>
    get() {
        return previousTransactions.groupBy { it.dateTs }.map { entry ->
            val first = entry.value.first()
            val total = entry.value.sumByDouble { it.absoluteAmount.toDouble() }
            first.copy(
                transactionType = Transaction.TransactionType.MERGED,
                amount = total.toFloat()
            )
        }
    }

    fun printDayDist() {
        val grouped = previousTransactions.groupBy { it.dateTs.dayOfMonth() }
        grouped.entries.sortedBy { it.key }.forEach {
            println("${it.key}: ${"|".repeat(it.value.size)}")
        }
    }

    val interchargeDurationAverage: Double
    get() {
        return interchargeDurations.average()
    }

    private val representativeTransaction = previousTransactions.first()

    val interchargeDurationRange: ClosedFloatingPointRange<Double>
    get() {
        val durations = interchargeDurations
        if (durations.isEmpty()) {
            val amount1 = representativeTransaction.absoluteAmount.toDouble()
            return amount1..amount1
        }
        return durations.min()!!..durations.max()!!
    }

    val interchargeDurations: List<Double>
        get() {
            val sorted = mergedTransactions.sortedBy { it.dateTs }
            return sorted.windowed(2) { (left, right) ->
                val durationMillis = right.dateTs.time - left.dateTs.time
                val durationDays = durationMillis.toDouble() / (1000 * 60 * 60 * 24)
                durationDays
            }
        }

    val distinctDays: Double
        get() = previousTransactions.map { it.dateTs.dayOfMonth() }.distinct().size.toDouble()

    val count: Double
        get() = previousTransactions.distinctBy { it.dateTs }.size.toDouble()

    val billFactor: Double
        get() = count / distinctDays

    val description: String
        get() = representativeTransaction.description

    val accountName: String
        get() = representativeTransaction.accountName

    val amount: ClosedFloatingPointRange<Float>
        get() {
            val amounts = previousTransactions.map { it.absoluteAmount }
            return amounts.min()!!..amounts.max()!!
        }

    // fun billFactor: Float
    // fun nextExpectedDate: Date
    // fun nextExpectedAmount(percentile): Float
}
