package com.pratik.resumechatbot.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pratik.resumechatbot.R;
import java.util.ArrayList;
import java.util.List;

public class ResumeAdapter extends RecyclerView.Adapter<ResumeAdapter.ViewHolder> {
    private List<String> resumes = new ArrayList<>();
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(String filename);
    }

    public ResumeAdapter(OnDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }

    public void setResumes(List<String> resumes) {
        if (resumes != null) {
            this.resumes = resumes;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resume, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String filename = resumes.get(position);
        holder.tvFilename.setText(filename);
        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(filename));
    }

    @Override
    public int getItemCount() {
        return resumes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFilename;
        Button btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvFilename = itemView.findViewById(R.id.tv_filename);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
