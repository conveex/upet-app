package com.upet.data.remote

import com.upet.data.remote.dto.AddPaymentMethodRequest
import com.upet.data.remote.dto.ClientHomeResponse
import com.upet.data.remote.dto.ClientPaymentMethodsResponse
import com.upet.data.remote.dto.CreatePetRequest
import com.upet.data.remote.dto.LoginRequest
import com.upet.data.remote.dto.LoginResponse
import com.upet.data.remote.dto.RegisterClientRequest
import com.upet.data.remote.dto.RegisterResponse
import com.upet.data.remote.dto.RegisterWalkerRequest
import com.upet.data.remote.dto.UpdateClientRequest
import com.upet.data.remote.dto.UpdatePhotoRequest
import com.upet.data.remote.dto.UpdatePhotoResponse
import com.upet.data.remote.dto.UpdateWalkerPhotoRequest
import com.upet.data.remote.dto.UpdateWalkerPhotoResponse
import com.upet.data.remote.dto.UpdateWalkerRequest
import com.upet.data.remote.dto.UserProfileResponse
import com.upet.data.remote.dto.WalkResponse
import com.upet.data.remote.dto.WalkerHomeResponse
import com.upet.data.remote.dto.WalkerProfileResponse
import com.upet.data.remote.dto.CreatePetResponse
import com.upet.data.remote.dto.DeactivateAccountResponse
import com.upet.data.remote.dto.DeletePetResponse
import com.upet.data.remote.dto.PetDetailResponse
import com.upet.data.remote.dto.PetsResponse
import com.upet.data.remote.dto.UpdatePetRequest
import com.upet.data.remote.dto.UpdatePetResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // ---------- LOGIN ----------
    @POST("/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

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

    // ---------- OBTENER PERFIL CLIENTE -----------
    @GET("api/v1/users/me")
    suspend fun getMyProfile(): retrofit2.Response<UserProfileResponse>

    // ---------- OBTENER PERFIL PASEADOR -----------
    @GET("api/v1/walkers/me")
    suspend fun getMyWalkerProfile(): Response<WalkerProfileResponse>

    //----------- ACTUALIZAR PERFIL CLIENTE ---------
    @PUT("/api/v1/users/me")
    suspend fun  updateProfileClient(
        @Body request: UpdateClientRequest
    ): Response<Unit>

    //----------- ACTUALIZAR PERFIL PASEADOR ---------
    @PUT("/api/v1/walkers/me")
    suspend fun  updateProfileWalker(
        @Body request: UpdateWalkerRequest
    ): Response<Unit>

    //------------ FOTO CLIIENTE -------------
    @PUT("/api/v1/users/me/photo")
    suspend fun updateUserPhoto(
        @Body request: UpdatePhotoRequest
    ): Response<UpdatePhotoResponse>

    //------------ FOTO PASEADOR -------------
    @PUT("/api/v1/walkers/me/photo")
    suspend fun updateWalkerPhoto(
        @Body request: UpdateWalkerPhotoRequest
    ): Response<UpdateWalkerPhotoResponse>

    //------------- CREAR MASCOTA -------------
    @POST("/api/v1/pets")
    suspend fun createPet(
        @Body request: CreatePetRequest
    ): Response<CreatePetResponse>

    //------------- LISTAR MASCOTAS -------------
    @GET("/api/v1/pets")
    suspend fun getMyPets(): Response<PetsResponse>

    @GET("/api/v1/pets/{id}")
    suspend fun getPetById(
        @Path("id") petId: String
    ): Response<PetDetailResponse>

    //------------- ACTUALIZAR MASCOTA ----------
    @PUT("/api/v1/pets/{id}")
    suspend fun updatePet(
        @Path("id") petId: String,
        @Body request: UpdatePetRequest
    ): Response<UpdatePetResponse>

    //------------- ELIMINAR CUENTA ------------
    @DELETE("/api/v1/users/me")
    suspend fun deactivateAccount(): Response<DeactivateAccountResponse>

    //------------- ELIMINAR MASCOTA ------------
    @DELETE("/api/v1/pets/{id}")
    suspend fun deletePet(
        @Path("id") petId: String
    ): Response<DeletePetResponse>

    //------------- MDP CLIENTE -----------------
    @POST("/api/v1/client/payment-methods")
    suspend fun addPaymentMethod(
        @Body request: AddPaymentMethodRequest
    ): Response<ClientPaymentMethodsResponse>

    //-------------- METODOS DE PAGO -------------
    @GET("/api/v1/client/payment-methods")
    suspend fun getClientPaymentMethods(): Response<ClientPaymentMethodsResponse>

}