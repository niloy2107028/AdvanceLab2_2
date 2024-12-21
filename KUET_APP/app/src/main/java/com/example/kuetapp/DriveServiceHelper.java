package com.example.kuetapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DriveServiceHelper {

    public final Drive mDriveService;
    private final Context context;

    // Constructor to initialize the Drive API service and context
    public DriveServiceHelper(Drive driveService, Context context) {
        this.mDriveService = driveService;
        this.context = context;
    }

    // Interface for handling file upload callbacks
    public interface FileUploadCallback {
        void onSuccess(String fileId); // Called on successful upload

        void onFailure(Exception e);  // Called on failure
    }


    public void createFileFromInputStream(InputStream inputStream, Uri fileUri, String folderName, FileUploadCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                // Retrieve file name from URI
                String fileName = getFileNameFromUri(fileUri);

                // Create metadata for the file
                File fileMetadata = new File();
                fileMetadata.setName(fileName);

                // Dynamically determine the folder ID
                String folderId = getFolderId(folderName);
                fileMetadata.setParents(Collections.singletonList(folderId)); // Add parent folder ID

                // Define the media content for upload
                AbstractInputStreamContent mediaContent = new InputStreamContent(
                        getMimeTypeFromUri(fileUri), inputStream);

                // Prepare the request to create the file on Google Drive
                Drive.Files.Create request = mDriveService.files()
                        .create(fileMetadata, mediaContent)
                        .setFields("id");

                // Add progress listener for upload tracking
                MediaHttpUploader uploader = request.getMediaHttpUploader();
                uploader.setProgressListener(uploader1 -> {
                    switch (uploader1.getUploadState()) {
                        case MEDIA_IN_PROGRESS:
                            Log.d("UploadProgress", "Upload in progress: " + uploader1.getProgress());
                            break;
                        case MEDIA_COMPLETE:
                            Log.d("UploadProgress", "Upload complete!");
                            break;
                        default:
                            break;
                    }
                });

                // Execute the file upload request
                File uploadedFile = request.execute();
                Log.d("DriveServiceHelper", "File uploaded successfully. File ID: " + uploadedFile.getId());

                // Notify success
                if (callback != null) {
                    callback.onSuccess(uploadedFile.getId());
                }
            } catch (Exception e) {
                Log.e("DriveServiceHelper", "Error uploading file", e);

                // Notify failure
                if (callback != null) {
                    callback.onFailure(e);
                }
            } finally {
                executor.shutdown();
            }
        });
    }


    private String getFileNameFromUri(Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    // Check if the DISPLAY_NAME column exists
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }

        return result;
    }


    private String getMimeTypeFromUri(Uri uri) {
        return context.getContentResolver().getType(uri);
    }


    String getFolderId(String folderName) throws IOException {
        Drive.Files.List request = mDriveService.files().list()
                .setQ("name = '" + folderName + "' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                .setFields("files(id, name)");

        com.google.api.services.drive.model.FileList fileList = request.execute();

        if (fileList.getFiles().isEmpty()) {
            return createFolder(folderName); // Create folder if not found
        } else {
            return fileList.getFiles().get(0).getId(); // Return the folder ID
        }
    }

    private String createFolder(String folderName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File folder = mDriveService.files().create(fileMetadata)
                .setFields("id")
                .execute();
        return folder.getId();
    }


    //change


    public void deleteFile(String fileId, FileDeleteCallback callback) {
        new Thread(() -> {
            try {
                mDriveService.files().delete(fileId).execute();
                callback.onSuccess();
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }

    public interface FileDeleteCallback {
        void onSuccess();

        void onFailure(Exception e);
    }


}
