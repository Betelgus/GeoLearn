package com.example.geolearn.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    // 1. Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(Country country);
    }

    private OnItemClickListener listener;

    // 2. Add a constructor to accept the listener
    public BookmarkAdapter(List<Country> countries, OnItemClickListener listener) {
        this.countryList = countries;
        this.listener = listener;
    }

    public void setCountries(List<Country> countries) {
        this.countryList = countries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Updated to use the specific XML file provided in context
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookmark, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Country country = countryList.get(position);
        holder.tvName.setText(country.name.common);
        holder.tvRegion.setText(country.region);

        // Load Image Logic
        if (country.flags != null && country.flags.png != null) {
            String imgName = country.flags.png.toLowerCase().replace(" ", "_");
            if (imgName.contains(".")) imgName = imgName.substring(0, imgName.lastIndexOf('.'));

            int resId = holder.itemView.getContext().getResources().getIdentifier(
                    imgName, "drawable", holder.itemView.getContext().getPackageName());

            if (resId != 0) {
                Glide.with(holder.itemView.getContext()).load(resId).into(holder.imgFlag);
            }
        }

        // 3. Set the click listener on the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(country);
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

        ViewHolder(View itemView) {
            super(itemView);
            // Matching IDs from item_bookmark.xml
            imgFlag = itemView.findViewById(R.id.imgFlag);
            tvName = itemView.findViewById(R.id.tvCountryName);
            tvRegion = itemView.findViewById(R.id.tvRegion);
        }
    }
}