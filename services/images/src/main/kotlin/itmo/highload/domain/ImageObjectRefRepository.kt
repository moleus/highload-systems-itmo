package itmo.highload.domain

import itmo.highload.infrastructure.minio.model.S3ObjectRef
import reactor.core.publisher.Mono

interface ImageObjectRefRepository {
    fun findById(id: Int): Mono<S3ObjectRef>
    fun save(s3ObjectRef: S3ObjectRef): Mono<S3ObjectRef>
    fun deleteById(id: Int): Mono<Void>
}
