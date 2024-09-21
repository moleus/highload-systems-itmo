package itmo.highload.model

import java.io.Serializable

data class OwnershipId(
    val customer: Int = 0,
    val animal: Int = 0
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
