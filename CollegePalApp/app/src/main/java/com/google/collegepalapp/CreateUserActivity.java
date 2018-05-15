package com.google.collegepalapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class CreateUserActivity extends AppCompatActivity {
    boolean hasIntent = false;
    private TextView buttonText;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String UID;
    private String selectedCourse;
    private String selectedDepartment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
        buttonText = (TextView) findViewById(R.id.textViewContinueCreate);

        if (getIntent().hasExtra("change")) {
            hasIntent = true;
            buttonText.setText("Back to Classes List");
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("LOGGED:", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("LOGGED:", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        Spinner spinnerDepart = (Spinner) findViewById(R.id.spinnerDepartment);
        Spinner spinnerCourse = (Spinner) findViewById(R.id.spinnerCourse);

        RelativeLayout continueButton = (RelativeLayout) findViewById(R.id.continueBtn);
        //continueButton.setVisibility(View.INVISIBLE);

        ArrayAdapter<String> departAdapt = new ArrayAdapter<String>(CreateUserActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.departments));
        departAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepart.setAdapter(departAdapt);
        spinnerDepart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartment = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


            ArrayAdapter<String> courseAdapt = new ArrayAdapter<String>(CreateUserActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.compScienceCourses));
            courseAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCourse.setAdapter(courseAdapt);
            spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedCourse = parent.getItemAtPosition(position).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create user and not a course change by existing user

                if (selectedCourse != null) {
                    if (!hasIntent)
                    {
                        Intent intent = new Intent(CreateUserActivity.this, FinishCreateUserActivity.class);
                        intent.putExtra("course", selectedCourse);
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(CreateUserActivity.this, MainActivity.class);
                        //intent.putExtra("course",selectedCourse);
                        writeClassToUserDb(selectedCourse);
                        startActivity(intent);
                        //Have to write this to the db!!!!!!!!!!!
                    }
                }else {
                    Toast toast = Toast.makeText(CreateUserActivity.this, "Pick a department and course", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    public void writeClassToUserDb(String selectedCourse) {
        {
            user = mAuth.getCurrentUser();
            UID = user.getUid();

            //Commit to DB here
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            DatabaseReference myRef = database.getReference("users/" + UID);
            myRef.child("class").setValue(selectedCourse);
            Log.i("INFORMATION:", "SUCCESS");
            Toast toast = Toast.makeText(CreateUserActivity.this, "Success:Course Changed", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
