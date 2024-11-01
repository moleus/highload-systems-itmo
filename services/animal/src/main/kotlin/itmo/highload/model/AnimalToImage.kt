package itmo.highload.model

import jakarta.persistence.IdClass
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "animal_to_image")
@IdClass(AnimalToImageKey::class)
data class AnimalToImage(
    @Column("animal_id")
    @Id
    var animalId: Int,

    @Column("image_id")
    @Id
    val imageId: Int
)
