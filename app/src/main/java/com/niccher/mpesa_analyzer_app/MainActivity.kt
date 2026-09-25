package com.niccher.mpesa_analyzer_app

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
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.niccher.mpesa_analyzer_app.databinding.ActivityMainBinding
import com.niccher.mpesa_analyzer_app.helpers.AppPrefs
import com.niccher.mpesa_analyzer_app.R

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
                R.id.navi_home, R.id.navi_graph, R.id.navi_transactions, R.id.navi_history
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Hide bottom navigation bar when in Settings or Profile or Info or Credits or Summary
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navi_settings, R.id.navi_profile, R.id.navi_app_info, R.id.navi_app_credits, R.id.navi_summary -> {
                    navView.visibility = View.GONE
                }
                else -> {
                    navView.visibility = View.VISIBLE
                }
            }
        }

        checkAndRequestSmsPermission()
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: android.content.Intent?) {
        val action = intent?.getStringExtra("NOTIFICATION_ACTION") ?: return
        val amount = intent.getFloatExtra(com.niccher.mpesa_analyzer_app.receivers.NotificationActionReceiver.EXTRA_AMOUNT, 0f)
        val counterparty = intent.getStringExtra(com.niccher.mpesa_analyzer_app.receivers.NotificationActionReceiver.EXTRA_COUNTERPARTY) ?: ""
        
        when (action) {
            com.niccher.mpesa_analyzer_app.receivers.NotificationActionReceiver.ACTION_SPLIT_BILL -> {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Split Bill")
                    .setMessage("Split KES $amount ($counterparty) among how many people?")
                    .setPositiveButton("Split 50/50") { _, _ ->
                        val share = amount / 2f
                        Toast.makeText(this, "Each person owes: KES ${"%.2f".format(share)}", Toast.LENGTH_LONG).show()
                    }
                    .setNeutralButton("Cancel", null)
                    .show()
            }
            com.niccher.mpesa_analyzer_app.receivers.NotificationActionReceiver.ACTION_ADD_NOTE -> {
                val input = android.widget.EditText(this).apply {
                    hint = "e.g. Lunch with colleagues"
                }
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Add Note")
                    .setMessage("Attach note to KES $amount ($counterparty):")
                    .setView(input)
                    .setPositiveButton("Save") { _, _ ->
                        val note = input.text.toString()
                        Toast.makeText(this, "Note saved: $note", Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton("Cancel", null)
                    .show()
            }
            com.niccher.mpesa_analyzer_app.receivers.NotificationActionReceiver.ACTION_CHANGE_CATEGORY -> {
                val categories = arrayOf("Mobile Money", "Food & Dining", "Shopping", "Transport", "Bills & Utilities", "Entertainment", "Personal", "Family")
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Change Category ($counterparty)")
                    .setItems(categories) { _, which ->
                        Toast.makeText(this, "Category updated to: ${categories[which]}", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                val m = menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", java.lang.Boolean.TYPE)
                m.isAccessible = true
                m.invoke(menu, true)
            } catch (e: Exception) {
                Log.e("Menu", "Could not set optional icons visible", e)
            }
        }

        // Apply theme-adaptive colors to each menu item icon
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val icon = item.icon ?: continue
            val colorRes = when (item.itemId) {
                R.id.menu_profile -> R.color.menu_icon_profile
                R.id.menu_settings -> R.color.menu_icon_settings
                R.id.navi_app_info -> R.color.menu_icon_info
                R.id.navi_app_credits -> R.color.menu_icon_credits
                else -> R.color.menu_icon_color
            }
            val color = ContextCompat.getColor(this, colorRes)
            val wrapped = DrawableCompat.wrap(icon.mutate())
            DrawableCompat.setTint(wrapped, color)
            item.icon = wrapped
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom)
        return when (item.itemId) {
            R.id.menu_ask_ai -> {
                startActivity(android.content.Intent(this, com.niccher.mpesa_analyzer_app.chat.ChatActivity::class.java))
                true
            }
            R.id.menu_settings -> {
                navController.navigate(R.id.navi_settings)
                true
            }
            R.id.menu_profile -> {
                navController.navigate(R.id.navi_profile)
                true
            }
            R.id.navi_app_info -> {
                navController.navigate(R.id.navi_app_info)
                true
            }
            R.id.navi_app_credits -> {
                navController.navigate(R.id.navi_app_credits)
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
        // Reset the lock when the app is backgrounded, but preserve unlock state during
        // brief transitions (e.g., launching ChatActivity, QR scanner, or LockActivity transition)
        val elapsed = System.currentTimeMillis() - LockActivity.lastUnlockTime
        if (elapsed > 30_000L) {
            LockActivity.isUnlocked = false
        }
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
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            needed.add(android.Manifest.permission.READ_SMS)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                needed.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), perm_sms)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            perm_sms -> {
                val results = permissions.zip(grantResults.toTypedArray()).toMap()
                val smsOk = results[android.Manifest.permission.READ_SMS]
                    ?.let { it == PackageManager.PERMISSION_GRANTED } ?: true
                if (!smsOk) {
                    Toast.makeText(this, "SMS permission denied — Fetch and Sync will not work", Toast.LENGTH_LONG).show()
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