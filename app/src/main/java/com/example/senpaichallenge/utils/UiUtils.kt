package com.example.senpaichallenge.utils

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar

object UiUtils {
    fun toggleLoadingButton(
        button: Button,
        progressBar: ProgressBar,
        isLoading: Boolean,
        defaultText: String
    ) {
        if (isLoading) {
            button.isEnabled = false
            button.text = ""
            progressBar.visibility = View.VISIBLE
        } else {
            button.isEnabled = true
            button.text = defaultText
            progressBar.visibility = View.GONE
        }
    }

    fun toggleLoadingGoogle(
        googleLayout: LinearLayout,
        progressBar: ProgressBar,
        isLoading: Boolean
    ) {
        if (isLoading) {
            googleLayout.isEnabled = false
            progressBar.visibility = View.VISIBLE
        } else {
            googleLayout.isEnabled = true
            progressBar.visibility = View.GONE
        }
    }
}
