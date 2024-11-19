package ba.sum.fsre.mymath.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.models.User;

public class DetailsFragment extends Fragment {

    FirebaseFirestore db;

    FirebaseAuth mAuth;

    public DetailsFragment() {
        super();
    }

    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_details, container, false);
        this.db = FirebaseFirestore.getInstance();

        this.mAuth = FirebaseAuth.getInstance();

        EditText firstNameTxt = v.findViewById(R.id.firstNameTxt);
        EditText lastNameTxt = v.findViewById(R.id.lastNameTxt);
        EditText dateOfBirthTxt = v.findViewById(R.id.dateOfBirthTxt);
        EditText telephoneTxt = v.findViewById(R.id.telephoneTxt);

        Button saveProfileBtn = v.findViewById(R.id.saveProfileBtn);

        String uid = mAuth.getCurrentUser().getUid();

        this.db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()){
                    User user = document.toObject(User.class);
                    firstNameTxt.setText(user.getFirstName());
                    lastNameTxt.setText(user.getLastName());
                    dateOfBirthTxt.setText(user.getDateOfBirth());
                    telephoneTxt.setText(user.getTelephone());
                }
            }
        });


        saveProfileBtn.setOnClickListener(view -> {
            String firstName = firstNameTxt.getText().toString();
            String lastName = lastNameTxt.getText().toString();
            String dateOfBirth = dateOfBirthTxt.getText().toString();
            String telephone = telephoneTxt.getText().toString();
            User newUser = new User(firstName, lastName, dateOfBirth, telephone);
            db.collection("users").document(uid).set(newUser);
        });
        return v;
    }
}