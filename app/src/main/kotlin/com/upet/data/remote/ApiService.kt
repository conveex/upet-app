package com.upet.data.remote

import com.upet.data.remote.dto.ClientHomeResponse
import com.upet.data.remote.dto.CreatePetRequest
import com.upet.data.remote.dto.LoginRequest
import com.upet.data.remote.dto.LoginResponse
import com.upet.data.remote.dto.RegisterClientRequest
import com.upet.data.remote.dto.RegisterResponse
import com.upet.data.remote.dto.RegisterWalkerRequest
import com.upet.data.remote.dto.WalkResponse
import com.upet.data.remote.dto.WalkerHomeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // ---------- LOGIN ----------
    @POST("/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // ---------- CREATE PET ----------
    @POST("ruta-descripcion/create-pet")
    suspend fun createPet(
        @Body request: CreatePetRequest
    ): Response<Unit>

    // ---------- REGISTER CLIENT ----------
    @POST("/auth/register")
    suspend fun registerClient(
        @Body request: RegisterClientRequest
    ): Response<RegisterResponse>

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

    // ---------- REGISTER WALKER ----------
    @POST("auth/register/walker")
    suspend fun registerWalker(
        @Body request: RegisterWalkerRequest
    ): Response<RegisterResponse>

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