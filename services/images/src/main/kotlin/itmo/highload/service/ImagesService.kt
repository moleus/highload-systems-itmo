package itmo.highload.service

import itmo.highload.minio.MinioConfig
import itmo.highload.minio.MinioStorage
import itmo.highload.model.ImageRef
import itmo.highload.repository.ImageRefRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono
import java.util.*

@Service
class ImagesService @Autowired constructor(
    private val imageRefRepository: ImageRefRepository,
    private val minioStorage: MinioStorage,
    private val minioConfig: MinioConfig
) {
    fun getImageById(id: Int): Mono<ImageRef> {
        return imageRefRepository.findById(id)
    }

    fun saveImage(data: MultipartFile): Mono<ImageRef> {
        val uuid = UUID.randomUUID().toString()
        val fileName = "$uuid/${data.originalFilename}"
        val imageRef = ImageRef(url = fileName)

        if (!minioStorage.isBucketExists(minioConfig.defaultBucketName)) {
            minioStorage.createBucket(minioConfig.defaultBucketName)
        }

        minioStorage.putObject(minioConfig.defaultBucketName, fileName, data.contentType ?: "", data.bytes)
        return imageRefRepository.save(imageRef)
    }
}
