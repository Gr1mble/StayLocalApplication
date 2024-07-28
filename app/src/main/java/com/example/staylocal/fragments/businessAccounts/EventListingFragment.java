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
import com.example.staylocal.databinding.EventListingItemBinding;
import com.example.staylocal.databinding.FragmentEventListingBinding;
import com.example.staylocal.fragments.models.Business;
import com.example.staylocal.fragments.models.Event;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class EventListingFragment extends Fragment {
    EventAdapter adapter;
    ListenerRegistration listenerRegistration;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Event> mEvents = new ArrayList<>();
    public EventListingFragment() {
        // Required empty public constructor
    }

    FragmentEventListingBinding binding;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentEventListingBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new EventAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        listenerRegistration = db.collection("events")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            error.printStackTrace();
                            return;
                        }
                        mEvents.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Event event = doc.toObject(Event.class);
                            Log.d("EVENT-TEST", "onEvent: "+event.getName());
                            if(!mEvents.contains(event)){
                                mEvents.add(event);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
        binding.buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.addEvent();
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

    class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder>{
        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            EventListingItemBinding itemBinding = EventListingItemBinding.inflate(getLayoutInflater(), parent, false);
            return new EventViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            holder.setupUI(mEvents.get(position));
        }

        @Override
        public int getItemCount() {
            return mEvents.size();
        }

        class EventViewHolder extends RecyclerView.ViewHolder{
            EventListingItemBinding itemBinding;
            Event mEvent;
            public EventViewHolder(EventListingItemBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }

            public void setupUI(Event event){
                mEvent = event;
                itemBinding.eventName.setText(event.getName());
                itemBinding.eventDescription.setText(event.getDescription());
                itemBinding.eventDates.setText(event.getDates());
                itemBinding.eventDuration.setText(event.getDurration());

                itemBinding.imageBttnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.gotoEditEvent(event);
                    }
                });

                itemBinding.imageBttnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.collection("events").document(mEvent.getDocId()).delete();
                        mEvents.remove(event);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    EventPageListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof EventPageListener){
            mListener = (EventPageListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement EventPageListener");
        }
    }

    public interface EventPageListener{
        void gotoBusinessHome();
        void gotoBusinessEvents();
        void gotoEditEvent(Event event);
        void addEvent();
        void logout();
    }
}