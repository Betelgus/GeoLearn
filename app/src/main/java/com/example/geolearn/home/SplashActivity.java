package com.example.geolearn.home;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.geolearn.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 1. Find Views
        ImageView bgImage = findViewById(R.id.imgBackground); // The blue lines background
        ImageView logo = findViewById(R.id.imgLogo);
        View ripple1 = findViewById(R.id.ripple1);
        View ripple2 = findViewById(R.id.ripple2);
        View ripple3 = findViewById(R.id.ripple3);

        // --- NEW: ANIMATE THE BACKGROUND (Blue Lines) ---
        // 2. Animate Background: Slow Zoom In + Subtle Rotation
        // Scale from 1.0 (normal) to 1.2 (zoomed in)
        ObjectAnimator bgScaleX = ObjectAnimator.ofFloat(bgImage, "scaleX", 1.0f, 1.2f);
        ObjectAnimator bgScaleY = ObjectAnimator.ofFloat(bgImage, "scaleY", 1.0f, 1.2f);

        // Optional: Subtle Rotation (from 0 to 5 degrees) to make lines tilt
        ObjectAnimator bgRotate = ObjectAnimator.ofFloat(bgImage, "rotation", 0f, 5f);

        // Combine background animations
        AnimatorSet bgAnimSet = new AnimatorSet();
        bgAnimSet.setDuration(5000); // 5 Seconds (Slow movement)
        bgAnimSet.setInterpolator(new LinearInterpolator()); // Smooth constant speed
        bgAnimSet.playTogether(bgScaleX, bgScaleY, bgRotate);
        bgAnimSet.start();
        // ------------------------------------------------


        // 3. Start Ripple Animations (Same as before)
        pulseAnimation(ripple1, 0);
        pulseAnimation(ripple2, 700);
        pulseAnimation(ripple3, 1400);

        // 4. Logo "Breathing" (Same as before)
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logo, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logo, "scaleY", 1f, 1.1f, 1f);
        logoScaleX.setRepeatCount(ObjectAnimator.INFINITE);
        logoScaleY.setRepeatCount(ObjectAnimator.INFINITE);
        logoScaleX.setDuration(1500);
        logoScaleY.setDuration(1500);
        logoScaleX.start();
        logoScaleY.start();

        // 5. Timer to move to Guest Main Menu
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, GuestMainMenuActivity.class);
            startActivity(intent);
            finish();
        }, 4000); // 4 seconds total
    }

    // Helper method for ripples
    private void pulseAnimation(View target, long delay) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, "scaleX", 1f, 6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, "scaleY", 1f, 6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(target, "alpha", 0.5f, 0f);

        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setRepeatMode(ObjectAnimator.RESTART);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatMode(ObjectAnimator.RESTART);
        alpha.setRepeatCount(ObjectAnimator.INFINITE);
        alpha.setRepeatMode(ObjectAnimator.RESTART);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.setDuration(2500);
        animatorSet.setStartDelay(delay);
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.start();
    }
}