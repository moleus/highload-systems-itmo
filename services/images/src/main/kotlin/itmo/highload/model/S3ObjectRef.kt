package itmo.highload.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "s3_object_ref")
data class S3ObjectRef(
    @Id
    @Column("id")
    val id: Int = 0,

    @Column("bucket")
    val bucket: String,

    @Column("key")
    val key: String,
)
