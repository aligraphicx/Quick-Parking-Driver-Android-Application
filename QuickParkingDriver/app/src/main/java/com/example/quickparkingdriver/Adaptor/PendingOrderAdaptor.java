package com.example.quickparkingdriver.Adaptor;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.quickparkingdriver.NotificationType;
import com.example.quickparkingdriver.R;
import com.example.quickparkingdriver.VehicalInformation;

import java.util.List;

public class PendingOrderAdaptor extends RecyclerView.Adapter<PendingOrderAdaptor.MyViewHolder> {
    private Context context;
    private List<VehicalInformation> vehicalInformationList;
    private List<String> keys;


    public PendingOrderAdaptor(Context context, List<VehicalInformation> vehicalInformationList, List<String> keys) {
        this.context = context;
        this.vehicalInformationList = vehicalInformationList;
        this.keys = keys;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.pending_order,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        VehicalInformation information=vehicalInformationList.get(position);
        holder.vehicleName.setText(information.getVehicalCompany());
        holder.vehicleNumber.setText(information.getVehicalNumber());
        holder.vehicleOwner.setText(keys.get(position));

    }

    @Override
    public int getItemCount() {
        return vehicalInformationList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{



        public TextView vehicleName;
        public TextView vehicleNumber;
        public TextView vehicleOwner;


        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);

            vehicleName=itemView.findViewById(R.id.pending_order_Title);
            vehicleNumber=itemView.findViewById(R.id.pending_order_number);
            vehicleOwner=itemView.findViewById(R.id.pending_order_owner);
        }
    }


}
