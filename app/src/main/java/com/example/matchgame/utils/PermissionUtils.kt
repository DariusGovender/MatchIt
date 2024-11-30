package com.example.matchgame.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.example.matchgame.R

object PermissionUtil {
    private const val PERMISSION_PREFS = "permission_prefs"
    private const val NOTIFICATION_DIALOG_SHOWN = "notification_dialog_shown"

    // Check if notification permissions are granted
    fun isNotificationPermissionGranted(context: Context): Boolean {
        val notificationManager = NotificationManagerCompat.from(context)
        return notificationManager.areNotificationsEnabled()
    }

    // Show rationale dialog for notifications
    fun showNotificationRationaleDialog(activity: Activity, onAgree: () -> Unit) {
        val sharedPrefs: SharedPreferences = activity.getSharedPreferences(PERMISSION_PREFS, Context.MODE_PRIVATE)
        val shown = sharedPrefs.getBoolean(NOTIFICATION_DIALOG_SHOWN, false)

        if (!shown) {
            AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.permission_required))
                .setMessage(activity.getString(R.string.permission_required_message))
                .setPositiveButton(activity.getString(R.string.ok)) { _, _ ->
                    // Set preference that dialog has been shown
                    sharedPrefs.edit().putBoolean(NOTIFICATION_DIALOG_SHOWN, true).apply()
                    onAgree()
                }
                .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        } else {
            // Directly call onAgree if dialog has been shown before
            onAgree()
        }
    }

    // Direct users to app settings to enable notification permissions
    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:${activity.packageName}")
        }
        activity.startActivity(intent)
    }
}
