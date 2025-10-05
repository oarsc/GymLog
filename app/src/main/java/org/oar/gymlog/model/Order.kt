package org.oar.gymlog.model

enum class Order(val code: String) {
    ALPHABETICALLY("alphabetically"),
    LAST_USED("last_used");

    companion object {
        fun getByCode(code: String): Order {
            return values()
                    .filter { order -> order.code == code }
                    .getOrElse(0) {
                        throw IllegalArgumentException("No order found for name \"$code\"") }
        }
    }
}