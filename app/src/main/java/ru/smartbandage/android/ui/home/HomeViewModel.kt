package ru.smartbandage.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _url = MutableLiveData<String>()
    val url: LiveData<String> = _url

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        _url.value = "https://appassets.androidplatform.net/assets/index.html"
        _isLoading.value = true
    }

    fun onPageStarted() {
        _isLoading.value = true

    }

    fun onPageFinished() {
        _isLoading.value = false
    }
}
