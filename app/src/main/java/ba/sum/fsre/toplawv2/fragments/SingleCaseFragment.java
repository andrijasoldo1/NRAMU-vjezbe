package ba.sum.fsre.toplawv2.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.toplawv2.PdfViewerActivity;
import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.models.Case;

public class SingleCaseFragment extends Fragment {

    private Case currentCase;
    private FirebaseFirestore db;
    private TextView nameTextView, priceTextView, caseTypeTextView, descriptionTextView, statusTextView, userTextView, documentText;
    private LinearLayout documentThumbnailContainer, thumbnailContainer;
    private List<String> attachedDocumentsBase64 = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_case, container, false);

        nameTextView = view.findViewById(R.id.case_title);
        userTextView = view.findViewById(R.id.name);
        priceTextView = view.findViewById(R.id.price);
        caseTypeTextView = view.findViewById(R.id.case_expertise);
        descriptionTextView = view.findViewById(R.id.description);
        statusTextView = view.findViewById(R.id.case_status);
        documentThumbnailContainer = view.findViewById(R.id.documentThumbnailContainer);
        thumbnailContainer = view.findViewById(R.id.thumbnailContainer);
        documentText = view.findViewById(R.id.document_text);

        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        Button applyButton = view.findViewById(R.id.apply_case_button);
        applyButton.setVisibility(View.GONE); // sakrij dok ne provjerimo status

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    Boolean isApproved = doc.getBoolean("isApproved");
                    if (Boolean.TRUE.equals(isApproved)) {
                        applyButton.setVisibility(View.VISIBLE);
                        applyButton.setOnClickListener(v -> {
                            if (currentCase != null && currentCase.getUserId() != null) {
                                OfferFormFragment offerFormFragment = OfferFormFragment.newInstance(currentCase.getUserId());
                                requireActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, offerFormFragment)
                                        .addToBackStack(null)
                                        .commit();
                            } else {
                                Toast.makeText(requireContext(), "Cannot apply, case data is not loaded.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Greška pri provjeri statusa korisnika", Toast.LENGTH_SHORT).show();
                });


        ImageButton addToStatusButton = view.findViewById(R.id.add_to_status_button);
        addToStatusButton.setOnClickListener(v -> {
            if (currentCase != null) {
                showStatusChoiceDialog(currentCase.getId());
            } else {
                Toast.makeText(requireContext(), "Case not loaded", Toast.LENGTH_SHORT).show();
            }
        });

        String caseId = getArguments() != null ? getArguments().getString("CASE_ID") : null;
        if (caseId != null) {
            loadCaseDetails(caseId);
        } else {
            Toast.makeText(requireContext(), "No case ID provided.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadCaseDetails(String caseId) {
        db = FirebaseFirestore.getInstance();
        db.collection("cases").document(caseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentCase = documentSnapshot.toObject(Case.class);
                        if (currentCase != null) {
                            currentCase.setId(documentSnapshot.getId());
                            updateUI();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Case not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load case details", Toast.LENGTH_SHORT).show());
    }

    private void updateUI() {
        if (currentCase != null) {
            nameTextView.setText(currentCase.getName() != null ? currentCase.getName() : "No name provided");
            priceTextView.setText(String.format("%.2f KM", currentCase.getPrice()));
            caseTypeTextView.setText(currentCase.getTypeOfCase() != null ? currentCase.getTypeOfCase() : "Unknown type");
            descriptionTextView.setText(currentCase.getDescription() != null ? currentCase.getDescription() : "No description provided");
            statusTextView.setText(currentCase.getStatus() != null ? currentCase.getStatus() : "No status available");

            if (currentCase.getUserId() != null) {
                db.collection("users").document(currentCase.getUserId())
                        .get()
                        .addOnSuccessListener(userSnapshot -> {
                            if (userSnapshot.exists()) {
                                String userName = userSnapshot.getString("eMail");
                                userTextView.setText(userName != null ? userName : "Unknown User");
                            } else {
                                userTextView.setText("User not found");
                            }
                        })
                        .addOnFailureListener(e -> userTextView.setText("Error loading user details"));
            } else {
                userTextView.setText("Anonymous");
            }

            if (currentCase.getAttachedDocumentation() != null) {
                attachedDocumentsBase64 = currentCase.getAttachedDocumentation();
                updateAttachedDocsListView();
            }
        }
    }

    private void updateAttachedDocsListView() {
        thumbnailContainer.removeAllViews();

        if (attachedDocumentsBase64.isEmpty()) {
            documentThumbnailContainer.setVisibility(View.GONE);
            documentText.setVisibility(View.GONE);
            return;
        }

        documentThumbnailContainer.setVisibility(View.VISIBLE);
        documentText.setVisibility(View.VISIBLE);

        for (int i = 0; i < attachedDocumentsBase64.size(); i++) {
            final int index = i;
            LinearLayout itemLayout = new LinearLayout(requireContext());
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(8, 8, 8, 8);

            ImageView thumbnail = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 400
            );
            thumbnail.setLayoutParams(params);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Bitmap pdfThumbnail = generatePdfThumbnailFromBase64(attachedDocumentsBase64.get(index));
            if (pdfThumbnail != null) {
                thumbnail.setImageBitmap(pdfThumbnail);
            } else {
                thumbnail.setImageResource(android.R.drawable.ic_menu_report_image);
            }

            thumbnail.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), PdfViewerActivity.class);
                intent.putExtra(PdfViewerActivity.EXTRA_PDF_BASE64, attachedDocumentsBase64.get(index));
                startActivity(intent);
            });

            itemLayout.addView(thumbnail);
            thumbnailContainer.addView(itemLayout);
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

    private ParcelFileDescriptor createParcelFileDescriptorFromInputStream(InputStream inputStream) throws IOException {
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
    }

    private void showStatusChoiceDialog(String caseId) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Dodaj slučaj u:")
                .setItems(new CharSequence[]{"Prihvaćeni slučajevi", "Riješeni slučajevi"}, (dialog, which) -> {
                    String collection = (which == 0) ? "accepted_cases" : "resolved_cases";
                    saveCaseStatusToUser(caseId, collection);
                })
                .setNegativeButton("Odustani", null)
                .show();
    }

    private void saveCaseStatusToUser(String caseId, String collection) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection(collection)
                .document(caseId)
                .set(new CaseReference(caseId))
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(requireContext(), "Slučaj dodan u " + (collection.equals("accepted_cases") ? "prihvaćene" : "riješene"), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public static class CaseReference {
        public String caseId;
        public long timestamp;

        public CaseReference() {} // Required for Firestore

        public CaseReference(String caseId) {
            this.caseId = caseId;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static SingleCaseFragment newInstance(String caseId) {
        SingleCaseFragment fragment = new SingleCaseFragment();
        Bundle args = new Bundle();
        args.putString("CASE_ID", caseId);
        fragment.setArguments(args);
        return fragment;
    }
}
