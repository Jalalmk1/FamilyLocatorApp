package com.example.bottomnavpdf.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_ONLY
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import java.io.File
import java.io.IOException


class PdfRequestHandler : RequestHandler() {
    override fun canHandleRequest(data: Request): Boolean {
        val scheme = data.uri.scheme
        return SCHEME_PDF == scheme
    }

    @Throws(IOException::class)
    override fun load(data: Request, arg1: Int): Result? {
        val fileDescriptor = ParcelFileDescriptor.open(File(data.uri.path), MODE_READ_ONLY)
        val renderer = PdfRenderer(fileDescriptor)
        val pageCount = renderer.pageCount
        if (pageCount > 0) {
            val page = renderer.openPage(0)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            return Result(bitmap, LoadedFrom.DISK)
        }
        return null
    }

    companion object {
        var SCHEME_PDF = "pdf"
    }
}
