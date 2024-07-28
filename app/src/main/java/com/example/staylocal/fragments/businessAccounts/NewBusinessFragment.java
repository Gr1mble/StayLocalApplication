package com.example.staylocal.fragments.businessAccounts;

import static android.app.Activity.RESULT_OK;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.staylocal.databinding.FragmentNewBusinessBinding;
import com.example.staylocal.fragments.models.Business;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class NewBusinessFragment extends Fragment {

    private Uri imgUri;
    private Bitmap bitmap;
    private String url;
    private boolean imagePathChanged;
    ImageView business_img;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final int REQUEST_CODE_IMAGE_PICK = 1;
    FragmentNewBusinessBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewBusinessBinding.inflate(inflater, container, false);

        // Register ActivityResultLauncher here
        registerImagePicker();

        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateSubmitButton();
        imagePathChanged = false;
        Log.d("Img Path", "This is Boolean: " + imagePathChanged);

        business_img = binding.imgBusiness;

        binding.editTextBusinessName.addTextChangedListener(textWatcher);
        binding.editTextAddress.addTextChangedListener(textWatcher);
        binding.editTextOpen.addTextChangedListener(textWatcher);
        binding.editTextClose.addTextChangedListener(textWatcher);
        binding.editTextPhoneNumber.addTextChangedListener(textWatcher);

        binding.homeButton.setOnClickListener(v -> mListener.gotoBusinessHome());

        binding.eventButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoBusinessEvents();
            }
        });

        binding.profileButton.setOnClickListener(v -> mListener.logout());

        binding.buttonSelectOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                binding.editTextOpen.setText(hourOfDay + ":" + minute);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        binding.buttonSelectClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                binding.editTextClose.setText(hourOfDay + ":" + minute);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateFields()){
                    uploadImg();
                }
            }
        });
    }

    private void registerImagePicker() {
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imgUri = result.getData().getData();
                        Log.d("Img Path", "This is Image Uri: " + imgUri);

                        if(imgUri != null){
                            imagePathChanged = true;
                            Log.d("Img Path", "This is Boolean: " + imagePathChanged);
                            validateFields();
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imgUri);
                                business_img.setImageBitmap(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
        Log.d("Img Path", "This is Image intent: " + intent);
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

    private void uploadImg(){
        if(imgUri != null){
            final StorageReference ref = storage.getReference().child("photo/"+imgUri.getPathSegments());
            ref.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if(uri != null){
                                url = uri.toString();
                                saveBusinessData();
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

    private void saveBusinessData() {
        String name = binding.editTextBusinessName.getText().toString();
        String address = binding.editTextAddress.getText().toString();
        String imgUrl = url;
        String hours = binding.editTextOpen.getText().toString() + " - " + binding.editTextClose.getText().toString();
        double rating = 0.0;
        String phone_number = binding.editTextPhoneNumber.getText().toString();

        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(imgUrl) || TextUtils.isEmpty(hours) || TextUtils.isEmpty(phone_number)){
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
        } else {
            String docId = db.collection("businesses").document().getId();
            DocumentReference docRef = db.collection("businesses").document(docId);

            Business business = new Business(name, address, imgUrl, hours, rating, phone_number,null);
            getAddressCoordinates(address, business);

            HashMap<String, Object> data = new HashMap<>();
            data.put("docId",docId);
            data.put("userId", mAuth.getCurrentUser().getUid());
            data.put("name", name);
            data.put("address", address);
            data.put("imgURL", imgUrl);
            data.put("hours", hours);
            data.put("rating", rating);
            data.put("phone", phone_number);
            data.put("latitude", business.getLatitude());
            data.put("longitude", business.getLongitude());

            docRef.set(data)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d("CONSOLE", "onComplete: Business created successfully");
                                mListener.gotoBusinessHome();
                            } else {
                                Toast.makeText(getContext(), "Failed to create business: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }



    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Not needed
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Update submit button based on validation whenever text changes
            updateSubmitButton();
        }
    };

    private void updateSubmitButton() {
        boolean allFieldsFilled = validateFields();
        Log.d("SubmitButton", "All fields filled: " + allFieldsFilled); // Log the validation result
        binding.buttonSubmit.setEnabled(allFieldsFilled);
        if (allFieldsFilled) {
            binding.buttonSubmit.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6750a4")));
        } else {
            // Reset button color if validation fails
            binding.buttonSubmit.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        }
    }

    private boolean validateFields() {
        String name = binding.editTextBusinessName.getText().toString();
        String address = binding.editTextAddress.getText().toString();
        String hours = binding.editTextOpen.getText().toString() + " - " + binding.editTextClose.getText().toString();
        String phone_number = binding.editTextPhoneNumber.getText().toString();


        String imageCheck = String.valueOf(imagePathChanged);

        boolean fieldsFilled = !TextUtils.isEmpty(name) && !TextUtils.isEmpty(address) && !TextUtils.isEmpty(hours) && !TextUtils.isEmpty(phone_number) && imagePathChanged; // Add imagePathChanged to the validation

        Log.d("FieldValidation", "Name: " + !TextUtils.isEmpty(name));
        Log.d("FieldValidation", "Address: " + !TextUtils.isEmpty(address));
        Log.d("FieldValidation", "Hours: " + !TextUtils.isEmpty(hours));
        Log.d("FieldValidation", "Phone number: " + !TextUtils.isEmpty(phone_number));
        Log.d("FieldValidation", "Image Path Changed: " + imageCheck);

        return fieldsFilled;
    }

    NewListingPageListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NewListingPageListener) {
            mListener = (NewListingPageListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement NewListingPageListener");
        }
    }

    public interface NewListingPageListener {
        void gotoBusinessHome();
        void gotoBusinessEvents();
        void logout();
    }
}
