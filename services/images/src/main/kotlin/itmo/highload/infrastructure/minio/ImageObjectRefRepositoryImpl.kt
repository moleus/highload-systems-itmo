package itmo.highload.infrastructure.minio

import itmo.highload.domain.ImageObjectRefRepository
import itmo.highload.infrastructure.minio.model.S3ObjectRef
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageObjectRefRepositoryImpl : R2dbcRepository<S3ObjectRef, Int>, ImageObjectRefRepository
