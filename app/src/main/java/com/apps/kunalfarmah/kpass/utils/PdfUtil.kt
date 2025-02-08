package com.apps.kunalfarmah.kpass.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.apps.kunalfarmah.kpass.MainActivity
import com.apps.kunalfarmah.kpass.R
import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.events.Event
import com.itextpdf.kernel.events.IEventHandler
import com.itextpdf.kernel.events.PdfDocumentEvent
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.EncryptionConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.ListItem
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.ListNumberingType
import com.itextpdf.layout.properties.TextAlignment
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream


object PdfUtil {

    fun exportPasswordsToPdf(context: Context, data: List<PasswordMap>, fileUri: Uri, password: String) {
        val contentResolver = context.contentResolver
        try {
            contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                createPdfWithPassword(context, outputStream, data, password)
            }
            (context as MainActivity).runOnUiThread {
                Toast.makeText(context, context.getString(R.string.passwords_exported_successfully), Toast.LENGTH_SHORT).show()
            }
            Log.d("exportListToPdfWithPassword", "PDF created successfully at $fileUri")
        } catch (e: IOException) {
            (context as MainActivity).runOnUiThread {
                Toast.makeText(
                    context,
                    context.getString(R.string.something_went_wrong_exporting_the_passwords_please_try_again),
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.e("exportListToPdfWithPassword", "Error creating PDF: " + e.message)
        } catch (e: SecurityException) {
            Log.e("exportListToPdfWithPassword", "Security error creating PDF: " + e.message)
        }
    }

    private fun createPdfWithPassword(context: Context, outputStream: OutputStream, data: List<PasswordMap>, password: String) {
        try {
            // Create a PdfDocument with a password
            val writerProperties = WriterProperties()
                .setStandardEncryption(
                    password.toByteArray(),
                    null,
                    EncryptionConstants.ALLOW_COPY,
                    EncryptionConstants.ENCRYPTION_AES_256
                )
            val writer = PdfWriter(outputStream, writerProperties)
            val pdfDocument = PdfDocument(writer)

            // Add header event handler
            val headerHandler = HeaderHandler(context)
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, headerHandler)

            // Create a Document object
            val document = Document(pdfDocument, PageSize.A4)

            // Set top margin to create space for the header
            val headerHeight = 150f // Adjust this value as needed
            document.setMargins(headerHeight, 30f, 30f, 30f)


            // Create a list
            val list = com.itextpdf.layout.element.List(ListNumberingType.DECIMAL).setFont(PdfFontFactory.createFont("Helvetica"))

            // Add data rows
            data.forEach{item ->
                list.add(ListItem(item.toDecryptedString()))
            }

            // Add the list to the document
            document.add(list)

            // Close the document
            document.close()
        } catch (e: IOException) {
            Toast.makeText(context, context.getString(R.string.something_went_wrong_exporting_the_passwords_please_try_again), Toast.LENGTH_SHORT).show()
            Log.e("createPdfWithPassword", "Error creating PDF: " + e.message)
        }
    }


    // Custom Header Handler
    class HeaderHandler(private val context: Context) : IEventHandler {
        override fun handleEvent(event: Event) {
            val pdfDocumentEvent = event as PdfDocumentEvent
            val pdfPage: PdfPage = pdfDocumentEvent.page
            val pageSize: Rectangle = pdfPage.pageSize
            val pdfCanvas = PdfCanvas(pdfPage)
            val canvas = Canvas(pdfCanvas, pageSize)

            // Header text (App Name)
            val appName = getAppName(context)
            val headerParagraph = Paragraph(appName)
                .setFont(PdfFontFactory.createFont())
                .setFontSize(20f)
                .setTextAlignment(TextAlignment.CENTER)

            // Draw the header
            canvas.showTextAligned(headerParagraph, pageSize.width / 2, pageSize.top - 60, TextAlignment.CENTER)

            // Add image to header (App Icon)
            val image = getAppIcon(context)
            val imageWidth = 100f
            val imageHeight = 100f
            val x = pageSize.left + 185
            val y = pageSize.top - 100
            canvas.add(image.scaleToFit(imageWidth, imageHeight).setFixedPosition(x, y))

            // Close the canvas
            canvas.close()
        }

        private fun getAppIcon(context: Context): Image {
            val drawable = AppCompatResources.getDrawable(context, R.mipmap.ic_launcher_foreground)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray: ByteArray = stream.toByteArray()
            val imageData = ImageDataFactory.create(byteArray)
            return Image(imageData)
        }

        private fun getAppName(context: Context): String {
            val applicationInfo: ApplicationInfo = context.applicationInfo
            val stringId: Int = applicationInfo.labelRes
            return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(stringId)
        }
    }

}