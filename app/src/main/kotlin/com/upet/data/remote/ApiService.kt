package com.upet.data.remote

import com.upet.data.remote.dto.AcceptWalkRequest
import com.upet.data.remote.dto.AcceptWalkResponse
import com.upet.data.remote.dto.AddPaymentMethodRequest
import com.upet.data.remote.dto.AddPaymentMethodResponse
import com.upet.data.remote.dto.AddPaymentMethodWalkerRequest
import com.upet.data.remote.dto.AddPaymentMethodWalkerResponse
import com.upet.data.remote.dto.CalculateRouteRequestDto
import com.upet.data.remote.dto.CalculateRouteResponse
import com.upet.data.remote.dto.CancelWalkResponse
import com.upet.data.remote.dto.ClientHomeResponse
import com.upet.data.remote.dto.ClientPaymentMethodsResponse
import com.upet.data.remote.dto.CreatePetRequest
import com.upet.data.remote.dto.CreatePetResponse
import com.upet.data.remote.dto.CreateWalkRequest
import com.upet.data.remote.dto.CreateWalkResponse
import com.upet.data.remote.dto.DeactivateAccountResponse
import com.upet.data.remote.dto.DeletePetResponse
import com.upet.data.remote.dto.EndWalkRequest
import com.upet.data.remote.dto.LoginRequest
import com.upet.data.remote.dto.LoginResponse
import com.upet.data.remote.dto.PaymentMethodCatalogDto
import com.upet.data.remote.dto.PetDetailResponse
import com.upet.data.remote.dto.PetsResponse
import com.upet.data.remote.dto.RegisterClientRequest
import com.upet.data.remote.dto.RegisterResponse
import com.upet.data.remote.dto.RegisterWalkerRequest
import com.upet.data.remote.dto.StartWalkRequest
import com.upet.data.remote.dto.UpdateClientRequest
import com.upet.data.remote.dto.UpdateFcmTokenRequest
import com.upet.data.remote.dto.UpdateFcmTokenResponse
import com.upet.data.remote.dto.UpdatePetRequest
import com.upet.data.remote.dto.UpdatePetResponse
import com.upet.data.remote.dto.UpdatePhotoRequest
import com.upet.data.remote.dto.UpdatePhotoResponse
import com.upet.data.remote.dto.UpdateWalkerPhotoRequest
import com.upet.data.remote.dto.UpdateWalkerPhotoResponse
import com.upet.data.remote.dto.UpdateWalkerRequest
import com.upet.data.remote.dto.UserProfileResponse
import com.upet.data.remote.dto.WalkDetailResponse
import com.upet.data.remote.dto.WalkListResponse
import com.upet.data.remote.dto.WalkResponse
import com.upet.data.remote.dto.WalkerHomeResponse
import com.upet.data.remote.dto.WalkerPaymentMethodsResponse
import com.upet.data.remote.dto.WalkerProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // ---------- LOGIN ----------
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // ---------- REGISTER CLIENT ----------
    @POST("auth/register")
    suspend fun registerClient(
        @Body request: RegisterClientRequest
    ): Response<RegisterResponse>

    // ---------- CLIENT HOME (Placeholder) ----------
    @GET("ruta-descripcion/client/{clientId}/home")
    suspend fun getClientHome(
        @Path("clientId") clientId: String
    ): Response<ClientHomeResponse>

    // ---------- REGISTER WALKER ----------
    @POST("auth/register/walker")
    suspend fun registerWalker(
        @Body request: RegisterWalkerRequest
    ): Response<RegisterResponse>

    // ---------- WALKER HOME (Placeholder) ----------
    @GET("ruta-descripcion/walker/{walkerId}/home")
    suspend fun getWalkerHome(
        @Path("walkerId") walkerId: String
    ): Response<WalkerHomeResponse>

    // ---------- PERFILES ----------
    @GET("api/v1/users/me")
    suspend fun getMyProfile(): Response<UserProfileResponse>

    @GET("api/v1/walkers/me")
    suspend fun getMyWalkerProfile(): Response<WalkerProfileResponse>

    @PUT("api/v1/users/me")
    suspend fun updateProfileClient(
        @Body request: UpdateClientRequest
    ): Response<Unit>

    @PUT("api/v1/walkers/me")
    suspend fun updateProfileWalker(
        @Body request: UpdateWalkerRequest
    ): Response<WalkerProfileResponse>

    @PUT("api/v1/users/me/photo")
    suspend fun updateUserPhoto(
        @Body request: UpdatePhotoRequest
    ): Response<UpdatePhotoResponse>

    @PUT("api/v1/walkers/me/photo")
    suspend fun updateWalkerPhoto(
        @Body request: UpdateWalkerPhotoRequest
    ): Response<UpdateWalkerPhotoResponse>

    //------------- MASCOTAS -------------
    @POST("api/v1/pets")
    suspend fun createPet(
        @Body request: CreatePetRequest
    ): Response<CreatePetResponse>

    @GET("api/v1/pets")
    suspend fun getMyPets(): Response<PetsResponse>

    @GET("api/v1/pets/{id}")
    suspend fun getPetById(
        @Path("id") petId: String
    ): Response<PetDetailResponse>

    @PUT("api/v1/pets/{id}")
    suspend fun updatePet(
        @Path("id") petId: String,
        @Body request: UpdatePetRequest
    ): Response<UpdatePetResponse>

    @DELETE("api/v1/pets/{id}")
    suspend fun deletePet(
        @Path("id") petId: String
    ): Response<DeletePetResponse>

    //-------------- METODOS DE PAGO -------------
    @GET("payment-methods")
    suspend fun getPaymentMethodsCatalog(): Response<List<PaymentMethodCatalogDto>>

    @GET("api/v1/client/payment-methods")
    suspend fun getClientPaymentMethods(): Response<ClientPaymentMethodsResponse>

    @DELETE("api/v1/client/payment-methods/{methodId}")
    suspend fun deleteClientPaymentMethod(
        @Path("methodId") methodId: String
    ): Response<ClientPaymentMethodsResponse>

    @POST("api/v1/client/payment-methods")
    suspend fun addPaymentMethod(
        @Body request: AddPaymentMethodRequest
    ): Response<AddPaymentMethodResponse>

    @GET("api/v1/walker/payment-methods")
    suspend fun getWalkerPaymentMethods(): Response<WalkerPaymentMethodsResponse>

    @DELETE("api/v1/walker/payment-methods/{methodId}")
    suspend fun deleteWalkerPaymentMethod(
        @Path("methodId") methodId: String
    ): Response<WalkerPaymentMethodsResponse>

    @POST("api/v1/walker/payment-methods")
    suspend fun createWalkerPaymentMethod(
        @Body request: AddPaymentMethodWalkerRequest
    ): Response<AddPaymentMethodWalkerResponse>

    //------------- PASEOS -------------
    @POST("api/v1/walks/calculate-route")
    suspend fun calculateRoute(
        @Body request: CalculateRouteRequestDto
    ): Response<CalculateRouteResponse>

    @POST("api/v1/walks")
    suspend fun createWalk(
        @Body request: CreateWalkRequest
    ): Response<CreateWalkResponse>

    @GET("api/v1/walks/pending")
    suspend fun getClientPendingWalks(): Response<WalkListResponse>

    @GET("api/v1/walks/active")
    suspend fun getClientActiveWalks(): Response<WalkListResponse>

    @GET("api/v1/walker/walks/active")
    suspend fun getWalkerActiveWalks(): Response<WalkListResponse>

    @GET("api/v1/walks/{id}")
    suspend fun getWalkDetail(
        @Path("id") walkId: String
    ): Response<WalkDetailResponse>

    @DELETE("api/v1/walks/{id}")
    suspend fun cancelWalk(
        @Path("id") walkId: String
    ): Response<CancelWalkResponse>

    @GET("api/v1/walks/available")
    suspend fun getAvailableWalks(): Response<WalkListResponse>

    @GET("api/v1/walks/available/{id}")
    suspend fun getAvailableWalkDetail(
        @Path("id") walkId: String
    ): Response<WalkDetailResponse>

    @POST("api/v1/walks/{id}/accept")
    suspend fun acceptWalk(
        @Path("id") walkId: String,
        @Body request: AcceptWalkRequest
    ): Response<AcceptWalkResponse>

    @POST("api/v1/walks/{walkId}/start")
    suspend fun startWalk(
        @Path("walkId") walkId: String,
        @Body request: StartWalkRequest
    ): Response<WalkDetailResponse>

    @POST("api/v1/walks/{walkId}/end")
    suspend fun endWalk(
        @Path("walkId") walkId: String,
        @Body request: EndWalkRequest
    ): Response<WalkDetailResponse>

    // ------------ VARIOS ------------
    @DELETE("api/v1/users/me")
    suspend fun deactivateAccount(): Response<DeactivateAccountResponse>

    @POST("api/v1/users/fcm-token")
    suspend fun updateFcmToken(
        @Body request: UpdateFcmTokenRequest
    ): Response<UpdateFcmTokenResponse>
}
