package io.poyarzun.transactions

import org.springframework.integration.endpoint.AbstractMessageSource

class TransactionMessageSource(val mint: MintService): AbstractMessageSource<List<Transaction>>() {
    override fun getComponentType() = "input-channel-adapter"

    override fun doReceive(): Any = mint.begin().use {
        it.login()
        return it.getTransactions()
    }
}
