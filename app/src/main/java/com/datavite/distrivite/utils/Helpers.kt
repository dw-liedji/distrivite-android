package com.datavite.distrivite.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object Helpers {

    /**
     * Share the PDF file
     */
     fun sharePDF(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Bill PDF"))
    }
}