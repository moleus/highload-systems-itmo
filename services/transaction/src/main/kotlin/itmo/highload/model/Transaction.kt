package itmo.highload.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime


@Table(name = "transaction")
data class Transaction(
    @Id
    @Column("id")
    val id: Int = 0,

    @Column("date_time")
    val dateTime: LocalDateTime,

    @Column("user_id")
    val userId: Int,

    @Column("balance_id")
    val balanceId: Int,

    @Column("money_amount")
    val moneyAmount: Int,

    @Column("is_donation")
    val isDonation: Boolean
)
