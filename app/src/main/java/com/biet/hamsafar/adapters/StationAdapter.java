package com.biet.hamsafar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.biet.hamsafar.databinding.ItemContainingStationBinding;
import com.biet.hamsafar.models.Station;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class StationAdapter extends FirestoreRecyclerAdapter<Station,StationAdapter.StationHolder> {



    private OnItemClickListener listener;
    public StationAdapter(@NonNull FirestoreRecyclerOptions<Station> options) {
        super(options);
    }
        @NonNull
    @Override
    public StationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainingStationBinding itemContainingStationBinding =
                ItemContainingStationBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new StationHolder(itemContainingStationBinding);
    }

    @Override
    protected void onBindViewHolder(@NonNull StationHolder holder, int position, @NonNull Station model) {
        holder.setStationData(model);
    }

    public void deleteItem(int position){
        getSnapshots().getSnapshot(position).getReference().delete();

    }
    class StationHolder extends RecyclerView.ViewHolder {
        ItemContainingStationBinding binding;

        public StationHolder(@NonNull ItemContainingStationBinding itemContainingStationBinding) {
            super(itemContainingStationBinding.getRoot());
            binding = itemContainingStationBinding;
        }

        public void setStationData(Station station){
            binding.busId.setText(station.getId());
            binding.busName.setText(station.getName());
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION&& listener!=null){
                        listener.onItemClick(getSnapshots().getSnapshot(position),position);
                    }
                }
            });

        }
    }


    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot,int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


}
