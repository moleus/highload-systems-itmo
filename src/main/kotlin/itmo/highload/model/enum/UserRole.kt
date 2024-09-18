package itmo.highload.model.enum

import lombok.RequiredArgsConstructor
import org.springframework.security.core.GrantedAuthority
import java.util.*

@RequiredArgsConstructor
enum class UserRole : GrantedAuthority {

    CUSTOMER,
    EXPENSE_MANAGER,
    ADOPTION_MANAGER,
    SUPERUSER;

    override fun getAuthority(): String {
        return super.toString().uppercase(Locale.getDefault())
    }
}
