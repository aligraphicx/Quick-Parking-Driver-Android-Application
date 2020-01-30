package com.example.quickparkingdriver;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class NotificationAdaptor extends RecyclerView.Adapter<NotificationAdaptor.MyViewHolder> {
    private Context context;
    private List<NotificationType> notificationlist;


    public NotificationAdaptor(Context context, List<NotificationType> notificationlist) {
        this.context = context;
        this.notificationlist = notificationlist;
    }

    @NonNull
    @Override
    public NotificationAdaptor.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.notification_row,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdaptor.MyViewHolder holder, final int position) {


        holder.notificationName.setText(notificationlist.get(position).getNotificationName());
        holder.notificationName.setText(notificationlist.get(position).getNotificationData());
    }

    @Override
    public int getItemCount() {
        return notificationlist.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{



        public TextView notificationName;
        public TextView notificationData;


        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);

            notificationName=itemView.findViewById(R.id.notificationName);
            notificationData=itemView.findViewById(R.id.notificationData);

        }
    }


}
