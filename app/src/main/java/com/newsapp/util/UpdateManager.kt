package com.newsapp.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {

    private const val UPDATE_JSON_URL = "https://raw.githubusercontent.com/durgexh/news/refs/heads/main/update.json"

    data class UpdateInfo(
        val isUpdateAvailable: Boolean,
        val latestVersionCode: Int,
        val apkUrl: String
    )

    suspend fun checkForUpdate(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(UPDATE_JSON_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                
                val latestVersionCode = jsonObject.getInt("latest_version_code")
                val apkUrl = jsonObject.getString("apk_url")
                
                // Get current app version
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pInfo.longVersionCode.toInt()
                } else {
                    pInfo.versionCode
                }

                return@withContext UpdateInfo(
                    isUpdateAvailable = latestVersionCode > currentVersionCode,
                    latestVersionCode = latestVersionCode,
                    apkUrl = apkUrl
                )
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Failed to check for updates", e)
        }
        return@withContext null
    }

    fun downloadAndInstallUpdate(context: Context, apkUrl: String) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(apkUrl)
        
        // Define the destination file
        val fileName = "NewsApp_Update.apk"
        val destinationDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val destinationFile = File(destinationDir, fileName)
        
        // Remove old file if it exists to avoid (1) duplicates
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        val request = DownloadManager.Request(uri)
            .setTitle("Downloading NewsApp Update")
            .setDescription("Please wait while the new version is downloaded.")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadId = downloadManager.enqueue(request)

        // Register a BroadcastReceiver to listen for the completion of this specific download
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId && context != null) {
                    installApk(context, destinationFile)
                    context.unregisterReceiver(this)
                }
            }
        }
        
        // Register receiver (handle Android 13+ stricter export rules)
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, intentFilter)
        }
    }

    private fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        // Use FileProvider on Android 7+ (API 24+) to safely grant URI permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            val apkUri = Uri.fromFile(apkFile)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Failed to start install intent", e)
        }
    }
}
