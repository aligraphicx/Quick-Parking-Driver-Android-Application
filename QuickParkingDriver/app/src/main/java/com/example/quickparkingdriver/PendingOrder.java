package com.example.quickparkingdriver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.quickparkingdriver.Adaptor.PendingOrderAdaptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PendingOrder extends AppCompatActivity {

    RecyclerView recyclerView;
    private static final String TAG = "PendingOrder";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_order);
        recyclerView=findViewById(R.id.pendingOrderRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(PendingOrder.this));

        FirebaseDatabase.getInstance().getReference("Drivers Personal Information").child(FirebaseAuth.getInstance().getUid()).child("Pending Order")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final List<VehicalInformation> vehicalInformations=new ArrayList<>();
                            final List<String> keys=new ArrayList<>();

                           // Toast.makeText(PendingOrder.this, String.valueOf(dataSnapshot.getChildrenCount()), Toast.LENGTH_SHORT).show();

                            for (DataSnapshot child:dataSnapshot.getChildren()){

                                Log.d(TAG, "onDataChange: "+child.getKey());
                                keys.add(child.getKey());
                                child.getRef().child("Vehicle Information").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        Toast.makeText(PendingOrder.this, dataSnapshot.getValue(VehicalInformation.class).getOwnerName(), Toast.LENGTH_SHORT).show();
                                        vehicalInformations.add(dataSnapshot.getValue(VehicalInformation.class));
                                        recyclerView.setAdapter(new PendingOrderAdaptor(PendingOrder.this,vehicalInformations,keys));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
//
                      }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Destroye", Toast.LENGTH_SHORT).show();
    }
}
