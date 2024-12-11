package ba.sum.fsre.mymath.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.models.User;

public class DetailsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText firstNameTxt, lastNameTxt, eMailTxt, dateOfBirthTxt, telephoneTxt, genderTxt,
            addressTxt, placeOfBirthTxt, universityTxt, yearStartTxt, yearFinishTxt,
            expertiseTxt, roleTxt, cvTxt;

    private ImageView profileImageView;
    private Button requestLawyerStatusButton, selectPictureBtn;
    private String base64Image;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_details, container, false);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Bind UI elements
        bindViews(v);

        // Fetch user data
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid != null) {
            loadUserData(uid);
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        // Save button functionality
        Button saveProfileBtn = v.findViewById(R.id.saveProfileBtn);
        saveProfileBtn.setOnClickListener(view -> {
            if (uid != null) {
                saveUserDetails(uid);
            } else {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });

        // Request lawyer status button functionality
        requestLawyerStatusButton.setOnClickListener(view -> {
            if (uid != null) {
                requestLawyerStatus(uid);
            } else {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });

        // Select picture button functionality
        selectPictureBtn.setOnClickListener(view -> openImagePicker());

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
        expertiseTxt = v.findViewById(R.id.expertiseTxt);
        roleTxt = v.findViewById(R.id.roleTxt);
        cvTxt = v.findViewById(R.id.cvTxt);
        profileImageView = v.findViewById(R.id.profileImageView);
        requestLawyerStatusButton = v.findViewById(R.id.requestLawyerStatusButton);
        selectPictureBtn = v.findViewById(R.id.selectPictureBtn);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);

            // Convert image to Base64
            convertImageToBase64(selectedImageUri);
        }
    }

    private void convertImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] imageBytes = baos.toByteArray();
            base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Save Base64 string to Firestore
            saveImageToFirestore(base64Image);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToFirestore(String base64Image) {
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid != null) {
            db.collection("users").document(uid)
                    .update("picture", base64Image)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update picture", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadUserData(String uid) {
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    User user = document.toObject(User.class);
                    if (user != null) {
                        populateFields(user);
                        updateRequestButtonState(user);
                    }
                } else {
                    Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
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
        expertiseTxt.setText(user.getAreaOfExpertise());
        roleTxt.setText(user.getRole());
        cvTxt.setText(user.getCV());

        if (user.getPicture() != null && !user.getPicture().isEmpty()) {
            byte[] decodedBytes = Base64.decode(user.getPicture(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            profileImageView.setImageBitmap(bitmap);
        }
    }

    private void requestLawyerStatus(String uid) {
        db.collection("users").document(uid).update("lawyerRequestPending", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Lawyer request submitted.", Toast.LENGTH_SHORT).show();
                    requestLawyerStatusButton.setEnabled(false);
                    requestLawyerStatusButton.setText("Request Pending");
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to submit request.", Toast.LENGTH_SHORT).show());
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
        // Validate input fields before saving
        if (validateFields()) {
            User updatedUser = new User(
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
                    expertiseTxt.getText().toString(),
                    false,
                    false,
                    roleTxt.getText().toString(),
                    cvTxt.getText().toString(),
                    base64Image // Save Base64 image
            );

            db.collection("users").document(uid).set(updatedUser).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to save profile.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private boolean validateFields() {
        if (firstNameTxt.getText().toString().isEmpty() || lastNameTxt.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Name fields cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (eMailTxt.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (yearStartTxt.getText().toString().isEmpty() || yearFinishTxt.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "University years cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
