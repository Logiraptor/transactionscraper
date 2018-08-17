package io.poyarzun.transactions

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import java.io.InputStream

object Csv {
    inline fun <reified T> parse(input: InputStream): List<T> {
        val mapper = CsvMapper()
        val schema = mapper.schemaFor(T::class.java).withSkipFirstDataRow(true)
        val reader = mapper.readerFor(T::class.java).with(schema)

        return reader.readValues<T>(input).readAll()
    }
}
