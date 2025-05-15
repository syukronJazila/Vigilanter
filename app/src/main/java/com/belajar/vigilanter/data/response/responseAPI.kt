package com.belajar.vigilanter.data.response

import com.google.gson.annotations.SerializedName

data class CreatedUserResponse(

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)

data class LoginResponse(

    @field:SerializedName("status")
    val status: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("username")
    val username: String,

    @field:SerializedName("user_id")
    val user_id: Int
)

data class GetLaporResponse(

    @field:SerializedName("diajukan")
    val diajukan: List<Laporan>,

    @field:SerializedName("selesai")
    val selesai: List<Laporan>,

    @field:SerializedName("message")
    val message: String,

)

data class HapusLaporResponse(

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    )

data class Laporan(

    @field:SerializedName("jenis_laporan")
    val jenis_laporan: String,

    @field:SerializedName("nama_kejahatan")
    val nama_kejahatan: String,

    @field:SerializedName("deskripsi")
    val deskripsi: String,

    @field:SerializedName("status")
    val status: String,

    @field:SerializedName("waktu")
    val waktu: String,

    @field:SerializedName("latitude")
    val latitude: Double,

    @field:SerializedName("longitude")
    val longitude: Double,

    @field:SerializedName("tempat")
    val tempat: String,

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("video_url")
    val video_url: String,
)

data class LaporanVideoResponse(

    @field:SerializedName("status")
    val status: String,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("file_path")
    val filePath: String,

)

data class LaporResponse(

    @field:SerializedName("isTerkirim")
    val status: Boolean,

    @field:SerializedName("message")
    val message: String,
)

data class videoResponse(

    @field:SerializedName("status")
    val error: String,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("file_path")
    val filePath: String,

)