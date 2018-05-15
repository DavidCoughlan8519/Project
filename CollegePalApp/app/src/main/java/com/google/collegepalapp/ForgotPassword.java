package com.google.collegepalapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ForgotPassword extends AppCompatActivity {

    private EditText emailAddress;
    private RelativeLayout forgotPasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        emailAddress = (EditText)findViewById(R.id.forgotEmail);
        forgotPasswordBtn = (RelativeLayout) findViewById(R.id.sendResetButton);

        forgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailAddress.getText().toString();
               if(email != null || isEmailValid(email))
                {
                    FirebaseAuth auth = FirebaseAuth.getInstance();

                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ForgotPassword.this, "Sent Email For Password Reset",
                                                Toast.LENGTH_LONG).show();

                                        Intent intent = new Intent(ForgotPassword.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(ForgotPassword.this, "Send Email For Password Reset Failed",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
                else{
                   Toast.makeText(ForgotPassword.this, "Email blank or not valid",
                           Toast.LENGTH_SHORT).show();
               }

            }
        });

    }

    private boolean isEmailValid(String email) {

        boolean isValid = false;
        if(email.contains("@"))
        {
            isValid = true;
            return isValid;
        }
        return isValid;
    }
}
