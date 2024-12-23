package com.myapp.music;

import android.content.ContentResolver;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MediaPlayerManager {
    private static MediaPlayer mediaPlayer;
    private static Song currentSong;  // 记录当前播放的歌曲
    private static boolean isPlaying = false;
    private static boolean isSingleLoop = false; // 当前是否为单曲循环模式
    private static boolean isRandom = false; // 当前是否为随机播放模式
    private static List<Song> playlist = new ArrayList<>(); // 播放列表对象
    private static int currentSongIndex = -1; // 当前播放的歌曲索引

    public static MediaPlayer getInstance() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        return mediaPlayer;
    }

    public static void playSong(Context context, Song song, List<Song> playlist, TextView songNameTextView) {
        // 如果歌单为空或者歌曲路径为空，则直接返回
        if (playlist == null || playlist.isEmpty()) {
            Toast.makeText(context, "播放列表为空或歌曲信息无效", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("MediaPlayer", "当前播放模式: " + (isRandom ? "随机播放" : isSingleLoop ? "单曲循环" : "顺序播放"));
        // 如果是随机播放模式，随机选择一首歌曲
        // 如果是随机播放模式，随机选择一首歌曲
        if (isRandom) {
            int randomIndex = (int) (Math.random() * playlist.size());
            song = playlist.get(randomIndex);
            Log.d("MediaPlayer", "随机播放: 选择的歌曲索引 " + randomIndex);
        }
        // 检查是否是继续播放当前歌曲
        if (currentSong != null && currentSong.equals(song) && mediaPlayer != null && !isPlaying) {
            mediaPlayer.start(); // 恢复播放
            isPlaying = true;
            songNameTextView.setText(song.getName() + " - " + song.getArtist());
            return;
        }

        // 停止并释放之前的播放器资源
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // 获取歌曲文件路径并解析
        String filePath = song.getFilePath();
        Uri songUri = Uri.parse(filePath);
        Log.d("FilePath", "歌曲文件路径: " + filePath);

        try {
            // 使用 content:// 处理的逻辑
            if (filePath.startsWith("content://")) {
                ContentResolver resolver = context.getContentResolver();
                InputStream inputStream = resolver.openInputStream(songUri);
                if (inputStream == null) {
                    Toast.makeText(context, "无法读取文件", Toast.LENGTH_SHORT).show();
                    return;
                }
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(context, songUri);
            } else {
                File file = new File(filePath);
                if (!file.exists()) {
                    Toast.makeText(context, "歌曲文件不存在", Toast.LENGTH_SHORT).show();
                    return;
                }
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(context, songUri);
            }
            // 更新 UI：显示歌曲名字和艺术家
            songNameTextView.setText(song.getName() + " - " + song.getArtist());
            // 准备并开始播放歌曲
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            currentSong = song; // 更新当前歌曲
            currentSongIndex = playlist.indexOf(song); // 更新当前播放歌曲的索引

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d("MediaPlayer", "歌曲播放完成，进入下一步");

                if (isSingleLoop) {
                    // 单曲循环模式：重新播放当前歌曲
                    Log.d("MediaPlayer", "单曲循环，重新播放当前歌曲");
                    playSong(context, currentSong, playlist, songNameTextView);
                } else if (isRandom) {
                    // 随机播放模式：随机播放下一首
                    Log.d("MediaPlayer", "随机播放下一首");
                    int randomIndex = (int) (Math.random() * playlist.size());  // 随机选择一首歌
                    Log.d("MediaPlayer", "选择的随机歌曲索引: " + randomIndex);
                    Song randomSong = playlist.get(randomIndex);  // 获取随机歌曲
                    playSong(context, randomSong, playlist, songNameTextView);  // 播放随机歌曲
                } else {
                    // 顺序播放模式
                    int nextIndex = playlist.indexOf(currentSong) + 1;
                    if (nextIndex >= playlist.size()) {
                        nextIndex = 0; // 重置为第一首歌
                    }
                    playSong(context, playlist.get(nextIndex), playlist, songNameTextView);
                }
            });

        } catch (Exception e) {
            Log.e("MediaPlayer", "播放失败", e);
            Toast.makeText(context, "播放失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public static void pause() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public static void resume() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    public static void setSingleLoop(boolean singleLoop) {
        isSingleLoop = singleLoop;
    }

    public static boolean isSingleLoop() {
        return isSingleLoop;
    }

    public static void setRandom(boolean random) {
        isRandom = random;
    }

    public static boolean isRandom() {
        return isRandom;
    }

    public static void setPlaylist(List<Song> newPlaylist, int startIndex) {
        playlist = newPlaylist;
        currentSongIndex = startIndex;
    }

    public static List<Song> getPlaylist() {
        return playlist;
    }

    public static int getCurrentSongIndex() {
        return currentSongIndex;
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static Song getCurrentSong() {
        return currentSong;
    }

    public static void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            currentSong = null;
        }
    }

    private static boolean isInOtherPage = false;

    public static void setIsInOtherPage(boolean status) {
        isInOtherPage = status;
    }

    public static boolean isInOtherPage() {
        return isInOtherPage;
    }
}
