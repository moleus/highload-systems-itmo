package itmo.highload.repository

import itmo.highload.model.ImageRef
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageRefRepository : R2dbcRepository<ImageRef, Int>
