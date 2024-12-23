package com.myapp.music;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DynamicAdapter extends RecyclerView.Adapter<DynamicAdapter.DynamicViewHolder> {
    private Context context;
    private List<Dynamic> dynamicList;
    private OnItemLongClickListener onItemLongClickListener;
    private String userName;
    private UserDatabaseHelper dbHelper;
    private String userEmail;  // 当前用户的邮箱

    public DynamicAdapter(Context context, List<Dynamic> dynamicList,String userEmail,OnItemLongClickListener listener) {
        this.context = context;
        this.userEmail = userEmail;  // 设置邮箱
        this.dynamicList = dynamicList;
        this.onItemLongClickListener = listener;
        dbHelper = new UserDatabaseHelper(context);  // 确保初始化 dbHelper
    }

    @NonNull
    @Override
    public DynamicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dynamic, parent, false);
        return new DynamicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DynamicViewHolder holder, int position) {
        Dynamic dynamic = dynamicList.get(position);
//        holder.tvUserEmail.setText(dynamic.getUserEmail());
        userName = dbHelper.getUserNameByEmail(dynamic.getUserEmail());
        holder.tvUserEmail.setText(userName);
        holder.tvContent.setText(dynamic.getContent());
//        holder.tvMusicInfo.setText(dynamic.getMusicid());
        holder.tvTimestamp.setText(dynamic.getTimestamp());
        // 设置歌曲信息

        holder.songNameTextView.setText(dynamic.getSongName());
        holder.artistNameTextView.setText(dynamic.getArtistName());
        // 设置歌曲图片
        if (dynamic.getSongImage() != null) {
            int resId = context.getResources().getIdentifier(dynamic.getSongImage(), "drawable", context.getPackageName());
            if (resId != 0) {
                holder.songImageView.setImageResource(resId);
            }
        }
        // 添加长按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(dynamic, position);
            }
            return true;
        });
        // 设置点击事件，点击进入动态详情页面
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DynamicDetailActivity.class);
            intent.putExtra("dynamicId", dynamic.getId()); // 传递动态ID
            intent.putExtra("userEmail", userEmail); // 传递动态ID
            intent.putExtra("content", dynamic.getContent()); // 传递动态内容
            intent.putExtra("timestamp", dynamic.getTimestamp()); // 传递时间戳
            intent.putExtra("songName", dynamic.getSongName()); // 传递歌曲名称
            intent.putExtra("artistName", dynamic.getArtistName()); // 传递艺术家名称
            intent.putExtra("songImage", dynamic.getSongImage()); // 传递歌曲图片
            context.startActivity(intent); // 启动动态详情页面
        });
    }

    @Override
    public int getItemCount() {
        return dynamicList.size();
    }

    public static class DynamicViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserEmail, tvContent, tvMusicInfo, tvTimestamp, songNameTextView, artistNameTextView;
        ImageView songImageView;
        public DynamicViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);

            tvContent = itemView.findViewById(R.id.tvContent);
//            tvMusicInfo = itemView.findViewById(R.id.tvMusicInfo);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            songNameTextView = itemView.findViewById(R.id.songNameTextView1);
            artistNameTextView = itemView.findViewById(R.id.artistNameTextView1);
            songImageView = itemView.findViewById(R.id.songImageView1);
        }
    }
    // 长按监听接口
    public interface OnItemLongClickListener {
        void onItemLongClick(Dynamic dynamic, int position);
    }
}
