package com.niccher.my_mpesa_analyzer.fragments.Settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.helpers.AppPrefs

class Frag_Settings : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.frag_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchDarkTheme = view.findViewById<SwitchMaterial>(R.id.switch_dark_theme)
        val switchBiometric = view.findViewById<SwitchMaterial>(R.id.switch_biometric)

        // Load current preferences
        switchDarkTheme.isChecked = AppPrefs.isDarkThemeEnabled(requireContext())
        switchBiometric.isChecked = AppPrefs.isBiometricEnabled(requireContext())

        // Dark Theme toggle
        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            AppPrefs.setDarkThemeEnabled(requireContext(), isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Biometric Lock toggle
        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            AppPrefs.setBiometricEnabled(requireContext(), isChecked)
        }
    }
}
