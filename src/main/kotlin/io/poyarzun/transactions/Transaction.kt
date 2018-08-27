package io.poyarzun.transactions

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*
import java.text.SimpleDateFormat
import org.springframework.data.elasticsearch.annotations.Document
import java.text.ParseException
import javax.persistence.Id


@JsonPropertyOrder(
    "date",
    "description",
    "originalDescription",
    "amount",
    "transactionType",
    "category",
    "accountName",
    "labels",
    "notes"
)
@Document(indexName = "transactions", type = "transaction")
data class Transaction(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val date: String = "",
    val description: String = "",
    val originalDescription: String = "",
    val amount: Float = 0f,
    val transactionType: TransactionType? = null,
    val category: String = "",
    val accountName: String = "",
    val labels: String = "",
    val notes: String = ""
) {

    enum class TransactionType {
        @JsonProperty("credit")
        CREDIT,
        @JsonProperty("debit")
        DEBIT,
        @JsonProperty("merged")
        MERGED
    }

    val absoluteAmount: Float
        get() {
            val accountDebtMap = setOf(
                "CREDIT CARD",
                "MAZDA LEASE",
                "Stafford Loans U.S. DEPARTMENT OF EDUCATION",
                "Auto and Property Bill",
                "Direct Sub Stafford Loan",
                "Direct Unsub Stafford Loan"
            )

            val isDebt = accountName in accountDebtMap
            return when {
                transactionType == TransactionType.CREDIT && isDebt -> -amount
                transactionType == TransactionType.DEBIT && !isDebt -> -amount
                else -> amount
            }
        }

    val dateTs: Date
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern ="yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    get() {
        try {
            val format = SimpleDateFormat("M/d/yyyy")
            return format.parse(date)
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }
    }
}