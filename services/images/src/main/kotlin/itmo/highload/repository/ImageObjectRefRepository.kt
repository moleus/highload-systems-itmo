package itmo.highload.repository

import itmo.highload.model.S3ObjectRef
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageObjectRefRepository : R2dbcRepository<S3ObjectRef, Int>
