package com.example.staylocal.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.staylocal.R;
import com.example.staylocal.databinding.FragmentBusinessDetailsBinding;
import com.example.staylocal.fragments.models.Business;
import com.squareup.picasso.Picasso;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BusinessDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BusinessDetailsFragment extends Fragment {
    private static final String ARG_PARAM_BUSINESS = "business";
    private Business mBusiness;
    private String mParam2;

    public BusinessDetailsFragment() {
        // Required empty public constructor
    }

    public static BusinessDetailsFragment newInstance(Business mBusiness) {
        BusinessDetailsFragment fragment = new BusinessDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_BUSINESS, mBusiness);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBusiness = (Business) getArguments().getSerializable(ARG_PARAM_BUSINESS);
        }
    }

    FragmentBusinessDetailsBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBusinessDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //populate UI elements using binding
        binding.businessName2.setText(mBusiness.getName());
        binding.businessAddress.setText(mBusiness.getAddress());
        binding.businessPrice.setText(mBusiness.getPrice());
        binding.businessHours.setText(mBusiness.getHours());

        //populate UI Address element color/style (Blue + Underlined)
        binding.businessAddress.setTextColor(Color.BLUE);
        binding.businessAddress.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        if(mBusiness.getRating() == 0){
            binding.imgBusinessReview.setVisibility(View.INVISIBLE);
            binding.foodTruckTag2.setText("Food Truck");
        } else if (mBusiness.getRating() == 0.5) {
            binding.imgBusinessReview.setImageResource(R.drawable.review_ribbon_small_0_half);
        } else if (mBusiness.getRating() == 1) {
            binding.imgBusinessReview.setImageResource(R.drawable.review_ribbon_small_1_half);
        } else if (mBusiness.getRating() == 1.5) {
            binding.imgBusinessReview.setImageResource(R.drawable.review_ribbon_small_1_half);
        }  else if (mBusiness.getRating() == 2) {
            binding.imgBusinessReview.setImageResource(R.drawable.review_ribbon_small_2);
        } else if (mBusiness.getRating() == 2.5) {
            binding.imgBusinessReview.setImageResource(R.drawable.review_ribbon_small_2_half);
        } else if (mBusiness.getRating() == 3) {
            binding.imgBusinessReview.setImageResource(R.drawable.review_ribbon_small_3);
        } else if (mBusiness.getRating() == 3.5) {
            binding.imgBusinessReview.setImageResource(R.drawable.review_ribbon_small_3_half);
        } else if (mBusiness.getRating() == 4) {
            binding.imgBusinessReview.setImageResource(R.drawable.review_ribbon_small_4);
        } else if (mBusiness.getRating() == 4.5) {
            binding.imgBusinessReview.setImageResource(R.drawable.review_ribbon_small_4_half);
        } else if(mBusiness.getRating() == 5) {
            binding.imgBusinessReview.setImageResource(R.drawable.review_ribbon_small_5);
        } else {
            binding.imgBusinessReview.setVisibility(View.INVISIBLE);
        }

        if(Objects.equals(mBusiness.getPhone(), "")){
            binding.businessPhone.setText("No Phone Number Found");
        } else {
            binding.businessPhone.setText(mBusiness.getPhone());
        }

        Picasso.get().load(mBusiness.getImgURL()).into(binding.businessPicture2);

        binding.businessAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoMap_B(mBusiness);
            }
        });
        //implement logic for buttons
        binding.buttonReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoHome();
            }
        });
    }


    BusinessDetailsListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof BusinessDetailsListener){
            mListener = (BusinessDetailsListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement BusinessDetailsListener");
        }
    }

    public interface BusinessDetailsListener{
        void gotoHome();
        void gotoMap();
        void gotoMap_B(Business mBusiness);
    }
}