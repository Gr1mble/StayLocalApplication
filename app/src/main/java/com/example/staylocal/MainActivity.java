package com.example.staylocal;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.staylocal.fragments.BusinessDetailsFragment;
import com.example.staylocal.fragments.FoodTruckFragment;
import com.example.staylocal.fragments.HomeFragment;
import com.example.staylocal.fragments.MapFragment;
import com.example.staylocal.fragments.NotificationFragment;
import com.example.staylocal.fragments.ProfileFragment;
import com.example.staylocal.fragments.auth.LoginFragment;
import com.example.staylocal.fragments.auth.SignupFragment;
import com.example.staylocal.fragments.businessAccounts.BusinessListingFragment;
import com.example.staylocal.fragments.businessAccounts.EditBusinessFragment;
import com.example.staylocal.fragments.businessAccounts.EditEventFragment;
import com.example.staylocal.fragments.businessAccounts.EventListingFragment;
import com.example.staylocal.fragments.businessAccounts.NewBusinessFragment;
import com.example.staylocal.fragments.businessAccounts.NewEventFragment;
import com.example.staylocal.fragments.models.Business;
import com.example.staylocal.fragments.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements
        LoginFragment.LoginListener,
        SignupFragment.SignupListener,
        HomeFragment.HomePageListener,
        ProfileFragment.ProfileListener,
        BusinessDetailsFragment.BusinessDetailsListener,
        MapFragment.MapPageListener, NotificationFragment.NotificationListener,
        BusinessListingFragment.BusinessPageListener,
        NewBusinessFragment.NewListingPageListener,
        EditBusinessFragment.EditListingPageListener,
        FoodTruckFragment.FoodTruckListener,
        EventListingFragment.EventPageListener,
        NewEventFragment.NewEventPageListener,
        EditEventFragment.EditEventPageListener {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(mAuth.getCurrentUser() == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rootView, new LoginFragment())
                    .commit();
        } else {
            authCompleted();
        }

    }

    @Override
    public void gotoSignup() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new SignupFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoLogin() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void authCompleted() {
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()){
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    boolean isAdmin = Objects.requireNonNull(task.getResult().getBoolean("isBusiness"));
                    if(isAdmin){
                        gotoBusinessHome();
                    } else {
                        gotoHome();
                    }
                });
    }

    @Override
    public void logout() {
        mAuth.signOut();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new LoginFragment())
                .commit();
    }

    @Override
    public void gotoNotifications() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new NotificationFragment())
                .commit();
    }
    @Override
    public void addBusiness() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new NewBusinessFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoHome(){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new HomeFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoBusinessHome() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new BusinessListingFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoEditBusiness(Business business) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, EditBusinessFragment.newInstance(business))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoBusinessEvents() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new EventListingFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoEditEvent(Event event) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, EditEventFragment.newInstance(event))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void addEvent() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new NewEventFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoProfile(){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new ProfileFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoMap() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new MapFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoFoodTruck() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new FoodTruckFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoMap_B(Business business) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new MapFragment(business))
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void gotoBusinessDetails(Business business) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, BusinessDetailsFragment.newInstance(business))
                .addToBackStack(null)
                .commit();
    }




}