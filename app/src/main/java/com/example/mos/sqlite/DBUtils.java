package com.example.mos.sqlite;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.example.mos.CustomConstants;
import com.example.mos.MainActivity;
import com.example.mos.googleDrive.GoogleDriveUtils;
import com.example.mos.notesRV.NoteItemModel;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DBUtils {

    private final static String FILE_NAME = "MOS.csv";
    private final static String APP_DIRECTORY = CustomConstants.DRIVE_APP_DIR;
    private final static String DRIVE_DIRECTORY_TYPE = "application/vnd.google-apps.folder";
    private final static String DRIVE_FILE_TYPE = "application/x-sqlite3";
    private final static String LOCAL_FILE_TYPE = "application/x-sqlite3";
    private final static String CUSTOM_LOG_TAG = CustomConstants.CUSTOM_LOG_TAG;

    public static void createDBbackupLocalFile(Context context, String dbName){
        try {
            FileChannel outputStream = context.openFileOutput(dbName, MODE_PRIVATE).getChannel();
            String dbPath = context.getDatabasePath(dbName).getAbsolutePath();
            File dbFile = new File(dbPath);
            FileChannel inputStream = new FileInputStream(dbFile).getChannel();
            outputStream.transferFrom(inputStream,0,inputStream.size());
            outputStream.close();
        } catch (Exception e) {
            Log.e(CUSTOM_LOG_TAG, "createDBbackupLocalFile: ", e);
        }
    }

    public static void backupDBtoDrive(Context context, String dbName, Drive googleDriveService){
        createDBbackupLocalFile(context, dbName);
        createOrOverrideFileInDrive(context, googleDriveService, dbName);
    }

    public static void backupDBtoDriveRedownloadAndRefreshLocalDB(Context context, String dbName, Drive googleDriveService){
        createDBbackupLocalFile(context, dbName);
        createOrOverrideFileInDriveRedownloadAndRefreshLocalDB(context, googleDriveService, dbName);
    }

    private static void createOrOverrideFileInDriveRedownloadAndRefreshLocalDB(Context context, Drive googleDriveService, String filename) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    com.google.api.services.drive.model.File directoryMetadata = new com.google.api.services.drive.model.File();
                    directoryMetadata.setName(APP_DIRECTORY);
                    directoryMetadata.setMimeType(DRIVE_DIRECTORY_TYPE);
                    String dirID = GoogleDriveUtils.driveDirID(APP_DIRECTORY, googleDriveService);
                    if(dirID==null) {
                        com.google.api.services.drive.model.File Directory = googleDriveService.files().create(directoryMetadata)
                                .setFields("id")
                                .execute();
                        dirID = Directory.getId();
                    }
                    List<String> parentDirectory = Collections.singletonList(dirID);
                    com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                    fileMetadata.setName(filename);
                    fileMetadata.setMimeType(DRIVE_FILE_TYPE);
                    java.io.File filePath = context.getFileStreamPath(filename);
                    FileContent mediaContent = new FileContent(LOCAL_FILE_TYPE, filePath);

                    String fileID = GoogleDriveUtils.driveFileID(filename, dirID, googleDriveService);
                    Log.d(CUSTOM_LOG_TAG, "fileID: "+fileID);
                    if(fileID==null){
                        fileMetadata.setParents(parentDirectory);
                        googleDriveService.files().create(fileMetadata, mediaContent)
                                .setFields("id")
                                .execute();
                        Log.d(CUSTOM_LOG_TAG, "done uploading");
                    } else {
                        googleDriveService.files().update(fileID,fileMetadata,mediaContent).execute();
                        Log.d(CUSTOM_LOG_TAG, "done overwriting");
                    }
                    GoogleDriveUtils.downloadFileSaveAndUpdateDBlocalFile(filename,googleDriveService,filename,APP_DIRECTORY,context,filename,filename);
                    MainActivity.notesRVupdating=false;
                } catch (Exception e) {
                    Log.e(CUSTOM_LOG_TAG, "run: in CreateFIleIndrive", e);
                }
            }
        });
        thread.start();
    }


    private static void createOrOverrideFileInDrive(Context context,Drive googleDriveService,String filename) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    com.google.api.services.drive.model.File directoryMetadata = new com.google.api.services.drive.model.File();
                    directoryMetadata.setName(APP_DIRECTORY);
                    directoryMetadata.setMimeType(DRIVE_DIRECTORY_TYPE);
                    String dirID = GoogleDriveUtils.driveDirID(APP_DIRECTORY, googleDriveService);
                    if(dirID==null) {
                        com.google.api.services.drive.model.File Directory = googleDriveService.files().create(directoryMetadata)
                                .setFields("id")
                                .execute();
                        dirID = Directory.getId();
                    }
                    List<String> parentDirectory = Collections.singletonList(dirID);
                    com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                    fileMetadata.setName(filename);
                    fileMetadata.setMimeType(DRIVE_FILE_TYPE);
                    java.io.File filePath = context.getFileStreamPath(filename);
                    FileContent mediaContent = new FileContent(LOCAL_FILE_TYPE, filePath);

                    String fileID = GoogleDriveUtils.driveFileID(filename, dirID, googleDriveService);
                    Log.d(CUSTOM_LOG_TAG, "fileID: "+fileID);
                    if(fileID==null){
                        fileMetadata.setParents(parentDirectory);
                        googleDriveService.files().create(fileMetadata, mediaContent)
                                .setFields("id")
                                .execute();
                        Log.d(CUSTOM_LOG_TAG, "done uploading");
                    } else {
                        googleDriveService.files().update(fileID,fileMetadata,mediaContent).execute();
                        Log.d(CUSTOM_LOG_TAG, "done overwriting");
                    }

                    if (context instanceof Activity){
                        Activity activity = (Activity) context;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(CUSTOM_LOG_TAG, "run: in CreateFIleIndrive", e);
                }
            }
        });
        thread.start();
    }

    public static ArrayList<NoteItemModel> convertResultToNotes(ArrayList<Map<String,String>> result){
        ArrayList<NoteItemModel> notes = new ArrayList<>();
        for(Map<String, String> row: result){
            NoteItemModel note = new NoteItemModel();
            note.setState(row.get(NoteTableContract.NoteTable.COLUMN_NAME_STATE));
            note.setCategory(row.get(NoteTableContract.NoteTable.COLUMN_NAME_CATEGORY));
            note.setContent(row.get(NoteTableContract.NoteTable.COLUMN_NAME_CONTENT));
            note.setClassification(row.get(NoteTableContract.NoteTable.COLUMN_NAME_CLASSIFICATION));
            note.setDbRowNo(row.get(BaseColumns._ID));

            notes.add(note);
        }
        return notes;
    }

    public static void updateLocalDBwithLocalBackupFile(Context context, String backupFileName, String databaseName){
        File currentDB = context.getDatabasePath(databaseName);
        File backupFile = new File(context.getFilesDir(), backupFileName);
        try {
            if(currentDB.exists()) {
                try (FileChannel backupStream = context.openFileInput(backupFileName).getChannel()) {
                FileChannel databaseStream = new FileOutputStream(currentDB).getChannel();
                databaseStream.transferFrom(backupStream, 0, backupStream.size());
                backupStream.close();
                databaseStream.close();
            }
        }

        } catch (IOException e) {
            Log.e(CUSTOM_LOG_TAG, "updateLocalDBwithLocalBackupFile: ", e);
        }

    }

    public static void deleteRow(String notificationID, SQLiteDatabase dbw) {
        dbw.delete(NoteTableContract.NoteTable.TABLE_NAME, "_ID = ?", new String[]{notificationID});
    }
}
