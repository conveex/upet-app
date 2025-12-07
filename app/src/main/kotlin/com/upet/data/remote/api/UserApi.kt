package com.upet.data.remote.api

import com.upet.data.remote.dto.RegisterClientRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {


    // ---------- REGISTER CLIENT ----------
    @POST("ruta-descripcion/register-client")
    suspend fun registerClient(
        @Body request: RegisterClientRequest
    ): Response<Unit>

    // ---------- CLIENT HOME ----------
    @GET("ruta-descripcion/client/{clientId}/home")
    suspend fun getClientHome(
        @Path("clientId") clientId: String
    ): Response<ClientHomeResponse>

    // ---------- PASEOS ACTIVOS (Client o Walker) ----------
    @GET("ruta-descripcion/user/{userId}/active-walks")
    suspend fun getActiveWalks(
        @Path("userId") userId: String
    ): Response<List<WalkResponse>>
}

