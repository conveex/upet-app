package com.upet.data.remote.api

import com.upet.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // ---------- REGISTER WALKER ----------
    @POST("ruta-descripcion/register-walker")
    suspend fun registerWalker(
        @Body request: RegisterWalkerRequest
    ): Response<Unit>

    // ---------- WALKER HOME ----------
    @GET("ruta-descripcion/walker/{walkerId}/home")
    suspend fun getWalkerHome(
        @Path("walkerId") walkerId: String
    ): Response<WalkerHomeResponse>

    // ---------- PASEOS PENDIENTES (Walker) ----------
    @GET("ruta-descripcion/walker/{walkerId}/pending-walks")
    suspend fun getPendingWalks(
        @Path("walkerId") walkerId: String
    ): Response<List<WalkResponse>>
}