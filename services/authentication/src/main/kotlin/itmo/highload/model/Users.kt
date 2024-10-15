package itmo.highload.model

import itmo.highload.security.Role
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

@Table("users")
data class Users(

    @Id
    @Column("id")
    val id: Int = 0,

    @Column("login")
    val login: String,

    @Column("password")
    val password: String,

    @Column("role")
    val role: Role,

    @Column("creation_date")
    val creationDate: LocalDate
)
