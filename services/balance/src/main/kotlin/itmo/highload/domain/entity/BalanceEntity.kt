package itmo.highload.domain.entity

data class BalanceEntity(
    val id: Int = 0,
    val purpose: String,
    var moneyAmount: Int
)
