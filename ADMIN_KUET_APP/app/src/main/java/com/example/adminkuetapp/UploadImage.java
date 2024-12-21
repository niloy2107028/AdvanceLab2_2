package com.example.adminkuetapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BitmapCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.Collections;

public class UploadImage extends AppCompatActivity {


    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;
    private static final int REQUEST_CODE_PICK_FILE = 101;

    private DriveServiceHelper driveServiceHelper;


    private Spinner imageCategory;
    private CardView selectImage;
    private Button imageUpload;
    private ImageView galleryImagePreview;
    private String category;

    private Uri selectedFileUri;
    private TextView selectedFileTextView;

    private Bitmap bitmap;

    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        //EdgeToEdge.enable(this); for getting red color

        setContentView(R.layout.activity_upload_image);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageCategory = findViewById(R.id.image_category);
        selectImage = findViewById(R.id.addGalleryImage);
        imageUpload = findViewById(R.id.uploadImageButton);
        galleryImagePreview = findViewById(R.id.galleryImagePreview);

        selectedFileTextView = findViewById(R.id.selectedFileTextViewGallery);

        reference = FirebaseDatabase.getInstance().getReference().child("Gallery");


        String[] items = new String[]{"Select Category", "Convocation", "Independence Day", "Other Events"};

        imageCategory.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items));

        imageCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                category = imageCategory.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkStoragePermission()) {
                    openFilePicker();
                } else {
                    requestStoragePermission();
                }
            }
        });

        imageUpload.setOnClickListener(view -> {
            if (category.equals("Select Category")) {
                Toast.makeText(this, "Please select a category!", Toast.LENGTH_SHORT).show();
            } else if (selectedFileUri == null) {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            } else {
                uploadFileToDrive(selectedFileUri);
            }


        });

        // Check if the user is already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            // Request sign-in if not signed in
            requestSignIn();
        } else {
            // Initialize driveServiceHelper if user is signed in
            Log.d("UploadImage", "User is already signed in: " + account.getEmail());
            GoogleAccountCredential credential = GoogleAccountCredential
                    .usingOAuth2(UploadImage.this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());

            Drive googleDriveService = new Drive.Builder(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    credential)
                    .setApplicationName("My Drive Tutorial")
                    .build();

            driveServiceHelper = new DriveServiceHelper(googleDriveService, UploadImage.this);
        }


    }

    private void requestSignIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        startActivityForResult(client.getSignInIntent(), 400);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 400 && resultCode == RESULT_OK) {
            handleSignInIntent(data);
        } else if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
            handleFileSelection(data.getData());
        }
    }

    private void handleFileSelection(Uri fileUri) {
        if (fileUri != null) {
            selectedFileUri = fileUri;

            selectedFileTextView.setText(getFileName(fileUri));


            String mimeType = getContentResolver().getType(fileUri);

            if (mimeType != null && mimeType.startsWith("image/")) {
                // Display the selected image
                galleryImagePreview.setImageURI(fileUri);
            } else {
                Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to get file URI", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            String name = cursor.getString(nameIndex);
            cursor.close();
            return name != null ? name : "Untitled";
        }
        return "Untitled"; // Fallback if no name found
    }


    private void handleSignInIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(googleSignInAccount -> {
                    GoogleAccountCredential credential = GoogleAccountCredential
                            .usingOAuth2(UploadImage.this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleSignInAccount.getAccount());

                    Drive googleDriveService = new Drive.Builder(
                            new NetHttpTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName("My Drive Tutorial")
                            .build();

                    driveServiceHelper = new DriveServiceHelper(googleDriveService, UploadImage.this);
                })
                .addOnFailureListener(e -> {
                    Log.e("SignInError", "Failed to sign in", e);
                    Toast.makeText(UploadImage.this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");  // Restrict to image file types only.
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    private void uploadFileToDrive(Uri fileUri) {
        new UploadFileTask().execute(fileUri);
    }

    private String getGalleryFolderId() {
        try {
            // Query for a folder named "Gallery"
            String query = "mimeType='application/vnd.google-apps.folder' and name='Gallery' and trashed=false";
            Drive.Files.List request = driveServiceHelper.mDriveService.files().list()
                    .setQ(query)
                    .setFields("files(id, name)")
                    .setSpaces("drive");

            // Execute the query
            com.google.api.services.drive.model.FileList result = request.execute();

            // Check if folder is found
            if (result.getFiles().isEmpty()) {
                // Folder doesn't exist, create it
                return createGalleryFolder();
            } else {
                // Folder exists, return the folder ID
                return result.getFiles().get(0).getId();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Drive", "Error fetching folder ID: " + e.getMessage());
            return null; // Return null if an exception occurs
        }
    }

    private String createGalleryFolder() {
        try {
            // Create a new folder in Google Drive
            com.google.api.services.drive.model.File folderMetadata = new com.google.api.services.drive.model.File()
                    .setName("Gallery")
                    .setMimeType("application/vnd.google-apps.folder");

            // Create the folder on Google Drive
            com.google.api.services.drive.model.File folder = driveServiceHelper.mDriveService.files()
                    .create(folderMetadata)
                    .setFields("id")
                    .execute();

            // Return the folder ID
            return folder.getId();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Drive", "Error creating folder: " + e.getMessage());
            return null; // Handle exception properly
        }
    }


    private void saveToFirebase(String fileUrl) {

        reference=reference.child(category);


        String uniqueKey = reference.push().getKey();


        if (uniqueKey != null) {

            reference.child(uniqueKey).setValue(fileUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Image File uploaded and saved successfully in Firebase realtime database", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save image data in Firebase realtime database : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } else {
            Toast.makeText(this, "Error: Failed to generate unique key.", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
    }


    private class UploadFileTask extends AsyncTask<Uri, Void, Boolean> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(UploadImage.this);
            progressDialog.setTitle("Uploading to Google Drive and Firebase Realtime Database");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Uri... uris) {
            Uri fileUri = uris[0];
            String folderName = "Gallery";
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                if (inputStream != null) {
                    // Call your DriveServiceHelper to upload the file
                    driveServiceHelper.createFileFromInputStream(inputStream, fileUri,folderName,
                            new DriveServiceHelper.FileUploadCallback() {
                                @Override
                                public void onSuccess(String fileId) {
                                    runOnUiThread(() -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Image File uploaded successfully in Google Drive",
                                                Toast.LENGTH_SHORT).show();
                                        // Construct the file URL
                                        String fileUrl = "https://drive.google.com/file/d/" + fileId + "/view";
                                        saveToFirebase(fileUrl);
                                        finish(); // Call finish() here after everything has succeeded
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    runOnUiThread(() -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(),
                                                "Error uploading image file in Google Drive : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true; // Return true if upload is successful, else false
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
                progressDialog.dismiss();
                Toast.makeText(UploadImage.this, "Upload failed in Google Drive", Toast.LENGTH_SHORT).show();
            }
        }
    }


}