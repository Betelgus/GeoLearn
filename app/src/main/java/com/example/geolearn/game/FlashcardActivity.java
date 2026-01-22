package com.example.geolearn.game;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Import for colors

import com.bumptech.glide.Glide;
import com.example.geolearn.R;
import com.example.geolearn.api.Country;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration; // Import for cleanup
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlashcardActivity extends AppCompatActivity {

    // --- UI Components ---
    private ImageView imgFlag;
    private TextView tvCountryName, tvCapital, tvRegion;
    private Button btnPrev, btnNext;
    private ImageButton btnBookmark;
    private ProgressBar loadingProgressBar;

    // --- Data Variables ---
    private List<Country> countryList = new ArrayList<>();
    private int currentIndex = 0;

    // --- Firebase ---
    private FirebaseFirestore db;
    private Set<String> bookmarkedIds = new HashSet<>();
    private String userId;
    private boolean isGuest = true;

    // Fix: Listener Registration to prevent memory leaks
    private ListenerRegistration bookmarkListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        // 1. Initialize Views
        imgFlag = findViewById(R.id.imgFlag);
        tvCountryName = findViewById(R.id.tvCountryName);
        tvCapital = findViewById(R.id.tvCapital);
        tvRegion = findViewById(R.id.tvRegion);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnBookmark = findViewById(R.id.btnBookmark);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // Back Button Logic
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 2. Setup Firebase
        db = FirebaseFirestore.getInstance();

        // 3. CHECK SESSION
        checkUserSession();

        // 4. Load Data
        loadData();

        // 5. Button Listeners
        btnPrev.setOnClickListener(v -> showPreviousCard());
        btnNext.setOnClickListener(v -> showNextCard());
        btnBookmark.setOnClickListener(v -> toggleBookmark());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fix: Clean up listener when activity is destroyed
        if (bookmarkListener != null) {
            bookmarkListener.remove();
        }
    }

    private void checkUserSession() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Logic: If user is logged in AND not anonymous, they are a "User".
        // Otherwise, they are a "Guest".
        if (user != null && !user.isAnonymous()) {
            isGuest = false;
            userId = user.getUid();
            btnBookmark.setVisibility(View.VISIBLE);
            setupFirestoreListener();
        } else {
            isGuest = true;
            userId = null;
            btnBookmark.setVisibility(View.GONE);
        }
    }

    private void loadData() {
        String categoryParam = getIntent().getStringExtra("CATEGORY");
        final String category = (categoryParam != null) ? categoryParam : "random";

        if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.VISIBLE);

        Query query;
        if (category.equals("random")) {
            query = db.collection("flashcards");
        } else {
            query = db.collection("flashcards").whereEqualTo("category", category);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);
            countryList.clear();

            if (queryDocumentSnapshots != null) {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Country c = doc.toObject(Country.class);
                    if (c != null) {
                        c.id = doc.getId(); // Ensure your Country class has: public String id;
                        countryList.add(c);
                    }
                }
            }

            if (category.equals("random")) {
                Collections.shuffle(countryList);
            }

            if (!countryList.isEmpty()) {
                currentIndex = 0;
                updateUI();
            } else {
                Toast.makeText(this, "No cards found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);
            Log.e("FlashcardActivity", "Error loading data", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupFirestoreListener() {
        if (isGuest || userId == null) return;

        // Fix: Assign the listener to a variable so we can remove it later
        bookmarkListener = db.collection("users")
                .document(userId)
                .collection("bookmarks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FlashcardActivity", "Listen failed.", error);
                        return;
                    }

                    bookmarkedIds.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value) {
                            bookmarkedIds.add(doc.getId());
                        }
                    }
                    // Only update icon if data is ready
                    if (!countryList.isEmpty()) {
                        updateBookmarkIconState();
                    }
                });
    }

    private void updateUI() {
        if (countryList.isEmpty()) return;

        Country country = countryList.get(currentIndex);

        tvCountryName.setText(country.name.common);
        tvRegion.setText(country.region != null ? country.region : "");

        if (country.capital != null && !country.capital.isEmpty()) {
            tvCapital.setText(country.capital.get(0));
        } else {
            tvCapital.setText("N/A");
        }

        // Image Loading Logic with Null Safety
        if (country.flags != null && country.flags.png != null) {
            String imgName = country.flags.png.toLowerCase().replace(" ", "_");
            if (imgName.contains(".")) imgName = imgName.substring(0, imgName.lastIndexOf('.'));

            int resId = getResources().getIdentifier(imgName, "drawable", getPackageName());

            if (resId != 0) {
                Glide.with(this).load(resId).into(imgFlag);
            } else {
                imgFlag.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            imgFlag.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // --- VISIBILITY LOGIC ---
        // Forces the button state based on session every time card changes
        if (isGuest) {
            btnBookmark.setVisibility(View.GONE);
        } else {
            btnBookmark.setVisibility(View.VISIBLE);
            updateBookmarkIconState();
        }
    }

    private void updateBookmarkIconState() {
        if (isGuest || countryList.isEmpty()) return;

        Country current = countryList.get(currentIndex);
        // Safety check: if Country object has no ID, we can't bookmark it
        if (current.id == null) return;

        if (bookmarkedIds.contains(current.id)) {
            // Active Star (Yellow)
            btnBookmark.setImageResource(android.R.drawable.btn_star_big_on);
            // Fix: Use ContextCompat for colors
            btnBookmark.setColorFilter(ContextCompat.getColor(this, R.color.accent));
        } else {
            // Inactive Star (Gray)
            btnBookmark.setImageResource(android.R.drawable.btn_star_big_off);
            // Fix: Use ContextCompat for colors
            btnBookmark.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void toggleBookmark() {
        if (isGuest || userId == null) {
            Toast.makeText(this, "Log in to save bookmarks", Toast.LENGTH_SHORT).show();
            return;
        }

        if (countryList.isEmpty()) return;

        Country current = countryList.get(currentIndex);
        if (current.id == null) {
            Toast.makeText(this, "Error: Card ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("bookmarks")
                .document(current.id);

        if (bookmarkedIds.contains(current.id)) {
            // Remove Bookmark
            docRef.delete().addOnSuccessListener(aVoid ->
                    Toast.makeText(this, "Removed bookmark", Toast.LENGTH_SHORT).show());
        } else {
            // Add Bookmark
            Map<String, Object> data = new HashMap<>();
            data.put("timestamp", System.currentTimeMillis());
            data.put("name", current.name.common);

            docRef.set(data).addOnSuccessListener(aVoid ->
                    Toast.makeText(this, "Bookmark Saved!", Toast.LENGTH_SHORT).show());
        }
    }

    private void showPreviousCard() {
        if (currentIndex > 0) {
            currentIndex--;
            updateUI();
        }
    }

    private void showNextCard() {
        if (currentIndex < countryList.size() - 1) {
            currentIndex++;
            updateUI();
        }
    }
}