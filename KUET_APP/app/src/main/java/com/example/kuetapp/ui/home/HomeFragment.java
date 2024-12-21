package com.example.kuetapp.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.kuetapp.R;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeFragment extends Fragment {

    private TextView parsedTitle, parsedSummery, parsedDepartments;
    private ProgressBar progressBar;

    LinearLayout contentLayout ;

    private static final String URL = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&explaintext=true&titles=Khulna_University_of_Engineering_%26_Technology&format=json";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the UI components
        MaterialCardView mapImage = view.findViewById(R.id.mapImage);
        parsedDepartments = view.findViewById(R.id.homeDepartments);
        parsedTitle = view.findViewById(R.id.homeTitle);
        parsedSummery = view.findViewById(R.id.homeSummery);
        progressBar = view.findViewById(R.id.progressBar); // ProgressBar initialization

        contentLayout= view.findViewById(R.id.contentLayout);

        // Set the click listener to open the map
        mapImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });

        // Check network availability before starting the AsyncTask
        if (isNetworkAvailable()) {
            // Show ProgressBar and start AsyncTask to fetch Wikipedia data
            progressBar.setVisibility(View.VISIBLE);
            new FetchWikipediaData().execute();
        } else {
            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void openMap() {
        // Construct the URI for the location
        Uri uri = Uri.parse("geo:0,0?q=Khulna University of Engineering & Technology");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");  // Open in Google Maps

        // Check if there's an app to handle the intent
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);  // Start the activity to open the map
        } else {
            // Handle case where no map application is installed
            Toast.makeText(getContext(), "Map app not found", Toast.LENGTH_SHORT).show();
        }
    }

    // AsyncTask to fetch data from the Wikipedia API
    private class FetchWikipediaData extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {
            String[] data = new String[4]; // Store Title, Summary, Departments

            try {
                String response = fetchDataFromAPI();
                if (response != null) {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONObject query = jsonResponse.getJSONObject("query");
                    JSONObject pages = query.getJSONObject("pages");

                    // Extract the page content
                    String pageId = pages.keys().next();
                    JSONObject page = pages.getJSONObject(pageId);

                    // Extract and set data
                    data[0] = page.getString("title"); // Title
                    data[1] = extractSummary(page.getString("extract")); // Summary
                    data[2] = extractFacultiesAndDepartments(page.getString("extract")); // Faculties & Departments
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String[] data) {
            // Hide ProgressBar and show content
            progressBar.setVisibility(View.GONE); // Hide progress bar



            if (data != null) {
                parsedTitle.setText(data[0] != null ? data[0] : "No title available");
                parsedSummery.setText(data[1] != null ? data[1] : "No summary available");
                parsedDepartments.setText(data[2] != null ? data[2] : "No departments information available");

                // Make the content visible
                contentLayout.setVisibility(View.VISIBLE); // Show the content



            } else {
                Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        }

        // Fetch data from the Wikipedia API
        private String fetchDataFromAPI() {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                int read;
                while ((read = reader.read()) != -1) {
                    response.append((char) read);
                }

                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // Clean up the summary text
        private String extractSummary(String text) {
            text = text.replaceAll("==.*?==", ""); // Remove Wikipedia sections
            text = text.trim();
            int firstNewlineIndex = text.indexOf("\n");
            if (firstNewlineIndex != -1) {
                text = text.substring(0, firstNewlineIndex).trim();
            }
            return text;
        }

        // Extract Faculties and Departments from the text
        private String extractFacultiesAndDepartments(String text) {
            int startIndex = text.indexOf("\n\nFaculty of Civil Engineering");
            if (startIndex == -1) return "Faculties and Departments not found.";

            int endIndex = text.indexOf("\n\n", startIndex + 1);
            if (endIndex == -1) endIndex = text.length();

            return text.substring(startIndex, endIndex).trim();
        }



    }

    // Check if the device has an active internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
