package com.example.mos.googleDrive;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.mos.CustomConstants;
import com.example.mos.MainActivity;
import com.example.mos.sqlite.DBUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

public class GoogleDriveUtils {

    public GoogleDriveUtils(MainActivity context){
        onDriveUpdatedLocalDBlistener=context;
    }
    public interface OnDriveUpdatedLocalDBlistener{
        public void setOnDriveUpdatedLocalDBListener();
    }
    public static OnDriveUpdatedLocalDBlistener onDriveUpdatedLocalDBlistener;
    private final static String CUSTOM_LOG_TAG = CustomConstants.CUSTOM_LOG_TAG;

    public static GoogleSignInClient signInGoogleAndGetClient(Context context) {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_FILE))
                .requestEmail()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(context, signInOptions);
        return client;
    }

    public static Drive getDriveService(Context context,Intent intentData) throws ApiException {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intentData);
        GoogleSignInAccount account = task.getResult(ApiException.class);
        // Google Sign In was successful, authenticate with Google Drive
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        Drive googleDriveService = new Drive.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("MOS")
                .build();

        return googleDriveService;
    }

    public static Drive getDriveServiceFromLastSignedIn(Context context){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(DriveScopes.DRIVE_FILE));
        assert account != null;
        credential.setSelectedAccount(account.getAccount());

        Drive googleDriveService = new Drive.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("MOS")
                .build();
        return googleDriveService;
    }

    public static void downloadFileSaveAndUpdateDBlocalFile(String filename, Drive googleDriveService, String driveFileName, String driveDir, Context context, String dbName, String localBackupfileName){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String dirID = driveDirID(driveDir,googleDriveService);
                String fileID = driveFileID(driveFileName, dirID, googleDriveService);
                Log.e(CUSTOM_LOG_TAG, "fileID: "+fileID);
                try {
                    OutputStream outputStream = context.openFileOutput(filename,Context.MODE_PRIVATE);
                    googleDriveService.files().get(fileID).executeMediaAndDownloadTo(outputStream);
                    DBUtils.updateLocalDBwithLocalBackupFile(context,localBackupfileName,dbName);
                    onDriveUpdatedLocalDBlistener.setOnDriveUpdatedLocalDBListener();
                } catch (Exception e) {
                    Log.e(CUSTOM_LOG_TAG, "downloadFileAndSaveAs: ", e);
                }
            }
        });
        thread.start();
    }

    public static String driveDirID(String dirName,Drive googleDriveService) {
        String pageToken = null;
        do {
            FileList result;
            try {
                result = googleDriveService.files().list()
                        .setQ("name = '"+dirName+"' and mimeType = 'application/vnd.google-apps.folder' and trashed = false" )
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();
                for (com.google.api.services.drive.model.File file : result.getFiles()) {
                    if(file.getName().equals(dirName)) return file.getId();
                }
                pageToken = result.getNextPageToken();
            } catch (Exception e){
                Log.e(CUSTOM_LOG_TAG, "driveDirID: ", e);
            }
        } while (pageToken != null);
        return null;
    }

    public static String driveFileID(String filename, String dirID, Drive googleDriveService) {
        String pageToken = null;
        do {
            FileList result;
            try{
                result = googleDriveService.files().list()
                        .setQ("name = '" + filename + "' and '"+dirID+"' in parents and trashed = false")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();
                for (com.google.api.services.drive.model.File file : result.getFiles()) {
                    if(file.getName().equals(filename)) return file.getId();
                }
            } catch (IOException e) {
                Log.e(CUSTOM_LOG_TAG, "driveFileID: ", e);
            }
        } while(pageToken!=null);
        return null;
    }
}
