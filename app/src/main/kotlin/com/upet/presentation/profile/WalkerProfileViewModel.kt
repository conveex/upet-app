package com.upet.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.local.datastore.TokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalkerProfileViewModel @Inject constructor(
    val tokenStore: TokenDataStore
) : ViewModel() {

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            tokenStore.clearSession()
            onLoggedOut()
        }
    }
}
