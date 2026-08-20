package com.smartdialer

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.smartdialer.domain.usecase.T9Matcher
import com.smartdialer.ui.dialpad.DialPadController

class MainActivity : AppCompatActivity() {
    private lateinit var numberView: TextView
    private val dialPad = DialPadController()
    private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissions.launch(arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG))
        setContentView(buildUi())
    }

    private fun buildUi(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(28, 28, 28, 28)
        addView(TextView(context).apply { text = "Set SmartDialer as your default Phone app"; textSize = 20f })
        addView(Button(context).apply { text = "Open default Phone app settings"; setOnClickListener { requestDialerRole() } })
        addView(TextView(context).apply { text = "Without the default Phone and call-screening roles, incoming-call UI, screening, blocking, and call-log management are limited by Android." })
        numberView = TextView(context).apply { textSize = 32f; contentDescription = "Entered phone number" }; addView(numberView)
        val grid = GridLayout(context).apply { columnCount = 3 }
        listOf("1","2 ABC","3 DEF","4 GHI","5 JKL","6 MNO","7 PQRS","8 TUV","9 WXYZ","*","0 +","#").forEach { label ->
            grid.addView(Button(context).apply { text = label; minHeight = 96; setOnClickListener { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); appendDigit(label.first()) }; setOnLongClickListener { if (label.startsWith("0")) appendDigit('+') else appendDigit(label.first()); true } })
        }
        addView(grid)
        addView(LinearLayout(context).apply { addView(Button(context).apply { text = "Delete"; setOnClickListener { updateNumber(dialPad.delete()) }; setOnLongClickListener { updateNumber(dialPad.clear()); true } }); addView(Button(context).apply { text = "Call"; setOnClickListener { placeCall() } }) })
        addView(TextView(context).apply { text = "Home • Contacts • Recents • Favorites • Settings\nT9 example: 43556 matches ${T9Matcher.matches("HELLO", "43556")}" })
    }

    private fun appendDigit(c: Char) = updateNumber(dialPad.append(c))
    private fun updateNumber(value: String) { numberView.text = value }

    private fun requestDialerRole() {
        val roleManager = ContextCompat.getSystemService(this, RoleManager::class.java)
        if (roleManager?.isRoleAvailable(RoleManager.ROLE_DIALER) == true) startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER))
        else startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
    }

    private fun placeCall() {
        val number = numberView.text.toString(); if (number.isBlank()) return
        val uri = Uri.fromParts("tel", number, null)
        val telecom = getSystemService(TelecomManager::class.java)
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED) telecom.placeCall(uri, Bundle())
        else startActivity(Intent(Intent.ACTION_DIAL, uri))
    }
}
