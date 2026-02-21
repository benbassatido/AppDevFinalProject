package com.example.finalproject.ui.common

import android.content.Context
import android.widget.Toast

object ErrorHandler {
    
    // displays an error message using a toast notification
    fun showError(
        context: Context?,
        message: String?,
        defaultMessage: String = "An error occurred"
    ) {
        if (context == null) return
        
        val msg = when {
            !message.isNullOrBlank() -> message
            else -> defaultMessage
        }
        
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
    
    // displays a success message using a toast notification
    fun showSuccess(context: Context?, message: String) {
        if (context == null) return
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
