package com.example.adminkuetapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import com.google.api.services.drive.model.File;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import android.app.ProgressDialog;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;


public class UploadEbook extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;
    private static final int REQUEST_CODE_PICK_FILE = 101;

    private DriveServiceHelper driveServiceHelper;

    private TextView selectedpdfFileTextView;
    private TextInputEditText pdfTitle;
    private Uri selectedPdfFileUri;
    private ImageView ebookPreview;

    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_ebook);

        reference = FirebaseDatabase.getInstance().getReference().child("Ebooks");

        MaterialCardView uploadEbookCard = findViewById(R.id.uploadEbook);
        selectedpdfFileTextView = findViewById(R.id.selectedPdfFileTextView);
        pdfTitle = findViewById(R.id.pdfTitle);
        Button uploadpdfButton = findViewById(R.id.uploadPdfButton);


        ebookPreview = findViewById(R.id.ebookPreview);


        uploadEbookCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkStoragePermission()) {
                    openFilePicker();
                } else {
                    requestStoragePermission();
                }
            }
        });

        uploadpdfButton.setOnClickListener(view -> {
            if (pdfTitle.getText().toString().isEmpty()) {
                pdfTitle.setError("Enter a title");
                pdfTitle.requestFocus();
            } else if (selectedPdfFileUri == null) {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            } else {
                uploadFileToDrive(selectedPdfFileUri);
            }


        });

        // Check if the user is already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            // Request sign-in if not signed in
            requestSignIn();
        } else {
            // Initialize driveServiceHelper if user is signed in
            Log.d("UploadEbook", "User is already signed in: " + account.getEmail());
            GoogleAccountCredential credential = GoogleAccountCredential
                    .usingOAuth2(UploadEbook.this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());

            Drive googleDriveService = new Drive.Builder(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    credential)
                    .setApplicationName("My Drive Tutorial")
                    .build();

            driveServiceHelper = new DriveServiceHelper(googleDriveService, UploadEbook.this);
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
            selectedPdfFileUri = fileUri;
            selectedpdfFileTextView.setText(fileUri.getLastPathSegment());

            String mimeType = getContentResolver().getType(fileUri);

            if (mimeType != null && mimeType.equals("application/pdf")) {
                // Display the first page of the PDF
                renderPdfPage(fileUri, ebookPreview);
            } else if (mimeType != null && mimeType.equals("application/ppt")) {
                renderPdfPage(fileUri, ebookPreview);
            }
            else{
                Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to get file URI", Toast.LENGTH_SHORT).show();
        }
    }

    private void renderPdfPage(Uri pdfUri, ImageView ebookPreview) {
        try {
            ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(pdfUri, "r");
            if (fileDescriptor != null) {
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                PdfRenderer.Page page = pdfRenderer.openPage(0);

                // Create a Bitmap to render the page
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                // Set the Bitmap to the ImageView
                ebookPreview.setImageBitmap(bitmap);

                // Close the page and renderer
                page.close();
                pdfRenderer.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error rendering PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignInIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(googleSignInAccount -> {
                    Log.d("SignIn", "Sign-in successful! Account: " + googleSignInAccount.getEmail());

                    GoogleAccountCredential credential = GoogleAccountCredential
                            .usingOAuth2(UploadEbook.this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleSignInAccount.getAccount());

                    Drive googleDriveService = new Drive.Builder(
                            new NetHttpTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName("My Drive Tutorial")
                            .build();

                    // Initialize driveServiceHelper after sign-in
                    driveServiceHelper = new DriveServiceHelper(googleDriveService, UploadEbook.this);
                })
                .addOnFailureListener(e -> {
                    Log.e("SignInError", "Failed to sign in", e);
                    Toast.makeText(UploadEbook.this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");  // Allows all file types.
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    private void uploadFileToDrive(Uri fileUri) {
        new UploadFileTask().execute(fileUri);
    }

    private String getEbooksFolderId() {
        try {
            // Query for a folder named "Ebooks"
            String query = "mimeType='application/vnd.google-apps.folder' and name='Ebooks' and trashed=false";
            Drive.Files.List request = driveServiceHelper.mDriveService.files().list()
                    .setQ(query)
                    .setFields("files(id, name)")
                    .setSpaces("drive");

            // Execute the query
            com.google.api.services.drive.model.FileList result = request.execute();

            // Check if folder is found
            if (result.getFiles().isEmpty()) {
                // Folder doesn't exist, create it
                return createEbooksFolder();
            } else {
                // Folder exists, return the folder ID
                return result.getFiles().get(0).getId();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Drive", "Error fetching folder ID: " + e.getMessage());
            return null;  // Return null if an exception occurs
        }
    }

    private String createEbooksFolder() {
        try {
            // Create a new folder in Google Drive
            com.google.api.services.drive.model.File folderMetadata = new com.google.api.services.drive.model.File()
                    .setName("Ebooks")
                    .setMimeType("application/vnd.google-apps.folder");

            // Create the folder on Google Drive
            com.google.api.services.drive.model.File folder = driveServiceHelper.mDriveService.files().create(folderMetadata)
                    .setFields("id")
                    .execute();

            // Return the folder ID
            return folder.getId();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Drive", "Error creating folder: " + e.getMessage());
            return null;  // Handle exception properly
        }
    }


    private void saveToFirebase(String fileUrl) {
        String title = pdfTitle.getText().toString();
        String uniqueKey = reference.push().getKey();

        HashMap data=new HashMap();

        data.put("pdfTitle",title);
        data.put("pdfUrl",fileUrl);


        if (uniqueKey != null && !title.isEmpty()) {


            reference.child("pdf").child(uniqueKey).setValue(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Ebook pdf file uploaded and saved successfully in Firebase realtime database", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save ebook data in Firebase realtime database : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Error: Missing title or failed to generate unique key.", Toast.LENGTH_SHORT).show();
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
            progressDialog = new ProgressDialog(UploadEbook.this);
            progressDialog.setTitle("Uploading to Google Drive and Firebase Realtime Database");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Uri... uris) {
            Uri fileUri = uris[0];
            String folderName = "Ebooks";
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                if (inputStream != null) {
                    // Call your DriveServiceHelper to upload the file
                    driveServiceHelper.createFileFromInputStream(inputStream, fileUri, folderName, new DriveServiceHelper.FileUploadCallback() {  // where and how to initialize folderName?
                        @Override
                        public void onSuccess(String fileId) {
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "File uploaded successfully in Google Drive", Toast.LENGTH_SHORT).show();
                                // Construct the file URL
                                String fileUrl = "https://drive.google.com/file/d/" + fileId + "/view";
                                saveToFirebase(fileUrl);
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Error uploading file in Google Drive : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;  // Return true if upload is successful, else false
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
                progressDialog.dismiss();
                Toast.makeText(UploadEbook.this, "Upload failed in Google Drive", Toast.LENGTH_SHORT).show();
            }
        }
    }

}