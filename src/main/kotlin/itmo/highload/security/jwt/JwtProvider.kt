package itmo.highload.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret.access}") jwtAccessSecret: String,
    @Value("\${jwt.secret.refresh}") jwtRefreshSecret: String,
    @param:Value("\${jwt.expiration.access}") private val jwtAccessExpirationMinutes: Int,
    @param:Value("\${jwt.expiration.refresh}") private val jwtRefreshExpirationDays: Int
) {

    private val jwtAccessSecret: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret))
    private val jwtRefreshSecret: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret))

    fun generateAccessToken(login: String): String {
        val now = LocalDateTime.now()
        val accessExpirationInstant = now
            .plusMinutes(jwtAccessExpirationMinutes.toLong())
            .atZone(ZoneId.systemDefault())
            .toInstant()
        val accessExpiration = Date.from(accessExpirationInstant)
        return Jwts.builder()
            .setExpiration(accessExpiration)
            .setSubject(login)
            .signWith(jwtAccessSecret)
            .compact()
    }

    fun generateRefreshToken(login: String): String {
        val now = LocalDateTime.now()
        val refreshExpirationInstant = now
            .plusDays(jwtRefreshExpirationDays.toLong())
            .atZone(ZoneId.systemDefault())
            .toInstant()
        val refreshExpiration = Date.from(refreshExpirationInstant)
        return Jwts.builder()
            .setExpiration(refreshExpiration)
            .setSubject(login)
            .signWith(jwtRefreshSecret)
            .compact()
    }

    fun validateAccessToken(accessToken: String) {
        validateToken(accessToken, jwtAccessSecret)
    }

    fun validateRefreshToken(refreshToken: String) {
        validateToken(refreshToken, jwtRefreshSecret)
    }

    private fun validateToken(token: String, key: Key) {
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
    }

    fun getAccessClaims(accessToken: String): Claims {
        return getClaims(accessToken, jwtAccessSecret)
    }

    fun getRefreshClaims(refreshToken: String): Claims {
        return getClaims(refreshToken, jwtRefreshSecret)
    }

    private fun getClaims(token: String, key: Key): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
}
