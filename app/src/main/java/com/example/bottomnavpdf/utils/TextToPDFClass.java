package com.example.bottomnavpdf.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class TextToPDFClass {

    public static String convertTextToPdf(Context context, String name, String text) {
        // Create a new PdfDocument
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 500, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        TextPaint mTextPaint = new TextPaint();
        Canvas canvas = page.getCanvas();
        StaticLayout mTextLayout = new StaticLayout(text, mTextPaint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        canvas.save();
        float textX = 10;
        float textY = 10;
        canvas.translate(textX, textY);
        mTextLayout.draw(canvas);
        canvas.restore();
        document.finishPage(page);
        String path = Environment.getExternalStorageDirectory().toString() + "/PdfReader/" + "Text_to_PDF/";
        File file11 = new File(path);
        if (!file11.exists()) {
            file11.mkdirs();
        }
        File file = new File(path + name + ".pdf");
        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(context, "PDF file saved", Toast.LENGTH_SHORT).show();
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Something went wrong ,file not saved!", Toast.LENGTH_SHORT).show();
            Log.d("IMAGETOPDF", e.getMessage());
        }
        return file.getAbsolutePath();
    }
}
