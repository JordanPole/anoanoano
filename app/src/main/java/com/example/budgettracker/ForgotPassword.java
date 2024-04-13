package com.example.budgettracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPassword extends AppCompatActivity {

    private Button buttonResetPass;
    private EditText editTxtPassReset;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private final static String TAG = "ForgotPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Forgot Password");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        editTxtPassReset = findViewById(R.id.reset_email);
        buttonResetPass = findViewById(R.id.btn_resetPass);
        progressBar = findViewById(R.id.progressBar);

        buttonResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTxtPassReset.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(ForgotPassword.this,"Please enter your registered email", Toast.LENGTH_LONG).show();
                    editTxtPassReset.setError("Email is required");
                    editTxtPassReset.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(ForgotPassword.this,"Please enter your registered email", Toast.LENGTH_LONG).show();
                    editTxtPassReset.setError("Email is required");
                    editTxtPassReset.requestFocus();
                } else {
                    progressBar.setVisibility(View.GONE);
                    resetPassword(email);
                }
            }
        });
    }

    private void resetPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPassword.this,"Please check your inbox for password reset link",
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(ForgotPassword.this, MainActivity.class);

                    //clear stack instance & close activity
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e){
                        editTxtPassReset.setError("User does not exist!");
                        editTxtPassReset.requestFocus();
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(ForgotPassword.this,e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }

                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}