package itmo.highload.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "image_ref")
data class ImageRef(
    @Id
    @Column("id")
    val id: Int = 0,
    @Column("url")
    val url: String
)
