package com.forvia.gg_2nd_hw;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class map_tab extends Fragment implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    MapView nmapView;
    View    mView;

    private Context FragmentContext;
    private final int REQUEST_PERMISSION_CODE = 1234;
    private static final float DEFAULT_ZOOM = 12;


   private FusedLocationProviderClient mFusedLocationProviderClient;
   private String[] Permission_list = {
                                        android.Manifest.permission.INTERNET,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                        android.Manifest.permission.ACCESS_NETWORK_STATE
                                    };

    private FloatingActionButton fab; //poga virs kartes, ar kuru var centrēt karti uz ierīces atrašanās vietu.

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.map_tab, container, false);

        return mView;


    }

    @Override
    public void onViewCreated(View view,@Nullable Bundle savedInstanceState)
    {

        super.onViewCreated(view,  savedInstanceState);
        FragmentContext = getActivity().getApplicationContext();
        fab = (FloatingActionButton) getView().findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  getDeviceLocation();
                }
        });


        if(!has_permissions())
        {
            ask_permissions();
        }

        nmapView = (MapView) mView.findViewById(R.id.goo_map);
        if(nmapView!=null)
        {
            nmapView.onCreate(null);
            nmapView.onResume();
            nmapView.getMapAsync(this);
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap; //Lai var raustīt karti ar funkcijām.
        MapsInitializer.initialize(getContext());
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        getDeviceLocation();
    }




    private void getDeviceLocation(){

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(FragmentContext);

        try{
            if(has_permissions()){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){

                            Location currentLocation = (Location) task.getResult();

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                            markerOptions.title("Device Location");
                            mGoogleMap.clear();
                            mGoogleMap.addMarker(markerOptions);
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),  DEFAULT_ZOOM));

                        }else{

                            if(!has_permissions())
                            {
                                ask_permissions();
                            }
                        }
                    }
                });
            }
        }catch (SecurityException e){

        }
    }


    public void ask_permissions()
    {

        ActivityCompat.requestPermissions(getActivity(), Permission_list, REQUEST_PERMISSION_CODE);
    }

    public boolean has_permissions()
    {

      for(int x = 0; x<Permission_list.length;x++)
                {
                    if (ContextCompat.checkSelfPermission(FragmentContext,Permission_list[x])!=PackageManager.PERMISSION_GRANTED)
                    {
                        return false;
                    }
                }

        return true;
    }


}