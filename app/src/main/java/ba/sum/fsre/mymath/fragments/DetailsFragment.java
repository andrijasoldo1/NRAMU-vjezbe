package ba.sum.fsre.mymath.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.LocationPickerActivity;
import ba.sum.fsre.mymath.models.User;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_details, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Bind views
        bindViews(v);

        // Initialize expertise list
        loadExpertiseOptions();

        // Load user data
        String uid = getUserId();
        if (uid != null) {
            loadUserData(uid);
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        // Button listeners
        saveProfileBtn.setOnClickListener(view -> {
            if (uid != null) {
                saveUserDetails(uid);
            } else {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });

        selectPictureBtn.setOnClickListener(view -> openImagePicker());

        requestLawyerStatusButton.setOnClickListener(view -> {
            if (uid != null) {
                requestLawyerStatus(uid);
            } else {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });

        getLocationBtn.setOnClickListener(view -> checkLocationPermission());

        selectLocationBtn.setOnClickListener(view -> openLocationPicker());

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
        cvTxt = v.findViewById(R.id.cvTxt);

        profileImageView = v.findViewById(R.id.profileImageView);
        selectPictureBtn = v.findViewById(R.id.selectPictureBtn);
        requestLawyerStatusButton = v.findViewById(R.id.requestLawyerStatusButton);
        saveProfileBtn = v.findViewById(R.id.saveProfileBtn);
        getLocationBtn = v.findViewById(R.id.getLocationBtn);
        selectLocationBtn = v.findViewById(R.id.selectLocationBtn);
    }

    private String getUserId() {
        return mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
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
            populateExpertiseSpinner();
        });
    }

    private void populateExpertiseSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, expertiseList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expertiseSpinner.setAdapter(adapter);
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
        cvTxt.setText(user.getCV());

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
                Integer.parseInt(yearStartTxt.getText().toString()),
                Integer.parseInt(yearFinishTxt.getText().toString()),
                expertiseSpinner.getSelectedItem().toString(),
                false,
                false,
                roleTxt.getText().toString(),
                cvTxt.getText().toString(),
                base64Image
        );

        db.collection("users").document(uid).set(user).addOnSuccessListener(aVoid ->
                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to save profile.", Toast.LENGTH_SHORT).show()
        );
    }

    private void requestLawyerStatus(String uid) {
        db.collection("users").document(uid).update("lawyerRequestPending", true).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Request submitted", Toast.LENGTH_SHORT).show();
            requestLawyerStatusButton.setEnabled(false);
            requestLawyerStatusButton.setText("Request Pending");
        });
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
            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Location access error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPlaceName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String placeName = addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
                placeOfBirthTxt.setText(placeName);
            } else {
                Toast.makeText(getContext(), "Unable to determine location name.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error fetching location name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openLocationPicker() {
        Intent intent = new Intent(getContext(), LocationPickerActivity.class);
        startActivityForResult(intent, LOCATION_PICKER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            LatLng selectedLocation = data.getParcelableExtra(LocationPickerActivity.SELECTED_LOCATION);
            if (selectedLocation != null) {
                double latitude = selectedLocation.latitude;
                double longitude = selectedLocation.longitude;
                fetchPlaceName(latitude, longitude);
            }
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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void convertImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            saveImageToFirestore(base64Image);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToFirestore(String base64Image) {
        String uid = getUserId();
        if (uid != null) {
            db.collection("users").document(uid).update("picture", base64Image);
        }
    }
}
