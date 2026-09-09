package com.niccher.mpesa_analyzer_app.fragments.Info

import com.niccher.mpesa_analyzer_app.R
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Info_Data(val name_title: String, val name_desc: String, var name_icon: Int = 0)

class InfoViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is info Fragment"
    }
    val text: LiveData<String> = _text

    private val _items = MutableLiveData<List<Info_Data>>()
    val items: LiveData<List<Info_Data>> = _items

    init {
        loadItems()
    }

    private fun loadItems() {
        val itemList = listOf(
            Info_Data("Profile", "Information about my account", R.drawable.ic_person),
            Info_Data("App Info", "Info like version, permissions.", R.drawable.ic_info),
            Info_Data("App Credits", "The Libraries and other open source resources used in creating the app", R.drawable.ic_info)
        )
        _items.value = itemList
    }
}