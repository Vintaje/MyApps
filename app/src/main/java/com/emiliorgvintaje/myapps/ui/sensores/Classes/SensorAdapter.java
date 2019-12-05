package com.emiliorgvintaje.myapps.ui.sensores.Classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emiliorgvintaje.myapps.R;

import java.util.ArrayList;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.ViewHolder> {
    private ArrayList<String> sensoreslist;

    public SensorAdapter(ArrayList<String> sensoreslist){
        this.sensoreslist = sensoreslist;
    }
    @NonNull
    @Override
    public SensorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.sensor_item, parent, false);
        SensorAdapter.ViewHolder viewHolder = new SensorAdapter.ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SensorAdapter.ViewHolder holder, int position) {
        final String name = sensoreslist.get(position);
        holder.nombre.setText(name);

    }

    @Override
    public int getItemCount() {
        return sensoreslist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView nombre;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = (TextView) itemView.findViewById(R.id.tvSensorName);

        }
    }
}
