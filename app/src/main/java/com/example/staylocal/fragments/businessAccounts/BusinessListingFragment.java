package com.example.staylocal.fragments.businessAccounts;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.staylocal.R;
import com.example.staylocal.databinding.BusinessListingItemBinding;
import com.example.staylocal.databinding.FragmentBusinessListingBinding;
import com.example.staylocal.fragments.models.Business;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BusinessListingFragment extends Fragment {

    public BusinessListingFragment() {
        // Required empty public constructor
    }

    BusinessAdapter adapter;
    FragmentBusinessListingBinding binding;
    ListenerRegistration listenerRegistration;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<Business> mBusinesses = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBusinessListingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new BusinessAdapter();
        binding.recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView2.setAdapter(adapter);

        listenerRegistration = db.collection("businesses")
                .whereEqualTo("userId",mAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            error.printStackTrace();
                            return;
                        }
                        mBusinesses.clear();
                        for(QueryDocumentSnapshot doc : value){
                            Log.d("Processing", "onEvent: "+doc.toString());
                            Business business = doc.toObject(Business.class);
                            if(!mBusinesses.contains(business)){
                                mBusinesses.add(business);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        binding.buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.addBusiness();
            }
        });

        binding.homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoBusinessHome();
            }
        });
        binding.eventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoBusinessEvents();
            }
        });
        binding.profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.logout();
            }
        });
    }


    class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.BusinessViewHolder>{
        @NonNull
        @Override
        public BusinessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            BusinessListingItemBinding itemBinding = BusinessListingItemBinding.inflate(getLayoutInflater(),parent,false);
            return new BusinessViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull BusinessViewHolder holder, int position) {
            holder.setupUI(mBusinesses.get(position));
        }

        @Override
        public int getItemCount() {
            return mBusinesses.size();
        }

        class BusinessViewHolder extends RecyclerView.ViewHolder{
            BusinessListingItemBinding itemBinding;
            Business mBusiness;
            public BusinessViewHolder(BusinessListingItemBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }

            public void setupUI(Business business){
                mBusiness = business;
                itemBinding.businessName.setText(business.getName());
                itemBinding.businessAddress.setText(business.getAddress());
                Picasso.get().load(business.getImgURL()).into(itemBinding.businessPicture);

                itemBinding.imageBttnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.gotoEditBusiness(business);
                    }
                });
                itemBinding.imageBttnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.collection("businesses")
                                .document(mBusiness.getDocId()).delete();
                        mBusinesses.remove(business);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    BusinessPageListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof BusinessPageListener){
            mListener = (BusinessPageListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement BusinessPageListener");
        }
    }

    public interface BusinessPageListener{
        void gotoBusinessHome();
        void gotoEditBusiness(Business business);
        void gotoBusinessEvents();
        void logout();
        void addBusiness();
    }
}

