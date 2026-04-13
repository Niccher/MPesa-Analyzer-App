package com.niccher.my_mpesa_analyzer

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.niccher.my_mpesa_analyzer.databinding.ActivityMainBinding
import com.niccher.my_mpesa_analyzer.helpers.AppPrefs
import com.niccher.my_mpesa_analyzer.R

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var perm_sms: Int = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved theme preference before inflation
        if (AppPrefs.isDarkThemeEnabled(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register the toolbar as the action bar so the options menu renders
        setSupportActionBar(binding.toolbar)

        // Hardening: Prevent screenshots and peeking in recent apps
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_bottom)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navi_home, R.id.navi_graph, R.id.navi_transactions, R.id.navi_history, R.id.navi_info
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Hide bottom navigation bar when in Settings or Profile
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navi_settings, R.id.navi_profile -> {
                    navView.visibility = View.GONE
                }
                else -> {
                    navView.visibility = View.VISIBLE
                }
            }
        }

        checkAndRequestSmsPermission()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom)
        return when (item.itemId) {
            R.id.menu_settings -> {
                navController.navigate(R.id.navi_settings)
                true
            }
            R.id.menu_profile -> {
                navController.navigate(R.id.navi_profile)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        checkAndRequestSmsPermission()
    }

    override fun onStop() {
        super.onStop()
        // Reset the lock when the app is fully hidden (NOT onPause, which fires on system dialogs too)
        LockActivity.isUnlocked = false
    }

    override fun onResume() {
        super.onResume()
        checkLock()
        checkAndRequestSmsPermission()
    }

    private fun checkLock() {
        if (!LockActivity.isUnlocked) {
            val intent = android.content.Intent(this, LockActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkAndRequestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS), perm_sms)
        } else {
            //readSms() // Read SMS if permission is already granted
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            perm_sms -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //readSms() // Read SMS after permission is granted
                } else {
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

//    private fun readSms() {
//        val uri = Uri.parse("content://sms/inbox")
//        val cursor = contentResolver.query(uri, null, null, null, null)
//
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
//                val body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
//                Log.e("SMS", "From: $address, Body: $body")
//            }
//            cursor.close()
//        }
//    }

}