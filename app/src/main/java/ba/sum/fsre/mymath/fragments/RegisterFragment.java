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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import ba.sum.fsre.mymath.R;

public class RegisterFragment extends Fragment {

    FirebaseAuth mAuth;

    public RegisterFragment() {
        super();
    }

    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        this.mAuth = FirebaseAuth.getInstance();

        EditText registerEmailTxt = v.findViewById(R.id.registerEmailTxt);
        EditText registerPasswordTxt = v.findViewById(R.id.registerPasswordTxt);
        EditText registerPasswordConfirmTxt = v.findViewById(R.id.registerConfirmPasswordTxt);
        Button registerBtn = v.findViewById(R.id.registerNewBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = registerEmailTxt.getText().toString();
                String password = registerPasswordTxt.getText().toString();
                String passwordConfirm = registerPasswordConfirmTxt.getText().toString();

                if (password.equals(passwordConfirm)) {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                // Registration successful
                                Toast.makeText(v.getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                            } else {
                                // Registration failed
                                Toast.makeText(v.getContext(), "Registration failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(v.getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return v;
    }
}