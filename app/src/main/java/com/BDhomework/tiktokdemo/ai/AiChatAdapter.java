package com.BDhomework.tiktokdemo.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.BDhomework.tiktokdemo.R;

import java.util.List;

public class AiChatAdapter extends RecyclerView.Adapter<AiChatAdapter.VH> {

    private static final int TYPE_AI = 0; // assistant
    private static final int TYPE_ME = 1; // user

    private final List<AiMessage> data;

    public AiChatAdapter(List<AiMessage> data) {
        this.data = data;
    }

    @Override
    public int getItemViewType(int position) {
        AiMessage m = data.get(position);
        return "user".equals(m.role) ? TYPE_ME : TYPE_AI;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = (viewType == TYPE_ME)
                ? R.layout.item_chat_me
                : R.layout.item_chat_ai;

        View v = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.tvMsg.setText(data.get(position).content);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMsg;

        VH(@NonNull View itemView) {
            super(itemView);
            tvMsg = itemView.findViewById(R.id.tv_msg);
        }
    }
}
