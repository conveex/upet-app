package com.upet.data.remote.api

import com.upet.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // ---------- LOGIN ----------
    @POST("ruta-descripcion/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}