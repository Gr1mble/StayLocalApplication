package com.example.staylocal.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.staylocal.R;
import com.example.staylocal.databinding.FragmentMapBinding;
import com.example.staylocal.fragments.models.Business;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapFragment extends Fragment {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FragmentMapBinding binding;
    private MapPageListener mListener;
    private ArrayList<Business> nearbyBusinesses = new ArrayList<>();
    private Business mbusiness;

    public MapFragment() {}

    public MapFragment(Business business) {
        mbusiness = business;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        binding.mapsButton.setImageResource(R.drawable.map_icon_active);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onMapReady(@NonNull GoogleMap googleMap) {
                    if (hasLocationPermission()) {
                        googleMap.getUiSettings().setZoomControlsEnabled(true);

                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequest.setInterval(120000); // 5 min

                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                if (locationResult != null && locationResult.getLastLocation() != null) {
                                    Location location = locationResult.getLastLocation();
                                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    MarkerOptions userMarkerOptions = new MarkerOptions()
                                            .position(userLocation)
                                            .title("Your Location")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                    googleMap.addMarker(userMarkerOptions);

                                    // Fetch and display nearby businesses
                                    generateNearbyBusinesses(userLocation, googleMap);

                                    // Move camera to user's location
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                                } else {
                                    // Handle case where location is null
                                    Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, null);
                    } else {
                        requestLocationPermission();
                    }
                }
            });
        }

        binding.homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoHome();
            }
        });

        binding.profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoProfile();
            }
        });

        binding.mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoMap();
            }
        });
        binding.foodTruckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoFoodTruck();
            }
        });
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onViewCreated(requireView(), null);
            } else {
                Toast.makeText(getContext(), "Location Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void generateNearbyBusinesses(LatLng userLocation, GoogleMap googleMap) {
        nearbyBusinesses.clear();

        OkHttpClient client = new OkHttpClient();

        //Parameterized URL
        String url = "https://api.yelp.com/v3/businesses/search?latitude="+userLocation.latitude+"&longitude="+userLocation.longitude+"&radius=5000&sort_by=best_match&limit=20";

        // Fetch data from Yelp API
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer ndrKKA5Tw0RrHMUXGfjdA-xPP_eLUQQAjl9NWbl-TwbIdddd0GcHOmO45XjAXCYbsCLJl3u87yaaVPm3b1LppZtZ8lJiN3MsjuwlUdJWqh8tJfXTHeNrkG9Oa7r4ZXYx")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    assert response.body() != null;
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray businessArray = jsonObject.getJSONArray("businesses");

                        for (int i = 0; i < businessArray.length(); i++) {
                            JSONObject businessObject = businessArray.getJSONObject(i);

                            String name = businessObject.getString("name");
                            String address = businessObject.getJSONObject("location").getString("address1");
                            String imgURL = "";
                            if(businessObject.has("image_url") && !businessObject.isNull("image_url")) {
                                imgURL = businessObject.getString("image_url");
                            }
                            String hours = "Hours Not listed on YELP";
                            double rating = businessObject.getDouble("rating");

                            rating = rating / 0.5;
                            int roundedIncrements = (int) Math.floor(rating);
                            double processedRating = roundedIncrements * 0.5;

                            String phone = businessObject.getString("phone");

                            Business business = new Business(name, address, imgURL, hours, processedRating, phone, null);

                            JSONObject coordinates = businessObject.getJSONObject("coordinates");
                            business.setLatitude(coordinates.getDouble("latitude"));
                            business.setLongitude(coordinates.getDouble("longitude"));

                            JSONArray categoriesArray = businessObject.getJSONArray("categories");
                            for (int j=0; j<categoriesArray.length(); j++){
                                JSONObject categoryObject = categoriesArray.getJSONObject(j);
                                String category = categoryObject.getString("alias");
                                business.addCategory(category);
                            }

                            nearbyBusinesses.add(business);

                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateMapUI(userLocation, googleMap);
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateMapUI(LatLng userLocation, GoogleMap googleMap) {
        // Update camera position to include all markers
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (googleMap != null) {
            // Create a copy of the nearbyBusinesses list to avoid ConcurrentModificationException
            ArrayList<Business> businessesCopy = new ArrayList<>(nearbyBusinesses);

            if(mbusiness == null){
                for (Business business : businessesCopy) { // Iterate over the copy
                    LatLng businessLocation = new LatLng(business.getLatitude(), business.getLongitude());
                    MarkerOptions businessMarkerOptions;

                    businessMarkerOptions = new MarkerOptions()
                            .position(businessLocation)
                            .title(business.getName())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                    googleMap.addMarker(businessMarkerOptions);
                    builder.include(new LatLng(business.getLatitude(), business.getLongitude()));
                }
            } else {
                LatLng businessLocation = new LatLng(mbusiness.getLatitude(), mbusiness.getLongitude());
                MarkerOptions businessMarkerOptions;
                businessMarkerOptions = new MarkerOptions()
                        .position(businessLocation)
                        .title(mbusiness.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                googleMap.addMarker(businessMarkerOptions);
                builder.include(new LatLng(mbusiness.getLatitude(), mbusiness.getLongitude()));
            }
            // Add user's location marker
            MarkerOptions userMarkerOptions = new MarkerOptions()
                    .position(userLocation)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            googleMap.addMarker(userMarkerOptions);

            builder.include(userLocation);
            LatLngBounds bounds = builder.build();
            int padding = 100;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.moveCamera(cu);
        }
    }


        @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MapFragment.MapPageListener){
            mListener = (MapFragment.MapPageListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement HomePageListener");
        }
    }

    public interface MapPageListener {
        void gotoHome();
        void gotoProfile();
        void gotoMap();

        void gotoFoodTruck();
    }
}
