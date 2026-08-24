package com.smartdialer

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.smartdialer.domain.RuleStore
import com.smartdialer.ui.dialpad.DialPadController

class MainActivity : AppCompatActivity() {
    private lateinit var numberView: TextView
    private lateinit var rules: RuleStore
    private val dialPad = DialPadController()
    private val permissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rules = RuleStore(this)
        permissions.launch(
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG
            )
        )
        setContentView(buildUi())
    }

    private fun buildUi(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(28, 28, 28, 18)
        setBackgroundColor(0xfff8f7ff.toInt())
        addView(TextView(context).apply {
            text = "SmartDialer"
            textSize = 30f
            setTextColor(0xff17151f.toInt())
        })
        addView(TextView(context).apply {
            text = "Private calls. Your rules."
            textSize = 15f
            setTextColor(0xff625f70.toInt())
        })
        addView(MaterialButton(context).apply {
            text = "Set as default phone app"
            setOnClickListener { requestDialerRole() }
        })
        addView(ghostDndCard())
        numberView = TextView(context).apply {
            textSize = 34f
            gravity = Gravity.CENTER
            minHeight = 76
            contentDescription = "Entered phone number"
            setTextColor(0xff17151f.toInt())
        }
        addView(numberView)
        val grid = GridLayout(context).apply {
            columnCount = 3
            useDefaultMargins = true
        }
        listOf(
            "1", "2 ABC", "3 DEF", "4 GHI", "5 JKL", "6 MNO",
            "7 PQRS", "8 TUV", "9 WXYZ", "*", "0 +", "#"
        ).forEach { label ->
            grid.addView(MaterialButton(context).apply {
                text = label
                minHeight = 82
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(4, 4, 4, 4)
                }
                setOnClickListener {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    updateNumber(dialPad.append(label.first()))
                }
                setOnLongClickListener {
                    updateNumber(dialPad.append(if (label.startsWith("0")) '+' else label.first()))
                    true
                }
            })
        }
        addView(grid)
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(MaterialButton(context).apply {
                text = "Clear"
                setOnClickListener { updateNumber(dialPad.clear()) }
            }, LinearLayout.LayoutParams(0, -2, 1f))
            addView(MaterialButton(context).apply {
                text = "Call"
                setOnClickListener { placeCall() }
            }, LinearLayout.LayoutParams(0, -2, 1f))
        })
        addView(TextView(context).apply {
            text = "Home  •  Contacts  •  Recents  •  Settings"
            gravity = Gravity.CENTER
            setPadding(0, 18, 0, 0)
            setTextColor(0xff625f70.toInt())
        })
    }

    private fun ghostDndCard(): View = MaterialCardView(this).apply {
        radius = 24f
        setCardBackgroundColor(0xffebe8ff.toInt())
        setContentPadding(20, 14, 20, 14)
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(Switch(context).apply {
                text = "Ghost DND"
                textSize = 18f
                isChecked = rules.ghostDndEnabled
                setOnCheckedChangeListener { _, value -> rules.ghostDndEnabled = value }
            })
            addView(TextView(context).apply {
                text = "Silence a chosen caller with Android call screening. The call remains in history."
                setTextColor(0xff514d61.toInt())
            })
            val input = TextInputLayout(context).apply { hint = "Phone number to silence" }
            val number = TextInputEditText(context).apply {
                inputType = android.text.InputType.TYPE_CLASS_PHONE
                setText(rules.ghostDndNumber)
            }
            input.addView(number)
            addView(input)
            addView(MaterialButton(context).apply {
                text = "Save Ghost DND number"
                setOnClickListener {
                    rules.ghostDndNumber = number.text?.toString().orEmpty()
                    number.setText(rules.ghostDndNumber)
                }
            })
        })
    }

    private fun updateNumber(value: String) {
        numberView.text = value
    }

    private fun requestDialerRole() {
        val roleManager = ContextCompat.getSystemService(this, RoleManager::class.java)
        if (roleManager?.isRoleAvailable(RoleManager.ROLE_DIALER) == true) {
            startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER))
        } else {
            startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        }
    }

    private fun placeCall() {
        val number = numberView.text.toString()
        if (number.isBlank()) return
        val uri = Uri.fromParts("tel", number, null)
        val telecom = getSystemService(TelecomManager::class.java)
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
            telecom != null
        ) {
            telecom.placeCall(uri, Bundle())
        } else {
            startActivity(Intent(Intent.ACTION_DIAL, uri))
        }
    }
}