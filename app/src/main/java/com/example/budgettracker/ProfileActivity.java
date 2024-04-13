package com.example.budgettracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtWelcome, txtName, txtEmail, txtBirth, txtGender, txtMobile;
    private ProgressBar progressBar;
    private String name, email, birth, gender, mobile;
    private ImageView imageView;
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("User Profile");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        swipeToRefresh();

        txtWelcome = findViewById(R.id.show_welcome);
        txtName = findViewById(R.id.userName);
        txtEmail = findViewById(R.id.userEmail);
        txtBirth = findViewById(R.id.userBirth);
        txtGender = findViewById(R.id.userGender);
        txtMobile = findViewById(R.id.userMobile);
        progressBar = findViewById(R.id.progressBar);

        //set onclicklistener on image view

        imageView =  findViewById(R.id.image_dp);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, UploadProfilePic.class);
                startActivity(intent);
            }
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null){
            Toast.makeText(ProfileActivity.this, "User details not available at the moment.",
                    Toast.LENGTH_LONG).show();
        } else {
            checkifEmailVerified(firebaseUser);
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }
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

    private void checkifEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }


    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Email Not Verified!");
        builder.setMessage("Please verify your email now.");

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

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        //data extraction on registered users
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserDeets readUserDetails = snapshot.getValue(UserDeets.class);
                if (readUserDetails != null){
                    name = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    birth = readUserDetails.birth;
                    gender = readUserDetails.gender;
                    mobile = readUserDetails.mobile;

                    txtWelcome.setText("Welcome, " + name + "!");
                    txtName.setText(name);
                    txtEmail.setText(email);
                    txtBirth.setText(birth);
                    txtGender.setText(gender);
                    txtMobile.setText(mobile);

                    //set user img(after uploading)
                    Uri uri = firebaseUser.getPhotoUrl();

                    //picasso again since img is not local
                    Picasso.get().load(uri).into(imageView);
                } else {
                    Toast.makeText(ProfileActivity.this, "Something went wrong!",
                            Toast.LENGTH_LONG).show();

                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Something went wrong!",
                        Toast.LENGTH_LONG).show();
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
            NavUtils.navigateUpFromSameTask(ProfileActivity.this);
            finish();
        } else if (id == R.id.menu_refresh){
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent (ProfileActivity.this, UpdateProfile.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_profile) {
            Intent intent = new Intent (ProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_email) {
            Intent intent = new Intent (ProfileActivity.this, UpdateEmail.class);
            startActivity(intent);
        } else if (id == R.id.menu_change_pass) {
            Intent intent = new Intent (ProfileActivity.this, ChangePassword.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_delete) {
            Intent intent = new Intent (ProfileActivity.this, DeleteUser.class);
            startActivity(intent);
        } /*else if (id == R.id.menu_home) {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
        }*/else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(ProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);

            //clear stack instance & close activity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(ProfileActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}