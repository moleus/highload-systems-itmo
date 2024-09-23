package itmo.highload.model.enum

import org.springframework.security.core.GrantedAuthority
import java.util.*

enum class Role : GrantedAuthority {
    CUSTOMER,
    EXPENSE_MANAGER,
    ADOPTION_MANAGER,
    SUPERUSER;

    override fun getAuthority(): String {
        return name.uppercase(Locale.getDefault())
    }
}
