package ba.sum.fsre.toplawv2.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ba.sum.fsre.toplawv2.PdfViewerActivity;
import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.models.User;

public class LawyerPageFragment extends Fragment {

    private static final String ARG_LAWYER_EMAIL = "lawyerEmail";
    private FirebaseFirestore db;

    private ImageButton lawyerBackButton;
    private ImageView lawyerProfileImageView;
    private TextView lawyerNameText;
    private TextView lawyerTypeText;
    private TextView lawyerEmailText;
    private TextView lawyerPhoneText;
    private TextView lawyerDocumentText;
    private LinearLayout lawyerDocumentThumbnailContainer;
    private LinearLayout lawyerThumbnailContainer;
    private RatingBar lawyerOverallRatingBar;
    private TextView lawyerOverallRatingText;
    private LinearLayout lawyerReviewsContainer;

    private final List<String> documentBase64List = new ArrayList<>();

    public static LawyerPageFragment newInstance(String lawyerEmail) {
        LawyerPageFragment fragment = new LawyerPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LAWYER_EMAIL, lawyerEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lawyer_page, container, false);
        db = FirebaseFirestore.getInstance();

        // Initialize views
        lawyerBackButton = view.findViewById(R.id.LawyerBackButton);
        lawyerProfileImageView = view.findViewById(R.id.LawyerProfileImageView);
        lawyerNameText = view.findViewById(R.id.LawyerNameText);
        lawyerTypeText = view.findViewById(R.id.LawyerTypeText);
        lawyerEmailText = view.findViewById(R.id.LawyerEmailText);
        lawyerPhoneText = view.findViewById(R.id.LawyerPhoneText);
        lawyerDocumentText = view.findViewById(R.id.LawyerDocumentText);
        lawyerDocumentThumbnailContainer = view.findViewById(R.id.LawyerDocumentThumbnailContainer);
        lawyerThumbnailContainer = view.findViewById(R.id.LawyerThumbnailContainer);
        lawyerOverallRatingBar = view.findViewById(R.id.LawyerOverallRatingBar);
        lawyerOverallRatingText = view.findViewById(R.id.LawyerOverallRatingText);
        lawyerReviewsContainer = view.findViewById(R.id.LawyerReviewsContainer);

        // Handle back button
        lawyerBackButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // Get email and load data
        if (getArguments() != null) {
            String lawyerEmail = getArguments().getString(ARG_LAWYER_EMAIL);
            loadLawyerData(lawyerEmail);
        }

        return view;
    }

    private void loadLawyerData(String email) {
        db.collection("users").whereEqualTo("eMail", email).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        User lawyer = doc.toObject(User.class);
                        if (lawyer != null) {
                            lawyerNameText.setText(lawyer.getFirstName() + " " + lawyer.getLastName());
                            lawyerTypeText.setText(lawyer.getAreaOfExpertise() != null ? lawyer.getAreaOfExpertise() : "No expertise");
                            lawyerEmailText.setText(lawyer.geteMail());
                            lawyerPhoneText.setText(lawyer.getTelephone() != null ? lawyer.getTelephone() : "No phone");

                            setProfileImage(lawyer.getPicture());
                            loadRating(email);
                            loadReviews(email);

                            // If lawyer has documents
                            if (lawyer.getCV() != null && !lawyer.getCV().isEmpty()) {
                                documentBase64List.clear();
                                documentBase64List.add(lawyer.getCV());
                                populateDocumentThumbnails();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to load lawyer data", Toast.LENGTH_SHORT).show());
    }

    private void setProfileImage(String base64Image) {
        if (base64Image != null && !base64Image.isEmpty()) {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            lawyerProfileImageView.setImageBitmap(bitmap);
        } else {
            lawyerProfileImageView.setImageResource(R.drawable.ic_profile);
        }
    }

    private void loadRating(String email) {
        db.collection("reviews")
                .whereEqualTo("lawyerEmail", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    float sum = 0;
                    int count = querySnapshot.size();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Number rating = doc.getDouble("rating");
                        if (rating != null) sum += rating.floatValue();
                    }

                    if (count > 0) {
                        float avg = sum / count;
                        lawyerOverallRatingBar.setRating(avg);
                        lawyerOverallRatingText.setText(String.format(Locale.getDefault(), "%.1f", avg));
                    } else {
                        lawyerOverallRatingText.setText("No ratings yet");
                        lawyerOverallRatingBar.setRating(0);
                    }
                });
    }

    private void loadReviews(String email) {
        db.collection("reviews")
                .whereEqualTo("lawyerEmail", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    lawyerReviewsContainer.removeAllViews();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String comment = doc.getString("reviewText");
                        float rating = doc.getDouble("rating") != null ? doc.getDouble("rating").floatValue() : 0;

                        View reviewItem = LayoutInflater.from(getContext()).inflate(R.layout.item_review, lawyerReviewsContainer, false);

                        TextView name = reviewItem.findViewById(R.id.reviewerName);
                        TextView text = reviewItem.findViewById(R.id.reviewText);
                        RatingBar stars = reviewItem.findViewById(R.id.reviewRatingBar);
                        ImageView reviewerImageView = reviewItem.findViewById(R.id.reviewerImageView);

                        // Call loadReviewerName and update the TextView directly
                        loadReviewerName(doc.getString("reviewerEmail"), name);

                        text.setText(comment != null ? comment : "No comment");
                        stars.setRating(rating);

                        // Load reviewer image
                        loadReviewerImage(doc.getString("reviewerEmail"), reviewerImageView);

                        lawyerReviewsContainer.addView(reviewItem);
                    }
                });
    }


    private void loadReviewerName(String reviewerEmail, TextView reviewerNameTextView) {
        db.collection("users").whereEqualTo("eMail", reviewerEmail).get()
                .addOnSuccessListener(userSnapshot -> {
                    if (!userSnapshot.isEmpty()) {
                        User reviewer = userSnapshot.getDocuments().get(0).toObject(User.class);
                        if (reviewer != null) {
                            String firstName = reviewer.getFirstName();
                            String lastName = reviewer.getLastName();
                            String name = "Anonymous"; // Default name

                            if (firstName != null && lastName != null) {
                                name = firstName + " " + lastName;
                            } else if (firstName != null) {
                                name = firstName;
                            } else if (lastName != null) {
                                name = lastName;
                            }

                            reviewerNameTextView.setText(name);  // Set the name in the TextView
                        }
                    } else {
                        reviewerNameTextView.setText("Anonymous"); // Fallback if no user is found
                    }
                });
    }

    private void loadReviewerImage(String reviewerEmail, ImageView reviewerImageView) {
        db.collection("users").whereEqualTo("eMail", reviewerEmail).get()
                .addOnSuccessListener(userSnapshot -> {
                    if (!userSnapshot.isEmpty()) {
                        User reviewer = userSnapshot.getDocuments().get(0).toObject(User.class);
                        if (reviewer != null && reviewer.getPicture() != null) {
                            byte[] decodedBytes = Base64.decode(reviewer.getPicture(), Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            reviewerImageView.setImageBitmap(bitmap);
                        } else {
                            reviewerImageView.setImageResource(R.drawable.ic_profile);
                        }
                    }
                });
    }


    private void populateDocumentThumbnails() {
        lawyerThumbnailContainer.removeAllViews();


        if (documentBase64List.isEmpty()) {
            lawyerDocumentThumbnailContainer.setVisibility(View.GONE);
            lawyerDocumentText.setVisibility(View.GONE);
            return;
        }

        lawyerDocumentThumbnailContainer.setVisibility(View.VISIBLE);
        lawyerDocumentText.setVisibility(View.VISIBLE);

        for (int i = 0; i < documentBase64List.size(); i++) {
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

            Bitmap pdfThumbnail = generatePdfThumbnailFromBase64(documentBase64List.get(index));
            if (pdfThumbnail != null) {
                thumbnail.setImageBitmap(pdfThumbnail);
            } else {
                thumbnail.setImageResource(android.R.drawable.ic_menu_report_image);
            }

            thumbnail.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), PdfViewerActivity.class);
                intent.putExtra(PdfViewerActivity.EXTRA_PDF_BASE64, documentBase64List.get(index));
                startActivity(intent);
            });

            itemLayout.addView(thumbnail);
            lawyerThumbnailContainer.addView(itemLayout);
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




