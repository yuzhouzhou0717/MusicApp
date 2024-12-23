package com.myapp.music;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private final List<Song> playlist;
    private final OnRemoveFromPlaylistListener removeFromPlaylistListener;

    public PlaylistAdapter(List<Song> playlist, OnRemoveFromPlaylistListener removeFromPlaylistListener) {
        this.playlist = playlist;
        this.removeFromPlaylistListener = removeFromPlaylistListener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        return new PlaylistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Song song = playlist.get(position);
        holder.songNameTextView.setText(song.getName());
        holder.artistTextView.setText(song.getArtist());

        // 设置删除按钮点击事件
        holder.removeButton.setOnClickListener(v -> {
            if (removeFromPlaylistListener != null) {
                removeFromPlaylistListener.onRemoveFromPlaylist(song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlist.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView songNameTextView, artistTextView;
        ImageButton removeButton;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            songNameTextView = itemView.findViewById(R.id.songName);
            artistTextView = itemView.findViewById(R.id.artistName);
            removeButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    // 删除按钮点击事件接口
    public interface OnRemoveFromPlaylistListener {
        void onRemoveFromPlaylist(Song song);
    }
}
