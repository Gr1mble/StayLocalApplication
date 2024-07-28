package com.example.staylocal.fragments.businessAccounts;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.staylocal.databinding.FragmentEditBusinessBinding;
import com.example.staylocal.fragments.models.Business;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EditBusinessFragment extends Fragment {

    private Uri imgUri;
    private Bitmap bitmap;
    private String url;
    ImageView business_img;
    private static final String ARG_PARAM_BUSINESS = "business";
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Business mBusiness;
    public EditBusinessFragment() {
        // Required empty public constructor
    }


    public static EditBusinessFragment newInstance(Business business) {
        EditBusinessFragment fragment = new EditBusinessFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_BUSINESS, business);
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

    FragmentEditBusinessBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditBusinessBinding.inflate(inflater,container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DocumentReference docRef = db.collection("businesses").document(mBusiness.getDocId());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //load Business Obj data into UI
                Picasso.get().load(mBusiness.getImgURL()).into(binding.imgBusiness);
                binding.editTextBusinessNameEdit.setText(mBusiness.getName());
                binding.editTextAddressEdit.setText(mBusiness.getAddress());
                binding.editTextPhoneNumberEdit.setText(mBusiness.getPhone());

                String[] hours = mBusiness.getHours().split(" - ");
                binding.editTextOpenEdit.setText(hours[0]);
                binding.editTextCloseEdit.setText(hours[1]);


                binding.buttonSubmitEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("SUBMIT", "onClick: Button clicked");
                        uploadImg();
                    }
                });
                binding.homeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.gotoBusinessHome();
                    }
                });
                binding.eventButton3.setOnClickListener(new View.OnClickListener() {
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
        });
    }

    private void registerImagePicker() {
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imgUri = result.getData().getData();
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imgUri);
                            business_img.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        binding.buttonUpload.setOnClickListener(v -> selectImg(launcher));
    }

    private void selectImg(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        launcher.launch(intent);
    }

    private void uploadImg(){
        url = mBusiness.getImgURL();
        if(url != null){
            updateBusinessData();
        } else if (imgUri != null){
            final StorageReference ref = storage.getReference().child("photo/"+imgUri.getPathSegments());
            ref.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if(uri != null){
                                url = uri.toString();
                                updateBusinessData();
                            }
                        }
                    }). addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getAddressCoordinates(String address, Business business) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                Log.d("Address Coordinates", "Latitude: " + latitude + ", Longitude: " + longitude);
                business.setLongitude(longitude);
                business.setLatitude(latitude);
            } else {
                Log.d("Address Coordinates", "No location found for the given address");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Address Coordinates", "Error converting address to coordinates: " + e.getMessage());
        }
    }

    private void updateBusinessData() {
        String name = binding.editTextBusinessNameEdit.getText().toString();
        String address = binding.editTextAddressEdit.getText().toString();
        String imgUrl = url;
        String hours = binding.editTextOpenEdit.getText().toString() + " - " + binding.editTextCloseEdit.getText().toString();
        double rating = mBusiness.getRating();
        String phone_number = binding.editTextPhoneNumberEdit.getText().toString();

        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(imgUrl) || TextUtils.isEmpty(hours) || TextUtils.isEmpty(phone_number)){
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
        } else {
            DocumentReference docRef = db.collection("businesses").document(mBusiness.getDocId());

            Business business = new Business(name, address, imgUrl, hours, rating, phone_number,null);
            getAddressCoordinates(address, business);

            HashMap<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("address", address);
            data.put("imgURL", imgUrl);
            data.put("hours", hours);
            data.put("phone", phone_number);
            data.put("latitude", business.getLatitude());
            data.put("longitude", business.getLongitude());

            docRef.update(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        mListener.gotoBusinessHome();
                    } else {
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    EditListingPageListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof EditListingPageListener){
            mListener = (EditListingPageListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement EditListingPageListener");
        }
    }

    public interface EditListingPageListener{
        void gotoBusinessHome();
        void gotoBusinessEvents();
        void logout();
    }
}