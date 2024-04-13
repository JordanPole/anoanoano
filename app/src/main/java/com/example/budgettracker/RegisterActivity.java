package com.example.budgettracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText editName, editEmail, editBirth, editMobile,
            editPass, editConfirm;
    private ProgressBar progressBar;
    private RadioGroup radioGrpGender;
    private RadioButton radioGenderSelect;
    private DatePickerDialog picker;
    private static final String TAG="RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        //getSupportActionBar().setTitle("Register");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Register");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Toast.makeText(RegisterActivity.this, "You can register now", Toast.LENGTH_LONG).show();

        progressBar = findViewById(R.id.progressBar);
        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editBirth = findViewById(R.id.edit_birth);
        editMobile = findViewById(R.id.edit_mobile);
        editPass = findViewById(R.id.edit_password);
        editConfirm = findViewById(R.id.edit_confirm);

        // RadioButton Gender
        radioGrpGender = findViewById(R.id.radioGroup);
        radioGrpGender.clearCheck();

        //date picker setup
        editBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                //date picker dialog
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editBirth.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        Button buttonReg = findViewById(R.id.reg_btn);
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectGenderId = radioGrpGender.getCheckedRadioButtonId();
                radioGenderSelect = findViewById(selectGenderId);

                String textName = editName.getText().toString();
                String textEmail = editEmail.getText().toString();
                String textBirth = editBirth.getText().toString();
                String textMobile = editMobile.getText().toString();
                String textPassword = editPass.getText().toString();
                String textConfirm = editConfirm.getText().toString();
                String textGender;

                //regex mobile no. validation
                String mobileValid = "^09[0-9]{9}$"; //number needs to start with 09 and the rest kahit ano basta 11 digits
                Matcher mobileMatch;
                Pattern mobilePattern = Pattern.compile(mobileValid);
                mobileMatch = mobilePattern.matcher(textMobile);

                if (TextUtils.isEmpty(textName)){
                    Toast.makeText(RegisterActivity.this, "Please enter your name", Toast.LENGTH_LONG).show();
                    editName.setError("Name is required");
                    editName.requestFocus();
                } else if (TextUtils.isEmpty(textEmail)){
                    Toast.makeText(RegisterActivity.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    editEmail.setError("Email is required");
                    editEmail.requestFocus();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    Toast.makeText(RegisterActivity.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    editEmail.setError("Valid email is required");
                    editEmail.requestFocus();
                } else if (TextUtils.isEmpty(textBirth)){
                    Toast.makeText(RegisterActivity.this, "Please enter your Date of Birth", Toast.LENGTH_LONG).show();
                    editBirth.setError("Date of Birth is required");
                    editBirth.requestFocus();
                } else if (radioGrpGender.getCheckedRadioButtonId() == -1){
                    Toast.makeText(RegisterActivity.this, "Please select your gender", Toast.LENGTH_LONG).show();
                    radioGenderSelect.setError("Gender is required");
                    radioGenderSelect.requestFocus();
                } else if(TextUtils.isEmpty(textMobile)){
                    Toast.makeText(RegisterActivity.this, "Please enter your Mobile Number", Toast.LENGTH_LONG).show();
                    editMobile.setError("Mobile No. is required");
                    editMobile.requestFocus();
                } else if (textMobile.length() !=11) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter your Mobile Number", Toast.LENGTH_LONG).show();
                    editMobile.setError("Mobile No. should be 11 digits");
                    editMobile.requestFocus();
                } else if (!mobileMatch.find()) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter your Mobile Number", Toast.LENGTH_LONG).show();
                    editMobile.setError("Mobile No. is not valid, It should start in 09");
                    editMobile.requestFocus();
                } else if (TextUtils.isEmpty(textPassword)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your Password", Toast.LENGTH_LONG).show();
                    editPass.setError("Password is required");
                    editPass.requestFocus();
                } else if (textPassword.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password should be at least 8 digits", Toast.LENGTH_LONG).show();
                    editPass.setError("Password is too weak");
                    editPass.requestFocus();
                } else if (TextUtils.isEmpty(textConfirm)) {
                    Toast.makeText(RegisterActivity.this, "Please confirm your Password", Toast.LENGTH_LONG).show();
                    editConfirm.setError("Password confirmation is required");
                    editConfirm.requestFocus();
                } else if (!textPassword.equals(textConfirm)) {
                    Toast.makeText(RegisterActivity.this, "Password doesn't match", Toast.LENGTH_LONG).show();
                    editConfirm.setError("Password confirmation is required");
                    editConfirm.requestFocus();
                    //reset password shit
                    editPass.clearComposingText();
                    editConfirm.clearComposingText();
                } else {
                    textGender = radioGenderSelect.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(textName, textEmail, textBirth, textGender, textMobile,textPassword);
                    
                }
            }
        });


    }

    //user credentials
    private void registerUser(String textName, String textEmail, String textBirth, String textGender, String textMobile, String textPassword) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        //Create user profile shit
        auth.createUserWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(RegisterActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            FirebaseUser firebaseUser = auth.getCurrentUser();

                            //update display name user
                            UserProfileChangeRequest profileChange = new UserProfileChangeRequest.Builder().setDisplayName(textName).build();
                            firebaseUser.updateProfile(profileChange);

                            //user data lagay sa realtimedatabase
                            UserDeets userDeets = new UserDeets(textBirth, textGender, textMobile);

                            //data extraction
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
                            reference.child(firebaseUser.getUid()).setValue(userDeets).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){
                                        //vverify ang email
                                        firebaseUser.sendEmailVerification();
                                        Toast.makeText(RegisterActivity.this, "User registered successfully, Please verify your email",
                                                Toast.LENGTH_LONG).show();


                                    Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                                    // to prevent user returning to register activity on pressing back button after registration
                                    intent.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish(); //close register activity

                                    } else {
                                        Toast.makeText(RegisterActivity.this, "User registered failed, Please try again",
                                                Toast.LENGTH_LONG).show();
                                    }
                                    progressBar.setVisibility(View.GONE);

                                }
                            });

                        } else {
                            try{
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e){
                                editPass.setError("Your Password is too weak, Use mix lowercase & uppercase letters, symbols, and numbers.");
                                editPass.requestFocus();
                            } catch (FirebaseAuthEmailException e){
                                editPass.setError("Your Email is invalid or already in use.");
                                editPass.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e){
                                editPass.setError("User is already registered with this email.");
                                editPass.requestFocus();
                            } catch (Exception e){
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
