package com.example.staylocal.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.OnSuccessListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FoodTruckFragment extends Fragment {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    FragmentMapBinding binding;
    FoodTruckListener mListener;
    public Business mbusiness;

    private LatLng selectedBusinessLocation;

    ArrayList<Business> nearbyBusinesses = new ArrayList<>();

    public FoodTruckFragment() {
    }

    public FoodTruckFragment(Business business) {
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
        binding.foodTruckButton.setImageResource(R.drawable.foodtruck_icon_active);
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

                        // Display user's location
                        fusedLocationProviderClient.getLastLocation()
                                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                            MarkerOptions userMarkerOptions = new MarkerOptions()
                                                    .position(userLocation)
                                                    .title("Your Location")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                            googleMap.addMarker(userMarkerOptions);

                                            if (mbusiness == null) {
                                                // Fetch and display nearby businesses only if no selected business
                                                generateNearbyFoodTrucks(userLocation, googleMap);
                                            } else {
                                                // If selected business exists, display it and adjust camera to show both selected business and user's location
                                                selectedBusinessLocation = new LatLng(mbusiness.getLatitude(), mbusiness.getLongitude());
                                                MarkerOptions businessMarkerOptions = new MarkerOptions()
                                                        .position(selectedBusinessLocation)
                                                        .title(mbusiness.getName())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                                googleMap.addMarker(businessMarkerOptions);

                                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                builder.include(selectedBusinessLocation);
                                                builder.include(userLocation);
                                                LatLngBounds bounds = builder.build();
                                                int padding = 100;
                                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                                googleMap.moveCamera(cu);
                                            }
                                        }
                                    }
                                });
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

    @SuppressLint("StaticFieldLeak")
    private void generateNearbyFoodTrucks(LatLng userLocation, GoogleMap googleMap) {
        nearbyBusinesses.clear();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Document doc = Jsoup.connect("https://foodtruckscharlotte.org").get();

                    Elements truckElements = doc.select("div.eve_row");

                    Calendar calendar = Calendar.getInstance();
                    long currentTimeMillis = calendar.getTimeInMillis();

                    for (Element truckElement : truckElements) {
                        if (truckElement.attr("day").equals("Today")) {
                            String name = truckElement.select("h3.eve_name a").text();
                            String address = truckElement.select("div.eve_street").text();
                            double latitude = Double.parseDouble(truckElement.attr("lat"));
                            double longitude = Double.parseDouble(truckElement.attr("lng"));
                            String hours = truckElement.select("div.eve_time").text();
                            String imgURL = "https://foodtruckscharlotte.org" + truckElement.select("div.eve_pic img").attr("src");

                            if (isTruckOpenNow(hours)) {
                                if(isInCharlotte(latitude,longitude)) {
                                    Business business = new Business(name, address, imgURL, hours, 0.0, "", null);
                                    business.setLatitude(latitude);
                                    business.setLongitude(longitude);
                                    business.addCategory("Food Truck");
                                    nearbyBusinesses.add(business);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }




            @Override
            protected void onPostExecute(Void aVoid) {
                // Update UI after fetching data
                updateMapUI(userLocation, googleMap);
            }
        }.execute();
    }

    private boolean isInCharlotte(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                String locality = addresses.get(0).getLocality();
                // Check if the city is Charlotte
                if(locality != null && locality.equalsIgnoreCase("Charlotte")){
                    Log.d("LOCATION TESTING 2", "isInCharlotte: "+locality);
                    return true;
                } else {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private boolean isTruckOpenNow(String hours) {
        if (hours != null && hours.length() > 0 && hours.contains("-")) {
            String[] openingHoursArray = hours.split("-");

            String[] patterns = {
                    "\\b(\\d{1,2})(?::(\\d{2}))?(?:\\s*([ap]m))\\b",
                    "\\b(\\d{1,2})(?:(\\d{2}))(?:\\s*([ap]m))\\b"
            };

            try {
                Calendar calendar = Calendar.getInstance();
                Date currentTime = calendar.getTime();

                for (String pattern : patterns) {
                    Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = regex.matcher(openingHoursArray[0].trim());
                    if (matcher.find()) {
                        int hour = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
                        int minute = matcher.group(2) != null ? Integer.parseInt(Objects.requireNonNull(matcher.group(2))) : 0;

                        if (matcher.group(3) != null && Objects.requireNonNull(matcher.group(3)).equalsIgnoreCase("pm") && hour < 12) {
                            hour += 12;
                        }

                        // Set opening time
                        Calendar openingTime = Calendar.getInstance();
                        openingTime.set(Calendar.HOUR_OF_DAY, hour);
                        openingTime.set(Calendar.MINUTE, minute);

                        // Parse closing time using the same pattern
                        matcher = regex.matcher(openingHoursArray[1].trim());
                        if (matcher.find()) {
                            // Extract hour and minute groups
                            hour = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
                            minute = matcher.group(2) != null ? Integer.parseInt(Objects.requireNonNull(matcher.group(2))) : 0;

                            // Adjust hour for PM times
                            if (matcher.group(3) != null && Objects.requireNonNull(matcher.group(3)).equalsIgnoreCase("pm") && hour < 12) {
                                hour += 12;
                            }

                            // Set closing time
                            Calendar closingTime = Calendar.getInstance();
                            closingTime.set(Calendar.HOUR_OF_DAY, hour);
                            closingTime.set(Calendar.MINUTE, minute);

                            // Check if the current time is within the opening hours
                            if (currentTime.after(openingTime.getTime()) && currentTime.before(closingTime.getTime())) {
                                return true;
                            }
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // Handle number format exception
                e.printStackTrace();
            }
        }
        return false;
    }


    private void updateMapUI(LatLng userLocation, GoogleMap googleMap) {
        if (googleMap != null) {
            for (Business business : nearbyBusinesses) {
                LatLng businessLocation = new LatLng(business.getLatitude(), business.getLongitude());
                MarkerOptions businessMarkerOptions;


                    new LoadImageTask(business, businessLocation, googleMap).execute();

            }

            // Add user's location marker
            MarkerOptions userMarkerOptions = new MarkerOptions()
                    .position(userLocation)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            googleMap.addMarker(userMarkerOptions);

            // Update camera position to include all markers
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Business business : nearbyBusinesses) {
                builder.include(new LatLng(business.getLatitude(), business.getLongitude()));
            }
            builder.include(userLocation);
            LatLngBounds bounds = builder.build();
            int padding = 100;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.moveCamera(cu);
        }
    }

    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        private Business business;
        private LatLng businessLocation;
        private GoogleMap googleMap;

        public LoadImageTask(Business business, LatLng businessLocation, GoogleMap googleMap) {
            this.business = business;
            this.businessLocation = businessLocation;
            this.googleMap = googleMap;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                URL url = new URL(business.getImgURL());
                return BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                // Image loaded successfully, add marker with image
                MarkerOptions businessMarkerOptions = new MarkerOptions()
                        .position(businessLocation)
                        .title(business.getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                googleMap.addMarker(businessMarkerOptions);
            } else {
                // Error loading image, add marker with default icon
                MarkerOptions businessMarkerOptions = new MarkerOptions()
                        .position(businessLocation)
                        .title(business.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                googleMap.addMarker(businessMarkerOptions);
            }
        }
    }





    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FoodTruckListener) {
            mListener = (FoodTruckListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FoodTruckMapListener");
        }
    }

    public interface FoodTruckListener {
        void gotoHome();
        void gotoProfile();
        void gotoMap();

        void gotoFoodTruck();
    }
}