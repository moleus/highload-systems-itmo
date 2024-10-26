package itmo.highload.minio

data class S3Object(
    val name: String,
)

interface S3Storage {
    fun createBucket(bucketName: String)
    fun deleteBucket(bucketName: String)
    fun listBuckets(): List<String>
    fun isBucketExists(bucketName: String): Boolean

    fun putObject(bucketName: String, fileName: String, fileType: String, data: ByteArray)
    fun deleteObject(bucketName: String, objectName: String)
    fun listObjects(bucketName: String): List<S3Object>
}
