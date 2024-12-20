package itmo.highload.minio

import io.minio.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MinioStorage @Autowired constructor(
    private val minioClient: MinioClient,
) : S3Storage {
    override fun isBucketExists(bucketName: String): Boolean {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())
    }

    override fun createBucket(bucketName: String) {
        minioClient.makeBucket(
            MakeBucketArgs.builder().bucket(bucketName).build()
        )
    }

    override fun deleteBucket(bucketName: String) {
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build())
    }

    override fun listBuckets(): List<String> {
        return minioClient.listBuckets().map { it.name() }
    }

    override fun putObject(
        bucketName: String, fileName: String, fileType: String, data: PartDataStream
    ): ObjectPutResult {
        val writeResponse = minioClient.putObject(
            PutObjectArgs.builder().bucket(bucketName).`object`(fileName).contentType(fileType)
                .stream(data.stream, data.size, data.partSize).build()
        )
        return ObjectPutResult(writeResponse.etag())
    }

    override fun deleteObject(bucketName: String, objectName: String) {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).`object`(objectName).build())
    }

    override fun listObjects(bucketName: String): List<S3Object> =
        minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build()).map {
            S3Object(
                name = it.get().objectName(),
            )
        }
}
