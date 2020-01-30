package com.example.quickparkingdriver;



import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    private EditText userEmail;
    private EditText userPassword;
    private Button userLoginBtn;
    private TextView dontHaveAccount;
    FirebaseAuth.AuthStateListener stateListener;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(getSupportActionBar().isShowing()){
            getSupportActionBar().hide();
        }
        firebaseAuth=FirebaseAuth.getInstance();
        stateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser=firebaseAuth.getCurrentUser();
                if(firebaseUser!=null)
                {
                    startActivity(new Intent(Login.this, MainActivity.class));
                    finish();
//                    FirebaseAuth.getInstance().signOut();
//                    Toast.makeText(UserLogin.this, "Signout", Toast.LENGTH_SHORT).show();

                }
            }
        };

        userEmail=findViewById(R.id.email);
        userPassword=findViewById(R.id.password);
        userLoginBtn=findViewById(R.id.loginBtn);
      //  dontHaveAccount=findViewById(R.id.dontHaveAccount);
//        dontHaveAccount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               // startActivity(new Intent(Login.this,UserSignUp.class));
//            }
//        });
        userLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String e=userEmail.getText().toString();
                String p=userPassword.getText().toString();
                if(e.isEmpty()){

                    userEmail.setError("Empty Email not allowed");
                }else if(p.isEmpty()){

                    userPassword.setError("Empty Password not allowed");
                }else{

                    LoginUser(e,p);

                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(stateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(stateListener);
    }

    private void LoginUser(String email,String pass){

        final ProgressDialog progressDialog=new ProgressDialog(Login.this);
        progressDialog.setTitle("Validation . . .");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,pass
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {

                if(task.isSuccessful()){

                    Toast.makeText(Login.this, "User Found", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }else{

                    Toast.makeText(Login.this, "USer Not Found", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });

    }
}
