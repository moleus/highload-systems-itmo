package itmo.highload.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable

@Entity
@Table(name = "ownerships")
@IdClass(OwnershipId::class)
data class Ownership(
    @Id
    val customerId: Int,

    @Id
    val animalId: Int
)

data class OwnershipId(
    val customerId: Int = 0,
    val animalId: Int = 0
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
