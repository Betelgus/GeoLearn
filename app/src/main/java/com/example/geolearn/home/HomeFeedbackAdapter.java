package com.example.geolearn.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geolearn.R;
import com.example.geolearn.feedback.feedback; // Updated import
import java.util.List;

public class HomeFeedbackAdapter extends RecyclerView.Adapter<HomeFeedbackAdapter.ViewHolder> {
    private List<feedback> feedbackList;

    public HomeFeedbackAdapter(List<feedback> feedbackList) {
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_feedback, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        feedback item = feedbackList.get(position);

        // Use the username we will fetch from the users collection
        holder.tvUserName.setText(item.username != null ? item.username : "Anonymous");
        holder.tvFeedbackText.setText(item.feedbackText);
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvFeedbackText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            // CRITICAL: Must be tvFeedbackText to match your XML
            tvFeedbackText = itemView.findViewById(R.id.tvFeedbackText);
        }
    }
}
