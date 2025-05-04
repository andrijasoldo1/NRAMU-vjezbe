package ba.sum.fsre.toplawv2.fragments; // Replace with your actual package name

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import ba.sum.fsre.toplawv2.DetailsActivity;
import ba.sum.fsre.toplawv2.PdfViewerActivity;
import ba.sum.fsre.toplawv2.R;

import ba.sum.fsre.toplawv2.models.User; // Your user model

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ImageView profileImageView;
    private TextView nameText, emailText, roleText, expertiseText, phoneText;
    private ImageButton editProfileButton, backButton;

    private LinearLayout thumbnailContainer;
    private LinearLayout documentThumbnailContainer;
    private TextView documentText;
    private List<String> attachedDocumentsBase64 = new ArrayList<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Bind views
        bindViews(view);

        // Load user data
        String uid = getUserId();
        if (uid != null) {
            loadUserProfile(uid);
        } else {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
        }

        // Set click listeners
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DetailsActivity.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });


        return view;
    }

    private void bindViews(View view) {
        profileImageView = view.findViewById(R.id.profileImageView);
        nameText = view.findViewById(R.id.nameText);
        emailText = view.findViewById(R.id.emailText);
        phoneText = view.findViewById(R.id.phoneText);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        thumbnailContainer = view.findViewById(R.id.thumbnailContainer);
        documentThumbnailContainer = view.findViewById(R.id.documentThumbnailContainer);
        documentText = view.findViewById(R.id.documentText);
        backButton = view.findViewById(R.id.backButton);

    }



    private String getUserId() {
        return mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
    }

    private void loadUserProfile(String uid) {
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    displayUserProfile(user);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }



    private void displayUserProfile(User user) {
        // Set user name
        String fullName = user.getFirstName() + " " + user.getLastName();
        nameText.setText(fullName);

        if (user.getCV() != null && !user.getCV().isEmpty()) {
            attachedDocumentsBase64.clear();
            attachedDocumentsBase64.add(user.getCV());
            updateAttachedDocsListView();
        }
        emailText.setText(user.geteMail());

        phoneText.setText(user.getTelephone());

        // Load profile image if available
        if (user.getPicture() != null && !user.getPicture().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(user.getPicture(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                profileImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("ProfileFragment", "Error loading profile image", e);
                // Set a default profile image
                profileImageView.setImageResource(R.drawable.logo);
            }
        } else {
            // Set a default profile image
            profileImageView.setImageResource(R.drawable.logo);
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

}
