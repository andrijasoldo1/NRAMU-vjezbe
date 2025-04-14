package ba.sum.fsre.toplawv2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.StartActivity;

public class RegisterFragment extends Fragment {
    private FirebaseAuth mAuth;
    private TextInputLayout emailLayout, passwordLayout, confirmPasswordLayout;
    private TextInputEditText emailInput, passwordInput, confirmPasswordInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        mAuth = FirebaseAuth.getInstance();

        // Dohvaćanje TextInputLayout i EditText polja
        emailLayout = view.findViewById(R.id.registerEmailTxt);
        passwordLayout = view.findViewById(R.id.registerPasswordTxt);
        confirmPasswordLayout = view.findViewById(R.id.registerConfirmPasswordTxt);

        emailInput = (TextInputEditText) emailLayout.getEditText();
        passwordInput = (TextInputEditText) passwordLayout.getEditText();
        confirmPasswordInput = (TextInputEditText) confirmPasswordLayout.getEditText();

        MaterialButton registerButton = view.findViewById(R.id.submitButton);

        // Klik na gumb za registraciju
        registerButton.setOnClickListener(v -> registerUser());

        return view;
    }

    private void registerUser() {
        // Provjera da li su unijeti podaci (null provjera zbog sigurnosti)
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";
        String confirmPassword = confirmPasswordInput.getText() != null ? confirmPasswordInput.getText().toString().trim() : "";

        // Provjera unosa
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            if (isAdded()) {
                Toast.makeText(getContext(), "Molimo popunite sva polja!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (!password.equals(confirmPassword)) {
            if (isAdded()) {
                Toast.makeText(getContext(), "Lozinke se ne podudaraju!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Pokretanje Firebase registracije
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!isAdded()) return; // Provjera da fragment još uvijek postoji

            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Registracija uspješna!", Toast.LENGTH_SHORT).show();

                if (getActivity() != null) {
                    // Prebacivanje na HomeActivity nakon registracije
                    startActivity(new Intent(getActivity(), StartActivity.class));
                    getActivity().finish();
                }
            } else {
                Toast.makeText(getContext(), "Registracija nije uspjela: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
