package com.example.geolearn.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Import ImageButton
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.geolearn.R;
import com.example.geolearn.api.Country;
import java.util.ArrayList;
import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    private List<Country> countryList = new ArrayList<>();

    // 1. UPDATED Interface: Added onDeleteClick
    public interface OnItemClickListener {
        void onItemClick(Country country);
        void onDeleteClick(Country country, int position); // New method
    }

    private OnItemClickListener listener;

    public BookmarkAdapter(List<Country> countries, OnItemClickListener listener) {
        this.countryList = countries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookmark, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Country country = countryList.get(position);
        holder.tvName.setText(country.name.common);
        holder.tvRegion.setText(country.region);

        if (country.flags != null && country.flags.png != null) {
            String imgName = country.flags.png.toLowerCase().replace(" ", "_");
            if (imgName.contains(".")) imgName = imgName.substring(0, imgName.lastIndexOf('.'));
            int resId = holder.itemView.getContext().getResources().getIdentifier(
                    imgName, "drawable", holder.itemView.getContext().getPackageName());
            if (resId != 0) {
                Glide.with(holder.itemView.getContext()).load(resId).into(holder.imgFlag);
            }
        }

        // Click on the whole card (show details)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(country);
        });

        // 2. Click on the Delete Button (Trash Icon)
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                // Pass the specific position so we can animate removal
                listener.onDeleteClick(country, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return countryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFlag;
        TextView tvName, tvRegion;
        ImageButton btnDelete; // New reference

        ViewHolder(View itemView) {
            super(itemView);
            imgFlag = itemView.findViewById(R.id.imgFlag);
            tvName = itemView.findViewById(R.id.tvCountryName);
            tvRegion = itemView.findViewById(R.id.tvRegion);
            btnDelete = itemView.findViewById(R.id.btnDelete); // Bind ID
        }
    }
}