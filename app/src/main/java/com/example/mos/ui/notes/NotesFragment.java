package com.example.mos.ui.notes;

import static com.example.mos.CustomConstants.ALL_CATEGORIES;
import static com.example.mos.CustomConstants.ALL_CLASSES;
import static com.example.mos.CustomConstants.ALL_STATES;
import static com.example.mos.CustomConstants.DISCARDED_STATE;
import static com.example.mos.CustomConstants.EMOTION_CLASSIFICATION;
import static com.example.mos.CustomConstants.EXPERIMENTAL_STATE;
import static com.example.mos.CustomConstants.FINANCIAL_CLASSIFICATION;
import static com.example.mos.CustomConstants.FOR;
import static com.example.mos.CustomConstants.FOR_KARTIK;
import static com.example.mos.CustomConstants.FOR_KETAN;
import static com.example.mos.CustomConstants.MENTAL_CLASSIFICATION;
import static com.example.mos.CustomConstants.MINDSET_CATEGORY;
import static com.example.mos.CustomConstants.PHYSICAL_CLASSIFICATION;
import static com.example.mos.CustomConstants.ROMANTIC_CLASSIFICATION;
import static com.example.mos.CustomConstants.RULES_CATEGORY;
import static com.example.mos.CustomConstants.SOCIAL_CLASSIFICATION;
import static com.example.mos.CustomConstants.TESTED_STATE;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mos.databinding.FragmentNotesBinding;
import com.example.mos.ui.SharedViewModel;
import com.example.mos.ui.notes.notesRV.NoteItemModel;
import com.example.mos.ui.notes.notesRV.NotesRVadapter;
import com.example.mos.sqlite.SQLiteDbQueries;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class NotesFragment extends Fragment{

    private FragmentNotesBinding binding;
    SQLiteDbQueries dbHelper;
    SharedViewModel sharedViewModel;
    NotesRVadapter notesRVadapter;
    Button classFilterBtn;
    Button categoryFilterBtn;
    Button stateFilterBtn;
    RecyclerView noteRV;
    public static Boolean notesRVupdating=false;
    ArrayList<NoteItemModel> notes = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        noteRV = binding.notesRV;
//        noteRV.setVisibility(View.GONE);
        notesRVadapter = new NotesRVadapter(requireContext(), notes);
        noteRV.setLayoutManager(new LinearLayoutManager(requireContext()));
        noteRV.setAdapter(notesRVadapter);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.isDbUpdated().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    refreshNotesRV();
                }
            }
        });

        FloatingActionButton floatingActionButton = binding.noteAddFAB;
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addNoteActIntent = new Intent(requireContext(), AddEditNoteActivity.class);
                notesRVupdating=false;
                startActivity(addNoteActIntent);
            }
        });

        classFilterBtn = binding.classFilterBtn;
        categoryFilterBtn = binding.categoryFilterBtn;
        stateFilterBtn = binding.stateFilterBtn;

        classFilterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = classFilterBtn.getText().toString();
                if(label.equals(ALL_CLASSES)) classFilterBtn.setText(PHYSICAL_CLASSIFICATION);
                else if(label.equals(PHYSICAL_CLASSIFICATION)) classFilterBtn.setText(SOCIAL_CLASSIFICATION);
                else if(label.equals(MENTAL_CLASSIFICATION)) classFilterBtn.setText(EMOTION_CLASSIFICATION);
                else if(label.equals(EMOTION_CLASSIFICATION)) classFilterBtn.setText(FINANCIAL_CLASSIFICATION);
                else if(label.equals(FINANCIAL_CLASSIFICATION)) classFilterBtn.setText(ALL_CLASSES);
                else if(FOR.equals(FOR_KETAN)){
                    if(label.equals(SOCIAL_CLASSIFICATION)) classFilterBtn.setText(ROMANTIC_CLASSIFICATION);
                    else if(label.equals(ROMANTIC_CLASSIFICATION)) classFilterBtn.setText(MENTAL_CLASSIFICATION);
                }
                else if(FOR.equals(FOR_KARTIK)){
                    if(label.equals(SOCIAL_CLASSIFICATION)) classFilterBtn.setText(MENTAL_CLASSIFICATION);
                }

                filterRVnotes(classFilterBtn, categoryFilterBtn, stateFilterBtn);
            }
        });

        categoryFilterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = categoryFilterBtn.getText().toString();
                if(label.equals(ALL_CATEGORIES)) categoryFilterBtn.setText(MINDSET_CATEGORY);
                else if(label.equals(MINDSET_CATEGORY)) categoryFilterBtn.setText(RULES_CATEGORY);
                else if(label.equals(RULES_CATEGORY)) categoryFilterBtn.setText(ALL_CATEGORIES);

                filterRVnotes(classFilterBtn, categoryFilterBtn, stateFilterBtn);
            }
        });

        stateFilterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = stateFilterBtn.getText().toString();
                if(label.equals(ALL_STATES)) stateFilterBtn.setText(EXPERIMENTAL_STATE);
                else if(label.equals(EXPERIMENTAL_STATE)) stateFilterBtn.setText(TESTED_STATE);
                else if(label.equals(TESTED_STATE)) stateFilterBtn.setText(DISCARDED_STATE);
                else if(label.equals(DISCARDED_STATE)) stateFilterBtn.setText(ALL_STATES);

                filterRVnotes(classFilterBtn, categoryFilterBtn, stateFilterBtn);
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        filterRVnotes(classFilterBtn,categoryFilterBtn,stateFilterBtn);
    }

    private ArrayList<NoteItemModel> filterNotes(String CLASS , String CATEGORY, String STATE){
        ArrayList<NoteItemModel> notes = new ArrayList<>();
        notes.addAll(refreshNotesArraylistFromLocalDB());
        Iterator<NoteItemModel> noteIterator = notes.iterator();
        if(!CLASS.equals(ALL_CLASSES)) {
            while(noteIterator.hasNext()){
                NoteItemModel note = noteIterator.next();
                if (!note.getClassification().equals(CLASS)) noteIterator.remove();
            }
        }
        noteIterator = notes.iterator();
        if(!CATEGORY.equals(ALL_CATEGORIES)) {
            while(noteIterator.hasNext()){
                NoteItemModel note = noteIterator.next();
                if (!note.getCategory().equals(CATEGORY)) noteIterator.remove();
            }
        }
        noteIterator = notes.iterator();
        if(!STATE.equals(ALL_STATES)){
            while(noteIterator.hasNext()){
                NoteItemModel note = noteIterator.next();
                if (!note.getState().equals(STATE)) noteIterator.remove();
            }
        }
        return notes;
    }

    private void filterRVnotes(Button classFilterBtn, Button categoryFilterBtn, Button stateFilterBtn){
        String classFilter = classFilterBtn.getText().toString();
        String categoryFilter = categoryFilterBtn.getText().toString();
        String stateFilter = stateFilterBtn.getText().toString();

        notes.clear();
        notes.addAll(filterNotes(classFilter,categoryFilter,stateFilter));
        Collections.reverse(notes);
        notesRVadapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private ArrayList<NoteItemModel> refreshNotesArraylistFromLocalDB() {
        dbHelper = new SQLiteDbQueries(getContext());
        SQLiteDatabase dbr = dbHelper.getReadableDatabase();
        ArrayList<NoteItemModel> notes = dbHelper.getAllNotes(dbr);
        return notes;
    }

    private void refreshNotesRV(){
        notes.clear();
        notes.addAll(refreshNotesArraylistFromLocalDB());
        Collections.reverse(notes);
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notesRVadapter.notifyDataSetChanged();
            }
        });
    }
}