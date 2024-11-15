package com.pavlov.MyShadowGallery.util

import android.app.Activity
import android.widget.Toast
import com.pavlov.MyShadowGallery.R
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

fun Activity.showManualKeyInputDialog(thisTitle: String, onKeyEntered: (String) -> Unit) {
    val inputEditText = EditText(this)
    val builder = AlertDialog.Builder(this)

    val titleTextView = TextView(this)
    titleTextView.text = thisTitle
    titleTextView.setPadding(10, 10, 10, 10) // Add padding if needed
    titleTextView.gravity = Gravity.CENTER

    builder.setCustomTitle(titleTextView)
    builder.setView(inputEditText)

    builder.setPositiveButton("✔️") { _, _ ->
        val enteredKey = inputEditText.text.toString()
        onKeyEntered(enteredKey)
    }

    builder.setNegativeButton("❌") { dialog, _ ->
        dialog.cancel()
    }

    builder.show()
}

fun Activity.showYesNoDialog(thisTitle: String, onDeleteConfirmed: () -> Unit) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(thisTitle)
    val titleTextView = TextView(this)
    titleTextView.text = thisTitle

    val digits = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9")

    // Устанавливаем цифры как элементы списка
    builder.setItems(digits) { _, which ->
        val selectedDigit = digits[which]

        // Проверяем, является ли выбранная цифра утвердительной
        if (selectedDigit == "8") {
            onDeleteConfirmed()
        } else {
            // В случае, если выбрана не та цифра, просто отменяем диалог
            builder.create().cancel()
        }
    }

    builder.show()
}

fun Activity.showLoadingIndicator() {
}

fun Activity.hideLoadingIndicator(cornerLeft: Boolean) {
}


fun Activity.startSmallLoadingIndicator() {
    showToast(getString(R.string.background_process))
}

fun Activity.stopSmallLoadingIndicator() {
}

fun Activity.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}


