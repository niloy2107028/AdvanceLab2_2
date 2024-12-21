package com.example.kuetapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kuetapp.R;
import com.example.kuetapp.ui.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private TextView name, batch, department, roll, email;
    private Button logout;
    private ImageView image;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        name = view.findViewById(R.id.studentName);
        batch = view.findViewById(R.id.studentBatch);
        department = view.findViewById(R.id.studentDepartment);
        roll = view.findViewById(R.id.studentRoll);
        email = view.findViewById(R.id.studentEmail);
        logout = view.findViewById(R.id.userLogout);

        image=view.findViewById(R.id.studentImage);

        // Initialize Firebase Database reference
        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        // Check if user is logged in
        if (user != null) {
            fetchUserData(user.getUid());
        } else {
            navigateToLogin();
        }

        // Logout button listener
        logout.setOnClickListener(v -> {
            auth.signOut();
            navigateToLogin();
        });

        return view;
    }

    private void fetchUserData(String uid) {
        // Fetch data for the current user by UID
        reference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Assuming your data model looks something like this
                    String userName = snapshot.child("name").getValue(String.class);
                    String userBatch = snapshot.child("batch").getValue(String.class);
                    String userDepartment = snapshot.child("dept").getValue(String.class);
                    String userRoll = snapshot.child("roll").getValue(String.class);
                    String userImageUrl = snapshot.child("imageUrl").getValue(String.class);


                    // Set the retrieved data to the TextViews
                    name.setText(userName != null ? userName : "N/A");
                    batch.setText(userBatch != null ? userBatch : "N/A");
                    department.setText(userDepartment != null ? userDepartment : "N/A");
                    roll.setText(userRoll != null ? userRoll : "N/A");
                    email.setText(user.getEmail());

                    assert userImageUrl != null;
                    userImageUrl=convertGoogleDriveLink(userImageUrl);

                    try {
                        Picasso.get()
                                .load(userImageUrl)
                                .placeholder(R.drawable.avatarprofile) // Temporary image while loading
                                .error(R.drawable.error_image)            // Image to display on error
                                .into(image);


                    } catch (Exception e) {
                        e.printStackTrace();
                        image.setImageResource(R.drawable.error_image);
                    }


                } else {
                    Toast.makeText(getActivity(), "User data not found!", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors (e.g., permissions issue, connectivity issue)
                Toast.makeText(getActivity(), "Failed to fetch user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Redirect to Login if user is null
        if (user == null) {
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish(); // Ensure the current activity is finished
    }

    private String convertGoogleDriveLink(String link) {
        if (link.contains("drive.google.com/file/d/")) {
            String fileId = link.split("/d/")[1].split("/")[0];
            return "https://drive.google.com/uc?id=" + fileId;
        }
        return link; // Return as is if not a Google Drive link
    }
}
