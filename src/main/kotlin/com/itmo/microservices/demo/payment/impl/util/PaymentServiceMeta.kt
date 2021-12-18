package com.itmo.microservices.demo.payment.impl.util

import java.lang.StringBuilder

class PaymentServiceMeta {

    companion object {

        private fun getExternalServiceUrl() : String {
            return "http://tps:8080"
        }

        fun makeTransactionUri() : String {

            val sb = StringBuilder()

            sb.append(getExternalServiceUrl())
            sb.append("/transactions")

            return sb.toString()
        }
    }
}