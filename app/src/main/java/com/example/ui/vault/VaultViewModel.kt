package com.example.ui.vault

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VaultPhoto
import com.example.data.VaultRepository
import com.example.data.VaultSecurityManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class VaultUiState(
    val photos: List<VaultPhoto> = emptyList(),
    val selectedPhotoIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val viewingPhoto: VaultPhoto? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val showPostImportDialog: Boolean = false,
    val lastImportedPhotos: List<VaultPhoto> = emptyList(),
    val showChangePasscodeDialog: Boolean = false,
    val currentCameraFile: File? = null,
    val cameraUri: Uri? = null,
    val statusMessage: String? = null
)

class VaultViewModel(
    private val repository: VaultRepository,
    private val securityManager: VaultSecurityManager,
    private val onLockVault: () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())

    val state: StateFlow<VaultUiState> = combine(
        _uiState,
        repository.allPhotos
    ) { currentUi, photoList ->
        currentUi.copy(photos = photoList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultUiState()
    )

    fun importPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val newlyImported = mutableListOf<VaultPhoto>()
            for (uri in uris) {
                val photo = repository.importPhoto(uri)
                if (photo != null) {
                    newlyImported.add(photo)
                }
            }
            if (newlyImported.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        lastImportedPhotos = newlyImported,
                        showPostImportDialog = true,
                        statusMessage = "Secured ${newlyImported.size} photo(s) in Vault"
                    )
                }
            }
        }
    }

    fun prepareCameraCapture(): Uri {
        val (file, uri) = repository.prepareCameraCapture()
        _uiState.update {
            it.copy(
                currentCameraFile = file,
                cameraUri = uri
            )
        }
        return uri
    }

    fun onCameraCaptureSuccess() {
        val file = _uiState.value.currentCameraFile ?: return
        viewModelScope.launch {
            if (file.exists() && file.length() > 0) {
                repository.saveCameraCapture(file)
                _uiState.update {
                    it.copy(
                        currentCameraFile = null,
                        cameraUri = null,
                        statusMessage = "Secret photo captured and secured"
                    )
                }
            }
        }
    }

    fun deleteOriginalsFromDevice(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        onFinished: () -> Unit
    ) {
        val imported = _uiState.value.lastImportedPhotos
        viewModelScope.launch {
            imported.forEach { photo ->
                photo.originalUri?.let { uriStr ->
                    repository.getStorageManager().deleteOriginalFromPhone(
                        uriString = uriStr,
                        deleteSenderLauncher = launcher,
                        onSuccess = {
                            viewModelScope.launch {
                                repository.markOriginalDeleted(photo.id)
                            }
                        },
                        onError = { /* Ignored or handled */ }
                    )
                }
            }
            _uiState.update {
                it.copy(showPostImportDialog = false)
            }
            onFinished()
        }
    }

    fun dismissPostImportDialog() {
        _uiState.update { it.copy(showPostImportDialog = false) }
    }

    fun openPhotoViewer(photo: VaultPhoto) {
        _uiState.update { it.copy(viewingPhoto = photo) }
    }

    fun closePhotoViewer() {
        _uiState.update { it.copy(viewingPhoto = null) }
    }

    fun togglePhotoSelection(photoId: Long) {
        _uiState.update { curr ->
            val updated = curr.selectedPhotoIds.toMutableSet()
            if (updated.contains(photoId)) {
                updated.remove(photoId)
            } else {
                updated.add(photoId)
            }
            curr.copy(
                selectedPhotoIds = updated,
                isSelectionMode = updated.isNotEmpty()
            )
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedPhotoIds = emptySet(),
                isSelectionMode = false
            )
        }
    }

    fun deleteSelectedPhotos() {
        val selectedIds = _uiState.value.selectedPhotoIds
        val currentPhotos = _uiState.value.photos
        val toDelete = currentPhotos.filter { it.id in selectedIds }

        viewModelScope.launch {
            repository.deletePhotos(toDelete)
            _uiState.update {
                it.copy(
                    selectedPhotoIds = emptySet(),
                    isSelectionMode = false,
                    showDeleteConfirmDialog = false,
                    statusMessage = "Deleted ${toDelete.size} photo(s)"
                )
            }
        }
    }

    fun deleteViewingPhoto() {
        val photo = _uiState.value.viewingPhoto ?: return
        viewModelScope.launch {
            repository.deletePhoto(photo)
            _uiState.update {
                it.copy(
                    viewingPhoto = null,
                    statusMessage = "Photo removed from Vault"
                )
            }
        }
    }

    fun changePasscode(newPasscode: String, recovery: String?) {
        securityManager.setPasscode(newPasscode, recovery)
        _uiState.update {
            it.copy(
                showChangePasscodeDialog = false,
                statusMessage = "Passcode updated successfully"
            )
        }
    }

    fun openChangePasscodeDialog() {
        _uiState.update { it.copy(showChangePasscodeDialog = true) }
    }

    fun dismissChangePasscodeDialog() {
        _uiState.update { it.copy(showChangePasscodeDialog = false) }
    }

    fun lockVault() {
        securityManager.lock()
        onLockVault()
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
