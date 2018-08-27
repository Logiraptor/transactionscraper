package io.poyarzun.transactions

data class Account(
    val name: String,
    var balance: Float
) {
    fun process(transaction: Transaction) {
        if (name != transaction.accountName) {
            throw IllegalArgumentException("transaction.accountName")
        }
        balance += transaction.absoluteAmount
    }
}