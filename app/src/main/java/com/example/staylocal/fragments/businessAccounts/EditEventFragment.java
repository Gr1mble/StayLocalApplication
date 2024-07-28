package com.example.staylocal.fragments.businessAccounts;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.staylocal.R;
import com.example.staylocal.databinding.FragmentEditEventBinding;
import com.example.staylocal.fragments.models.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;

public class EditEventFragment extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String ARG_PARAM_EVENT = "event";
    private Event mEvent;
    public EditEventFragment() {
        // Required empty public constructor
    }
    public static EditEventFragment newInstance(Event event) {
        EditEventFragment fragment = new EditEventFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEvent = (Event) getArguments().getSerializable(ARG_PARAM_EVENT);
        }
    }

    FragmentEditEventBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DocumentReference docRef = db.collection("events").document(mEvent.getDocId());

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                binding.editTextEventNameEdit.setText(mEvent.getName());
                binding.editTextDescriptionEdit.setText(mEvent.getDescription());

                String[] dates = mEvent.getDates().split(" till ");
                binding.editTextEventStartEdit.setText(dates[0]);
                binding.editTextEventStopEdit.setText(dates[1]);

                String[] hours = mEvent.getDurration().split(" - ");
                binding.editTextEventStartTimeEdit.setText(hours[0]);
                binding.editTextEventEndTimeEdit.setText(hours[1]);

                binding.btnEventStartEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                binding.editTextEventStartEdit.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            }
                        }, year, month, day);
                        datePickerDialog.show();
                    }
                });
                binding.btnEventEndEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                binding.editTextEventStopEdit.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            }
                        }, year, month, day);
                        datePickerDialog.show();
                    }
                });
                binding.btnEventStartTimeEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar c = Calendar.getInstance();
                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        int minute = c.get(Calendar.MINUTE);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                        binding.editTextEventStartTimeEdit.setText(hourOfDay + ":" + minute);
                                    }
                                }, hour, minute, false);
                        timePickerDialog.show();
                    }
                });
                binding.btnEventEndTimeEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar c = Calendar.getInstance();
                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        int minute = c.get(Calendar.MINUTE);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                        binding.editTextEventEndTimeEdit.setText(hourOfDay + ":" + minute);
                                    }
                                }, hour, minute, false);
                        timePickerDialog.show();
                    }
                });
                binding.buttonSubmitEventEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(binding.editTextEventNameEdit.getText().toString().isEmpty()){
                            Toast.makeText(getActivity(), "Please enter a Event Name", Toast.LENGTH_SHORT).show();
                        } else if(binding.editTextDescriptionEdit.getText().toString().isEmpty()){
                            Toast.makeText(getActivity(), "Please enter a Event Description", Toast.LENGTH_SHORT).show();
                        } else {
                            String dates = binding.editTextEventStartEdit.getText().toString() + " till " + binding.editTextEventStopEdit.getText().toString();
                            String durration = binding.editTextEventStartTimeEdit.getText().toString() + " - " + binding.editTextEventEndTimeEdit.getText().toString();

                            HashMap<String, Object> data = new HashMap<>();
                            data.put("name", binding.editTextEventNameEdit.getText().toString());
                            data.put("description", binding.editTextDescriptionEdit.getText().toString());
                            data.put("dates", dates);
                            data.put("durration", durration);

                            docRef.update(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mListener.gotoBusinessEvents();
                                    } else {
                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
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
        });
    }

    EditEventPageListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof EditEventPageListener){
            mListener = (EditEventPageListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement EditEventPageListener");
        }
    }

    public interface EditEventPageListener{
        void gotoBusinessHome();
        void gotoBusinessEvents();
        void logout();
    }
}