package itmo.highload.model

import itmo.highload.model.enum.Gender
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.GenerationType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Column
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLEnumJdbcType

@Entity
@Table(name = "customers")
data class Customer(
    @Id
    val id: Int,

    @Column(nullable = false, unique = true, length = 20)
    @NotBlank(message = "Phone is mandatory")
    @Pattern(regexp = "^7[0-9]{10}\$", message = "Invalid phone number")
    val phone: String,

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @Column(nullable = false)
    val gender: Gender,

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Address is mandatory")
    @Size(max = 50, message = "Address cannot exceed 50 characters")
    val address: String
)
