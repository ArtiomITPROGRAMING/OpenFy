package com.example.openfy.features.community.sync

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object ShareIntentHelper {

    /**
     * Shares a [SharePayload] as a file via Android system share sheet.
     */
    fun shareAsFile(context: Context, payload: SharePayload) {
        try {
            val sharesDir = File(context.cacheDir, "shares").apply { mkdirs() }
            val cleanTitle = payload.title.replace(Regex("[^a-zA-Z0-9а-яА-ЯёЁ_-]"), "_")
            val extension = when (payload.type) {
                ShareType.THEME -> "thm"
                ShareType.PLAYLIST -> "openfy"
                ShareType.TRACK_META -> "json"
            }
            val shareFile = File(sharesDir, "$cleanTitle.$extension")
            shareFile.writeText(payload.toCompressedString())

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                shareFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, payload.title)
                putExtra(Intent.EXTRA_TEXT, "Файл OpenFy: ${payload.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Поделиться «${payload.title}»").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка отправки файла: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Copies text / payload string to device clipboard.
     */
    fun copyToClipboard(context: Context, text: String, label: String = "OpenFy Share") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Скопировано в буфер обмена!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка копирования: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
