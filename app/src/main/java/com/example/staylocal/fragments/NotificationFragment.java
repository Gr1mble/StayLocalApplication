package com.example.staylocal.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.staylocal.databinding.EventListItemBinding;
import com.example.staylocal.databinding.FragmentNotificationBinding;
import com.example.staylocal.databinding.NotificationListItemBinding;
import com.example.staylocal.fragments.models.Event;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import android.content.Context;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotificationFragment#} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment {

    private String mParam1;
    private String mParam2;
    public NotificationFragment() {}

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Event> mEvents = new ArrayList<>();
    FragmentNotificationBinding binding;
    NotificationAdapter adapter;
    ListenerRegistration listenerRegistration;
    NotificationListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new NotificationAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

         listenerRegistration = db.collection("events")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            error.printStackTrace();
                            return;
                        }
                        mEvents.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Event event = doc.toObject(Event.class);
                            Log.d("EVENT-TEST", "onEvent: " + event.getName());
                            if (!mEvents.contains(event)) {
                                mEvents.add(event);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoProfile();
            }
        });
    }

    class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

        public NotificationAdapter() {}

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            EventListItemBinding itemBinding = EventListItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent, false);
            return new NotificationViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            holder.setupUI(mEvents.get(position));
        }

        @Override
        public int getItemCount() {return mEvents.size();}

        public class NotificationViewHolder extends RecyclerView.ViewHolder {

            EventListItemBinding itemBinding;
            Event mEvent;

            public NotificationViewHolder(@NonNull EventListItemBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }

            public void setupUI(Event event) {
                itemBinding.eventName.setText(event.getName());
                itemBinding.eventDescription.setText(event.getDescription());
                itemBinding.eventDates.setText(event.getDates());
                itemBinding.eventDuration.setText(event.getDurration());
            }
        }
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof NotificationFragment.NotificationListener){
            mListener = (NotificationFragment.NotificationListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement Notification Listener");
        }
    }

    public interface NotificationListener{
        void gotoHome();
        void gotoProfile();
        void gotoMap();
    }
}