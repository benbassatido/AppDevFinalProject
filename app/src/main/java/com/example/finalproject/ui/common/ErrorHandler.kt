package com.example.finalproject.ui.common

import android.content.Context
import android.widget.Toast

object ErrorHandler {
    
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
    
    fun showSuccess(context: Context?, message: String) {
        if (context == null) return
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    fun showInfo(context: Context?, message: String) {
        if (context == null) return
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
