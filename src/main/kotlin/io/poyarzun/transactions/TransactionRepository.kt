package io.poyarzun.transactions

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface TransactionRepository : ElasticsearchRepository<Transaction, String>
