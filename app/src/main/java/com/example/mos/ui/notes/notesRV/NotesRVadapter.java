package com.example.mos.ui.notes.notesRV;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mos.ui.notes.AddEditNoteActivity;
import com.example.mos.CustomConstants;
import com.example.mos.R;
import com.example.mos.googleDrive.GoogleDriveUtils;
import com.example.mos.sqlite.DBUtils;
import com.example.mos.sqlite.SQLiteDbQueries;
import com.example.mos.ui.notes.NotesFragment;

import java.util.ArrayList;

public class NotesRVadapter extends RecyclerView.Adapter {
    Context context;
    ArrayList<NoteItemModel> notes;

    public NotesRVadapter(Context context, ArrayList<NoteItemModel> notes){
        this.context=context;
        this.notes=notes;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_item_note, parent, false);
        NoteItemViewHolder itemViewHolder = new NoteItemViewHolder(view);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NoteItemViewHolder noteHolder = (NoteItemViewHolder) holder;
        NoteItemModel note = notes.get(position);
        noteHolder.classification.setText(note.getClassification());
        noteHolder.content.setText(note.getContent());
        noteHolder.category.setText(note.getCategory());
        noteHolder.state.setText(note.getState());

        noteHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotesFragment.notesRVupdating=false;
                Intent editNoteIntent = new Intent(context, AddEditNoteActivity.class);
                editNoteIntent.putExtra(CustomConstants.CALLED_BY,CustomConstants.NOTES_RV_ADAPTER);
                editNoteIntent.putExtra(CustomConstants.NOTE_CATEGORY,note.getCategory());
                editNoteIntent.putExtra(CustomConstants.NOTE_CLASSIFICATION,note.getClassification());
                editNoteIntent.putExtra(CustomConstants.NOTE_STATE,note.getState());
                editNoteIntent.putExtra(CustomConstants.NOTE_CONTENT,note.getContent());
                editNoteIntent.putExtra(CustomConstants.NOTE_ID,note.getDbRowNo());

                context.startActivity(editNoteIntent);
            }
        });

        noteHolder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                noteHolder.layout.setOnClickListener(null);
                String dbRowId = note.getDbRowNo();
                SQLiteDbQueries dbHelper = new SQLiteDbQueries(context);
                SQLiteDatabase dbw = dbHelper.getWritableDatabase();

                Toast.makeText(context, "Deleting", Toast.LENGTH_SHORT).show();
                SQLiteDbQueries.deleteRow(dbRowId,dbw);
                DBUtils.backupDBtoDriveRedownloadAndRefreshLocalDB(context, SQLiteDbQueries.DATABASE_NAME,GoogleDriveUtils.getDriveServiceFromLastSignedIn(context));
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteItemViewHolder extends RecyclerView.ViewHolder {
        TextView classification, content, category, state;
        ConstraintLayout layout;
        public NoteItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.classification = itemView.findViewById(R.id.rvNoteClassification);
            this.content = itemView.findViewById(R.id.rvNoteContent);
            this.category = itemView.findViewById(R.id.rvNoteCategory);
            this.state = itemView.findViewById(R.id.rvNoteState);
            this.layout = itemView.findViewById(R.id.noteItemLayout);
        }
    }
}