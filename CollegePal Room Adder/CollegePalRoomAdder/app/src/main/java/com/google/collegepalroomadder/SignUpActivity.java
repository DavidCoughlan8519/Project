package com.google.collegepalroomadder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignUpActivity extends AppCompatActivity {

    private EditText emailET;
    private EditText passwordET;
    private EditText passwordConfirmET;
    private Button signUpBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        emailET = (EditText)findViewById(R.id.emailET);
        passwordET = (EditText)findViewById(R.id.passwordET);
        passwordConfirmET = (EditText)findViewById(R.id.passwordConfirmET);
        signUpBtn = (Button)findViewById(R.id.createUserBtn);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    public boolean isValidPassword(String password, String passwordConfirm) {
        boolean result = false;
        if (password.equals(passwordConfirm)) {
            result = true;
        }
        return result;
    }

    public boolean isValidEmail(String email) {
        boolean result = false;
        if (email.contains("@") && !email.isEmpty()) {
            result = true;
        }
        return result;
    }

    public void registerUser() {
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String passwordConfirm = passwordConfirmET.getText().toString().trim();
        boolean isPasswordValid;
        boolean isEmailValid;

        if (isValidEmail(email)) {
            isEmailValid = true;
        } else {
            //TODO handle email being wrong output to user use red
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isValidPassword(password, passwordConfirm)) {
            isPasswordValid = true;
        } else {
            //TODO handle passwords not matching
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isEmailValid && isPasswordValid) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Toast.makeText(SignUpActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_LONG).show();
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Authentication failed." + task.getException(),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                //Get who is signed in
                                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                //Get their uid for the path to the database
                                if(currentFirebaseUser.getUid() != null) {
                                    String userUid = currentFirebaseUser.getUid();
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    myRef = database.getReference("users/" + userUid);
                                    DatabaseReference classRef = myRef.child("privilege");
                                    classRef.setValue("admin");
                                    Log.i("INFORMATION:","SUCCESS");
                                }

                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                finish();
                            }
                        }
                    });
        }
    }
}
