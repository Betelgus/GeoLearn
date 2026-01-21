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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlashcardActivity extends AppCompatActivity {

    // UI Components
    private ImageView imgFlag;
    private TextView tvCountryName, tvCapital, tvRegion;
    private Button btnPrev, btnNext;
    private ImageButton btnBookmark;
    private ProgressBar loadingProgressBar;

    // Data Variables
    private List<Country> countryList = new ArrayList<>();
    private int currentIndex = 0;

    // Firebase
    private FirebaseFirestore db;
    private Set<String> bookmarkedIds = new HashSet<>();
    private String userId;
    private boolean isGuest = true; // Default to true for safety

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

        // 3. CHECK SESSION (Hides button if Guest)
        checkUserSession();

        // 4. Load Data
        loadData();

        // 5. Button Listeners
        btnPrev.setOnClickListener(v -> showPreviousCard());
        btnNext.setOnClickListener(v -> showNextCard());
        btnBookmark.setOnClickListener(v -> toggleBookmark());
    }

    private void checkUserSession() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // You are a USER only if: user exists AND user is NOT anonymous
        if (user != null && !user.isAnonymous()) {
            isGuest = false;
            userId = user.getUid();
            btnBookmark.setVisibility(View.VISIBLE); // Show Star
            setupFirestoreListener();
        } else {
            isGuest = true;
            userId = null;
            btnBookmark.setVisibility(View.GONE); // Hide Star
        }
    }

    private void loadData() {
        String categoryParam = getIntent().getStringExtra("CATEGORY");
        final String category = (categoryParam != null) ? categoryParam : "random";

        if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.VISIBLE);

        com.google.firebase.firestore.Query query;

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
                        c.id = doc.getId(); // Save the Firestore ID
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
        // Double check: Never run this if guest
        if (isGuest || userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("bookmarks")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) return;

                        bookmarkedIds.clear();
                        if (value != null) {
                            for (DocumentSnapshot doc : value) {
                                bookmarkedIds.add(doc.getId());
                            }
                        }
                        // Refresh the star icon if data is loaded
                        if (!countryList.isEmpty()) {
                            updateBookmarkIconState();
                        }
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

        // Image Loading
        if (country.flags != null && country.flags.png != null) {
            String imgName = country.flags.png.toLowerCase().replace(" ", "_");
            if (imgName.contains(".")) imgName = imgName.substring(0, imgName.lastIndexOf('.'));

            int resId = getResources().getIdentifier(imgName, "drawable", getPackageName());

            if (resId != 0) {
                Glide.with(this).load(resId).into(imgFlag);
            } else {
                imgFlag.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Visibility Check: Ensure button status is correct for every card
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
        if (current.id == null) return;

        if (bookmarkedIds.contains(current.id)) {
            // Filled Star
            btnBookmark.setImageResource(android.R.drawable.btn_star_big_on);
            btnBookmark.setColorFilter(getResources().getColor(R.color.accent));
        } else {
            // Outline Star
            btnBookmark.setImageResource(android.R.drawable.btn_star_big_off);
            btnBookmark.setColorFilter(getResources().getColor(R.color.text_secondary));
        }
    }

    private void toggleBookmark() {
        // Security check: Stop guests from clicking
        if (isGuest || userId == null) {
            Toast.makeText(this, "Please login to save bookmarks", Toast.LENGTH_SHORT).show();
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
            docRef.delete().addOnSuccessListener(aVoid ->
                    Toast.makeText(this, "Removed bookmark", Toast.LENGTH_SHORT).show());
        } else {
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