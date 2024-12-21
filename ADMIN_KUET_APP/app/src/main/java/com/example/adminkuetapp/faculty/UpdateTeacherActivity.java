package com.example.adminkuetapp.faculty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.adminkuetapp.DriveServiceHelper;
import com.example.adminkuetapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Collections;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


public class UpdateTeacherActivity extends AppCompatActivity {

    private ImageView updateTeacherImage;
    private EditText updateTeacherName, updateTeacherEmail, updateTeacherPost;
    private Button updateTeacherBtn, deleteTeacherBtn;

    private static final int REQUEST_CODE_PICK_FILE = 101;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;

    private String name, email, image, post, uniqueKey, category;

    private Uri selectedFileUri;

    private DriveServiceHelper driveServiceHelper;

    private DatabaseReference reference, dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_teacher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        post = getIntent().getStringExtra("post");
        image = getIntent().getStringExtra("image");

        assert image != null;
        image = convertGoogleDriveLink(image);

        updateTeacherImage = findViewById(R.id.updateTeacherImage);
        updateTeacherName = findViewById(R.id.updateTeacherName);
        updateTeacherPost = findViewById(R.id.updateTeacherPost);
        updateTeacherEmail = findViewById(R.id.updateTeacherEmail);
        updateTeacherBtn = findViewById(R.id.updateTeacherBtn);
        deleteTeacherBtn = findViewById(R.id.deleteTeacherBtn);


        uniqueKey = getIntent().getStringExtra("key");
        category = getIntent().getStringExtra("category");


        reference = FirebaseDatabase.getInstance().getReference().child("Teachers");

        try {
            Picasso.get()
                    .load(image)
                    .placeholder(R.drawable.avatarprofile) // Temporary image while loading
                    .error(R.drawable.error_image)            // Image to display on error
                    .into(updateTeacherImage);


        } catch (Exception e) {
            e.printStackTrace();
            updateTeacherImage.setImageResource(R.drawable.error_image);
        }

        // Only if `image` is valid


        Log.d("ImageURL", "Image URL: " + image);


        updateTeacherEmail.setText(email);
        updateTeacherName.setText(name);
        updateTeacherPost.setText(post);


        updateTeacherImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkStoragePermission()) {
                    openFilePicker();
                } else {
                    requestStoragePermission();
                }
            }
        });

        updateTeacherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation();
            }
        });

        deleteTeacherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deleteFromGoogleDrive();

            }
        });


        // Check if the user is already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            // Request sign-in if not signed in
            requestSignIn();
        } else {
            // Initialize driveServiceHelper if user is signed in
            Log.d("AddFaculty", "User is already signed in: " + account.getEmail());
            GoogleAccountCredential credential = GoogleAccountCredential
                    .usingOAuth2(UpdateTeacherActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());

            Drive googleDriveService = new Drive.Builder(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    credential)
                    .setApplicationName("My Drive Tutorial")
                    .build();

            driveServiceHelper = new DriveServiceHelper(googleDriveService, UpdateTeacherActivity.this);
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


    private void checkValidation() {
        name = updateTeacherName.getText().toString();
        email = updateTeacherEmail.getText().toString();
        post = updateTeacherPost.getText().toString();
        if (name.isEmpty()) {
            updateTeacherName.setError("Empty");
            updateTeacherName.requestFocus();
        } else if (email.isEmpty()) {
            updateTeacherEmail.setError("Empty");
            updateTeacherEmail.requestFocus();
        } else if (post.isEmpty()) {
            updateTeacherPost.setError("Empty");
            updateTeacherPost.requestFocus();
        } else if (selectedFileUri == null) {

            updateToFirebase(image);
        } else {
            uploadFileToDrive(selectedFileUri);
        }
    }


    private void handleFileSelection(Uri fileUri) {
        if (fileUri != null) {
            selectedFileUri = fileUri;

            String mimeType = getContentResolver().getType(fileUri);

            if (mimeType != null && mimeType.startsWith("image/")) {
                // Display the selected image
                updateTeacherImage.setImageURI(fileUri);
            } else {
                Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to get file URI", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignInIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(googleSignInAccount -> {
                    Log.d("SignIn", "Sign-in successful! Account: " + googleSignInAccount.getEmail());

                    GoogleAccountCredential credential = GoogleAccountCredential
                            .usingOAuth2(UpdateTeacherActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleSignInAccount.getAccount());

                    Drive googleDriveService = new Drive.Builder(
                            new NetHttpTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName("My Drive Tutorial")
                            .build();

                    // Initialize driveServiceHelper after sign-in
                    driveServiceHelper = new DriveServiceHelper(googleDriveService, UpdateTeacherActivity.this);
                })
                .addOnFailureListener(e -> {
                    Log.e("SignInError", "Failed to sign in", e);
                    Toast.makeText(UpdateTeacherActivity.this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");  // Restrict to image file types only.
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    private String convertGoogleDriveLink(String link) {
        if (link.contains("drive.google.com/file/d/")) {
            String fileId = link.split("/d/")[1].split("/")[0];
            return "https://drive.google.com/uc?id=" + fileId;
        }
        return link; // Return as is if not a Google Drive link
    }

    private void uploadFileToDrive(Uri fileUri) {

        if (fileUri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }
        new UploadFileTask().execute(fileUri);
    }


    private String getTeachersFolderId() {
        try {
            // Query for a folder named "Teachers"
            String query = "mimeType='application/vnd.google-apps.folder' and name='Teachers' and trashed=false";
            Drive.Files.List request = driveServiceHelper.mDriveService.files().list()
                    .setQ(query)
                    .setFields("files(id, name)")
                    .setSpaces("drive");

            // Execute the query
            com.google.api.services.drive.model.FileList result = request.execute();

            // Check if folder is found
            if (result.getFiles().isEmpty()) {
                // Folder doesn't exist, create it
                return createTeachersFolder();
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

    private String createTeachersFolder() {
        try {
            // Create a new folder in Google Drive
            com.google.api.services.drive.model.File folderMetadata = new com.google.api.services.drive.model.File()
                    .setName("Teachers")
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


    private void updateToFirebase(String fileUrl) {

        HashMap hp = new HashMap();

        hp.put("name", name);
        hp.put("email", email);
        hp.put("post", post);
        hp.put("image", fileUrl); //cant put image here otherwise old image will be uploaded


        reference.child(category).child(uniqueKey).updateChildren(hp).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(UpdateTeacherActivity.this, "Teacher details Updated successfully in Firebase realtime database", Toast.LENGTH_SHORT).show();


//                Intent intent = new Intent(UpdateTeacherActivity.this, UpdateFaculty.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
                finish();

                /*
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                This modifies the behavior of the navigation to the target activity (UpdateFaculty).

                Intent.FLAG_ACTIVITY_CLEAR_TASK:

                Clears the entire activity stack.
                Ensures all previous activities are removed, so when the user presses the back button, they won't return to the previous activity (UpdateTeacherActivity).
                Intent.FLAG_ACTIVITY_CLEAR_TOP:

                If the target activity (UpdateFaculty) is already in the stack, all activities on top of it are cleared, and the existing instance is reused instead of creating a new one.
                These flags are useful for managing navigation and preventing duplicate activities in the stack
                */

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateTeacherActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });


        //startActivity(new Intent(UpdateTeacherActivity.this, UpdateFaculty.class));  //back to previous activity


    }


    private void deleteFromGoogleDrive() {
        // Assuming `image` contains the file URL, extract file ID from it
        String fileId = extractFileIdFromUrl(image);


        if (fileId != null && driveServiceHelper != null) {   //fill id is null why???
            driveServiceHelper.deleteFile(fileId, new DriveServiceHelper.FileDeleteCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(UpdateTeacherActivity.this, "File deleted successfully from Google Drive", Toast.LENGTH_SHORT).show();
                        // Proceed to delete from Firebase
                        deleteTeacherFromFirebase();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(UpdateTeacherActivity.this, "Failed to delete file from Google Drive: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            Toast.makeText(this, "Invalid Drive file ID or DriveServiceHelper not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractFileIdFromUrl(String url) {
        if (url != null && url.contains("drive.google.com/uc?id=")) {
            String fileId = url.split("id=")[1];
            if (fileId.contains("&")) {
                fileId = fileId.split("&")[0];
            }
            return fileId;
        }
        return null;
    }


    private void deleteTeacherFromFirebase() {
        reference.child(category).child(uniqueKey).removeValue()
                .addOnCompleteListener(task -> {
                    Toast.makeText(UpdateTeacherActivity.this, "Teacher details deleted successfully from Firebase", Toast.LENGTH_SHORT).show();


//                    Intent intent = new Intent(UpdateTeacherActivity.this, UpdateFaculty.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UpdateTeacherActivity.this, "Failed to delete details from Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Enable storage permission from settings to use this feature.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                }
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
            progressDialog = new ProgressDialog(UpdateTeacherActivity.this);
            progressDialog.setTitle("Uploading to Google Drive and Updating Firebase Realtime Database");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Uri... uris) {
            Uri fileUri = uris[0];
            String folderName = "Teachers";
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                if (inputStream != null) {
                    // Call your DriveServiceHelper to upload the file
                    driveServiceHelper.createFileFromInputStream(inputStream, fileUri, folderName, new DriveServiceHelper.FileUploadCallback() {  // where and how to initialize folderName?
                        @Override
                        public void onSuccess(String fileId) {
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Teacher Image File uploaded successfully in Google Drive", Toast.LENGTH_SHORT).show();
                                // Construct the file URL
                                String fileUrl = "https://drive.google.com/file/d/" + fileId + "/view";
                                updateToFirebase(fileUrl);
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Error uploading teacher image file in Google Drive: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(UpdateTeacherActivity.this, "Upload failed in Google Drive", Toast.LENGTH_SHORT).show();
            }
        }
    }

}