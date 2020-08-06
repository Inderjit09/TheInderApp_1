package com.inderproduction.theinderapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.inderproduction.theinderapp.Utilities.Validations;

public class LoginActivity extends AppCompatActivity {
    private EditText username, password;
    private Button loginbutton;
    private TextView newuser, forgetpassword,skipLogin;
    private ProgressBar loginProgress;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private boolean isStartingActivity = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        if (auth.getCurrentUser() != null) {
            Intent loginIntent = new Intent(LoginActivity.this, ShoppingActivity.class);
            startActivity(loginIntent);
            finish();
        }

        username = findViewById(R.id.login_username);
        password = findViewById(R.id.login_password);
        loginbutton = findViewById(R.id.login_button);
        newuser = findViewById(R.id.login_newuser);
        skipLogin=findViewById(R.id.login_skip);
        loginProgress=findViewById(R.id.login_progress);

        if(getIntent().getStringExtra("sender") != null){
            isStartingActivity = false;
            skipLogin.setVisibility(View.GONE);
        };


        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Validations.isUsernameValid(username);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Validations.isEmailValid(username)) {
                    loginProgress.setVisibility(View.VISIBLE);
                    auth.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    loginProgress.setVisibility(View.INVISIBLE);
                                    if (task.isSuccessful()) {
                                        if(isStartingActivity){
                                            Intent loginIntent = new Intent(LoginActivity.this, ShoppingActivity.class);
                                            startActivity(loginIntent);

                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Incorrect Login", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        skipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent skipLoginIntent = new Intent(LoginActivity.this, ShoppingActivity.class);
                startActivity(skipLoginIntent);
                finish();
            }
        });

        newuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newUserIntent = new Intent(LoginActivity.this, Signup.class);
                startActivity(newUserIntent);
                finish();
            }

        });
    }
}
