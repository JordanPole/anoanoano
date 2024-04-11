package com.example.budgettracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

public class UpdateEmail extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView txtViewAuth;
    private String userOldEmail, userNewEmail, userPass;
    private Button btnUpdateEmail;
    private EditText editTxtNewEmail, editTxtPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Update Profile Details");
        }

        progressBar = findViewById(R.id.progressBar);
        editTxtPass = findViewById(R.id.edit_update_email_verify);
        editTxtNewEmail = findViewById(R.id.edit_update_email_new);
        txtViewAuth = findViewById(R.id.txtView_update_email_auth);
        btnUpdateEmail = findViewById(R.id.btn_pass_update);

        btnUpdateEmail.setEnabled(false); // disabled until authenticated
        editTxtNewEmail.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        //set old email from firebase to txtview
        userOldEmail = firebaseUser.getEmail();
        TextView txtViewOldEmail = findViewById(R.id.txtView_update_email_old);
        txtViewOldEmail.setText(userOldEmail);
        
        if(firebaseUser.equals("")){
            Toast.makeText(UpdateEmail.this, "Something went wrong! User details not available", Toast.LENGTH_SHORT).show();
        } else {
            reAuthentication(firebaseUser);
        }
    }

    private void reAuthentication(FirebaseUser firebaseUser) {
        Button btnVerify = findViewById(R.id.btn_pass_auth);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPass = editTxtPass.getText().toString();

                if(TextUtils.isEmpty(userPass)){
                    Toast.makeText(UpdateEmail.this, "Password is needed to continue", Toast.LENGTH_SHORT).show();
                    editTxtPass.setError("Please enter your password to continue");
                    editTxtPass.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail,userPass);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()){
                               progressBar.setVisibility(View.GONE);

                               Toast.makeText(UpdateEmail.this, "Password has been verified," +
                                       " you can update your email now", Toast.LENGTH_SHORT).show();

                               //set txtview to show if user is authenticated
                               txtViewAuth.setText("You are authenticated. Update your email now");

                               editTxtNewEmail.setEnabled(true);
                               editTxtPass.setEnabled(false);
                               btnVerify.setEnabled(false);
                               btnUpdateEmail.setEnabled(true);

                               btnUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       userNewEmail = editTxtNewEmail.getText().toString();
                                       if (TextUtils.isEmpty(userNewEmail)){
                                           Toast.makeText(UpdateEmail.this, "New email is required", Toast.LENGTH_SHORT).show();
                                           editTxtNewEmail.setError("Please enter new email");
                                           editTxtNewEmail.requestFocus();
                                       } else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()) {
                                           Toast.makeText(UpdateEmail.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                                           editTxtNewEmail.setError("Please provide valid email");
                                           editTxtNewEmail.requestFocus();
                                       } else if (userOldEmail.matches(userNewEmail)) {
                                           Toast.makeText(UpdateEmail.this, "New email cannot be the same as old email!", Toast.LENGTH_SHORT).show();
                                           editTxtNewEmail.setError("Please enter new email");
                                           editTxtNewEmail.requestFocus();
                                       } else {
                                           progressBar.setVisibility(View.VISIBLE);
                                           updateEmail(firebaseUser);
                                       }
                                   }
                               });
                           } else {
                               try {
                                   throw task.getException();
                               } catch (Exception e){
                                   Toast.makeText(UpdateEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                               }
                           }
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.verifyBeforeUpdateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()){
                    firebaseUser.sendEmailVerification();

                    Toast.makeText(UpdateEmail.this, "Email has been updated. Please verify your email now", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateEmail.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e){
                        Toast.makeText(UpdateEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
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

        if (id == R.id.menu_refresh){
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent (UpdateEmail.this, UpdateProfile.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_profile) {
            Intent intent = new Intent (UpdateEmail.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_email) {
            Intent intent = new Intent (UpdateEmail.this, UpdateEmail.class);
            startActivity(intent);
        } else if (id == R.id.menu_change_pass) {
            Intent intent = new Intent (UpdateEmail.this, ChangePassword.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_delete) {
            Intent intent = new Intent (UpdateEmail.this, DeleteUser.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menu_home) {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
        }*/else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(UpdateEmail.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateEmail.this, MainActivity.class);

            //clear stack instance & close activity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UpdateEmail.this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

}