@file:Suppress("WildcardImport")
package itmo.highload.model

import itmo.highload.security.Role
import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLEnumJdbcType
import java.time.LocalDate

@Entity
@Table(name = "users")
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(unique = true, nullable = false)
    @Size(min = 4, max = 50)
    val login: String,

    @Column(nullable = false)
    val password: String,

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @Column(nullable = false)
    val role: Role,

    @Column
    val creationDate: LocalDate
)

