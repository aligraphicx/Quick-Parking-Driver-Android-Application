package com.example.quickparkingdriver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toolbar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class SupportCenter extends AppCompatActivity {

    private EditText problemName;
    private EditText problemDiscription;
    private Button submitBtn;

    private static final String TAG = "SupportCenter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_center);

        if(getSupportActionBar().isShowing()){

            getSupportActionBar().hide();
        }

        problemName=findViewById(R.id.support_center_problemName);
        problemDiscription=findViewById(R.id.support_center_problemDiscription);
        submitBtn=findViewById(R.id.support_center_submit);




        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String problem=problemName.getText().toString();
                final String problemDis=problemDiscription.getText().toString();


                if(problem.isEmpty()){


                }else if(problemDis.isEmpty()){

                }else{

                    AlertDialog submitAlert= new AlertDialog.Builder(SupportCenter.this)
                            .setTitle("Report Submit")
                            .setMessage("Do you want to submit this report")
                            .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    SupportCenterData supportCenterData=new SupportCenterData();
                                    supportCenterData.setSupportTitle(problem);
                                    supportCenterData.setSupportDis(problemDis);
                                    supportCenterData.setId(FirebaseAuth.getInstance().getUid());
                                    supportCenterData.setTime(String.valueOf(Calendar.getInstance().getTimeInMillis()));

                                    final ProgressDialog progressDialog=new ProgressDialog(SupportCenter.this);
                                    progressDialog.setMessage("Wait for server response");
                                    progressDialog.show();


                                    adminDBLink().setValue(supportCenterData, new DatabaseReference.CompletionListener() {

                                        @Override
                                        public void onComplete(DatabaseError error, DatabaseReference ref) {

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressDialog.dismiss();
                                                    AlertDialog successAlert=new AlertDialog.Builder(SupportCenter.this)
                                                            .setCancelable(false)
                                                            .setMessage("Your Report Submit Sccessfuly our team contact you with in 3 hours")
                                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    finish();
                                                                }
                                                            }).create();
                                                    successAlert.show();
                                                    Log.d(TAG, "onComplete: Report Saved");
                                                }
                                            });

                                        }
                                    });
                                }
                            })
                            .setNegativeButton("Cancel",null)
                            .setCancelable(false)
                            .create();
                    submitAlert.show();
                }
            }
        });

    }


    private DatabaseReference adminDBLink(){

        FirebaseOptions firebaseOptions=new FirebaseOptions.Builder()
                .setDatabaseUrl("https://quick-parking-hr.firebaseio.com/")
                .setApplicationId("1:462382846200:android:3c27a391b7e9f1da0bab0d")
                .setApiKey("AIzaSyDveQv90Msu1auKNX6oQSqMYgxEdfuFgCk")
                .build();
        DatabaseReference adminDB;

        try {
            FirebaseApp.initializeApp(this,firebaseOptions,"adminDB");
            adminDB =FirebaseDatabase.getInstance(FirebaseApp.getInstance("adminDB")).getReference("Quick Parking Support").child("Driver")
                    .child(String.valueOf(Calendar.getInstance().getTimeInMillis()));
            Log.d(TAG, "sendOrderToDriver: ");
        }catch (Exception e){
            adminDB =FirebaseDatabase.getInstance(FirebaseApp.getInstance("adminDB")).getReference("Quick Parking Support").child("Driver")
                    .child(String.valueOf(Calendar.getInstance().getTimeInMillis()));

        }



        return adminDB;



    }
}