package com.example.kuetapp.ui.faculty;

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

import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewAdapter> {

    private List<TeacherData> list;
    private Context context;


    public TeacherAdapter(List<TeacherData> list, Context context) {
        this.list = list;
        this.context = context;


    }

    @NonNull
    @Override
    public TeacherViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.faculty_item_layout, parent, false);

        return new TeacherViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewAdapter holder, int position) {

        TeacherData item = list.get(position);
        holder.name.setText(item.getName());
        holder.email.setText(item.getEmail());
        holder.post.setText(item.getPost());


        if (item.getImage() != null && !item.getImage().isEmpty()) {
            String correctedUrl = convertGoogleDriveLink(item.getImage());
            Picasso.get()
                    .load(correctedUrl)
                    .placeholder(R.drawable.avatarprofile) // Show placeholder while loading
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
        return list.size();
    }

    public class TeacherViewAdapter extends RecyclerView.ViewHolder {

        private TextView name, email, post;

        private ImageView imageView;

        public TeacherViewAdapter(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.teacherName);
            email = itemView.findViewById(R.id.teacherEmail);
            post = itemView.findViewById(R.id.teacherPost);
            imageView = itemView.findViewById(R.id.teacherImage);


        }
    }


}
