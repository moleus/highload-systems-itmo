package itmo.highload.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.security.Key
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(@Value("\${jwt.secret.access}") jwtAccessSecret: String?,
                  @Value("\${jwt.secret.refresh}") jwtRefreshSecret: String?,
                  @param:Value("\${jwt.expiration.access}") private val jwtAccessExpirationMinutes: Int,
                  @param:Value("\${jwt.expiration.refresh}") private val jwtRefreshExpirationDays: Int) {

    private val jwtAccessSecret: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret))
    private val jwtRefreshSecret: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret))

    fun generateAccessToken(user: UserDetails): String {
        val now = LocalDateTime.now()
        val accessExpirationInstant = now
                .plusMinutes(jwtAccessExpirationMinutes.toLong())
                .atZone(ZoneId.systemDefault())
                .toInstant()
        val accessExpiration = Date.from(accessExpirationInstant)
        return Jwts.builder()
                .setSubject(user.username)
                .setExpiration(accessExpiration)
                .signWith(jwtAccessSecret)
                .compact()
    }

    fun generateRefreshToken(user: UserDetails): String {
        val now = LocalDateTime.now()
        val refreshExpirationInstant = now
                .plusDays(jwtRefreshExpirationDays.toLong())
                .atZone(ZoneId.systemDefault())
                .toInstant()
        val refreshExpiration = Date.from(refreshExpirationInstant)
        return Jwts.builder()
                .setSubject(user.username)
                .setExpiration(refreshExpiration)
                .signWith(jwtRefreshSecret)
                .compact()
    }

    fun validateAccessToken(accessToken: String): Boolean {
        return validateToken(accessToken, jwtAccessSecret)
    }

    fun validateRefreshToken(refreshToken: String): Boolean {
        return validateToken(refreshToken, jwtRefreshSecret)
    }

    private fun validateToken(token: String, key: Key): Boolean {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
            return true
        } catch (e: JwtException) {
            return false
        }
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
