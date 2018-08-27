package io.poyarzun.transactions

data class FinancialPosition(
    val accounts: List<Account>,
    val transactions: MutableList<Transaction>
) {
    fun process(transaction: Transaction) {
        val relevantAccount = accounts.find { it.name == transaction.accountName } ?: throw IllegalStateException("Missing account named ${transaction.accountName}")

        relevantAccount.process(transaction)

        transactions.add(transaction)
    }
}

// Core: Transaction, Account, Position(accounts, transactions)