package etf.nestor.preference.data.network

data class PartijaUpload(
    val password: String,
    val name: String?,
    val savedAt: Long,
    val json: String
)

data class PartijaSummary(
    val id: Int,
    val name: String?
)

data class PartijaDownload(
    val name: String?,
    val savedAt: Long,
    val json: String
)
