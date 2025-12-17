package com.upet.data.repository

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.EndWalkRequest
import com.upet.data.remote.dto.StartWalkRequest
import com.upet.data.remote.dto.WalkDetailDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class TrackingLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val timestampMillis: Long = 0
)

data class TrackingMeta(
    val active: Boolean = false,
    val polylineEncoded: String = ""
)

@Singleton
class WalkTrackingRepository @Inject constructor(
    private val api: ApiService,
    private val firestore: FirebaseFirestore
) {

    suspend fun startWalk(walkId: String, lat: Double, lng: Double): Result<WalkDetailDto> {
        return try {
            val response = api.startWalk(walkId, StartWalkRequest(lat, lng))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.walk)
            } else {
                Result.failure(Exception("Error al iniciar paseo: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun endWalk(walkId: String, lat: Double, lng: Double): Result<WalkDetailDto> {
        return try {
            val response = api.endWalk(walkId, EndWalkRequest(lat, lng))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.walk)
            } else {
                Result.failure(Exception("Error al finalizar paseo: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeWalkerLocation(walkId: String): Flow<LatLng?> {
        return firestore.collection("tracking")
            .document(walkId)
            .collection("latest")
            .document("latest")
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    val lat = snapshot.getDouble("lat")
                    val lng = snapshot.getDouble("lng")
                    if (lat != null && lng != null) {
                        LatLng(lat, lng)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
    }

    fun observeTrackingMeta(walkId: String): Flow<TrackingMeta> {
        return firestore.collection("tracking")
            .document(walkId)
            .collection("meta")
            .document("meta")
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(TrackingMeta::class.java) ?: TrackingMeta()
                } else {
                    TrackingMeta()
                }
            }
    }
}
