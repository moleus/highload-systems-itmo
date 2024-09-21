package itmo.highload.model.enums

import org.springframework.security.core.GrantedAuthority
import java.util.*

enum class UserRole : GrantedAuthority {
    CUSTOMER,
    EXPENSE_MANAGER,
    ADOPTION_MANAGER,
    SUPERUSER;

    override fun getAuthority(): String {
        return name.uppercase(Locale.getDefault())
    }
}