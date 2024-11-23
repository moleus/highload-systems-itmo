package itmo.highload.infrastructure.postgres.model

import jakarta.persistence.IdClass
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable

@Table(name = "animal_to_image")
@IdClass(AnimalToImageKey::class)
data class AnimalToImage(
    @Column("animal_id")
    var animalId: Int,

    @Column("image_id")
    val imageId: Int
)

data class AnimalToImageKey(
    val animalId: Int = 0,
    val imageId: Int = 0
): Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
