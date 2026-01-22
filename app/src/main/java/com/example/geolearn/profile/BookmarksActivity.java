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

// 1. Implement the Adapter Interface here
public class BookmarksActivity extends AppCompatActivity implements BookmarkAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private BookmarkAdapter adapter;

    private List<Country> bookmarkedCountries = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

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

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            userId = user.getUid();
            loadBookmarksFromCloud();
        } else {
            showEmptyState(true);
        }
    }

    private void loadBookmarksFromCloud() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(userId)
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
                        fetchCountryDetails(savedIds);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchCountryDetails(Set<String> savedIds) {
        db.collection("flashcards")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    bookmarkedCountries.clear();

                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot) {
                            if (savedIds.contains(doc.getId())) {
                                Country country = doc.toObject(Country.class);
                                if (country != null) {
                                    country.id = doc.getId();
                                    bookmarkedCountries.add(country);
                                }
                            }
                        }
                    }

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
            // Pass 'this' because we implemented the interface
            adapter = new BookmarkAdapter(bookmarkedCountries, this);
            recyclerView.setAdapter(adapter);
        }
    }

    // --- INTERFACE METHOD 1: CLICK ROW ---
    @Override
    public void onItemClick(Country country) {
        showCountryPopup(country);
    }

    // --- INTERFACE METHOD 2: DELETE CLICK ---
    @Override
    public void onDeleteClick(Country country, int position) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Remove Bookmark")
                .setMessage("Are you sure you want to remove " + country.name.common + " from your bookmarks?")
                .setPositiveButton("Remove", (dialog, which) -> deleteBookmarkFromFirebase(country, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteBookmarkFromFirebase(Country country, int position) {
        if (userId == null || country.id == null) return;

        // 1. Delete from Firebase
        db.collection("users")
                .document(userId)
                .collection("bookmarks")
                .document(country.id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // 2. Remove from Local List
                    bookmarkedCountries.remove(position);

                    // 3. Notify Adapter (Animation)
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, bookmarkedCountries.size());

                    Toast.makeText(this, "Removed " + country.name.common, Toast.LENGTH_SHORT).show();

                    // 4. Check if list is now empty
                    if (bookmarkedCountries.isEmpty()) {
                        showEmptyState(true);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showEmptyState(boolean isEmpty) {
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

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