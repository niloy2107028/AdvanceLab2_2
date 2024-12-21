package com.example.kuetapp.ui.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kuetapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Gallery_Adapter extends RecyclerView.Adapter<Gallery_Adapter.GalleryViewAdapter> {

    private Context context;
    private List<String> images;

    public Gallery_Adapter(Context context, List<String> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public GalleryViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_image, parent, false);
        return new GalleryViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewAdapter holder, int position) {
        String item = images.get(position);

        if (item != null && !item.isEmpty()) {
            String correctedUrl = convertGoogleDriveLink(item);
            Picasso.get()
                    .load(correctedUrl)
                    .placeholder(R.drawable.loading) // Show placeholder while loading
                    .error(R.drawable.error_image) // Show error image if loading fails
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.error_image); // Set error image if URL is empty
        }
    }

    private String convertGoogleDriveLink(String link) {
        if (link.contains("drive.google.com/file/d/")) {
            String fileId = link.split("/d/")[1].split("/")[0];
            return "https://drive.google.com/uc?id=" + fileId;
        }
        return link; // Return as is if not a Google Drive link
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class GalleryViewAdapter extends RecyclerView.ViewHolder {

        ImageView imageView;

        public GalleryViewAdapter(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
        }
    }
}
