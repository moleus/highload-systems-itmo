package itmo.highload.service

import io.github.oshai.kotlinlogging.KotlinLogging
import itmo.highload.minio.MinioConfig
import itmo.highload.minio.PartDataStream
import itmo.highload.minio.S3Storage
import itmo.highload.model.S3ObjectRef
import itmo.highload.repository.ImageObjectRefRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.net.ConnectException
import java.util.*

class ImageServiceException(message: String, e: Exception) : RuntimeException(message, e)

@Service
class ImagesService @Autowired constructor(
    private val imageObjectRefRepository: ImageObjectRefRepository,
    private val minioStorage: S3Storage,
    private val minioConfig: MinioConfig
) {
    private val logger = KotlinLogging.logger {}
    private val bucketName = minioConfig.defaultBucketName

    fun getImageById(id: Int): Mono<S3ObjectRef> {
        return imageObjectRefRepository.findById(id)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Image with ID $id not found")))
    }

    fun saveImage(data: FilePart): Mono<S3ObjectRef> {
        val uuid = UUID.randomUUID().toString()
        val fileName = "$uuid/${data.filename()}"
        val minioImageUrl = "${minioConfig.minioUrl}/$bucketName/$fileName"
        val s3ObjectRef = S3ObjectRef(
            bucket = bucketName,
            key = fileName,
            url = minioImageUrl
        )

        return data.content().collectList().map { parts ->
            val byteArray = parts.flatMap { it.asInputStream().readAllBytes().toList() }.toByteArray()
            PartDataStream(
                stream = byteArray.inputStream(),
                size = byteArray.size.toLong(),
                partSize = -1,
            )
        }.doOnNext {
            logger.info { "Uploading ${it.size} bytes to $fileName" }
        }.handle<PartDataStream> { partDataStream, sink ->
            try {
                minioStorage.putObject(
                    bucketName, fileName, data.headers().contentType.toString(), partDataStream
                )
            } catch (e: ConnectException) {
                sink.error(ImageServiceException("Failed to connect to MinIO", e))
            }
        }.doOnNext {
            logger.info { "Uploaded ${it.size} bytes to $fileName" }
            logger.info { "Image can be viewed in '$minioImageUrl'" }
        }.then(imageObjectRefRepository.save(s3ObjectRef))
    }

    fun deleteImageById(id: Int): Mono<Unit> {
        return imageObjectRefRepository.findById(id)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Image with ID $id not found")))
            .flatMap { obj ->
                imageObjectRefRepository.deleteById(id)
                    .then(Mono.fromCallable { minioStorage.deleteObject(obj.bucket, obj.key) })
            }
    }

    fun updateImageById(id: Int, data: FilePart): Mono<S3ObjectRef> {
        return imageObjectRefRepository.findById(id)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Image with ID $id not found")))
            .flatMap { existingObject ->
                val fileName = existingObject.key
                val bucketName = existingObject.bucket
                val minioImageUrl = "${minioConfig.minioUrl}/$bucketName/$fileName"

                data.content().collectList().map { parts ->
                    val byteArray = parts.flatMap { it.asInputStream().readAllBytes().toList() }.toByteArray()
                    PartDataStream(
                        stream = byteArray.inputStream(),
                        size = byteArray.size.toLong(),
                        partSize = -1,
                    )
                }.doOnNext {
                    logger.info { "Updating ${it.size} bytes to $fileName" }
                }.handle<PartDataStream> { partDataStream, sink ->
                    try {
                        minioStorage.putObject(
                            bucketName, fileName, data.headers().contentType.toString(), partDataStream
                        )
                    } catch (e: ConnectException) {
                        sink.error(ImageServiceException("Failed to connect to MinIO", e))
                    }
                }.doOnNext {
                    logger.info { "Updated ${it.size} bytes in $fileName" }
                    logger.info { "Updated image can be viewed in '$minioImageUrl'" }
                }.thenReturn(
                    existingObject.copy(url = minioImageUrl)
                )
            }
    }

}
