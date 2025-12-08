package com.datavite.distrivite.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil

/**
 * PDF export utility for bills with both READ and SHARE intents
 */
object BillPDFExporter {

    /**
     * Export bill to PDF and provide options to both VIEW and SHARE
     */
    fun exportBillToPDF(
        context: Context,
        composeView: ComposeView,
        billNumber: String,
        onSuccess: ((File) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        try {
            val file = generateBillPDF(context, composeView, billNumber)
            showPDFOptions(context, file, billNumber)
            onSuccess?.invoke(file)
        } catch (e: Exception) {
            val errorMsg = "Failed to generate PDF: ${e.message}"
            onError?.invoke(errorMsg)
        }
    }

    /**
     * Export bill to PDF and automatically OPEN for viewing
     */
    fun exportBillToPDFAndOpen(
        context: Context,
        composeView: ComposeView,
        billNumber: String,
        onSuccess: ((File) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        try {
            val file = generateBillPDF(context, composeView, billNumber)
            openPDF(context, file)
            onSuccess?.invoke(file)
        } catch (e: Exception) {
            val errorMsg = "Failed to generate PDF: ${e.message}"
            onError?.invoke(errorMsg)
        }
    }

    /**
     * Export bill to PDF and automatically SHARE
     */
    fun exportBillToPDFAndShare(
        context: Context,
        composeView: ComposeView,
        billNumber: String,
        onSuccess: ((File) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        try {
            val file = generateBillPDF(context, composeView, billNumber)
            sharePDF(context, file, billNumber)
            onSuccess?.invoke(file)
        } catch (e: Exception) {
            val errorMsg = "Failed to generate PDF: ${e.message}"
            onError?.invoke(errorMsg)
        }
    }

    /**
     * Export to PDF only (no automatic intent)
     */
    fun exportBillToPDFOnly(
        context: Context,
        composeView: ComposeView,
        billNumber: String,
        onSuccess: ((File) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): File? {
        return try {
            val file = generateBillPDF(context, composeView, billNumber)
            onSuccess?.invoke(file)
            file
        } catch (e: Exception) {
            val errorMsg = "Failed to generate PDF: ${e.message}"
            onError?.invoke(errorMsg)
            null
        }
    }

    private fun generateBillPDF(
        context: Context,
        composeView: ComposeView,
        billNumber: String
    ): File {
        val pageWidth = 592   // Receipt width
        val pageHeight = 842  // Receipt height

        // Measure the ComposeView
        composeView.measure(
            View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

        // Allow time for composition to complete
        Thread.sleep(50)

        val fullWidth = composeView.measuredWidth
        val fullHeight = composeView.measuredHeight

        if (fullWidth <= 0 || fullHeight <= 0) {
            throw IllegalStateException("ComposeView has invalid dimensions")
        }

        // Create bitmap
        val fullBitmap = Bitmap.createBitmap(fullWidth, fullHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(fullBitmap)
        composeView.draw(canvas)

        // PDF document setup
        val document = PdfDocument()

        // Handle pagination
        val pageCount = ceil(fullHeight.toDouble() / pageHeight).toInt()

        for (i in 0 until pageCount) {
            val startY = i * pageHeight
            var endY = startY + pageHeight
            if (endY > fullHeight) endY = fullHeight

            val currentPageHeight = endY - startY

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create()
            val page = document.startPage(pageInfo)

            if (startY < fullHeight) {
                val pageBitmap = Bitmap.createBitmap(fullBitmap, 0, startY, fullWidth, currentPageHeight)
                page.canvas.drawBitmap(pageBitmap, 0f, 0f, null)
                pageBitmap.recycle()
            }

            document.finishPage(page)
        }

        // Save file
        val fileName = "bill_${billNumber}_${System.currentTimeMillis()}.pdf"
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = File(outputDir, fileName)

        FileOutputStream(file).use { outputStream ->
            document.writeTo(outputStream)
        }

        // Cleanup
        document.close()
        fullBitmap.recycle()

        return file
    }

    /**
     * Show options to both VIEW and SHARE the PDF
     */
    private fun showPDFOptions(context: Context, file: File, billNumber: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        // Create VIEW intent
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }

        // Create SHARE intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Bill Receipt - $billNumber")
            putExtra(Intent.EXTRA_TEXT, "Bill receipt #$billNumber from TIQTAQ Restaurant")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Create chooser with both options
        val chooserIntent = Intent.createChooser(shareIntent, "Bill: $billNumber").apply {
            // Add VIEW option to the chooser
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(viewIntent))
        }

        // Check if there's a PDF viewer available for VIEW intent
        val viewerAvailable = viewIntent.resolveActivity(context.packageManager) != null

        if (viewerAvailable) {
            context.startActivity(chooserIntent)
        } else {
            // If no PDF viewer, just show share options
            context.startActivity(shareIntent)
        }
    }

    /**
     * Open PDF for viewing only
     */
    private fun openPDF(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }

        // Check if there's a PDF viewer app available
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to share if no PDF viewer
            sharePDF(context, file, file.nameWithoutExtension)
        }
    }

    /**
     * Share PDF only
     */
    private fun sharePDF(context: Context, file: File, billNumber: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Bill Receipt - $billNumber")
            putExtra(Intent.EXTRA_TEXT, "Bill receipt #$billNumber from TIQTAQ Restaurant")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share Bill: $billNumber")
        context.startActivity(chooser)
    }
}

