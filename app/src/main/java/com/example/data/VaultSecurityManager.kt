package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class VaultSecurityManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("calculator_vault_prefs", Context.MODE_PRIVATE)

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun isPasscodeSet(): Boolean {
        return prefs.contains(KEY_PASSCODE_HASH)
    }

    fun setPasscode(passcode: String, securityAnswer: String? = null): Boolean {
        if (passcode.isBlank()) return false
        val hash = hashString(passcode)
        val editor = prefs.edit().putString(KEY_PASSCODE_HASH, hash)
        if (!securityAnswer.isNullOrBlank()) {
            editor.putString(KEY_RECOVERY_HASH, hashString(securityAnswer.trim().lowercase()))
        }
        editor.apply()
        return true
    }

    fun verifyPasscode(enteredPasscode: String): Boolean {
        val storedHash = prefs.getString(KEY_PASSCODE_HASH, null) ?: return false
        val enteredHash = hashString(enteredPasscode)
        val matches = (storedHash == enteredHash)
        if (matches) {
            _isUnlocked.value = true
        }
        return matches
    }

    fun unlockDirectly() {
        _isUnlocked.value = true
    }

    fun lock() {
        _isUnlocked.value = false
    }

    fun resetPasscodeWithRecovery(answer: String, newPasscode: String): Boolean {
        val storedRecovery = prefs.getString(KEY_RECOVERY_HASH, null) ?: return false
        if (hashString(answer.trim().lowercase()) == storedRecovery) {
            return setPasscode(newPasscode)
        }
        return false
    }

    fun hasRecoveryAnswer(): Boolean {
        return prefs.contains(KEY_RECOVERY_HASH)
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_PASSCODE_HASH = "vault_passcode_hash"
        private const val KEY_RECOVERY_HASH = "vault_recovery_hash"
    }
}
