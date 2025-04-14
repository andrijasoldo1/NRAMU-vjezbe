package ba.sum.fsre.toplawv2.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import ba.sum.fsre.toplawv2.PdfViewerActivity;
import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.LocationPickerActivity;
import ba.sum.fsre.toplawv2.models.User;

public class DetailsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_PICKER_REQUEST_CODE = 200;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView firstNameTxt, lastNameTxt, eMailTxt, dateOfBirthTxt, telephoneTxt, genderTxt,
            addressTxt, placeOfBirthTxt, universityTxt, yearStartTxt, yearFinishTxt, roleTxt, cvTxt;

    private Spinner expertiseSpinner;
    private ImageView profileImageView;
    private Button requestLawyerStatusButton, selectPictureBtn, saveProfileBtn, getLocationBtn, selectLocationBtn;

    private String base64Image;
    private List<String> expertiseList;

    private static final int PICK_PDF_REQUEST = 3;
    private Button uploadCvBtn, viewCvBtn;
    private String base64CV;

    private LinearLayout cvThumbnailContainer, cvThumbnailPreview;
    private Button removeCvBtn;





    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_details, container, false);

        // Initialize Firebase and Location Services
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Bind views
        bindViews(v);

        // Load expertise options
        loadExpertiseOptions();

        // Load user data
        String uid = getUserId();
        if (uid != null) {
            loadUserData(uid);
        } else {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
        }

        // Button listeners
        saveProfileBtn.setOnClickListener(view -> {
            if (uid != null) {
                saveUserDetails(uid);
            } else {
                Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            }
        });

        selectPictureBtn.setOnClickListener(view -> openImagePicker());

        requestLawyerStatusButton.setOnClickListener(view -> {
            if (uid != null) {
                requestLawyerStatus(uid);
            } else {
                Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            }
        });

        getLocationBtn.setOnClickListener(view -> checkLocationPermission());

        selectLocationBtn.setOnClickListener(view -> openLocationPicker());

        uploadCvBtn.setOnClickListener(view -> openPdfPicker());

        // 👉 Tap on CV thumbnail to open full view
        cvThumbnailPreview.setOnClickListener(view -> {
            if (base64CV != null && !base64CV.isEmpty()) {
                openPdfViewerActivityWithBase64(base64CV);
            } else {
                Toast.makeText(getContext(), "Nema CV-a za prikaz.", Toast.LENGTH_SHORT).show();
            }
        });

        // 👉 Remove CV button
        removeCvBtn.setOnClickListener(view -> removeCvFromProfile());

        return v;
    }



    private void bindViews(View v) {
        firstNameTxt = v.findViewById(R.id.firstNameTxt);
        lastNameTxt = v.findViewById(R.id.lastNameTxt);
        eMailTxt = v.findViewById(R.id.eMailTxt);
        dateOfBirthTxt = v.findViewById(R.id.dateOfBirthTxt);
        telephoneTxt = v.findViewById(R.id.telephoneTxt);
        genderTxt = v.findViewById(R.id.genderTxt);
        addressTxt = v.findViewById(R.id.addressTxt);
        placeOfBirthTxt = v.findViewById(R.id.placeOfBirthTxt);
        universityTxt = v.findViewById(R.id.universityTxt);
        yearStartTxt = v.findViewById(R.id.yearStartTxt);
        yearFinishTxt = v.findViewById(R.id.yearFinishTxt);
        expertiseSpinner = v.findViewById(R.id.expertiseSpinner);
        roleTxt = v.findViewById(R.id.roleTxt);
        uploadCvBtn = v.findViewById(R.id.uploadCvBtn);


        profileImageView = v.findViewById(R.id.profileImageView);
        selectPictureBtn = v.findViewById(R.id.selectPictureBtn);
        requestLawyerStatusButton = v.findViewById(R.id.requestLawyerStatusButton);
        saveProfileBtn = v.findViewById(R.id.saveProfileBtn);
        getLocationBtn = v.findViewById(R.id.getLocationBtn);
        selectLocationBtn = v.findViewById(R.id.selectLocationBtn);
        cvThumbnailContainer = v.findViewById(R.id.cvThumbnailContainer);
        cvThumbnailPreview = v.findViewById(R.id.cvThumbnailPreview);
        removeCvBtn = v.findViewById(R.id.removeCvBtn);

    }

    private void loadExpertiseOptions() {
        expertiseList = new ArrayList<>();
        db.collection("expertises").get().addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot doc : querySnapshot) {
                expertiseList.add(doc.getString("name"));
            }
            populateExpertiseSpinner();
        }).addOnFailureListener(e -> {
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
            populateExpertiseSpinner();
        });
    }

    private void populateExpertiseSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, expertiseList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expertiseSpinner.setAdapter(adapter);
    }

    private String getUserId() {
        return mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
    }

    private void loadUserData(String uid) {
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    populateFields(user);
                    updateRequestButtonState(user);
                }
            }
        });
    }

    private void populateFields(User user) {
        firstNameTxt.setText(user.getFirstName());
        lastNameTxt.setText(user.getLastName());
        eMailTxt.setText(user.geteMail());
        dateOfBirthTxt.setText(user.getDateOfBirth());
        telephoneTxt.setText(user.getTelephone());
        genderTxt.setText(user.getGender());
        addressTxt.setText(user.getAddress());
        placeOfBirthTxt.setText(user.getPlaceOfBirth());
        universityTxt.setText(user.getUniversity());
        yearStartTxt.setText(String.valueOf(user.getYearOfStartingUniversity()));
        yearFinishTxt.setText(String.valueOf(user.getYearOfFinishingUniversity()));
        roleTxt.setText(user.getRole());

        base64CV = user.getCV();
        if (base64CV != null && !base64CV.isEmpty()) {
            showPdfThumbnail(base64CV);
            cvThumbnailContainer.setVisibility(View.VISIBLE);
        } else {
            cvThumbnailContainer.setVisibility(View.GONE);
        }

        if (user.getPicture() != null) {
            byte[] decodedBytes = Base64.decode(user.getPicture(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            profileImageView.setImageBitmap(bitmap);
        }
    }


    private void updateRequestButtonState(User user) {
        if (user.isApproved()) {
            requestLawyerStatusButton.setEnabled(false);
            requestLawyerStatusButton.setText("Already a Lawyer");
        } else if (user.isLawyerRequestPending()) {
            requestLawyerStatusButton.setEnabled(false);
            requestLawyerStatusButton.setText("Request Pending");
        } else {
            requestLawyerStatusButton.setEnabled(true);
            requestLawyerStatusButton.setText("Request Lawyer Status");
        }
    }

    private void saveUserDetails(String uid) {
        // Retrieve text inputs safely
        String yearStartText = yearStartTxt.getText() != null ? yearStartTxt.getText().toString().trim() : "";
        String yearFinishText = yearFinishTxt.getText() != null ? yearFinishTxt.getText().toString().trim() : "";

        // Workaround for Java lambda restrictions
        final int[] yearStart = {0};
        final int[] yearFinish = {0};

        if (!TextUtils.isEmpty(yearStartText)) {
            try {
                yearStart[0] = Integer.parseInt(yearStartText);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Molimo unesite ispravnu godinu početka studiranja!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!TextUtils.isEmpty(yearFinishText)) {
            try {
                yearFinish[0] = Integer.parseInt(yearFinishText);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Molimo unesite ispravnu godinu završetka studiranja!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            User existingUser = documentSnapshot.exists() ? documentSnapshot.toObject(User.class) : null;

            // Determine the final image
            String finalImage = base64Image != null ? base64Image : (existingUser != null ? existingUser.getPicture() : null);

            // Create a new User object
            User user = new User(
                    firstNameTxt.getText().toString(),
                    lastNameTxt.getText().toString(),
                    eMailTxt.getText().toString(),
                    telephoneTxt.getText().toString(),
                    genderTxt.getText().toString(),
                    addressTxt.getText().toString(),
                    dateOfBirthTxt.getText().toString(),
                    placeOfBirthTxt.getText().toString(),
                    universityTxt.getText().toString(),
                    yearStart[0],  // Use array index
                    yearFinish[0], // Use array index
                    expertiseSpinner.getSelectedItem().toString(),
                    false,
                    false,
                    roleTxt.getText().toString(),
                    base64CV,
                    finalImage
            );

            db.collection("users").document(uid).set(user)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(getContext(), "Profil uspješno ažuriran!", Toast.LENGTH_SHORT).show()
                    ).addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Greška pri spremanju profila.", Toast.LENGTH_SHORT).show()
                    );
        });
    }





    private void requestLawyerStatus(String uid) {
        db.collection("users").document(uid).update("lawyerRequestPending", true).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Request submitted.", Toast.LENGTH_SHORT).show();
            requestLawyerStatusButton.setEnabled(false);
            requestLawyerStatusButton.setText("Request Pending");
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void convertImageToBase64(Uri imageUri) {
        try {
            // Check MIME type before proceeding
            String mimeType = requireContext().getContentResolver().getType(imageUri);
            if (mimeType == null || (!mimeType.equals("image/jpeg") && !mimeType.equals("image/jpg"))) {
                Toast.makeText(getContext(), "Samo JPG/JPEG formati su dozvoljeni!", Toast.LENGTH_SHORT).show();
                return; // Stop processing if the file is not JPG/JPEG
            }

            // Get file size before processing
            long fileSizeInBytes = getImageSizeInBytes(imageUri);
            long fileSizeInKB = fileSizeInBytes / 1024; // Convert to KB

            // If image is bigger than 1MB, show error and stop processing
            if (fileSizeInKB > 1024) { // 1MB = 1024KB
                Toast.makeText(getContext(), "Odabrana slika je prevelika! Maksimalna veličina je 1MB.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Try to load the image
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);

            if (bitmap == null) {
                Toast.makeText(getContext(), "Greška pri učitavanju slike.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Compress image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int quality = 50; // Default compression quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

            // Convert to Base64
            base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            // Show the image ONLY if it's valid
            profileImageView.setImageBitmap(bitmap);

            // Save image to Firestore
            saveImageToFirestore(base64Image);

        } catch (IOException e) {
            Toast.makeText(getContext(), "Greška pri obradi slike.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Neočekivana greška pri učitavanju slike.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }







    private long getImageSizeInBytes(Uri imageUri) {
        try {
            return requireContext().getContentResolver().openAssetFileDescriptor(imageUri, "r").getLength();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }


    private void saveImageToFirestore(String base64Image) {
        String uid = getUserId();
        if (uid != null) {
            db.collection("users").document(uid).update("picture", base64Image);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    fetchPlaceName(latitude, longitude);
                } else {
                    Toast.makeText(getContext(), "Unable to fetch location.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Location access error.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPlaceName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                String placeName = addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
                placeOfBirthTxt.setText(placeName);
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to fetch place name.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openLocationPicker() {
        Intent intent = new Intent(getContext(), LocationPickerActivity.class);
        startActivityForResult(intent, LOCATION_PICKER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            convertImageToBase64(imageUri); // Handle image validation and display inside this function
        }
        if (requestCode == LOCATION_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            LatLng selectedLocation = data.getParcelableExtra(LocationPickerActivity.SELECTED_LOCATION);
            if (selectedLocation != null) {
                fetchPlaceName(selectedLocation.latitude, selectedLocation.longitude);
            }
        }

        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri pdfUri = data.getData();
            convertPdfToBase64(pdfUri);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(getContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Odaberite CV PDF"), PICK_PDF_REQUEST);
    }


    private void convertPdfToBase64(Uri pdfUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(pdfUri);
            byte[] pdfBytes = new byte[inputStream.available()];
            inputStream.read(pdfBytes);
            base64CV = Base64.encodeToString(pdfBytes, Base64.DEFAULT);

            saveCvToFirestore(base64CV);
            showPdfThumbnail(base64CV);
            cvThumbnailContainer.setVisibility(View.VISIBLE);

            Toast.makeText(getContext(), "CV uspješno učitan.", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(getContext(), "Greška pri učitavanju PDF-a.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void saveCvToFirestore(String base64CV) {
        String uid = getUserId();
        if (uid != null) {
            db.collection("users").document(uid).update("CV", base64CV);
        }
    }


    private void openPdfViewerActivityWithBase64(String base64) {
        try {
            Intent intent = new Intent(requireContext(), PdfViewerActivity.class);
            intent.putExtra(PdfViewerActivity.EXTRA_PDF_BASE64, base64);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Greška pri otvaranju CV-a.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showPdfThumbnail(String base64Pdf) {
        try {
            byte[] pdfBytes = Base64.decode(base64Pdf, Base64.DEFAULT);
            File pdfFile = new File(requireContext().getCacheDir(), "cv_preview.pdf");

            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                fos.write(pdfBytes);
            }

            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
            PdfRenderer.Page page = pdfRenderer.openPage(0);

            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            ImageView thumbnail = new ImageView(getContext());
            thumbnail.setImageBitmap(bitmap);
            thumbnail.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);

            cvThumbnailPreview.removeAllViews();
            cvThumbnailPreview.addView(thumbnail);

            // ✅ Save Base64 as tag so you always have access to it on click
            cvThumbnailPreview.setTag(base64Pdf);

            page.close();
            pdfRenderer.close();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Greška pri učitavanju CV pregleda.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void removeCvFromProfile() {
        String uid = getUserId();
        if (uid != null) {
            db.collection("users").document(uid).update("CV", null)
                    .addOnSuccessListener(aVoid -> {
                        base64CV = null;
                        cvThumbnailContainer.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "CV uklonjen.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Greška pri uklanjanju CV-a.", Toast.LENGTH_SHORT).show();
                    });
        }
    }




}