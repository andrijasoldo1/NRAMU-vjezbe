package ba.sum.fsre.toplaw.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import ba.sum.fsre.toplaw.R;
import ba.sum.fsre.toplaw.StartActivity;

public class LoginFragment extends Fragment {
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private CheckBox rememberMeCheckBox;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "loginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // Initialize views
        emailLayout = view.findViewById(R.id.emailTxt);
        passwordLayout = view.findViewById(R.id.passwordTxt);
        emailInput = (TextInputEditText) emailLayout.getEditText();
        passwordInput = (TextInputEditText) passwordLayout.getEditText();
        rememberMeCheckBox = view.findViewById(R.id.rememberMeCheckBox);

        MaterialButton loginButton = view.findViewById(R.id.loginButton);
        MaterialButton googleSignInButton = view.findViewById(R.id.googleSignInBtn);
        MaterialButton forgotPasswordButton = view.findViewById(R.id.forgotPasswordBtn);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadSavedCredentials();

        // Set click listeners
        loginButton.setOnClickListener(v -> loginUser());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        forgotPasswordButton.setOnClickListener(v -> resetPassword());

        return view;
    }

    private void loadSavedCredentials() {
        boolean isRemembered = sharedPreferences.getBoolean(KEY_REMEMBER, false);

        if (isRemembered) {
            String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
            String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
            emailInput.setText(savedEmail);
            passwordInput.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
        }
    }

    private void saveCredentials(String email, String password, boolean remember) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (remember) {
            editor.putString(KEY_EMAIL, email);
            editor.putString(KEY_PASSWORD, password);
            editor.putBoolean(KEY_REMEMBER, true);
        } else {
            editor.clear();
        }
        editor.apply();
    }

    private void loginUser() {
        if (emailInput == null || passwordInput == null) {
            Toast.makeText(getContext(), "Error loading input fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            saveCredentials(email, password, rememberMeCheckBox.isChecked());
                            Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), StartActivity.class));
                            requireActivity().finish();
                        } else {
                            Toast.makeText(getContext(), "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Please fill in all fields!", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetPassword() {
        String email = emailInput.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Please enter your email.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Password reset email sent.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to send reset email.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(getContext(), "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(getActivity(), task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                Toast.makeText(getContext(), "Welcome " + (user != null ? user.getDisplayName() : ""), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), StartActivity.class));
            } else {
                Toast.makeText(getContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}