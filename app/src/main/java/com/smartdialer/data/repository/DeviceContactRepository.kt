package com.smartdialer.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

data class DeviceContact(val id: Long, val name: String, val number: String, val photoUri: String?, val starred: Boolean)

class DeviceContactRepository(private val context: Context) {
    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    fun contacts(query: String = "", favoritesOnly: Boolean = false, limit: Int = 200): List<DeviceContact> {
        if (!hasPermission()) return emptyList()
        val rows = mutableListOf<DeviceContact>()
        val selection = buildString {
            append("${ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER}=1")
            if (favoritesOnly) append(" AND ${ContactsContract.CommonDataKinds.Phone.STARRED}=1")
            if (query.isNotBlank()) append(" AND (${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?)")
        }
        val args = if (query.isBlank()) null else arrayOf("%$query%", "%$query%")
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED
        )
        context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, args, "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} COLLATE LOCALIZED ASC")?.use { cursor ->
            val id = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val name = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
            val number = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photo = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            val starred = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.STARRED)
            while (cursor.moveToNext() && rows.size < limit) rows += DeviceContact(cursor.getLong(id), cursor.getString(name).orEmpty(), cursor.getString(number).orEmpty(), cursor.getString(photo), cursor.getInt(starred) == 1)
        }
        return rows
    }
}
