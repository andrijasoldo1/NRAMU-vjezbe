package ba.sum.fsre.mymath.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.mymath.PdfViewerActivity;
import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.adapters.CaseAdapter;
import ba.sum.fsre.mymath.models.Case;

public class UserCasesFragment extends Fragment {

    private static final int PICK_DOCUMENT_REQUEST = 1;

    private FirebaseFirestore db;
    private ListView listView;
    private List<Case> userCases;
    private CaseAdapter adapter;

    private EditText caseNameInput, caseDescriptionInput, casePriceInput, caseTypeInput, caseStatusInput, caseAnonymousInput;
    private Button saveCaseButton, clearFormButton, selectDocumentButton, toggleFormButton;
    private boolean isFormVisible = true;

    private Case currentEditingCase = null;
    private List<Uri> attachedDocumentsUris;
    private List<String> attachedDocumentsBase64;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_cases, container, false);

        db = FirebaseFirestore.getInstance();
        listView = view.findViewById(R.id.listView);

        toggleFormButton = view.findViewById(R.id.toggleFormButton);
        caseNameInput = view.findViewById(R.id.caseNameInput);
        caseDescriptionInput = view.findViewById(R.id.caseDescriptionInput);
        casePriceInput = view.findViewById(R.id.casePriceInput);
        caseTypeInput = view.findViewById(R.id.caseTypeInput);
        caseStatusInput = view.findViewById(R.id.caseStatusInput);
        caseAnonymousInput = view.findViewById(R.id.caseAnonymousInput);
        saveCaseButton = view.findViewById(R.id.saveCaseButton);
        clearFormButton = view.findViewById(R.id.clearCaseFormButton);
        selectDocumentButton = view.findViewById(R.id.selectDocumentButton);

        userCases = new ArrayList<>();
        attachedDocumentsUris = new ArrayList<>();
        attachedDocumentsBase64 = new ArrayList<>();
        adapter = new CaseAdapter(requireContext(), userCases, this::populateFormForEditing);
        listView.setAdapter(adapter);

        loadUserCases();

        saveCaseButton.setOnClickListener(v -> saveOrUpdateCase());
        clearFormButton.setOnClickListener(v -> clearForm());
        selectDocumentButton.setOnClickListener(v -> selectDocument());
        toggleFormButton.setOnClickListener(v -> toggleFormVisibility());

        return view;
    }

    private void toggleFormVisibility() {
        ScrollView formScrollView = requireView().findViewById(R.id.formScrollView);
        if (isFormVisible) {
            formScrollView.setVisibility(View.GONE);
            toggleFormButton.setText("Show Form");
        } else {
            formScrollView.setVisibility(View.VISIBLE);
            toggleFormButton.setText("Hide Form");
        }
        isFormVisible = !isFormVisible;
    }

    private void selectDocument() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_DOCUMENT_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_DOCUMENT_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri documentUri = data.getData();
            if (documentUri != null) {
                attachedDocumentsUris.add(documentUri);
                convertDocumentToBase64(documentUri);
            }
        }
    }

    private void convertDocumentToBase64(Uri documentUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(documentUri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            String base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
            attachedDocumentsBase64.add(base64String);
            Toast.makeText(requireContext(), "Document attached successfully!", Toast.LENGTH_SHORT).show();
            updateAttachedDocsListView();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to attach document", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAttachedDocsListView() {
        LinearLayout container = requireView().findViewById(R.id.documentThumbnailContainer);
        container.removeAllViews();

        for (int i = 0; i < attachedDocumentsBase64.size(); i++) {
            final int index = i;

            LinearLayout itemLayout = new LinearLayout(requireContext());
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(8, 8, 8, 8);

            ImageView thumbnail = new ImageView(requireContext());
            thumbnail.setLayoutParams(new LinearLayout.LayoutParams(100, 100));

            Bitmap pdfThumbnail = generatePdfThumbnailFromBase64(attachedDocumentsBase64.get(index));
            if (pdfThumbnail != null) {
                thumbnail.setImageBitmap(pdfThumbnail);
            } else {
                thumbnail.setImageResource(R.drawable.ic_launcher_foreground);
            }

            thumbnail.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), PdfViewerActivity.class);
                intent.putExtra(PdfViewerActivity.EXTRA_PDF_BASE64, attachedDocumentsBase64.get(index));
                startActivity(intent);
            });

            itemLayout.addView(thumbnail);

            Button detachButton = new Button(requireContext());
            detachButton.setText("Detach");
            detachButton.setOnClickListener(v -> {
                attachedDocumentsBase64.remove(index);
                updateAttachedDocsListView();
            });
            itemLayout.addView(detachButton);

            container.addView(itemLayout);
        }
    }

    private Bitmap generatePdfThumbnailFromBase64(String base64Document) {
        try {
            byte[] decodedBytes = Base64.decode(base64Document, Base64.DEFAULT);
            InputStream inputStream = new ByteArrayInputStream(decodedBytes);

            ParcelFileDescriptor pfd = createParcelFileDescriptorFromInputStream(inputStream);
            if (pfd != null) {
                PdfRenderer renderer = new PdfRenderer(pfd);
                PdfRenderer.Page page = renderer.openPage(0);

                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                page.close();
                renderer.close();
                pfd.close();

                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ParcelFileDescriptor createParcelFileDescriptorFromInputStream(InputStream inputStream) {
        try {
            File tempFile = File.createTempFile("temp_pdf", ".pdf", requireContext().getCacheDir());
            tempFile.deleteOnExit();

            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadUserCases() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("cases").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    userCases.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Case aCase = document.toObject(Case.class);
                        if (aCase != null) {
                            aCase.setId(document.getId());
                            userCases.add(aCase);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to load cases: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveOrUpdateCase() {
        String name = caseNameInput.getText().toString().trim();
        String description = caseDescriptionInput.getText().toString().trim();
        String priceText = casePriceInput.getText().toString().trim();
        String type = caseTypeInput.getText().toString().trim();
        String status = caseStatusInput.getText().toString().trim();
        String anonymousText = caseAnonymousInput.getText().toString().trim();

        if (name.isEmpty() || priceText.isEmpty() || type.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceText);
        boolean isAnonymous = Boolean.parseBoolean(anonymousText);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (currentEditingCase == null) {
            Case newCase = new Case(name, userId, null, price, description, attachedDocumentsBase64, status, isAnonymous, type);
            db.collection("cases").add(newCase)
                    .addOnSuccessListener(documentReference -> {
                        newCase.setId(documentReference.getId());
                        userCases.add(newCase);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Case saved successfully!", Toast.LENGTH_SHORT).show();
                        clearForm();
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save case: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            currentEditingCase.setName(name);
            currentEditingCase.setDescription(description);
            currentEditingCase.setPrice(price);
            currentEditingCase.setTypeOfCase(type);
            currentEditingCase.setStatus(status);
            currentEditingCase.setAnonymous(isAnonymous);
            currentEditingCase.setAttachedDocumentation(attachedDocumentsBase64);

            db.collection("cases").document(currentEditingCase.getId())
                    .set(currentEditingCase)
                    .addOnSuccessListener(aVoid -> {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Case updated successfully!", Toast.LENGTH_SHORT).show();
                        clearForm();
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update case: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void clearForm() {
        caseNameInput.setText("");
        caseDescriptionInput.setText("");
        casePriceInput.setText("");
        caseTypeInput.setText("");
        caseStatusInput.setText("");
        caseAnonymousInput.setText("");
        attachedDocumentsBase64.clear();
        attachedDocumentsUris.clear();
        updateAttachedDocsListView();
        currentEditingCase = null;
    }

    private void populateFormForEditing(Case existingCase) {
        currentEditingCase = existingCase;

        caseNameInput.setText(existingCase.getName());
        caseDescriptionInput.setText(existingCase.getDescription());
        casePriceInput.setText(String.valueOf(existingCase.getPrice()));
        caseTypeInput.setText(existingCase.getTypeOfCase());
        caseStatusInput.setText(existingCase.getStatus());
        caseAnonymousInput.setText(String.valueOf(existingCase.isAnonymous()));

        attachedDocumentsBase64 = existingCase.getAttachedDocumentation() != null ? existingCase.getAttachedDocumentation() : new ArrayList<>();
        attachedDocumentsUris.clear();
        updateAttachedDocsListView();
    }
}
