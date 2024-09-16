package itmo.highload.model

import itmo.highload.model.enum.UserRole
import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "app_user")
data class User (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    @Size(min = 4, max = 50)
    val login: String,

    @Column(nullable = false)
    private val password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole

) : UserDetails {

    override fun getAuthorities(): List<GrantedAuthority?> {
        return listOf(role)
    }

    override fun getUsername(): String {
        return login
    }

    override fun getPassword(): String {
        return password
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
