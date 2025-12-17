package com.cnvx.upet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class UPetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // No necesitamos BuildConfig explícito para esto en la mayoría de los casos si es debuggable
        // Pero para evitar el error de referencia, podemos usar la clase generada de nuestro paquete
        if (com.cnvx.upet.BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
