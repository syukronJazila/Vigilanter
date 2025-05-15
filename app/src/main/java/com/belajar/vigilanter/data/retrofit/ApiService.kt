package com.belajar.vigilanter.data.retrofit

import com.belajar.vigilanter.data.response.CreatedUserResponse
import com.belajar.vigilanter.data.response.GetLaporResponse
import com.belajar.vigilanter.data.response.HapusLaporResponse
import com.belajar.vigilanter.data.response.LaporResponse
import com.belajar.vigilanter.data.response.LaporanVideoResponse
import com.belajar.vigilanter.data.response.LoginResponse
import com.belajar.vigilanter.data.response.videoResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("users.php")
    fun createUser(
        @Field("nama_depan") nama_d: String,
        @Field("nama_belakang") nama_b: String,
        @Field("jenis_kelamin") jenis_kelamin: String,
        @Field("tanggal_lahir") tanggal_lahir: String,
        @Field("nik") nik: String,
        @Field("no_telepon") no_telepon: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<CreatedUserResponse>

    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("getLaporan.php")
    fun getLaporan(
        @Field("user_id") user_id: Int,
    ): Call<GetLaporResponse>

    @FormUrlEncoded
    @POST("hapusLaporan.php")
    fun hapusLaporan(
        @Field("id") id: Int,
        @Field("status") status: String,
    ): Call<HapusLaporResponse>

    @Multipart
    @POST("videoLaporanUpload.php")
    fun submitLaporanWithVideo(
        @Part("user_id") userId: RequestBody,
        @Part("jenis_laporan") jenisLaporan: RequestBody,
        @Part("nama_kejahatan") namaKejahatan: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part("status") status: RequestBody,
        @Part("waktu") waktu: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("tempat") tempat: RequestBody?,
        @Part("real_path") realPath: RequestBody?,
        @Part video: MultipartBody.Part
    ): Call<LaporanVideoResponse>

//    @FormUrlEncoded
//    @POST("laporan.php")
//    fun lapor(
//        @Field("user_id") user_id: Int,
//        @Field("jenis_laporan") jenis_laporan: String,
//        @Field("nama_kejahatan") nama_kejahatan: String,
//        @Field("deskripsi") deskripsi: String,
//        @Field("status") status: String,
//        @Field("waktu") waktu: String,
//        @Field("latitude") latitude: Double,
//        @Field("longitude") longitude: Double,
//        @Field("tempat") tempat: String?,
//    ): Call<LaporResponse>

//    @Multipart
//    @POST("uploadVideo.php")
//    fun uploadVideo(
//        @Part video: MultipartBody.Part
//    ): Call<videoResponse>

}