package ba.sum.fsre.toplawv2.fragments;

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
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.toplawv2.PdfViewerActivity;
import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.adapters.CaseAdapter;
import ba.sum.fsre.toplawv2.models.Case;

public class UserCasesFragment extends Fragment {

    private static final int PICK_DOCUMENT_REQUEST = 1;

    private FirebaseFirestore db;
    private ListView listView;
    private List<Case> userCases;
    private CaseAdapter adapter;

    private EditText caseNameInput, caseDescriptionInput, casePriceInput;

    private CheckBox caseAnonymousInput;  // instead of EditText
    private Spinner typeOfCaseSpinner, statusSpinner;
    private Button saveCaseButton, clearFormButton, selectDocumentButton, toggleFormButton;

    private boolean isFormVisible = true;

    private boolean isListVisible = false;
    private Case currentEditingCase = null;
    private List<String> attachedDocumentsBase64;
    private List<String> expertiseList;
    private List<String> statusList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_cases, container, false);

        db = FirebaseFirestore.getInstance();
        listView = view.findViewById(R.id.listView);
        listView.setVisibility(View.GONE);
        // Initialize UI elements
        toggleFormButton = view.findViewById(R.id.toggleFormButton);
        caseNameInput = view.findViewById(R.id.caseNameInput);
        caseDescriptionInput = view.findViewById(R.id.caseDescriptionInput);
        casePriceInput = view.findViewById(R.id.casePriceInput);
        typeOfCaseSpinner = view.findViewById(R.id.caseTypeInput);
        statusSpinner = view.findViewById(R.id.status_spinner);
        caseAnonymousInput = view.findViewById(R.id.caseAnonymousInput);
        saveCaseButton = view.findViewById(R.id.saveCaseButton);
        clearFormButton = view.findViewById(R.id.clearCaseFormButton);
        selectDocumentButton = view.findViewById(R.id.selectDocumentButton);

        userCases = new ArrayList<>();
        attachedDocumentsBase64 = new ArrayList<>();
        expertiseList = new ArrayList<>();
        statusList = new ArrayList<>();
        adapter = new CaseAdapter(requireContext(), userCases, this::populateFormForEditing, true);
        listView.setAdapter(adapter);

        loadUserCases();
        loadExpertiseOptions();
        loadStatusOptions();

        saveCaseButton.setOnClickListener(v -> saveOrUpdateCase());
        clearFormButton.setOnClickListener(v -> clearForm());
        selectDocumentButton.setOnClickListener(v -> selectDocument());
        toggleFormButton.setOnClickListener(v -> toggleFormVisibility());

        return view;
    }




    private void loadExpertiseOptions() {
        db.collection("expertises").get()
                .addOnSuccessListener(querySnapshot -> {
                    expertiseList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        expertiseList.add(doc.getString("name"));
                    }
                    populateCaseTypeSpinner();
                })
                .addOnFailureListener(e -> {
                    expertiseList.add("Kazneno pravo");
                    expertiseList.add("Građansko pravo");
                    expertiseList.add("Trgovačko pravo");
                    expertiseList.add("Upravno pravo");
                    expertiseList.add("Radno pravo");
                    expertiseList.add("Obiteljsko pravo");
                    expertiseList.add("Nekretninsko pravo");
                    expertiseList.add("Intelektualno vlasništvo");
                    expertiseList.add("Međunarodno privatno pravo");
                    expertiseList.add("Ovršno pravo");
                    populateCaseTypeSpinner();
                });
    }

    private void loadStatusOptions() {
        statusList.clear();
        statusList.add("Open");
        statusList.add("In Progress");
        statusList.add("Closed");
        statusList.add("Archived");
        populateStatusSpinner();
    }

    private void populateCaseTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, expertiseList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeOfCaseSpinner.setAdapter(adapter);
    }

    private void populateStatusSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, statusList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);
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
                });
    }

    private void saveOrUpdateCase() {
        String name = caseNameInput.getText().toString().trim();
        String description = caseDescriptionInput.getText().toString().trim();
        String priceText = casePriceInput.getText().toString().trim();
        String typeOfCase = typeOfCaseSpinner.getSelectedItem().toString();
        String status = statusSpinner.getSelectedItem().toString();
        boolean isAnonymous = caseAnonymousInput.isChecked();

        if (name.isEmpty() || priceText.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceText);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (currentEditingCase == null) {
            Case newCase = new Case(name, userId, null, price, description, new ArrayList<>(attachedDocumentsBase64), status, isAnonymous, typeOfCase);
            db.collection("cases").add(newCase)
                    .addOnSuccessListener(documentReference -> {
                        newCase.setId(documentReference.getId());
                        userCases.add(newCase);
                        adapter.notifyDataSetChanged();
                        clearForm();
                    });
        } else {
            currentEditingCase.setName(name);
            currentEditingCase.setDescription(description);
            currentEditingCase.setPrice(price);
            currentEditingCase.setTypeOfCase(typeOfCase);
            currentEditingCase.setStatus(status);
            currentEditingCase.setAnonymous(isAnonymous);
            currentEditingCase.setAttachedDocumentation(new ArrayList<>(attachedDocumentsBase64));

            db.collection("cases").document(currentEditingCase.getId()).set(currentEditingCase)
                    .addOnSuccessListener(aVoid -> {
                        adapter.notifyDataSetChanged();
                        clearForm();
                        currentEditingCase = null;
                    });
        }
    }

    private void clearForm() {
        caseNameInput.setText("");
        caseDescriptionInput.setText("");
        casePriceInput.setText("");
        typeOfCaseSpinner.setSelection(0);
        statusSpinner.setSelection(0);
        caseAnonymousInput.setChecked(false);
        attachedDocumentsBase64.clear();
        updateAttachedDocsListView();
        currentEditingCase = null;
    }

    private void populateFormForEditing(Case existingCase) {
        currentEditingCase = existingCase;

        caseNameInput.setText(existingCase.getName());
        caseDescriptionInput.setText(existingCase.getDescription());
        casePriceInput.setText(String.valueOf(existingCase.getPrice()));
        caseAnonymousInput.setChecked(existingCase.isAnonymous());

        if (expertiseList.contains(existingCase.getTypeOfCase())) {
            typeOfCaseSpinner.setSelection(expertiseList.indexOf(existingCase.getTypeOfCase()));
        }
        if (statusList.contains(existingCase.getStatus())) {
            statusSpinner.setSelection(statusList.indexOf(existingCase.getStatus()));
        }

        attachedDocumentsBase64.clear();
        if (existingCase.getAttachedDocumentation() != null) {
            attachedDocumentsBase64.addAll(existingCase.getAttachedDocumentation());
        }
        updateAttachedDocsListView();
    }

    private void toggleFormVisibility() {
        androidx.core.widget.NestedScrollView formScrollView = requireView().findViewById(R.id.formScrollView);
        listView = requireView().findViewById(R.id.listView);
        formScrollView.setVisibility(isFormVisible ? View.GONE : View.VISIBLE);
        listView.setVisibility(isListVisible ? View.GONE : View.VISIBLE);
        toggleFormButton.setText(isFormVisible ? "Show Form" : "Hide Form");
        isFormVisible = !isFormVisible;
        isListVisible = !isListVisible;
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
            updateAttachedDocsListView();
            Toast.makeText(requireContext(), "Document attached successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to attach document", Toast.LENGTH_SHORT).show();
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

    private void updateAttachedDocsListView() {
        // Get the main container and thumbnail container
        LinearLayout documentThumbnailContainer = requireView().findViewById(R.id.documentThumbnailContainer);
        LinearLayout thumbnailContainer = requireView().findViewById(R.id.thumbnailContainer);
        Button detachButton = requireView().findViewById(R.id.detachDocumentButton);

        // Clear existing views in the thumbnail container
        thumbnailContainer.removeAllViews();

        if (attachedDocumentsBase64.isEmpty()) {
            // Hide the entire container if no documents exist
            documentThumbnailContainer.setVisibility(View.GONE);
            return;
        }

        // Show the container when there are documents
        documentThumbnailContainer.setVisibility(View.VISIBLE);

        for (int i = 0; i < attachedDocumentsBase64.size(); i++) {
            final int index = i;

            // Create a layout for each document thumbnail
            LinearLayout itemLayout = new LinearLayout(requireContext());
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(8, 8, 8, 8);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Create and configure the thumbnail
            ImageView thumbnail = new ImageView(requireContext());
            LinearLayout.LayoutParams thumbnailParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            thumbnail.setLayoutParams(thumbnailParams);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Bitmap pdfThumbnail = generatePdfThumbnailFromBase64(attachedDocumentsBase64.get(index));
            if (pdfThumbnail != null) {
                thumbnail.setImageBitmap(pdfThumbnail);
            } else {
                thumbnail.setImageResource(android.R.drawable.ic_menu_report_image);
            }

            // Open the PDF on thumbnail click
            thumbnail.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), PdfViewerActivity.class);
                intent.putExtra(PdfViewerActivity.EXTRA_PDF_BASE64, attachedDocumentsBase64.get(index));
                startActivity(intent);
            });

            // Add thumbnail to the item layout and container
            itemLayout.addView(thumbnail);
            thumbnailContainer.addView(itemLayout);
        }

        // Configure the detach button to clear all documents
        detachButton.setVisibility(View.VISIBLE);
        detachButton.setOnClickListener(v -> {
            attachedDocumentsBase64.clear();
            updateAttachedDocsListView();
        });
    }


}
