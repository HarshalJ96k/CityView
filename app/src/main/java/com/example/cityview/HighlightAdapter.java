package com.example.cityview;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class HighlightAdapter extends RecyclerView.Adapter<HighlightAdapter.HighlightViewHolder> {

    private final Context context;
    private final List<Highlight> highlightList;

    public HighlightAdapter(Context context, List<Highlight> highlightList) {
        this.context = context;
        this.highlightList = highlightList;
    }

    @NonNull
    @Override
    public HighlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_highlight, parent, false);
        return new HighlightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HighlightViewHolder holder, int position) {
        Highlight highlight = highlightList.get(position);

        holder.textViewName.setText(highlight.getName());

        Glide.with(context)
                .load(highlight.getImageUrl())
                .placeholder(R.drawable.highlight_downtown)
                .error(R.drawable.highlight_downtown)
                .into(holder.imageView);

        // ✅ OPEN IMAGE IN FULL-SCREEN DIALOG (BEST UX)
        holder.itemView.setOnClickListener(v -> {

            Dialog dialog = new Dialog(
                    context,
                    android.R.style.Theme_Black_NoTitleBar_Fullscreen
            );

            dialog.setContentView(R.layout.dialog_full_image);

            ImageView dialogImage =
                    dialog.findViewById(R.id.dialog_image);

            Glide.with(context)
                    .load(highlight.getImageUrl())
                    .into(dialogImage);

            // Tap image to close
            dialogImage.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return highlightList.size();
    }

    static class HighlightViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textViewName;

        public HighlightViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_highlight);
            textViewName = itemView.findViewById(R.id.text_highlight_name);
        }
    }
}
