package ba.sum.fsre.mymath;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class PdfViewerActivity extends AppCompatActivity {

    public static final String EXTRA_PDF_BASE64 = "EXTRA_PDF_BASE64";
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor fileDescriptor;

    private ImageView pdfImageView;
    private Button prevPageButton, nextPageButton;

    private int pageIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        pdfImageView = findViewById(R.id.pdfImageView);
        prevPageButton = findViewById(R.id.prevPageButton);
        nextPageButton = findViewById(R.id.nextPageButton);

        String base64Pdf = getIntent().getStringExtra(EXTRA_PDF_BASE64);
        if (base64Pdf == null || base64Pdf.isEmpty()) {
            Toast.makeText(this, "No PDF data provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayPdf(base64Pdf);

        prevPageButton.setOnClickListener(v -> showPage(pageIndex - 1));
        nextPageButton.setOnClickListener(v -> showPage(pageIndex + 1));
    }

    private void displayPdf(String base64Pdf) {
        try {
            byte[] pdfData = android.util.Base64.decode(base64Pdf, android.util.Base64.DEFAULT);

            // Create a temporary file to hold the PDF data
            File tempFile = File.createTempFile("temp_pdf", ".pdf", getCacheDir());
            tempFile.deleteOnExit();

            OutputStream outputStream = new FileOutputStream(tempFile);
            outputStream.write(pdfData);
            outputStream.close();

            fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(fileDescriptor);

            showPage(0);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to display PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPage(int pageIndex) {
        if (pdfRenderer == null || pageIndex < 0 || pageIndex >= pdfRenderer.getPageCount()) {
            return;
        }

        if (currentPage != null) {
            currentPage.close();
        }

        currentPage = pdfRenderer.openPage(pageIndex);
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        pdfImageView.setImageBitmap(bitmap);

        this.pageIndex = pageIndex;
        prevPageButton.setEnabled(pageIndex > 0);
        nextPageButton.setEnabled(pageIndex < pdfRenderer.getPageCount() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (currentPage != null) {
                currentPage.close();
            }
            if (pdfRenderer != null) {
                pdfRenderer.close();
            }
            if (fileDescriptor != null) {
                fileDescriptor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}