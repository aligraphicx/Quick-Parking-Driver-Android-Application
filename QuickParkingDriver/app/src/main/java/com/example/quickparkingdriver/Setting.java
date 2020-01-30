package com.example.quickparkingdriver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Setting extends AppCompatActivity implements View.OnClickListener {

    private ImageView profileView;
    private TextView profileName;
    private TextView profileEmail;
    private ImageView editProfilePic;
    private ImageView editProfileName;
    private ImageView editProfilePassword;
    private ImageView deleteAccount;
    private ImageView logout;


    private FirebaseUser currentUser;
    private static final String TAG = "PersonalProfile";


    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;




    private AlertDialog pass_dialog;
    private AlertDialog profile_dialog;
    private AlertDialog userName_dialog;
    private AlertDialog phoneNumber_dialog;

    private final int PROFILE_SELECT_CODE=1;





    private ImageView pofileChangeProfileView;
    private Uri profileChange_imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if(getSupportActionBar().isShowing()){
            getSupportActionBar().hide();
        }
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        initObjets();
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        setHeaderView();
        initPasswordChangeBox();
        initProfilePicBox();
        userNameChangeBoxInit();
        mobileChangeBoxInit();







    }

    private void addressChangeInit(){


        AlertDialog.Builder addresssBox=new AlertDialog.Builder(this).setTitle("Home Address");
        View idCardLayout=LayoutInflater.from(this).inflate(R.layout.address_change_layout,null,false);
        addresssBox.setView(idCardLayout);
        final AlertDialog addressAlert=addresssBox.create();
        addressAlert.show();

        final ProgressDialog progressDialog=new ProgressDialog(this);

        progressDialog.setTitle("Loading");
        progressDialog.setMessage("fetching previous data");

        progressDialog.show();

        final EditText addressInput=idCardLayout.findViewById(R.id.address_change_InputAddress);
        Button addressEditBtn=idCardLayout.findViewById(R.id.address_change_editBtn);
        Button addressSetBtn=idCardLayout.findViewById(R.id.address_change_setBtn);


        getMyDB().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String perviousAddress=dataSnapshot.getValue(DriverType.class).getAddress();
                progressDialog.dismiss();
                if(perviousAddress==null){

                    addressInput.setText("Address Unavailable");
                }else {

                    addressInput.setText(perviousAddress);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        addressEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressInput.setEnabled(true);
                addressInput.setText("");

            }
        });

        addressSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addressStr=addressInput.getText().toString();

                if(addressStr.length()>5){
                    progressDialog.setMessage("Updating your data . . .");
                    progressDialog.show();

                    Map<String,Object> addressUpdate=new HashMap<>();
                    addressUpdate.put("address",addressStr);
                    getMyDB().updateChildren(addressUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                Toast.makeText(Setting.this, "Adrress Updated", Toast.LENGTH_SHORT).show();
                                addressInput.setEnabled(false);
                                addressAlert.dismiss();
                                progressDialog.dismiss();
                            }else{

                                progressDialog.dismiss();
                            }
                        }
                    });

                }else {

                    addressInput.setError("Address to Short");
                }
            }
        });





    }



    private void mobileChangeBoxInit(){

        final AlertDialog.Builder phoneVarificationBox=new AlertDialog.Builder(Setting.this);
        phoneVarificationBox.setTitle("Phone Number Update");
        View phoneVarificationLayout=LayoutInflater.from(this).inflate(R.layout.phone_number_change_layout,null,false);
        phoneVarificationBox.setView(phoneVarificationLayout);
        phoneNumber_dialog=phoneVarificationBox.create();
        final EditText phoneNumber=phoneVarificationLayout.findViewById(R.id.phone_edit_number);
        Button phoneVarifyBtn=phoneVarificationLayout.findViewById(R.id.edit_phoneNumberBtn);


        phoneVarifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(phoneNumber.getText().toString().length()==13){
                    Map<String,Object> phoneUpdateMap=new HashMap<>();
                    phoneUpdateMap.put("mobile",phoneNumber.getText().toString());

                            getMyDB().updateChildren(phoneUpdateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(Setting.this, "Phone Updated", Toast.LENGTH_SHORT).show();
                                phoneNumber_dialog.dismiss();
                            }
                        }
                    });

                }else{
                    phoneNumber.setError("Wrong Phone number");
                    phoneNumber.requestFocus();
                }
            }
        });


    }

    private void userNameChangeBoxInit(){


        AlertDialog.Builder userNameBuilderBox=new AlertDialog.Builder(Setting.this);

        userNameBuilderBox.setTitle("Profile Name Changing");
        View userNameChangelayout=LayoutInflater.from(this).inflate(R.layout.username_update_layout,null,false);
        userNameBuilderBox.setView(userNameChangelayout);
        userNameBuilderBox.setNegativeButton("Cancel",null);

        final EditText firstName=userNameChangelayout.findViewById(R.id.username_change_firstName);
        final EditText lastName=userNameChangelayout.findViewById(R.id.username_change_lastName);
        Button userNameChangeBtn=userNameChangelayout.findViewById(R.id.username_change_changeBtn);
        final TextView errorMsg=userNameChangelayout.findViewById(R.id.username_change_error);
        final ProgressDialog nameLoading=new ProgressDialog(this);
        nameLoading.setTitle("Loading");
        nameLoading.setMessage("wait for server response");


        userNameChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nameLoading.show();
                String fNameStr=firstName.getText().toString();
                String lName=lastName.getText().toString();
                Map<String,Object> userNameUpdateMap=new HashMap<>();
                userNameUpdateMap.put("firstName",fNameStr);
                userNameUpdateMap.put("lastName",lName);
                getMyDB().updateChildren(userNameUpdateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            Toast.makeText(Setting.this, "Successfully Changed", Toast.LENGTH_SHORT).show();
                            nameLoading.dismiss();
                            refreshThisActivity();
                        }else{

                            Toast.makeText(Setting.this, "Something went to wrong", Toast.LENGTH_SHORT).show();
                            nameLoading.dismiss();

                        }
                    }
                });


                if(!fNameStr.isEmpty()&&!lName.isEmpty()){


                }else{


                    errorMsg.setText("Empty Name Not Allowed");
                }

            }
        });


        userName_dialog=userNameBuilderBox.create();



    }

    private void initPasswordChangeBox(){

        View changePasswordLayout= LayoutInflater.from(this).inflate(R.layout.change_password_layout,null,false);
        Button passChangeBtn=changePasswordLayout.findViewById(R.id.changePasswordBtn);
        final EditText cuPass=changePasswordLayout.findViewById(R.id.changePass_currentPassword);
        final EditText newPass=changePasswordLayout.findViewById(R.id.changePass_newPassword);
        final EditText conNewPass=changePasswordLayout.findViewById(R.id.change_con_New_Password);
        final TextView errorMesage=changePasswordLayout.findViewById(R.id.changePass_Error);


        AlertDialog.Builder alertDialog=new AlertDialog.Builder(Setting.this);
        alertDialog.setTitle("Password Change Box");
        alertDialog.setView(changePasswordLayout);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });


        pass_dialog=alertDialog.create();
        passChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newPassStr=newPass.getText().toString();
                final String con_newPassStr=conNewPass.getText().toString();
                final String cuPassStr=cuPass.getText().toString();
                if(!newPassStr.isEmpty()){

                    if(newPassStr.equals(con_newPassStr)){

                        AlertDialog.Builder passwordChangeLoadBuil=new AlertDialog.Builder(Setting.this);
                        passwordChangeLoadBuil.setTitle("Password Validation");
                        passwordChangeLoadBuil.setCancelable(false);
                        passwordChangeLoadBuil.setMessage("wait for server response");
                        final AlertDialog passChangeLoading=passwordChangeLoadBuil.create();
                        passChangeLoading.show();

                        firebaseAuth.signInWithEmailAndPassword(firebaseUser.getEmail(),cuPassStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){

                                    firebaseUser.updatePassword(newPassStr).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                Toast.makeText(Setting.this, "Password is changed", Toast.LENGTH_SHORT).show();

                                                passChangeLoading.dismiss();
                                                pass_dialog.dismiss();

                                            }
                                        }
                                    });


                                }else{


                                    errorMesage.setText("Current password is wrong");
                                    passChangeLoading.dismiss();
                                }



                            }
                        });



                    }else{

                        errorMesage.setText("Password not matched");
                    }
                }else{

                    errorMesage.setText("Empty Filed not allowed");
                }


            }
        });



    }


    private void initObjets(){

        profileView=findViewById(R.id.personal_profilePic);
        profileName=findViewById(R.id.personal_profileName);
        profileEmail=findViewById(R.id.personal_email);
        editProfilePic=findViewById(R.id.edit_profilePicture);
        editProfileName=findViewById(R.id.edit_profileName);
        editProfilePassword=findViewById(R.id.edit_password);
        deleteAccount=findViewById(R.id.edit_deleteAccount);
        logout=findViewById(R.id.edit_logout);


        editProfilePic.setOnClickListener(this);
        editProfilePassword.setOnClickListener(this);
        editProfileName.setOnClickListener(this);
        logout.setOnClickListener(this);





    }
    private void setHeaderView(){
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Fetching user data . . .");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        getMyDB().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DriverType userData=dataSnapshot.getValue(DriverType.class);
                if(userData!=null){
                    Log.d(TAG, "onComplete: "+userData.getFirstName());
                    Log.d(TAG, "onComplete: "+userData.getLastName());
                    Log.d(TAG, "onComplete: "+userData.getEmail());
                    Log.d(TAG, "onComplete: "+userData.getPhoneNumber());
                    Log.d(TAG, "onComplete: "+userData.getAddress());
                    Log.d(TAG, "onComplete: "+userData.getProfileLink());
                    profileName.setText(userData.getFirstName()+" "+userData.getLastName());
                    profileEmail.setText(userData.getEmail());

                    if(userData.getProfileLink()!=null){

                        Glide.with(getApplicationContext()).load(userData.getProfileLink()).override(512,512).circleCrop().into(profileView);

                    }
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void initProfilePicBox(){

        View profileLayout=LayoutInflater.from(Setting.this).inflate(R.layout.profile_change_layout,null,false);
        AlertDialog.Builder profileBoxBuiler=new AlertDialog.Builder(Setting.this);
        profileBoxBuiler.setView(profileLayout);
        profileBoxBuiler.setTitle("Profile Change Box");
        profileBoxBuiler.setNegativeButton("Cancel",null);
        profile_dialog=profileBoxBuiler.create();

        pofileChangeProfileView=profileLayout.findViewById(R.id.profile_change_profilePic);
        Button profileSelectBtn=profileLayout.findViewById(R.id.profile_change_selectPic);
        Button profileSetBtn=profileLayout.findViewById(R.id.profile_change_set);



        pofileChangeProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,PROFILE_SELECT_CODE);

            }
        });

        profileSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,PROFILE_SELECT_CODE);

            }
        });

        profileSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(Setting.this, "Clicked", Toast.LENGTH_SHORT).show();
                final ProgressDialog pL= new ProgressDialog(Setting.this);
                pL.setTitle("Picture Uploading");
                pL.setMessage("Uploading");
                if(profileChange_imageUri!=null){
                    final StorageReference storageReference=getStorageReference();
                    Toast.makeText(Setting.this, "Uploading Start", Toast.LENGTH_SHORT).show();
                    UploadTask uploadTask;
                    uploadTask = storageReference.putFile(profileChange_imageUri);
                    uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            Log.d(TAG, "onProgress: "+taskSnapshot.getTotalByteCount()/1024+"/"+taskSnapshot.getBytesTransferred()/1024);
                            pL.show();
                            pL.setMessage("Uploading "+taskSnapshot.getTotalByteCount()/1024+"KB /"+taskSnapshot.getBytesTransferred()/1024+"KB");



                        }
                    });



                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            // Continue with the task to get the download URL

                            return storageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>()

                    {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: "+"Uploaded");
                                Uri downloadUri = task.getResult();
                                pL.dismiss();
                                profile_dialog.dismiss();
                                Toast.makeText(Setting.this, "Profile Picture is seted", Toast.LENGTH_SHORT).show();


                                Map<String,Object> profileUpdateMap=new HashMap<>();
                                profileUpdateMap.put("profileLink",String.valueOf(downloadUri));
                                getMyDB().updateChildren(profileUpdateMap);

                                refreshThisActivity();
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });
                }


            }
        });



    }



    private void refreshThisActivity(){

        finish();
        startActivity(getIntent());

    }
    @Override
    public void onClick(View v) {


        switch (v.getId()){

            case R.id.edit_profilePicture:
                profile_dialog.show();
                break;

            case R.id.edit_profileName:
                userName_dialog.show();
                break;

            case R.id.edit_password:
                pass_dialog.show();
                break;

            case R.id.edit_logout:
                finish();
                firebaseAuth.signOut();
                break;



        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(resultCode==RESULT_OK&&requestCode==PROFILE_SELECT_CODE&&data!=null){

            Toast.makeText(this, "Selected", Toast.LENGTH_SHORT).show();
            Glide.with(Setting.this).load(data.getData()).circleCrop().into(pofileChangeProfileView);
            profileChange_imageUri=data.getData();
        }

        if(resultCode==RESULT_OK&&requestCode==2&&data!=null){

            if(data.getExtras().getInt("varification",0)==1){

                Toast.makeText(this, "Number Varified", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(this, "Varification Fail", Toast.LENGTH_SHORT).show();
            }

        }

    }




    private DatabaseReference getMyDB(){

        return  FirebaseDatabase.getInstance().getReference("Drivers Personal Information").child(FirebaseAuth.getInstance().getUid());
    }


    private StorageReference getStorageReference(){

        final StorageReference storageReference= FirebaseStorage.getInstance().getReference("UserSide").child("Personal_information").child(firebaseUser.getEmail()).child(profileChange_imageUri.getLastPathSegment());

        return  storageReference;
    }



}