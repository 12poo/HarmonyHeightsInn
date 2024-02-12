package com.example.harmonyheightsinn;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private TextView haveAnAccount;
    private EditText EditTextusername, EditTextpassword, EditTextconfirmPassword;
    private Button signup;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    boolean isAdmin = document.getBoolean("isAdmin");
                                    if (isAdmin) {
                                        Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                        startActivity(intent);
                                    }
                                    finish();
                                }
                            } else {
                                Toast.makeText(SignupActivity.this, "Failed to fetch user details.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_form);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        haveAnAccount = findViewById(R.id.haveAnAccount);
        EditTextusername = findViewById(R.id.username);
        EditTextpassword = findViewById(R.id.password);
        EditTextconfirmPassword = findViewById(R.id.confirmPassword);
        signup = findViewById(R.id.signup);

        haveAnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        signup.setOnClickListener(v -> {
            String email, password, confirmPassword;
            email = String.valueOf(EditTextusername.getText());
            password = String.valueOf(EditTextpassword.getText());
            confirmPassword = String.valueOf(EditTextconfirmPassword.getText());

            if(TextUtils.isEmpty(email)){
                Toast.makeText(SignupActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }

            if(TextUtils.isEmpty(password)){
                Toast.makeText(SignupActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!password.equals(confirmPassword)){
                Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                boolean isAdmin = ((Switch) findViewById(R.id.admin_switch)).isChecked();
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                Map<String, Object> user = new HashMap<>();
                                user.put("isAdmin", isAdmin);

                                db.collection("users").document(userId).set(user);
                                Toast.makeText(SignupActivity.this, "Account created",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(SignupActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

    }
}
