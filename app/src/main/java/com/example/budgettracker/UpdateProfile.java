package com.example.budgettracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfile extends AppCompatActivity {

    private EditText updateName, updateBirth, updateMobile;
    private RadioGroup updateGender;
    private RadioButton updateGenderSelect;
    private String txtName, txtBirth, txtGender, txtMobile;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Update Profile Details");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        progressBar = findViewById(R.id.progressBar);
        updateName = findViewById(R.id.editTxt_name);
        updateBirth = findViewById(R.id.editTxt_Birth);
        updateMobile = findViewById(R.id.editTxt_Mobile);

        updateGender = findViewById(R.id.group_update_gender);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //show profile data

        showProfile(firebaseUser);

        //upload picture
        TextView txtProfilePic = findViewById(R.id.txtView_update_picture);
        txtProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (UpdateProfile.this,UploadProfilePic.class);
                startActivity(intent);
                finish();
            }
        });

        //Update Email
        TextView txtUpdateEmail = findViewById(R.id.txtView_update_email);
        txtUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (UpdateProfile.this,UpdateEmail.class);
                startActivity(intent);
                finish();
            }
        });

        updateBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //extracting saved date through array
                String dateBirth[] = txtBirth.split("/");

                int day = Integer.parseInt(dateBirth[0]);
                int month = Integer.parseInt(dateBirth[1]) - 1; //to take care of month index starting from 0
                int year = Integer.parseInt(dateBirth[2]);


                //date picker dialog
                DatePickerDialog picker;
                picker = new DatePickerDialog(UpdateProfile.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        updateBirth.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        //Update profile btn
        Button btnUpdate = findViewById(R.id.btn_update_profile);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });

    }

    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGender = updateGender.getCheckedRadioButtonId();
        updateGenderSelect = findViewById(selectedGender);

        //regex mobile no. validation
        String mobileValid = "^09[0-9]{9}$"; //number needs to start with 09 and the rest kahit ano basta 11 digits
        Matcher mobileMatch;
        Pattern mobilePattern = Pattern.compile(mobileValid);
        mobileMatch = mobilePattern.matcher(txtMobile);

        if (TextUtils.isEmpty(txtName)){
            Toast.makeText(UpdateProfile.this, "Please enter your name", Toast.LENGTH_LONG).show();
            updateName.setError("Name is required");
            updateName.requestFocus();
        } else if (TextUtils.isEmpty(txtBirth)){
            Toast.makeText(UpdateProfile.this, "Please enter your Date of Birth", Toast.LENGTH_LONG).show();
            updateBirth.setError("Date of Birth is required");
            updateBirth.requestFocus();
        } else if (TextUtils.isEmpty(updateGenderSelect.getText())){
            Toast.makeText(UpdateProfile.this, "Please select your gender", Toast.LENGTH_LONG).show();
            updateGenderSelect.setError("Gender is required");
            updateGenderSelect.requestFocus();
        } else if(TextUtils.isEmpty(txtMobile)){
            Toast.makeText(UpdateProfile.this, "Please enter your Mobile Number", Toast.LENGTH_LONG).show();
            updateMobile.setError("Mobile No. is required");
            updateMobile.requestFocus();
        } else if (txtMobile.length() !=11) {
            Toast.makeText(UpdateProfile.this, "Please re-enter your Mobile Number", Toast.LENGTH_LONG).show();
            updateMobile.setError("Mobile No. should be 11 digits");
            updateMobile.requestFocus();
        } else if (!mobileMatch.find()) {
            Toast.makeText(UpdateProfile.this, "Please re-enter your Mobile Number", Toast.LENGTH_LONG).show();
            updateMobile.setError("Mobile No. is not valid, It should start in 09");
            updateMobile.requestFocus();
        } else {
            txtGender = updateGenderSelect.getText().toString();
            txtName = updateName.getText().toString();
            txtBirth = updateBirth.getText().toString();
            txtMobile = updateMobile.getText().toString();

            //enter user data to firebase database
            UserDeets writeDetails = new UserDeets(txtBirth, txtGender, txtMobile);

            //extract database reference for "Registered Users"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userID).setValue(writeDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){

                        //setting new display name
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().
                                setDisplayName(txtName).build();
                        firebaseUser.updateProfile(profileUpdate);

                        Toast.makeText(UpdateProfile.this, "Update Successful", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(UpdateProfile.this, ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(UpdateProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });

        }
    }

    //fetch data
    private void showProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        //extracting user references in forebase database
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        progressBar.setVisibility(View.VISIBLE);

        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserDeets readUserDeets = snapshot.getValue(UserDeets.class);
                if (readUserDeets != null){
                    txtName = firebaseUser.getDisplayName();
                    txtBirth = readUserDeets.birth;
                    txtGender = readUserDeets.gender;
                    txtMobile = readUserDeets.mobile;

                    updateName.setText(txtName);
                    updateBirth.setText(txtBirth);
                    updateMobile.setText(txtMobile);

                    //show gender through radio button
                    if(txtGender.equals("Male")){
                        updateGenderSelect = findViewById(R.id.radio_male);
                    } else {
                        updateGenderSelect = findViewById(R.id.radio_female);
                    }
                    updateGenderSelect.setChecked(true);
                } else {
                    Toast.makeText(UpdateProfile.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfile.this, "Something went wrong", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //Action bar meun
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //inflate menu items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //when item in menu selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(UpdateProfile.this);
            finish();
        } else if (id == R.id.menu_refresh){
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent (UpdateProfile.this, UpdateProfile.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_profile) {
            Intent intent = new Intent (UpdateProfile.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_update_email) {
            Intent intent = new Intent (UpdateProfile.this, UpdateEmail.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_change_pass) {
            Intent intent = new Intent (UpdateProfile.this, ChangePassword.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_delete) {
            Intent intent = new Intent (UpdateProfile.this, DeleteUser.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menu_home) {
            Intent intent = new Intent(UpdateProfile.this, HomeActivity.class);
            startActivity(intent);
        }*/else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(UpdateProfile.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateProfile.this, MainActivity.class);

            //clear stack instance & close activity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UpdateProfile.this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

}