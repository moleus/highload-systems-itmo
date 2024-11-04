package itmo.highload.minio

import java.io.InputStream

data class S3Object(
    val name: String,
)

data class PartDataStream(
    val stream: InputStream,
    val size: Long,
    val partSize: Long,
)

data class ObjectPutResult(
    val etag: String,
)

interface S3Storage {
    fun createBucket(bucketName: String)
    fun deleteBucket(bucketName: String)
    fun listBuckets(): List<String>
    fun isBucketExists(bucketName: String): Boolean

    fun putObject(bucketName: String, fileName: String, fileType: String, data: PartDataStream) : ObjectPutResult
    fun deleteObject(bucketName: String, objectName: String)
    fun listObjects(bucketName: String): List<S3Object>
}
