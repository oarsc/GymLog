package org.scp.gymlog.model

import java.util.Arrays

enum class Order(val code: String) {
    ALPHABETICALLY("alphabetically"),
    LAST_USED("last_used");

    companion object {
        fun getByCode(code: String): Order {
            return Arrays.stream(values())
                    .filter { order: Order -> order.code == code }
                    .findFirst()
                    .orElseThrow { IllegalArgumentException("No order found for name \"$code\"") }
        }
    }
}