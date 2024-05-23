package com.example.mos.ui.notes;

import static com.example.mos.CustomConstants.DISCARDED_STATE;
import static com.example.mos.CustomConstants.EMOTION_CLASSIFICATION;
import static com.example.mos.CustomConstants.EXPERIMENTAL_STATE;
import static com.example.mos.CustomConstants.FINANCIAL_CLASSIFICATION;
import static com.example.mos.CustomConstants.MENTAL_CLASSIFICATION;
import static com.example.mos.CustomConstants.MINDSET_CATEGORY;
import static com.example.mos.CustomConstants.PHYSICAL_CLASSIFICATION;
import static com.example.mos.CustomConstants.RULES_CATEGORY;
import static com.example.mos.CustomConstants.SOCIAL_CLASSIFICATION;
import static com.example.mos.CustomConstants.TESTED_STATE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mos.CustomConstants;
import com.example.mos.R;
import com.example.mos.googleDrive.GoogleDriveUtils;
import com.example.mos.sqlite.DBUtils;
import com.example.mos.sqlite.NoteTableContract;
import com.example.mos.sqlite.SQLiteDbQueries;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.services.drive.Drive;

public class AddEditNoteActivity extends AppCompatActivity {

    SQLiteDbQueries dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addEditNotelayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new SQLiteDbQueries(this);
        SQLiteDatabase dbw = dbHelper.getWritableDatabase();
        SQLiteDatabase dbr = dbHelper.getReadableDatabase();


        ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intentData = result.getData();
                        try {
                            Drive googleDriveService = GoogleDriveUtils.getDriveService(AddEditNoteActivity.this,intentData);
                            DBUtils.backupDBtoDrive(this, SQLiteDbQueries.DATABASE_NAME,googleDriveService);
                        } catch (ApiException e) {
                            // Google Sign In failed
                            Log.w("CustomTag", "Google sign in failed:", e);
                        }
                    }
                }
        );

        EditText editContent = findViewById(R.id.editContent);
        Toolbar toolbar = findViewById(R.id.addNoteToolbar);
        Button categoryBtn = findViewById(R.id.categoryBtn);
        Button classificationBtn = findViewById(R.id.classificationBtn);
        Button stateBtn = findViewById(R.id.stateBtn);
        FloatingActionButton addNoteBtn = findViewById(R.id.addNoteBtn);

        Intent callingIntent = getIntent();
        Bundle extras = callingIntent.getExtras();
        String calledFrom=null;
        if(extras!=null) calledFrom = extras.getString(CustomConstants.CALLED_BY);
        if(calledFrom!=null && calledFrom.equals(CustomConstants.NOTES_RV_ADAPTER)){
            toolbar.setTitle("Edit Note");
            editContent.setText(extras.getString(CustomConstants.NOTE_CONTENT));
            categoryBtn.setText(extras.getString(CustomConstants.NOTE_CATEGORY));
            classificationBtn.setText(extras.getString(CustomConstants.NOTE_CLASSIFICATION));
            stateBtn.setText(extras.getString(CustomConstants.NOTE_STATE));
            addNoteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String content = editContent.getText().toString();
                    String classification = classificationBtn.getText().toString();
                    String category = categoryBtn.getText().toString();
                    String state = stateBtn.getText().toString();

                    ContentValues values = new ContentValues();
                    values.put(NoteTableContract.NoteTable.COLUMN_NAME_CATEGORY,category);
                    values.put(NoteTableContract.NoteTable.COLUMN_NAME_CLASSIFICATION,classification);
                    values.put(NoteTableContract.NoteTable.COLUMN_NAME_CONTENT,content);
                    values.put(NoteTableContract.NoteTable.COLUMN_NAME_STATE,state);

                    dbHelper.updateRow(dbw, NoteTableContract.NoteTable.TABLE_NAME,values,extras.getString(CustomConstants.NOTE_ID));
                    signInAndBackupDBtoDrive(signInLauncher);
                }
            });
        }
        else {
            toolbar.setTitle("Add Note");
            addNoteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String content = editContent.getText().toString();
                    String classification = classificationBtn.getText().toString();
                    String category = categoryBtn.getText().toString();
                    String state = stateBtn.getText().toString();
                    dbHelper.addNote(dbw, classification, category, content, state);
                    signInAndBackupDBtoDrive(signInLauncher);
                }
            });
        }
        categoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = categoryBtn.getText().toString();
                if(label.equals(MINDSET_CATEGORY)) categoryBtn.setText(RULES_CATEGORY);
                else categoryBtn.setText(MINDSET_CATEGORY);
            }
        });

        classificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = classificationBtn.getText().toString();
                if(label.equals(PHYSICAL_CLASSIFICATION)) classificationBtn.setText(SOCIAL_CLASSIFICATION);
                else if(label.equals(SOCIAL_CLASSIFICATION)) classificationBtn.setText(MENTAL_CLASSIFICATION);
                else if(label.equals(MENTAL_CLASSIFICATION)) classificationBtn.setText(EMOTION_CLASSIFICATION);
                else if(label.equals(EMOTION_CLASSIFICATION)) classificationBtn.setText(FINANCIAL_CLASSIFICATION);
                else if(label.equals(FINANCIAL_CLASSIFICATION)) classificationBtn.setText(PHYSICAL_CLASSIFICATION);
            }
        });

        stateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = stateBtn.getText().toString();
                if(label.equals(EXPERIMENTAL_STATE)) stateBtn.setText(TESTED_STATE);
                else if(label.equals(TESTED_STATE)) stateBtn.setText(DISCARDED_STATE);
                else if(label.equals(DISCARDED_STATE)) stateBtn.setText(EXPERIMENTAL_STATE);
            }
        });
    }

    private void signInAndBackupDBtoDrive(ActivityResultLauncher<Intent> signInLauncher) {
        GoogleSignInClient client = GoogleDriveUtils.signInGoogleAndGetClient(AddEditNoteActivity.this);
        Intent signInIntent = client.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }
}