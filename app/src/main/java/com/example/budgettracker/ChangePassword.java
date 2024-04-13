package com.example.budgettracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private EditText editPassCurr, editPassNew, editPassConfirm;
    private TextView txtViewAuth;
    private Button btnChangePass, btnReAuthenticate;
    private ProgressBar progressBar;
    private String userPassCurr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Change Password");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        editPassCurr = findViewById(R.id.edit_change_pass);
        editPassNew = findViewById(R.id.edit_change_pass_new);
        editPassConfirm = findViewById(R.id.edit_confirm_pass_new);
        txtViewAuth = findViewById(R.id.txtView_change_pass_auth);
        progressBar = findViewById(R.id.progressBar);
        btnReAuthenticate = findViewById(R.id.btn_pass_auth);
        btnChangePass = findViewById(R.id.btn_pass_change);

        //disable new password and conform password
        editPassNew.setEnabled(false);
        editPassConfirm.setEnabled(false);
        btnChangePass.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser.equals("")){
            Toast.makeText(this, "Oops! Something went wrong",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePassword.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);
        }

    }

    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        btnReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPassCurr = editPassCurr.getText().toString();

                if(TextUtils.isEmpty(userPassCurr)){
                    Toast.makeText(ChangePassword.this, "Password is needed!", Toast.LENGTH_SHORT).show();
                    editPassCurr.setError("Please enter your current password to authenticate.");
                    editPassCurr.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    //reauthenticate user
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPassCurr);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                //disable first relative layout and enable second relative layout
                                editPassCurr.setEnabled(false);
                                editPassNew.setEnabled(true);
                                editPassConfirm.setEnabled(true);

                                btnReAuthenticate.setEnabled(false);
                                btnChangePass.setEnabled(true);

                                txtViewAuth.setText("You are authenticated/verified, change your password now.");
                                Toast.makeText(ChangePassword.this, "Password has been verified, change password now.", Toast.LENGTH_SHORT).show();

                                btnChangePass.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePass(firebaseUser);
                                    }
                                });


                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void changePass(FirebaseUser firebaseUser) {
        String newPass = editPassNew.getText().toString();
        String confirmPass = editPassConfirm.getText().toString();

        if (TextUtils.isEmpty(newPass)){
            Toast.makeText(this, "New password is needed.", Toast.LENGTH_SHORT).show();
            editPassNew.setError("Please enter your new password");
            editPassNew.requestFocus();
        } else if (TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
            editPassConfirm.setError("Please re-enter your new password");
            editPassConfirm.requestFocus();
        } else if (!newPass.matches(confirmPass)) {
            Toast.makeText(this, "Password did not match", Toast.LENGTH_SHORT).show();
            editPassConfirm.setError("Please re-enter same password");
            editPassConfirm.requestFocus();
        } else if (userPassCurr.matches(newPass)) {
            Toast.makeText(this, "New password cannot be the same as old password", Toast.LENGTH_SHORT).show();
            editPassNew.setError("Please enter a new password");
            editPassNew.requestFocus();
        } else {
            progressBar.setVisibility(View.VISIBLE);

            firebaseUser.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChangePassword.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePassword.this, ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
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
            NavUtils.navigateUpFromSameTask(ChangePassword.this);
            finish();
        } else if (id == R.id.menu_refresh){
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent (ChangePassword.this, UpdateProfile.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_profile) {
            Intent intent = new Intent (ChangePassword.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_update_email) {
            Intent intent = new Intent (ChangePassword.this, UpdateEmail.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_change_pass) {
            Intent intent = new Intent (ChangePassword.this, ChangePassword.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_delete) {
            Intent intent = new Intent (ChangePassword.this, DeleteUser.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menu_home) {
            Intent intent = new Intent(ChangePassword.this, HomeActivity.class);
            startActivity(intent);
        }*/else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(ChangePassword.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ChangePassword.this, MainActivity.class);

            //clear stack instance & close activity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(ChangePassword.this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}