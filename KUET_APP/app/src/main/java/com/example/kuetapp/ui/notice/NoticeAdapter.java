package com.example.kuetapp.ui.notice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kuetapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewAdapter> {

    private Context context;
    private ArrayList<NoticeData> list;
    private ArrayList<String> loadedUrls = new ArrayList<>(); // To store already loaded image URLs

    public NoticeAdapter(Context context, ArrayList<NoticeData> list) {
        this.context = context;
        this.list = list;
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

        holder.newsfeedNoticeTitle.setText(currentItem.getTitle());
        holder.date.setText(currentItem.getDate());
        holder.time.setText(currentItem.getTime());

        // Check if the image URL is already loaded
        if (currentItem.getFileUrl() != null && !loadedUrls.contains(correctedUrl)) {
            try {
                Picasso.get()
                        .load(correctedUrl)
                        .placeholder(R.drawable.loading) // Placeholder image
                        .error(R.drawable.error_image) // Error image
                        .into(holder.newsfeedNoticeImage);

                // Add the URL to the loadedUrls list to prevent re-loading
                loadedUrls.add(correctedUrl);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Set the error image if URL is not available or already loaded
            holder.newsfeedNoticeImage.setImageResource(R.drawable.error_image);
        }
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

        private final TextView newsfeedNoticeTitle, date, time;
        private final ImageView newsfeedNoticeImage;

        public NoticeViewAdapter(@NonNull View itemView) {
            super(itemView);
            newsfeedNoticeTitle = itemView.findViewById(R.id.newsfeedNoticeTitle);
            newsfeedNoticeImage = itemView.findViewById(R.id.newsfeedNoticeImage);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
        }
    }
}
