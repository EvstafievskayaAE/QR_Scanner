package com.example.qrcodescanner

import android.util.Patterns
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val linkModel = LinkModel(Url = "First initialization")

    fun updateUrl(Url: String) {
        linkModel.Url = Url
    }

    fun getUrl(): String {
        return linkModel.Url
    }

    fun isValidUrl(): Boolean {
        return Patterns.WEB_URL.matcher(linkModel.Url).matches()
    }
}
