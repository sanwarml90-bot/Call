package com.smartdialer

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.smartdialer.data.local.SettingsStore
import com.smartdialer.data.repository.CallLogRepository
import com.smartdialer.data.repository.DeviceContact
import com.smartdialer.data.repository.DeviceContactRepository
import com.smartdialer.data.repository.RecentCall
import com.smartdialer.domain.usecase.T9Matcher
import com.smartdialer.ui.dialpad.DialPadController

class MainActivity : AppCompatActivity() {
    private lateinit var content: LinearLayout
    private lateinit var settingsStore: SettingsStore
    private lateinit var contacts: DeviceContactRepository
    private lateinit var recents: CallLogRepository
    private val dialPad = DialPadController()
    private var selectedTab = 0
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { render() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsStore = SettingsStore(this)
        contacts = DeviceContactRepository(this)
        recents = CallLogRepository(this)
        requestNeededPermissions()
        setContentView(shell())
        render()
    }

    private fun shell(): View = FrameLayout(this).apply {
        val root = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 20, 20, 12); setBackgroundColor(0xfff8f9ff.toInt()) }
        content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(BottomNavigationView(context).apply {
            menu.add(0, 0, 0, "Home").setIcon(android.R.drawable.ic_menu_view)
            menu.add(0, 1, 1, "Recents").setIcon(android.R.drawable.ic_menu_recent_history)
            menu.add(0, 2, 2, "Contacts").setIcon(android.R.drawable.ic_menu_search)
            menu.add(0, 3, 3, "Favorites").setIcon(android.R.drawable.btn_star_big_on)
            menu.add(0, 4, 4, "Settings").setIcon(android.R.drawable.ic_menu_manage)
            setOnItemSelectedListener { selectedTab = it.itemId; render(); true }
        })
        addView(root)
        addView(ExtendedFloatingActionButton(context).apply { text = "Dial"; contentDescription = "Open dial pad"; setOnClickListener { showDialPad() } }, FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM or Gravity.END).apply { setMargins(0, 0, 28, 110) })
    }

    private fun render() {
        content.removeAllViews()
        content.addView(title("SmartDialer"))
        if (!isDefaultDialer()) content.addView(roleCard())
        when (selectedTab) {
            0 -> home()
            1 -> recentCalls()
            2 -> contactList(false)
            3 -> contactList(true)
            else -> settings()
        }
    }

    private fun home() {
        content.addView(searchBox("Search contacts or numbers") { contactList(false, it) })
        content.addView(section("Favorites")); val favorites = contacts.contacts(favoritesOnly = true, limit = 4); if (favorites.isEmpty()) showEmpty("No favorite contacts yet.") else favorites.forEach { content.addView(contactRow(it)) }
        content.addView(section("Recent calls")); val recent = recents.recentCalls(5); if (recent.isEmpty()) showEmpty("No recent calls available or permission denied.") else recent.forEach { content.addView(recentRow(it)) }
        content.addView(card("Rules status", "Unknown calls: ${settingsStore.unknownCallMode}\nAuto Call Cut: ${timerLabel(settingsStore.autoTimerSeconds)}"))
    }

    private fun contactList(favoritesOnly: Boolean, query: String = "") {
        content.addView(searchBox(if (favoritesOnly) "Search favorites" else "Search contacts") { contactList(favoritesOnly, it) })
        val rows = contacts.contacts(query, favoritesOnly)
        if (!contacts.hasPermission()) content.addView(errorCard("Contacts permission is required to display your contacts."))
        else if (rows.isEmpty()) showEmpty(if (favoritesOnly) "No favorites found." else "No contacts found.") else rows.forEach { content.addView(contactRow(it)) }
    }

    private fun recentCalls() {
        if (!recents.hasPermission()) content.addView(errorCard("Call log permission is required to display recent calls."))
        val rows = recents.recentCalls(); if (rows.isEmpty()) showEmpty("No call history found.") else rows.forEach { content.addView(recentRow(it)) }
    }

    private fun settings() {
        content.addView(section("Calling"))
        content.addView(timerSetting())
        content.addView(section("Incoming Calls"))
        content.addView(settingChoice("Unknown calls", listOf("Normal", "Silence", "Block", "Screen"), settingsStore.unknownCallMode) { settingsStore.unknownCallMode = it; render() })
        content.addView(section("Appearance"))
        content.addView(settingChoice("Theme", listOf("System", "Light", "Dark"), settingsStore.theme) { settingsStore.theme = it; render() })
        content.addView(MaterialSwitch(this).apply { text = "Haptic dial pad feedback"; isChecked = settingsStore.haptics; setOnCheckedChangeListener { _, checked -> settingsStore.haptics = checked } })
        content.addView(card("Privacy & limitations", "Private contacts, PIN, biometric lock, Room-backed rules, and encrypted storage are documented and ready for the next implementation phase. Android may still show system-level call traces depending on device, carrier, and role state."))
    }

    private fun showDialPad() {
        val number = TextView(this).apply { textSize = 30f; gravity = Gravity.CENTER; minHeight = 72; contentDescription = "Entered phone number" }
        var matches: TextView? = null
        fun sync() {
            val value = dialPad.currentValue()
            number.text = value
            matches?.text = contacts.contacts(limit = 5).filter { value.isNotBlank() && (T9Matcher.matches(it.name, value) || it.number.contains(value)) }.joinToString("\n") { it.name.ifBlank { it.number } }.ifBlank { "No contact matches" }
        }
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(number) }
        GridLayout(this).apply { columnCount = 3; listOf("1","2 ABC","3 DEF","4 GHI","5 JKL","6 MNO","7 PQRS","8 TUV","9 WXYZ","*","0 +","#").forEach { label -> addView(Button(context).apply { text = label; minHeight = 112; setOnClickListener { if (settingsStore.haptics) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); dialPad.append(label.first()); sync() }; setOnLongClickListener { if (label.startsWith("0")) dialPad.append('+') else dialPad.append(label.first()); sync(); true } }) }; body.addView(this) }
        matches = TextView(this).apply { text = "Type to match contacts with T9"; setPadding(0, 10, 0, 10) }; body.addView(matches!!)
        body.addView(LinearLayout(this).apply { gravity = Gravity.CENTER; addView(Button(context).apply { text = "Delete"; setOnClickListener { dialPad.delete(); sync() }; setOnLongClickListener { dialPad.clear(); sync(); true } }); addView(Button(context).apply { text = "Call"; setOnClickListener { placeCall(dialPad.currentValue()) } }) })
        MaterialAlertDialogBuilder(this).setTitle("Dial pad").setView(body).setNegativeButton("Close", null).show()
    }

    private fun placeCall(number: String) {
        if (number.isBlank()) return toast("Enter a phone number first.")
        val uri = Uri.fromParts("tel", number, null)
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) runCatching { getSystemService(TelecomManager::class.java).placeCall(uri, Bundle().apply { preferredPhoneAccount()?.let { putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it) } }) }.onFailure { toast("Unable to start the call.") }
        else startActivity(Intent(Intent.ACTION_DIAL, uri))
    }

    private fun preferredPhoneAccount(): PhoneAccountHandle? = runCatching { getSystemService(TelecomManager::class.java).callCapablePhoneAccounts.firstOrNull() }.getOrNull()
    private fun isDefaultDialer(): Boolean = getSystemService(TelecomManager::class.java).defaultDialerPackage == packageName
    private fun requestNeededPermissions() = permissionLauncher.launch(arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE))
    private fun requestDialerRole() { val role = ContextCompat.getSystemService(this, RoleManager::class.java); if (role?.isRoleAvailable(RoleManager.ROLE_DIALER) == true) startActivity(role.createRequestRoleIntent(RoleManager.ROLE_DIALER)) else startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)) }
    private fun addContact(number: String) = startActivity(Intent(ContactsContract.Intents.Insert.ACTION).setType(ContactsContract.RawContacts.CONTENT_TYPE).putExtra(ContactsContract.Intents.Insert.PHONE, number))
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun title(text: String) = TextView(this).apply { this.text = text; textSize = 30f; setPadding(4, 0, 0, 18) }
    private fun section(text: String) = TextView(this).apply { this.text = text; textSize = 18f; setPadding(4, 22, 4, 8) }
    private fun card(title: String, body: String) = MaterialCardView(this).apply { radius = 28f; setContentPadding(24, 22, 24, 22); addView(TextView(context).apply { text = "$title\n$body"; textSize = 16f }) }
    private fun errorCard(text: String) = card("Action needed", text)
    private fun roleCard() = MaterialCardView(this).apply { radius = 30f; setContentPadding(24, 24, 24, 24); addView(LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; addView(TextView(context).apply { text = "SmartDialer needs to be your default Phone app to provide incoming-call UI, screening, blocking, and call-log management." }); addView(Button(context).apply { text = "Set default Phone app"; setOnClickListener { requestDialerRole() } }) }) }
    private fun contactRow(contact: DeviceContact) = card(contact.name.ifBlank { "Unknown contact" }, "${contact.number}\n${if (contact.starred) "Favorite • " else ""}Tap to call, long press to add/edit").apply { setOnClickListener { placeCall(contact.number) }; setOnLongClickListener { addContact(contact.number); true } }
    private fun recentRow(call: RecentCall) = card(call.cachedName ?: call.number.ifBlank { "Unknown number" }, "${call.typeLabel} • ${call.whenLabel} • ${call.durationSeconds}s\nTap to call, long press to add to contacts").apply { setOnClickListener { placeCall(call.number) }; setOnLongClickListener { addContact(call.number); true } }
    private fun searchBox(hint: String, onSearch: (String) -> Unit) = TextInputLayout(this).apply { this.hint = hint; addView(TextInputEditText(context).apply { setSingleLine(); setOnEditorActionListener { view, _, _ -> onSearch(view.text?.toString().orEmpty()); true } }) }
    private fun showEmpty(text: String) { content.addView(card("Empty", text)) }
    private fun timerSetting(): View {
        val options = listOf(0L to "Off", 60L to "1 minute", 300L to "5 minutes", 600L to "10 minutes", 900L to "15 minutes", 1800L to "30 minutes")
        return card("Auto Call Cut", timerLabel(settingsStore.autoTimerSeconds)).apply {
            setOnClickListener { MaterialAlertDialogBuilder(this@MainActivity).setTitle("Auto Call Cut").setItems(options.map { it.second }.toTypedArray()) { _, which -> settingsStore.autoTimerSeconds = options[which].first; render() }.show() }
        }
    }
    private fun <T> settingChoice(title: String, options: List<T>, current: T, save: (T) -> Unit): View = card(title, current.toString()).apply { setOnClickListener { MaterialAlertDialogBuilder(this@MainActivity).setTitle(title).setItems(options.map { it.toString() }.toTypedArray()) { _, which -> save(options[which]) }.show() } }
    private fun timerLabel(seconds: Long) = if (seconds <= 0) "Off" else "${seconds / 60} minutes"
}
