package com.biet.hamsafar.adminActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.biet.hamsafar.R;
import com.biet.hamsafar.databinding.ActivitySetStationBinding;
import com.biet.hamsafar.models.Station;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SetStation extends AppCompatActivity implements OnMapReadyCallback {

    ActivitySetStationBinding binding;
    List<Address> addressList= new ArrayList<>();
    GoogleMap mGoogleMap;
    Station station;
    boolean editFlag,removeFlag=true;
    Marker onClickedMarker,onSearchMarker;
    private String keyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetStationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.round_close_24);
        setTitle("SetStation");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        String type = getIntent().getStringExtra("type");
        Log.d("ramramji",type);

        assert type != null;
        if(!(type.equals("add")||type.equals("edit"))){
            Log.d("ramramji",type+"1");
            type = "add";
        }

        if(type.equals("edit")){
            Log.d("ramramji",type+"2");
            binding.send.setText(R.string.update_button);
            editFlag = true;
            station = new Station();
            station.setId(getIntent().getStringExtra("id"));
            station.setName(getIntent().getStringExtra("name"));
           // double lt = Double.parseDouble(Objects.requireNonNull(getIntent().getStringExtra("latitude")));
           // double lo = Double.parseDouble(Objects.requireNonNull(getIntent().getStringExtra("longitude")));
            station.setLatitude(getIntent().getDoubleExtra("latitude",1235));
            station.setLongitude(getIntent().getDoubleExtra("longitude",1235));
            //station.setLongitude(lo);
          //  station.setLongitude(Double.parseDouble(Objects.requireNonNull(getIntent().getStringExtra("latitude"))));
          //  station.setLongitude(Double.parseDouble(Objects.requireNonNull(getIntent().getStringExtra("longitude"))));
            Log.d("ramramji",type+"4");
            keyId = getIntent().getStringExtra("keyId");
            binding.name.setText(station.getName());
            binding.id.setText(station.getId());
            binding.latitude.setText(String.valueOf(station.getLatitude()));
            binding.longitude.setText(String.valueOf(station.getLongitude()));
            Log.d("ramramji",type+"3");
        }else{
            editFlag = false;

        }



        initMap();
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData();
            }
        });


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mGoogleMap = googleMap;
       mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Log.d("ramram","onMapReady");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
          mGoogleMap.setMyLocationEnabled(true);
        }

        MarkerOptions mo = new MarkerOptions();

        if(editFlag){
            LatLng lt = new LatLng(station.getLatitude(),station.getLongitude());
            mo.position(lt);
            mo.title(station.getName());
            onClickedMarker = mGoogleMap.addMarker(mo);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(lt));

        }




        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {




                Geocoder geocoder = new Geocoder(SetStation.this);
                try {
                    Toast.makeText(SetStation.this, "click", Toast.LENGTH_SHORT).show();

                    ArrayList<Address>  arrAdr = (ArrayList<Address>) geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);


                    if(onClickedMarker!=null){
                        onClickedMarker.remove();
                    }

                    mo.position(latLng);
                    mo.title("you clicked here");
                    mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    onClickedMarker = mGoogleMap.addMarker(mo);
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    String lt = latLng.latitude+"";
                    String lo = latLng.longitude+"";
                    binding.latitude.setText(lt);
                    binding.longitude.setText(lo);
                    binding.fullAddress.setVisibility(View.VISIBLE);
                    binding.fullAddress.setText(arrAdr.get(0).getAddressLine(0));
                 //   Log.d("ramram",arrAdr.get(0).toString());
                   // assert arrAdr != null;
                   // binding.name.setText(arrAdr.get(0).getLocality());

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });





    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setQueryHint("Type here to search");




        MarkerOptions mo = new MarkerOptions();


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {


                if(onSearchMarker!=null){
                    onSearchMarker.remove();
                }

                if(!query.equals("")){
                    Geocoder geocoder = new Geocoder(SetStation.this);
                    try {
                        addressList = geocoder.getFromLocationName(query,1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    for(int i = 0; i< Objects.requireNonNull(addressList).size(); i++){
                        Address myAddress = addressList.get(i);
                        LatLng latLng = new LatLng(myAddress.getLatitude(),myAddress.getLongitude());
                        mo.position(latLng);
                        mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        mo.title(query);
                        binding.name.setText(query);
                        onSearchMarker =    mGoogleMap.addMarker(mo);

                        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(20.0F).build();
                        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    }

                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    private void sendData(){
        loading(true);

        if(binding.id.getText().toString().isEmpty()
                ||binding.name.getText().toString().isEmpty()
                ||binding.longitude.toString().isEmpty()
                ||binding.longitude.toString().isEmpty()){
            Toast.makeText(this, "fill all value", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore database = FirebaseFirestore.getInstance();


        station = new Station();
        station.setId(binding.id.getText().toString());
        station.setName(binding.name.getText().toString());
        station.setLatitude(Double.parseDouble(binding.latitude.getText().toString()));
        station.setLongitude(Double.parseDouble(binding.longitude.getText().toString()));


        if(editFlag){
            database = FirebaseFirestore.getInstance();
            DocumentReference documentReference =
                    database.collection("BusStation").document(keyId);
            documentReference.update(
                    "id",station.getId(),
                    "name",station.getName(),
                    "latitude",station.getLatitude(),
                    "longitude",station.getLongitude()
            ).addOnSuccessListener(dr ->{

                Toast.makeText(this, "update", Toast.LENGTH_SHORT).show();
                loading(false);
                finish();

            }).addOnFailureListener(exception ->{
                loading(false);
                Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });

        }else{

            database.collection("BusStation")
                    .add(station)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "send", Toast.LENGTH_SHORT).show();
                        binding.id.setText(null);
                        binding.name.setText(null);
                        binding.latitude.setText(null);
                        binding.longitude.setText(null);
                        loading(false);
                        finish();
                    })
                    .addOnFailureListener(exception -> {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                        loading(false);
                        finish();
                    });
        }




    }


    private void initMap() {

                SupportMapFragment supportMapFragment = new SupportMapFragment();
                supportMapFragment.getMapAsync(this);
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(binding.container.getId(), supportMapFragment);
                supportMapFragment.getMapAsync(this);
                ft.commit();
            }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.send.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.send.setVisibility(View.VISIBLE);
        }
    }



}