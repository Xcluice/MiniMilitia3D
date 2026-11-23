package com.xcluice.minimilitia3d

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
    private const val API_URL = "https://api.github.com/repos/Xcluice/MiniMilitia3D/releases/latest"
    private const val FALLBACK_URL = "https://github.com/Xcluice/MiniMilitia3D/releases/latest"

    fun check(activity: Activity) {
        Thread {
            try {
                val conn = (URL(API_URL).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "MiniMilitia3D-Android")
                }
                if (conn.responseCode != 200) return@Thread
                val json = JSONObject(conn.inputStream.bufferedReader().readText())
                val remoteTag = json.optString("tag_name", "").removePrefix("v")
                val notes = json.optString("body", "Bug fixes and improvements.").trim()
                val htmlUrl = json.optString("html_url", FALLBACK_URL)
                val installed = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName ?: "1.0.0"

                if (isNewer(remoteTag, installed)) {
                    activity.runOnUiThread {
                        if (!activity.isFinishing) showForceUpdate(activity, remoteTag, notes, htmlUrl)
                    }
                }
            } catch (_: Throwable) {
            }
        }.start()
    }

    private fun isNewer(remote: String, local: String): Boolean {
        val r = remote.split(".").mapNotNull { it.toIntOrNull() }
        val l = local.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(r.size, l.size)
        for (i in 0 until maxLen) {
            val rv = r.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }
            if (rv != lv) return rv > lv
        }
        return false
    }

    private fun showForceUpdate(activity: Activity, version: String, changelog: String, url: String) {
        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            background = GradientDrawable().apply {
                setColor(Color.rgb(28, 33, 41))
                cornerRadius = 24f
                setStroke(2, Color.rgb(65, 74, 88))
            }
        }
        card.addView(TextView(activity).apply {
            text = "What's new in v$version"
            setTextColor(Color.rgb(217, 138, 43))
            textSize = 14f
            setPadding(0, 0, 0, 12)
        })
        card.addView(TextView(activity).apply {
            text = changelog.ifBlank { "Performance and gameplay improvements." }
            setTextColor(Color.rgb(226, 232, 240))
            textSize = 13f
        })
        container.addView(card)

        AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert)
            .setTitle("Update Required")
            .setView(container)
            .setCancelable(false)
            .setPositiveButton("Download Update") { _, _ ->
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                activity.finish()
            }
            .show()
    }
}
