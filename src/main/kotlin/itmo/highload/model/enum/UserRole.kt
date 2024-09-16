package itmo.highload.model.enum

import lombok.RequiredArgsConstructor
import org.springframework.security.core.GrantedAuthority
import java.util.*

@RequiredArgsConstructor
enum class UserRole : GrantedAuthority {

    CLIENT,
    ADMIN;

    override fun getAuthority(): String {
        return super.toString().uppercase(Locale.getDefault())
    }
}
