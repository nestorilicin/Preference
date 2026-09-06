package etf.nestor.preference.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

const val BASE_URL = "http://192.168.0.20:8080/" // moja trenutna lokalna ipv4 adresa

interface PartijaApi {

    @POST("partije")
    suspend fun upload(@Body partija: PartijaUpload): PartijaSummary

    @GET("partije")
    suspend fun listPartije(): List<PartijaSummary>

    @GET("partije/{id}")
    suspend fun download(@Path("id") id: Int, @Query("password") password: String): PartijaDownload
}
