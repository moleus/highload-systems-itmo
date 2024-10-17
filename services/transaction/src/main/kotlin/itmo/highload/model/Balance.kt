package itmo.highload.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

@Entity
@Table(name = "balance")
data class Balance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Purpose is mandatory")
    @Size(max = 50, message = "Purpose cannot exceed 50 characters")
    val purpose: String,

    @Column(nullable = false)
    @PositiveOrZero(message = "Money amount cannot be negative")
    var moneyAmount: Int
)
