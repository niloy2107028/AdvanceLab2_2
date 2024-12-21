package com.example.adminkuetapp.notice;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adminkuetapp.DriveServiceHelper;
import com.example.adminkuetapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewAdapter> {

    private final Context context;
    private final ArrayList<NoticeData> list;
    private final DriveServiceHelper driveServiceHelper;

    public NoticeAdapter(Context context, ArrayList<NoticeData> list, DriveServiceHelper driveServiceHelper) {
        this.context = context;
        this.list = list;
        this.driveServiceHelper = driveServiceHelper;
    }

    @NonNull
    @Override
    public NoticeViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.newsfeed_item_layout, parent, false);
        return new NoticeViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewAdapter holder, int position) {
        NoticeData currentItem = list.get(position);
        String correctedUrl = convertGoogleDriveLink(currentItem.getFileUrl());

        holder.deleteNoticeTitle.setText(currentItem.getTitle());

        try {
            if (currentItem.getFileUrl() != null) {
                Picasso.get()
                        .load(correctedUrl)
                        .placeholder(R.drawable.loading) // Placeholder image
                        .error(R.drawable.error_image) // Error image
                        .into(holder.deleteNoticeImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.deleteNotice.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setMessage("Are you sure want to Delete this notice ? ");
            builder.setCancelable(true);

            builder.setPositiveButton(
                    "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {


                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Notice");
                            reference.child(currentItem.getUniqueKey()).removeValue().addOnCompleteListener(task -> {
                                Toast.makeText(context, "Notice deleted from Firebase database.", Toast.LENGTH_SHORT).show();
                                int currentPosition = holder.getAdapterPosition();
                                if (currentPosition != RecyclerView.NO_POSITION) {
                                    list.remove(currentPosition); // Remove item from the list
                                    notifyItemRemoved(currentPosition); // Notify RecyclerView
                                }

                                // Delete the file from Google Drive
                                deleteFromGoogleDrive(currentItem.getFileUrl());
                            }).addOnFailureListener(e ->
                                    Toast.makeText(context, "Failed to delete notice from Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );


                        }
                    }
            );
            builder.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    }
            );

            AlertDialog dialog = null;

            try {
                dialog = builder.create();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (dialog != null) {
                dialog.show();
            }


        });


    }

    private void deleteFromGoogleDrive(String url) {
        String fileId = extractFileIdFromUrl(url);

        if (fileId != null && driveServiceHelper != null) {
            driveServiceHelper.deleteFile(fileId, new DriveServiceHelper.FileDeleteCallback() {
                @Override
                public void onSuccess() {
                    // Ensure this runs on the main thread
                    ((AppCompatActivity) context).runOnUiThread(() ->
                            Toast.makeText(context, "File deleted successfully from Google Drive.", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onFailure(Exception e) {
                    // Ensure this runs on the main thread
                    ((AppCompatActivity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Failed to delete file from Google Drive: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            });
        } else {
            Toast.makeText(context, "Invalid Drive file ID or DriveServiceHelper not initialized.", Toast.LENGTH_SHORT).show();
        }
    }


    private String extractFileIdFromUrl(String url) {
        if (url != null) {
            if (url.contains("drive.google.com/file/d/")) {
                // Handle URLs of the form: https://drive.google.com/file/d/<fileId>/view
                return url.split("/d/")[1].split("/")[0];
            } else if (url.contains("drive.google.com/uc?id=")) {
                // Handle URLs of the form: https://drive.google.com/uc?id=<fileId>
                return url.split("id=")[1].split("&")[0];
            }
        }
        return null;
    }

    private String convertGoogleDriveLink(String link) {
        if (link != null && link.contains("drive.google.com/file/d/")) {
            String fileId = link.split("/d/")[1].split("/")[0];
            return "https://drive.google.com/uc?id=" + fileId;
        }
        return link; // Return as is if not a Google Drive link
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class NoticeViewAdapter extends RecyclerView.ViewHolder {
        private final Button deleteNotice;
        private final TextView deleteNoticeTitle;
        private final ImageView deleteNoticeImage;

        public NoticeViewAdapter(@NonNull View itemView) {
            super(itemView);
            deleteNotice = itemView.findViewById(R.id.deleteNotice);
            deleteNoticeTitle = itemView.findViewById(R.id.deleteNoticeTitle);
            deleteNoticeImage = itemView.findViewById(R.id.deleteNoticeImage);
        }
    }
}
