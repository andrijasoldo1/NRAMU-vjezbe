package ba.sum.fsre.mymath.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import ba.sum.fsre.mymath.DetailsActivity;
import ba.sum.fsre.mymath.R;

public class LoginFragment extends Fragment {
    FirebaseAuth mAuth;

    public LoginFragment() {
        super();
    }

    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        this.mAuth = FirebaseAuth.getInstance();
        EditText emailTxt = v.findViewById(R.id.emailTxt);
        EditText passwordTxt = v.findViewById(R.id.passwordTxt);
        Button loginBtn = v.findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailTxt.getText().toString();
                String password = passwordTxt.getText().toString();
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(v.getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(v.getContext(), DetailsActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(v.getContext(), "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return v;
    }
}