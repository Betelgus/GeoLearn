package com.example.geolearn.profile;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.geolearn.R;
import com.example.geolearn.api.Country;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookmarksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private BookmarkAdapter adapter;

    // We will store the final list of full Country objects here
    private List<Country> bookmarkedCountries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_bookmarks);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Bookmarks");
        }

        recyclerView = findViewById(R.id.recyclerBookmarks);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // START LOADING DATA
        loadBookmarksFromCloud();
    }

    private void loadBookmarksFromCloud() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showEmptyState(true);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. First, get the list of IDs the user has bookmarked
        db.collection("users")
                .document(user.getUid())
                .collection("bookmarks")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> savedIds = new HashSet<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        savedIds.add(doc.getId());
                    }

                    if (savedIds.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        showEmptyState(true);
                    } else {
                        // 2. We have the IDs, now fetch the actual Country details
                        fetchCountryDetails(savedIds);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchCountryDetails(Set<String> savedIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Optimization: Fetch ALL flashcards once and filter in memory.
        // (This is faster/easier than making 50 individual network calls)
        db.collection("flashcards")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    bookmarkedCountries.clear();

                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot) {
                            // Check if this card's ID is in our bookmark list
                            if (savedIds.contains(doc.getId())) {
                                Country country = doc.toObject(Country.class);
                                if (country != null) {
                                    // Ensure ID is set (sometimes Firestore object mapping needs help)
                                    country.id = doc.getId();
                                    bookmarkedCountries.add(country);
                                }
                            }
                        }
                    }

                    // Sort alphabetically
                    Collections.sort(bookmarkedCountries, (o1, o2) ->
                            o1.name.common.compareToIgnoreCase(o2.name.common));

                    displayData();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("Bookmarks", "Error fetching details", e);
                });
    }

    private void displayData() {
        progressBar.setVisibility(View.GONE);

        if (bookmarkedCountries.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
            adapter = new BookmarkAdapter(bookmarkedCountries, this::showCountryPopup);
            recyclerView.setAdapter(adapter);
        }
    }

    private void showEmptyState(boolean isEmpty) {
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // --- POPUP LOGIC (Same as before) ---
    private void showCountryPopup(Country country) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_flashcard_popup, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView imgPopupFlag = view.findViewById(R.id.imgPopupFlag);
        TextView tvPopupName = view.findViewById(R.id.tvPopupName);
        TextView tvPopupCapital = view.findViewById(R.id.tvPopupCapital);
        Button btnClose = view.findViewById(R.id.btnClosePopup);

        tvPopupName.setText(country.name.common);

        String capText = "No Capital Data";
        if (country.capital != null && !country.capital.isEmpty()) {
            capText = country.capital.get(0);
        }
        tvPopupCapital.setText(capText);

        // Load Image
        if (country.flags != null && country.flags.png != null) {
            String imgName = country.flags.png.toLowerCase().replace(" ", "_");
            if (imgName.contains(".")) imgName = imgName.substring(0, imgName.lastIndexOf('.'));

            int resId = getResources().getIdentifier(imgName, "drawable", getPackageName());
            if (resId != 0) {
                Glide.with(this).load(resId).into(imgPopupFlag);
            }
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}