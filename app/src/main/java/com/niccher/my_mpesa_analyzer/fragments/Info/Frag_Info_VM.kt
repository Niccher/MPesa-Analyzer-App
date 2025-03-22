package com.niccher.my_mpesa_analyzer.fragments.Info

import com.niccher.my_mpesa_analyzer.R
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Info_Data(val name_title: String, val name_desc: String, var name_icon: Int = 0)

class Frag_Info_VM : ViewModel() {

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
        // Simulate loading data (e.g., from a network request or database)
        val itemList = listOf(
            Info_Data("Profile", "Information about my account", R.mipmap.app_profile),
            Info_Data("App Info", "Info like version, permissions.", R.mipmap.app_info),
            Info_Data("App Credits", "The Libraries and other open source resources used in creating the app", R.mipmap.app_credits)
        )
        _items.value = itemList

    }
}