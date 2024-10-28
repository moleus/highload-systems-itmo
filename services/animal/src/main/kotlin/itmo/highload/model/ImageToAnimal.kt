package itmo.highload.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "animal_to_image")
data class ImageToAnimal(
    @Id
    val id: Int = 0,

    @Column("animal_id")
    var animalId: Int,

    @Column("image_id")
    val imageId: Int
)
