package com.example.quickparkingdriver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;


public class OrderTracer extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private TextView name;
    private TextView number;

    private Button driverCall;
    private Button driverMesg;
    private Button cancelOrder;
    private Button viewVehical;
    private Button myQRGenerator;

    private LocationManager locationManager;
    private DatabaseReference orderDB;
    private static final String TAG = "OrderTracer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracer);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        name = findViewById(R.id.orderUserName);
        number = findViewById(R.id.orderCallNumber);
        myQRGenerator = findViewById(R.id.qrBtn);
        myQRGenerator.setOnClickListener(this);
        driverCall = findViewById(R.id.userCall);
        driverCall.setOnClickListener(this);
        driverMesg = findViewById(R.id.userMesg);
        driverMesg.setOnClickListener(this);
        viewVehical = findViewById(R.id.viewVehical);
        viewVehical.setOnClickListener(this);
        cancelOrder = findViewById(R.id.orderCancel);
        cancelOrder.setOnClickListener(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        orderInitalizing();
        updateUserLocation();

        orderDB = FirebaseDatabase.getInstance().getReference("Drivers Personal Information").child(FirebaseAuth.getInstance().getUid()).child("LiveOrder");

        orderDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                LiveOrder order = dataSnapshot.getValue(LiveOrder.class);
                name.setText(order.getOrderUserName());
                number.setText(order.getOrderPhoneNumber());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        FirebaseDatabase.getInstance().getReference("Drivers Personal Information")
                .child(FirebaseAuth.getInstance().getUid())
                .child("LiveOrder").child("Vehicle Information")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        if (dataSnapshot.exists()) {


                            viewVehical.setVisibility(View.VISIBLE);
                            viewVehical.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    viewVehicalDialoge().show();

                                }
                            });
                        }
                        Log.d(TAG, "onChildAdded: " + s);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Log.d(TAG, "onChildChanged: ");
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ContextCompat.checkSelfPermission(OrderTracer.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(OrderTracer.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Toast.makeText(OrderTracer.this, "Now Location is " + location.getAltitude() + " " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                LatLng orderLocationHere = new LatLng(location.getLatitude(), location.getLongitude());

                updateLoactionDataInFirebase(orderLocationHere);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
    }


    private void orderInitalizing(){

        final DatabaseReference databaseReference= FirebaseDatabase.getInstance()
                .getReference("Drivers Personal Information")
                .child(FirebaseAuth.getInstance().getUid())
                .child("LiveOrder");

        Map<String,Object> orderStatus=new HashMap<>();
        orderStatus.put("orderStatus","accepted");
        databaseReference.updateChildren(orderStatus);

    }
    private void moveCamera(LatLng latLng, float zoom, String locationName){

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

    }

    private AlertDialog getQRAlertDialoge(String title){

        View view= LayoutInflater.from(OrderTracer.this).inflate(R.layout.qr_code,null);
        ImageView qrImage=view.findViewById(R.id.driverQR);
        generateQRCode(FirebaseAuth.getInstance().getUid(),qrImage);
        return  new AlertDialog.Builder(OrderTracer.this)
                .setTitle(title)
                .setView(view)
                .setNegativeButton("OK",null).create();
    }

    public void generateQRCode(String qrContent, ImageView qrSetLocation){

        QRCodeWriter qrCodeWriter=new QRCodeWriter();
        BitMatrix bitMatrix= null;
        try {
            bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 400, 400);
            int height=bitMatrix.getHeight();
            int width=bitMatrix.getWidth();
            Bitmap bmp=Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);
            for(int x=0;x<width;x++){
                for(int y=0;y<height;y++){
                    bmp.setPixel(x,y,bitMatrix.get(x,y)? Color.RED:Color.YELLOW);
                }
            }
            qrSetLocation.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }



    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.userCall:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:+92310483912"));//change the number
                startActivity(callIntent);
                break;
            case R.id.userMesg:

                break;

            case R.id.orderCancel:
                removeLiveOrderData();
                finish();
                break;

            case R.id.qrBtn:
                getQRAlertDialoge("QR CODE").show();
                break;
            case R.id.viewVehical:
                break;

        }
    }


    private void removeLiveOrderData(){

        final DatabaseReference gettingOrder = FirebaseDatabase.getInstance()
                .getReference("Drivers Personal Information")
                .child(FirebaseAuth.getInstance().getUid()).child("LiveOrder");
        gettingOrder.removeValue();
    }
    private DatabaseReference getOrderDB(){
        return  FirebaseDatabase.getInstance()
                .getReference("Drivers Personal Information")
                .child(FirebaseAuth.getInstance().getUid())
                .child("LiveOrder");

    }

    private AlertDialog viewVehicalDialoge(){


        View vehicleLayout=LayoutInflater.from(this).inflate(R.layout.vehicle_view,null);
        final AlertDialog vehicleInfo=new AlertDialog.Builder(this).setView(vehicleLayout).create();

        final TextView vName=vehicleLayout.findViewById(R.id.vehicleOwnerName);
        final TextView vNumber=vehicleLayout.findViewById(R.id.vehicleNumber);
        final TextView oName=vehicleLayout.findViewById(R.id.vehicleOwnerName);
        final TextView oID=vehicleLayout.findViewById(R.id.vehicleOwnerID);
        final TextView oAddress=vehicleLayout.findViewById(R.id.vehvileOwnerAddress);
        final TextView oEmail=vehicleLayout.findViewById(R.id.vehicleOwnerEmail);
        final TextView oNumber=vehicleLayout.findViewById(R.id.vehicleOwnerPhone);

        getOrderDB().child("Vehicle Information").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                VehicalInformation cVehicle=dataSnapshot.getValue(VehicalInformation.class);
                vName.setText(cVehicle.getVehicalCompany());
                vNumber.setText(cVehicle.getVehicalNumber());
                oName.setText(cVehicle.getOwnerName());
                oEmail.setText(cVehicle.getOwnerEmail());
                oAddress.setText(cVehicle.getOwnerAddress());
                oID.setText(cVehicle.getIdNumber());
                oNumber.setText(cVehicle.getOwnerMobile());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Button vehicaleConfirm=vehicleLayout.findViewById(R.id.vehicleConfirmBtn);

        vehicaleConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                viewVehical.setVisibility(View.GONE);
                myQRGenerator.setVisibility(View.VISIBLE);
                vehicleInfo.dismiss();
            }
        });


        return vehicleInfo;

    }


    private void updateLoactionDataInFirebase(LatLng latLng){


        Map<String,Object> locationMap= new HashMap<>();
        locationMap.put("driverLat",String.valueOf(latLng.latitude));
        locationMap.put("driverLng", String.valueOf(latLng.longitude));
        FirebaseDatabase.getInstance()
                .getReference("Drivers Personal Information")
                .child(FirebaseAuth.getInstance().getUid())
                .child("LiveOrder")
                .updateChildren(locationMap);


    }

    private void updateUserLocation(){

        Log.d(TAG, "updateUserLocation: ");
        FirebaseDatabase.getInstance().getReference("Drivers Personal Information")
                .child(FirebaseAuth.getInstance().getUid())
                .child("LiveOrder")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: "+dataSnapshot.getChildrenCount());
                        if(dataSnapshot.exists()){

                            LiveOrder p=dataSnapshot.getValue(LiveOrder.class);
                            Log.d(TAG, "onDataChange: "+p.getOrderUserName());

                            LatLng latLng=new LatLng(Double.valueOf(p.getLat()),Double.valueOf(p.getLan()));
                            mMap.addMarker(new MarkerOptions().position(latLng).title("User Stay Here"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            moveCamera(latLng,17,"Order Recive Location");
                        }else{
                            Toast.makeText(OrderTracer.this, "location snapshot not exist", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

}
