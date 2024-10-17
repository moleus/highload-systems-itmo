package itmo.highload.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table(name = "balance")
data class Balance(
    @Id
    val id: Int = 0,

    @Column("user_id")
    val purpose: String,

    @Column("money_amount")
    var moneyAmount: Int
)
