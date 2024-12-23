package com.myapp.music;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private  List<Song> songList,playlist;;
    private final OnSongItemClickListener onSongItemClickListener;
    private final OnPlayButtonClickListener onPlayButtonClickListener;
    private final OnPlaylistButtonClickListener onPlaylistButtonClickListener;
    private final Context context;
    // 构造函数，传入监听器
    public SongAdapter(List<Song> songList,
                       List<Song> playlist,
                       Context context,
                       OnSongItemClickListener itemClickListener,
                       OnPlayButtonClickListener playButtonClickListener,
                       OnPlaylistButtonClickListener playlistButtonClickListener) {
        this.songList = songList;
        this.playlist = playlist;
        this.context = context;
        this.onSongItemClickListener = itemClickListener;
        this.onPlayButtonClickListener = playButtonClickListener;
        this.onPlaylistButtonClickListener = playlistButtonClickListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        String imageName = song.getSongImage();
        Log.d("SongImage", "Song image name: " + imageName);

        // 去掉文件扩展名（如果有的话）
        String imageNameWithoutExtension = imageName.split("\\.")[0];
        Log.d("SongImage", "Song image name: " + imageNameWithoutExtension);

        holder.songNameTextView.setText(song.getName());
        holder.artistTextView.setText(song.getArtist());

        // 动态加载图片资源
        int imageResId = context.getResources().getIdentifier(imageNameWithoutExtension, "drawable", context.getPackageName());
        if (imageResId != 0) {
            holder.songImageView.setImageResource(imageResId);
        } else {
            holder.songImageView.setImageResource(R.drawable.ic_music2);  // 默认图片
        }

        // 设置播放按钮的点击事件
        holder.playButton.setOnClickListener(v -> {
            if (onPlayButtonClickListener != null) {
                onPlayButtonClickListener.onPlayButtonClick(song);
            }
        });

        // 设置添加到播放列表按钮的点击事件
        holder.addToPlaylistButton.setOnClickListener(v -> {
            if (onPlaylistButtonClickListener != null) {
                onPlaylistButtonClickListener.onAddToPlaylistClick(song);
            }
        });

        // 设置歌曲项点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onSongItemClickListener != null) {
                onSongItemClickListener.onSongItemClick(song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView songNameTextView, artistTextView;
        ImageView songImageView;
        ImageButton playButton, addToPlaylistButton;

        public SongViewHolder(View itemView) {
            super(itemView);
            songNameTextView = itemView.findViewById(R.id.songName);
            artistTextView = itemView.findViewById(R.id.songArtist);
            playButton = itemView.findViewById(R.id.playButton);
            songImageView = itemView.findViewById(R.id.songImageView);
            addToPlaylistButton = itemView.findViewById(R.id.playButton);
        }
    }
    public void setSongs(List<Song> newSongs) {
        this.songList = newSongs;
        notifyDataSetChanged();
    }
    // 播放按钮点击事件接口
    public interface OnPlayButtonClickListener {
        void onPlayButtonClick(Song song);
    }

    // 播放列表添加按钮点击事件接口
    public interface OnPlaylistButtonClickListener {
        void onAddToPlaylistClick(Song song);
    }

    // 点击事件接口
    public interface OnSongItemClickListener {
        void onSongItemClick(Song song);
    }
}
