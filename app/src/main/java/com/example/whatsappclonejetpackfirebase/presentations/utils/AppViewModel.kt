package com.example.whatsappclonejetpackfirebase.presentations.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsappclonejetpackfirebase.domain.repository.ContactRepository
import com.example.whatsappclonejetpackfirebase.domain.model.ContactModel
import com.example.whatsappclonejetpackfirebase.domain.model.UserProfileModel
import com.example.whatsappclonejetpackfirebase.utils.ScreenRoutes
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val contactRepository: ContactRepository
): ViewModel(){

    var userProfileListener: ListenerRegistration? = null
    val userProfile: MutableState<UserProfileModel?> = mutableStateOf(null)
    val contacts: MutableState<ArrayList<ContactModel>> = mutableStateOf(arrayListOf())

    suspend fun queryContacts(){
        viewModelScope.launch {
            contacts.value.addAll(contactRepository.readContacts())
        }
    }// queryContacts

    suspend fun listenerUserProfile(idUser: String): UserProfileModel?{
        removeListenerUserProfile()
        var userProfileResult: UserProfileModel? = null
        val userRef = db.collection("users").document(idUser)
        userProfileListener = userRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                userProfileResult = UserProfileModel(
                    idUser = snapshot.data?.getValue("idUser").toString(),
                    phoneNumbers = snapshot.data?.getValue("phoneNumbers").toString(),
                    photoUrl = snapshot.data?.getValue("photoUrl").toString(),
                    username = snapshot.data?.getValue("username").toString(),
                    about = snapshot.data?.getValue("about").toString(),
                )
            }
            userProfile.value = userProfileResult
        }
        return userProfileResult
    } // end userListener

    suspend fun removeListenerUserProfile () {
        userProfileListener?.remove()
    }

}