package com.myapp.music;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LikeSongsAdapter extends RecyclerView.Adapter<LikeSongsAdapter.LikeSongViewHolder> {
    private List<Song> likedSongs;
    private OnItemClickListener clickListener;
    // 构造函数
    public LikeSongsAdapter(List<Song> likedSongs, LikeSongsAdapter.OnItemClickListener clickListener) {
        this.likedSongs = likedSongs;
        this.clickListener = clickListener;
    }

    @Override
    public LikeSongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_liked_song, parent, false);
        return new LikeSongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LikeSongViewHolder holder, int position) {
        Song song = likedSongs.get(position);
        holder.songNameTextView.setText(song.getName());
        holder.artistTextView.setText(song.getArtist());

        // 设置歌曲封面（如果有）
        if (song.getSongImage() != null && !song.getSongImage().isEmpty()) {
            int imageResId = holder.itemView.getContext().getResources().getIdentifier(song.getSongImage(), "drawable", holder.itemView.getContext().getPackageName());
            holder.songImageView.setImageResource(imageResId);
        } else {
            holder.songImageView.setImageResource(R.drawable.ic_music); // 默认图片
        }

        // 设置点击事件，点击进入歌曲详情
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(song);  // 回调点击事件
            }
        });
    }

    // 更新已点赞的歌曲列表
    public void updateLikedSongs(List<Song> newLikedSongs) {
        this.likedSongs = newLikedSongs;
        notifyDataSetChanged();  // 通知适配器数据已改变
    }

    @Override
    public int getItemCount() {
        return likedSongs != null ? likedSongs.size() : 0;  // 返回歌曲数目，避免空指针异常
    }

    // ViewHolder类，保存视图引用
    public static class LikeSongViewHolder extends RecyclerView.ViewHolder {
        TextView songNameTextView;
        TextView artistTextView;
        ImageView songImageView;

        public LikeSongViewHolder(View itemView) {
            super(itemView);
            songNameTextView = itemView.findViewById(R.id.songNameTextView);
            artistTextView = itemView.findViewById(R.id.artistTextView);
            songImageView = itemView.findViewById(R.id.songImageView);
        }
    }
    // 定义一个接口用于点击事件回调
    public interface OnItemClickListener {
        void onItemClick(Song song);  // 点击项时回调该方法
    }
}
