package com.example.budgettracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "Login Activity";

    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Login");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        swipeToRefresh();

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.progressBar);

        authProfile = FirebaseAuth.getInstance();

        TextView txtForgot = findViewById(R.id.text_forgot_pass);
        txtForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Reset your password here", Toast.LENGTH_LONG).show();
                startActivity(new Intent(LoginActivity.this, ForgotPassword.class));
            }
        });
        //password icon
        ImageView imagevieweyecon = findViewById(R.id.imageView_show_hide_pass);
        imagevieweyecon.setImageResource(R.drawable.ic_hide_pwd);
        imagevieweyecon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //visible to invisible
                    loginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //change icon
                    imagevieweyecon.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    loginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imagevieweyecon.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        //login user
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textEmail = loginEmail.getText().toString();
                String textPass = loginPassword.getText().toString();

                if(TextUtils.isEmpty(textEmail)){
                    Toast.makeText(LoginActivity.this, "Please Enter your email", Toast.LENGTH_LONG).show();
                    loginEmail.setError("Email is required");
                    loginEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(LoginActivity.this, "Please re-enter your email", Toast.LENGTH_LONG).show();
                    loginEmail.setError(" Valid email is required");
                    loginEmail.requestFocus();
                } else if (TextUtils.isEmpty(textPass)) {
                    Toast.makeText(LoginActivity.this, "Please Enter your password", Toast.LENGTH_LONG).show();
                    loginPassword.setError("Password is required");
                    loginPassword.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmail,textPass);
                }
            }
        });

    }

    private void swipeToRefresh() {
        swipeContainer = findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startActivity(getIntent());
                finish();
                overridePendingTransition(0,0);
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void loginUser(String email, String pass) {
        authProfile.signInWithEmailAndPassword(email,pass).addOnCompleteListener(LoginActivity.this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    //get instance of current user
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    //check if email is verified
                    if (firebaseUser.isEmailVerified()){
                        Toast.makeText(LoginActivity.this, "You are logged in now", Toast.LENGTH_LONG).show();
                        //open profile
                        startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                        finish();
                    } else {
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut();
                        showAlertDialog();
                    }
                } else {
                    try{
                        throw task.getException();

                    } catch (FirebaseAuthInvalidUserException e){
                        loginEmail.setError("User does not exist! Please register");
                        loginEmail.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e){
                        loginEmail.setError("Invalid credentials!");
                        loginEmail.requestFocus();
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email Not Verified!");
        builder.setMessage("Please verify your email before logging in.");

        //open email app
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        //create alert box
        AlertDialog alertDialog = builder.create();
        //show alert
        alertDialog.show();
    }

    @Override
    protected void onStart(){
        super.onStart();
        if (authProfile.getCurrentUser() != null){
            Toast.makeText(LoginActivity.this, "Already Logged in!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "You can log in now.", Toast.LENGTH_LONG).show();
        }
    }
}