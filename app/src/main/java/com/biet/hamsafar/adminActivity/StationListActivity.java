package com.biet.hamsafar.adminActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.biet.hamsafar.adapters.StationAdapter;
import com.biet.hamsafar.adapters.WrapContentLinearLayoutManager;
import com.biet.hamsafar.databinding.ActivityStationListBinding;
import com.biet.hamsafar.models.Station;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class StationListActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference stationRef = db.collection("BusStation");
    private StationAdapter stationAdapter;
    private ActivityStationListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStationListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle("StationList");


        setUpRecyclerView();


        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StationListActivity.this, SetStation.class);
                intent.putExtra("type","add");
                startActivity(intent);
            }
        });
    }
    private void setUpRecyclerView(){
        Query query = stationRef.orderBy("id",Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Station> options = new FirestoreRecyclerOptions.Builder<Station>()
                .setQuery(query,Station.class)
                .build();

        stationAdapter = new StationAdapter(options);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this));
        binding.recyclerView.setAdapter(stationAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                stationAdapter.deleteItem(viewHolder.getAbsoluteAdapterPosition());
            }
        }).attachToRecyclerView(binding.recyclerView);

        stationAdapter.setOnItemClickListener(new StationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                   Station station = documentSnapshot.toObject(Station.class);
                   String id = documentSnapshot.getId();
                Log.d("ramramji",station.getName());
                Intent intent = new Intent(getApplicationContext(),SetStation.class);
                intent.putExtra("type","edit");
                intent.putExtra("keyId",id);
                intent.putExtra("name",station.getName());
                intent.putExtra("id",station.getId());
                intent.putExtra("latitude",station.getLatitude());
                intent.putExtra("longitude",station.getLongitude());
                startActivity(intent);

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        stationAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stationAdapter.stopListening();
    }
}