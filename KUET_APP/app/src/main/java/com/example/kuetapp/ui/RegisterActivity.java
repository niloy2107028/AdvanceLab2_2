package com.example.kuetapp.ui;

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
import android.widget.TextView;
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

import com.example.kuetapp.DriveServiceHelper;
import com.example.kuetapp.MainActivity;
import com.example.kuetapp.R;
import com.example.kuetapp.UserData;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class RegisterActivity extends AppCompatActivity {

    private EditText regName, regBatch, regDepartment, regRoll, regEmail, regPass;
    private Button register;
    private TextView login;
    private ImageView regImage;


    private DriveServiceHelper driveServiceHelper;


    private String name, email, pass, batch, department, roll;

    private FirebaseAuth auth;

    private Uri selectedFileUri;

    private static final int REQUEST_CODE_PICK_FILE = 101;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;

    private DatabaseReference reference;

    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Remove ActionBar programmatically
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        //EdgeToEdge.enable(this);   //for white status bar if want green then comment out


        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();


        regName = findViewById(R.id.registerstudentName);
        regEmail = findViewById(R.id.registerstudentEmail);
        regPass = findViewById(R.id.registerStudentPassword);
        regRoll = findViewById(R.id.registerstudentRoll);
        regDepartment = findViewById(R.id.registerstudentDepartment);
        regBatch = findViewById(R.id.registerstudentBatch);
        register = findViewById(R.id.registerstudentButton);
        regImage = findViewById(R.id.registerUserImage);

        reference = FirebaseDatabase.getInstance().getReference().child("Users");


        login = findViewById(R.id.openLogin);

        regImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkStoragePermission()) {
                    openFilePicker();
                } else {
                    requestStoragePermission();
                }
            }
        });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateUser();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
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
                    .usingOAuth2(RegisterActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());

            Drive googleDriveService = new Drive.Builder(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    credential)
                    .setApplicationName("My Drive Tutorial")
                    .build();

            driveServiceHelper = new DriveServiceHelper(googleDriveService, RegisterActivity.this);
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

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {

            startActivity(new Intent(this, MainActivity.class));
            finish();

        }

    }

    private void validateUser() {

        name = regName.getText().toString();
        email = regEmail.getText().toString();
        pass = regPass.getText().toString();
        roll = regRoll.getText().toString();
        batch = regBatch.getText().toString();
        department = regDepartment.getText().toString();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || roll.isEmpty() || batch.isEmpty() || department.isEmpty() || selectedFileUri == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            registerUser();
        }

    }

    private void registerUser() {
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            updateuser();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration Unsuccessful. Reason : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        userUid = auth.getUid();          // is this correct way ?


    }

    private void updateuser() {
        UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        auth.getCurrentUser().updateProfile(changeRequest)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        uploadFileToGoogleDrive(selectedFileUri);
                    }
                });

//        auth.signOut();
//
//        openlogin();
    }

    private void uploadFileToGoogleDrive(Uri fileUri) {
        // Upload file to Google Drive (this method should upload the file and return a URL)
        new UploadFileTask().execute(fileUri);
    }


    private void handleFileSelection(Uri fileUri) {
        if (fileUri != null) {
            selectedFileUri = fileUri;

            String mimeType = getContentResolver().getType(fileUri);

            if (mimeType != null && mimeType.startsWith("image/")) {
                // Display the selected image
                regImage.setImageURI(fileUri);
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
                            .usingOAuth2(RegisterActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleSignInAccount.getAccount());

                    Drive googleDriveService = new Drive.Builder(
                            new NetHttpTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName("My Drive Tutorial")
                            .build();

                    // Initialize driveServiceHelper after sign-in
                    driveServiceHelper = new DriveServiceHelper(googleDriveService, RegisterActivity.this);
                })
                .addOnFailureListener(e -> {
                    Log.e("SignInError", "Failed to sign in", e);
                    Toast.makeText(RegisterActivity.this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");  // Restrict to image file types only.
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }


    private String getusersFolderId() {
        try {
            // Query for a folder named "users"
            String query = "mimeType='application/vnd.google-apps.folder' and name='Users' and trashed=false";
            Drive.Files.List request = driveServiceHelper.mDriveService.files().list()
                    .setQ(query)
                    .setFields("files(id, name)")
                    .setSpaces("drive");

            // Execute the query
            com.google.api.services.drive.model.FileList result = request.execute();

            // Check if folder is found
            if (result.getFiles().isEmpty()) {
                // Folder doesn't exist, create it
                return createusersFolder();
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

    private String createusersFolder() {
        try {
            // Create a new folder in Google Drive
            com.google.api.services.drive.model.File folderMetadata = new com.google.api.services.drive.model.File()
                    .setName("Users")
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
            progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setTitle("Uploading to Google Drive and Firebase Realtime Database");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Uri... uris) {
            Uri fileUri = uris[0];
            String folderName = "Users";
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                if (inputStream != null) {
                    // Call your DriveServiceHelper to upload the file
                    driveServiceHelper.createFileFromInputStream(inputStream, fileUri, folderName, new DriveServiceHelper.FileUploadCallback() {  // where and how to initialize folderName?
                        @Override
                        public void onSuccess(String fileId) {
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "user Image File uploaded successfully in Google Drive", Toast.LENGTH_SHORT).show();
                                // Construct the file URL
                                String fileUrl = "https://drive.google.com/file/d/" + fileId + "/view";
                                saveToFirebase(fileUrl);
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Error uploading user image file in Google Drive: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "inputStream is null", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(RegisterActivity.this, "Upload failed in Google Drive", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveToFirebase(String fileUrl) {
        String userUid = auth.getUid();
        if (userUid != null) {
            UserData userData = new UserData(
                    regName.getText().toString(),
                    regEmail.getText().toString(),
                    regBatch.getText().toString(),
                    regDepartment.getText().toString(),
                    regRoll.getText().toString(),
                    fileUrl // Store the uploaded image URL here
            );
            reference.child(userUid).setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        // Once data is uploaded to Firebase, finish the activity
                        Toast.makeText(RegisterActivity.this, "User details successfully uploaded in Firebase database and registration Successful", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        finish();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(RegisterActivity.this, "Failed to save details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


}