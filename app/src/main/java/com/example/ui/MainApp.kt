package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.CalculatorViewModel
import com.example.data.VaultDatabase
import com.example.data.VaultRepository
import com.example.data.VaultSecurityManager
import com.example.data.VaultStorageManager
import com.example.ui.calculator.CalculatorScreen
import com.example.ui.vault.VaultGalleryScreen
import com.example.ui.vault.VaultViewModel

@Composable
fun MainApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val securityManager = remember { VaultSecurityManager(context) }
    val storageManager = remember { VaultStorageManager(context) }
    val database = remember { VaultDatabase.getInstance(context) }
    val repository = remember { VaultRepository(database.vaultPhotoDao(), storageManager) }

    val isUnlocked by securityManager.isUnlocked.collectAsStateWithLifecycle()

    val calculatorViewModel = remember {
        CalculatorViewModel(
            securityManager = securityManager,
            onUnlockVault = {
                securityManager.unlockDirectly()
            }
        )
    }

    val vaultViewModel = remember {
        VaultViewModel(
            repository = repository,
            securityManager = securityManager,
            onLockVault = {
                securityManager.lock()
            }
        )
    }

    // Auto-lock when the app goes to background / stops
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                securityManager.lock()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AnimatedContent(
        targetState = isUnlocked,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "AppScreenTransition",
        modifier = Modifier.fillMaxSize()
    ) { unlocked ->
        if (unlocked) {
            VaultGalleryScreen(
                viewModel = vaultViewModel,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CalculatorScreen(
                viewModel = calculatorViewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
