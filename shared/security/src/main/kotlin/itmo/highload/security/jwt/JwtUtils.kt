package itmo.highload.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import itmo.highload.security.Role
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.crypto.SecretKey


@Component
class JwtUtils(
    @Value("\${jwt.secret.sign}") jwtAccessSecret: String,
) {
    @Suppress("MagicNumber")
    private final val accessTokenExpirationTimeMinutes = 15

    private val jwtAccessSecret: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret))

    fun generateAccessToken(login: String, role: Role, userId: Int): String {
        val now = LocalDateTime.now()
        val accessExpirationInstant =
            now.plusMinutes(accessTokenExpirationTimeMinutes.toLong()).atZone(ZoneId.systemDefault()).toInstant()
        val accessExpiration = Date.from(accessExpirationInstant)
        val claims = mapOf("role" to role.name, "userId" to userId)
        return Jwts.builder()
            .expiration(accessExpiration)
            .subject(login)
            .issuedAt(Date())
            .claims(claims)
            .signWith(jwtAccessSecret)
            .compact()
    }

    fun validateAccessToken(accessToken: String) {
        validateToken(accessToken, jwtAccessSecret)
    }

    fun getAccessClaims(accessToken: String): Claims {
        return getClaims(accessToken, jwtAccessSecret)
    }

    fun extractUserId(accessToken: String): Int {
        val token = accessToken.replace("Bearer ", "")
        return getAccessClaims(token)["userId"] as Int
    }

    fun extractRole(accessToken: String): Role {
        val token = accessToken.replace("Bearer ", "")
        return Role.valueOf(getAccessClaims(token)["role"] as String)
    }

    private fun validateToken(token: String, key: SecretKey) {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
    }

    private fun getClaims(token: String, key: SecretKey): Claims {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
    }
}
