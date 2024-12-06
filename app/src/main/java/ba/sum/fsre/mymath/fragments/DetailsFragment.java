package ba.sum.fsre.mymath.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.models.User;

public class DetailsFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText firstNameTxt, lastNameTxt, eMailTxt, dateOfBirthTxt, telephoneTxt, genderTxt,
            addressTxt, placeOfBirthTxt, universityTxt, yearStartTxt, yearFinishTxt,
            expertiseTxt, roleTxt, cvTxt, pictureTxt;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_details, container, false);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Bind UI elements
        bindViews(v);

        // Fetch user data
        String uid = mAuth.getCurrentUser().getUid();
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

        return v;
    }

    private void bindViews(View v) {
        // Bind all EditText fields to their corresponding IDs in the layout
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
        pictureTxt = v.findViewById(R.id.pictureTxt);
    }

    private void loadUserData(String uid) {
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    User user = document.toObject(User.class);
                    if (user != null) {
                        populateFields(user);
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
        // Populate UI fields with user data
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
        pictureTxt.setText(user.getPicture());
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
                    false, // Default value for isApproved
                    roleTxt.getText().toString(),
                    cvTxt.getText().toString(),
                    pictureTxt.getText().toString()
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
