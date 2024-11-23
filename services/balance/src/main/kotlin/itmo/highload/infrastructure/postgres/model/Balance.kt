package itmo.highload.infrastructure.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table(name = "balance")
data class Balance(
    @Id
    @Column("id")
    val id: Int = 0,

    @Column("purpose")
    val purpose: String,

    @Column("money_amount")
    var moneyAmount: Int
)
