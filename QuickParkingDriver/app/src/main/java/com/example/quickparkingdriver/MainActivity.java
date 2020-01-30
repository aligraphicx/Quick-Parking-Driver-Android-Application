package com.example.quickparkingdriver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";


    AlertDialog orderDialog;
    TextView orderUserName;
    TextView orderPhone;
    ImageView acceptOrder;
    ImageView rejectOrder;

    LinearLayout notification;
    LinearLayout setting;
    LinearLayout logout;
    LinearLayout support;
    LinearLayout pendingOrder;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        getPermissions();

        View orderLayout = LayoutInflater.from(this).inflate(R.layout.order_request_view, null);
        orderDialog = new AlertDialog.Builder(MainActivity.this)
                .setView(orderLayout)
                .setTitle("Order Request")
                .setCancelable(false)
                .create();


        acceptOrder = orderLayout.findViewById(R.id.acceptOrder);
        rejectOrder = orderLayout.findViewById(R.id.rejectOrder);
        orderUserName = orderLayout.findViewById(R.id.orderName);
        orderPhone = orderLayout.findViewById(R.id.orderPhone);

        setting = findViewById(R.id.setting);
        pendingOrder=findViewById(R.id.pendingOrder);
        pendingOrder.setOnClickListener(this);
        notification = findViewById(R.id.notification);
        logout = findViewById(R.id.logout);
        support = findViewById(R.id.support);
        setting.setOnClickListener(this);
        notification.setOnClickListener(this);
        logout.setOnClickListener(this);
        support.setOnClickListener(this);
        orderDetection();
        orderController();
    }


    public void orderDetection() {
        final DatabaseReference orderDetectionDB = FirebaseDatabase.getInstance().getReference("Drivers Personal Information").child(FirebaseAuth.getInstance().getUid()).child("LiveOrder");

        orderDetectionDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange:  exist");
                    LiveOrder liveOrder = dataSnapshot.getValue(LiveOrder.class);

                    if (liveOrder.getOrderStatus().equals("live")) {
                        Log.d(TAG, "onDataChange: live");

                        orderUserName.setText(liveOrder.getOrderUserName());
                        orderPhone.setText(liveOrder.getOrderPhoneNumber());
                        createNotification(liveOrder.getOrderUserName(),"You Recive order from "+liveOrder.getOrderUserName());
                        orderDialog.show();
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void qrCodeScanner(){

        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan Driver QR");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }


    private void orderController() {

        rejectOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference gettingOrder = FirebaseDatabase.getInstance().getReference("Drivers Personal Information").child(FirebaseAuth.getInstance().getUid()).child("LiveOrder");
                gettingOrder.removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        orderDialog.dismiss();
                    }
                });


            }
        });

        acceptOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> orderStatus = new HashMap<>();
                orderStatus.put("orderStatus", "accepted");
                DatabaseReference myDB = FirebaseDatabase.getInstance().getReference("Drivers Personal Information").child(FirebaseAuth.getInstance().getUid())
                        .child("LiveOrder");

                myDB.updateChildren(orderStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // orderDialog.dismiss();
                            Log.d(TAG, "onComplete: Updated");
                            Intent intent = new Intent(MainActivity.this, OrderTracer.class);
                            startActivity(intent);

                        }
                    }
                });

            }
        });


    }




    public void createNotification(String title, String dis) {


        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "No id")
                .setSmallIcon(R.drawable.logo_quick_parking)
                .setContentTitle(title)
                .setContentText(dis)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.notification:
                startActivity(new Intent(MainActivity.this, com.example.quickparkingdriver.Notification.class));
                break;
            case R.id.setting:
                startActivity(new Intent(MainActivity.this, com.example.quickparkingdriver.Setting.class));

                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, com.example.quickparkingdriver.Login.class));
                finish();
                break;
            case R.id.support:
                startActivity(new Intent(MainActivity.this, com.example.quickparkingdriver.SupportCenter.class));
                break;

            case R.id.pendingOrder:

                startActivity(new Intent(MainActivity.this, com.example.quickparkingdriver.PendingOrder.class));

                break;


        }
    }


    private void getPermissions() {


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

           //getCurrentDeviceLocation();
        } else {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.options,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.driverQRCode:
                Toast.makeText(this, "Now", Toast.LENGTH_SHORT).show();
                getQRAlertDialoge("QR Code").show();
                return true;
            case R.id.driverQRScanner:
                Toast.makeText(this, "Now", Toast.LENGTH_SHORT).show();
                qrCodeScanner();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void generateQRCode(String qrContent, ImageView qrSetLocation) {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 400, 400);
            int height = bitMatrix.getHeight();
            int width = bitMatrix.getWidth();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.RED : Color.YELLOW);
                }
            }
            qrSetLocation.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                if(result.getContents() == null) {
                    Log.e("Scan*******", "Cancelled scan");

                } else {
                    Log.e("Scan", "Scanned");



                     String qrName=result.getContents().substring(0,3);
                     String qrPrice=result.getContents().substring(3,result.getContents().length());
                    Toast.makeText(this, qrName, Toast.LENGTH_SHORT).show();
                    if(qrName.equals("Ali")){

                        Toast.makeText(this, "Driver Validation Complete", Toast.LENGTH_SHORT).show();
                        new AlertDialog.Builder(MainActivity.this).setMessage("Your Order Is Submited Successfully your fee is|| R.s "+qrPrice+ " ||")
                                .setNegativeButton("OK",null)
                                .setTitle("Message")
                                .setCancelable(false)
                                .create()
                                .show();
                    }
                }
            } else {
                // This is important, otherwise the result will not be passed to the fragment
                super.onActivityResult(requestCode, resultCode, data);
            }
        }


    private AlertDialog getQRAlertDialoge(String title){

        View view= LayoutInflater.from(MainActivity.this).inflate(R.layout.qr_code,null);
        ImageView qrImage=view.findViewById(R.id.driverQR);
        generateQRCode(FirebaseAuth.getInstance().getUid(),qrImage);
        return  new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setView(view)
                .setNegativeButton("OK",null).create();
    }
}

