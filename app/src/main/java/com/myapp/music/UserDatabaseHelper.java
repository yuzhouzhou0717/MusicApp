package com.myapp.music;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class UserDatabaseHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "musicAppDB";
    private static final int DATABASE_VERSION = 24;



    private static final String TABLE_DYNAMIC = "dynamic";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_SONGS = "songs";
    private static final String TABLE_USER_LIKES = "user_likes";
    private static final String COMMENT_TABLE = "comment";
    private static final String DYCOMMENT_TABLE = "dycomment";
    private static final String GOOD_TABLE = "dynamic_likes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_ALBUM = "album";
    public static final String COLUMN_FILE_PATH = "filePath";
    private static final String COLUMN_IMAGE = "songImage";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "profile TEXT);";

    private static final String CREATE_TABLE_SONGS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SONGS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "artist TEXT, " +
                    "album TEXT, " +
                    "likes INTEGER DEFAULT 0, " +
                    "filePath TEXT," +
                    "songImage TEXT);"; // 新增 songImage 列

    private static final String CREATE_TABLE_USER_LIKES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USER_LIKES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email TEXT, " +
                    "song_id INTEGER NOT NULL, " +
                    "FOREIGN KEY (email) REFERENCES users(email), " +
                    "FOREIGN KEY(song_id) REFERENCES " + TABLE_SONGS + "(id), " +
                    "UNIQUE(email, song_id));";

    private static final String CREATE_COMMENT_TABLE =
            "CREATE TABLE IF NOT EXISTS comment (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "song_id INTEGER NOT NULL, " +
                    "email TEXT  NOT NULL, " +
                    "comment_text TEXT NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (email) REFERENCES users(email), " +
                    "FOREIGN KEY(song_id) REFERENCES songs(id));";

    private static final String CREATE_TABLE_DYNAMIC =
            "CREATE TABLE IF NOT EXISTS dynamic (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "userEmail TEXT NOT NULL," +
                    "content TEXT NOT NULL," +
                    "musicId INTEGER," +
                    "likes_count INTEGER DEFAULT 0,"+
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);";

    private static final String CREATE_DYCOMMENT_TABLE =
            "CREATE TABLE IF NOT EXISTS dycomment(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "dynamic_id INTEGER NOT NULL, " +
                    "email TEXT  NOT NULL, " +
                    "comment TEXT NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (email) REFERENCES users(email), " +
                    "FOREIGN KEY(dynamic_id) REFERENCES dynamic(id));";
    private static final String CREATE_GOOD_TABLE =
               "CREATE TABLE IF NOT EXISTS dynamic_likes (" +
                 "like_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "email TEXT NOT NULL," +
                "dynamic_id INTEGER NOT NULL," +
                "FOREIGN KEY(email) REFERENCES users(email)," +
                "FOREIGN KEY(dynamic_id) REFERENCES dynamic(id));";
    private Context context; // 声明 Context 变量

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;  // 保存 context
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_USERS);
            Log.d("Database", "用户表创建成功");
            db.execSQL(CREATE_TABLE_SONGS);
            Log.d("Database", "歌曲表创建成功");
            db.execSQL(CREATE_TABLE_USER_LIKES);
            Log.d("Database", "用户点赞表创建成功");
            db.execSQL(CREATE_COMMENT_TABLE);
            Log.d("Database", "评论表创建成功");
            db.execSQL(CREATE_TABLE_DYNAMIC);
            Log.d("Database", "动态表创建成功");
            db.execSQL(CREATE_DYCOMMENT_TABLE);
            db.execSQL(CREATE_GOOD_TABLE);
        } catch (SQLException e) {
            Log.e("Database", "表创建失败: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {

//            // 删除表（确保版本更新时删除表）
//            db.execSQL("DROP TABLE IF EXISTS dynamic_likes");
            // 如果当前版本低于2，添加新的列
            db.execSQL("ALTER TABLE dynamic ADD COLUMN likes_count INTEGER DEFAULT 0");
            onCreate(db);  // 重新创建数据库
        }
    }


    // 添加用户
    public void addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);

        // 设置 userName 默认为 "用户 + 邮箱账号"
        String userName = "用户" + email;
        values.put("userName", userName);

        db.insert(TABLE_USERS, null, values);
    }

    // 获取用户信息
    @SuppressLint("Range")
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{"email", "profile"},
                "email = ?", new String[]{email}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            User user = new User();
            user.setEmail(cursor.getString(cursor.getColumnIndex("email")));
            user.setProfile(cursor.getString(cursor.getColumnIndex("profile")));
            cursor.close();
            return user;
        }
        return null;
    }

    // 根据邮箱获取用户的用户名
    public String getUserNameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                "users",             // 表名
                new String[]{"userName"},  // 选择需要查询的列
                "email = ?",         // 查询条件
                new String[]{email}, // 查询条件的参数
                null, null, null     // 排序、分组等条件（这里不使用）
        );

        if (cursor != null && cursor.moveToFirst()) {
            // 获取查询结果中的 userName 字段的值
            @SuppressLint("Range") String userName = cursor.getString(cursor.getColumnIndex("userName"));
            cursor.close();
            return userName;
        }

        // 如果没有查询到结果，返回 null 或者默认值
        return null;
    }

    @SuppressLint("Range")
    public String getUserAvatarPathByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{"avatar_image_path"},
                "email = ?", new String[]{email}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String avatarPath = cursor.getString(cursor.getColumnIndex("avatar_image_path"));
            cursor.close();
            return avatarPath;  // 返回头像路径
        }
        return null;  // 如果没有设置头像，返回 null
    }

    // 获取所有用户的邮箱
    public List<String> getAllAccounts() {
        List<String> allEmails = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 查询所有用户
        Cursor cursor = db.query(
                "users",  // 表名
                new String[]{"email"},  // 查询的列
                null,  // 查询条件（不加限制，获取所有用户）
                null,  // 条件值
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex("email"));
                allEmails.add(email);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return allEmails;
    }

    // 更新用户资料
    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase(); // 修改为 getWritableDatabase
        ContentValues values = new ContentValues();
        values.put("profile", user.getProfile());  // 确保字段名为 "profile"

        int rowsAffected = db.update(TABLE_USERS,
                values,
                "email = ?", // 修正拼写错误
                new String[]{user.getEmail()});

        return rowsAffected > 0;
    }

    // 添加动态评论
    public void addDynamicComment(int dynamicId, String email, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("dynamic_id", dynamicId);
        values.put("email", email);
        values.put("comment", comment);

        try {
            db.insert(DYCOMMENT_TABLE, null, values);
        } catch (SQLException e) {
            Log.e("Database", "插入动态评论失败: " + e.getMessage());
        }
    }

    // 获取特定用户的动态评论
    public List<DynamicComment> getUserDYComments(String userEmail) {
        List<DynamicComment> comments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // SQL 查询语句
        String query = "SELECT * FROM dycomment WHERE email = ? ORDER BY timestamp DESC";
        Cursor cursor = db.rawQuery(query, new String[]{userEmail});

        // 遍历查询结果
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") int dynamicId = cursor.getInt(cursor.getColumnIndex("dynamic_id"));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex("email"));
                @SuppressLint("Range") String commentText = cursor.getString(cursor.getColumnIndex("comment"));
                @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));

                // 创建动态评论对象并添加到列表中
                DynamicComment comment = new DynamicComment(id, dynamicId, email, commentText, timestamp);
                comments.add(comment);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return comments;
    }

    @SuppressLint("Range")
    public List<DynamicComment> getDynamicComments(int dynamicId) {
        List<DynamicComment> comments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(DYCOMMENT_TABLE, new String[]{"id", "dynamic_id", "email", "comment", "timestamp"},
                "dynamic_id = ?", new String[]{String.valueOf(dynamicId)}, null, null, "timestamp DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 从Cursor中获取各列的值
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                int dynamicIdFromCursor = cursor.getInt(cursor.getColumnIndex("dynamic_id"));
                String email = cursor.getString(cursor.getColumnIndex("email"));
                String commentText = cursor.getString(cursor.getColumnIndex("comment"));
                String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));

                // 使用获取到的值创建DynamicComment对象
                DynamicComment comment = new DynamicComment(id, dynamicIdFromCursor, email, commentText, timestamp);

                // 将评论添加到列表
                comments.add(comment);
            }
            cursor.close();
        }

        return comments;
    }
    @SuppressLint("Range")
    // 根据动态ID获取动态信息的方法
    public Dynamic getDynamicById(int dynamicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DYNAMIC, new String[]{"id", "userEmail", "content", "musicId", "timestamp", "likes_count"},
                "id = ?", new String[]{String.valueOf(dynamicId)}, null, null, null);

        Dynamic dynamic = null;

        if (cursor != null && cursor.moveToFirst()) {
            // 读取数据
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
            @SuppressLint("Range") String userEmail = cursor.getString(cursor.getColumnIndex("userEmail"));
            @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex("content"));
            @SuppressLint("Range") int songId = cursor.getInt(cursor.getColumnIndex("musicId"));
            @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));
            // 获取与 songId 相关的歌曲信息
            Song song = getSongById(songId);  // 调用 getSongById 获取歌曲对象
            // 创建一个 Dynamic 对象并设置属性
            dynamic = new Dynamic(id, userEmail, content, songId, timestamp, song.getName(), song.getArtist(), song.getSongImage());
            cursor.close();
        }
        return dynamic;
    }

    // 删除评论
    public void deleteDynamicComment(int commentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(DYCOMMENT_TABLE, "id = ?", new String[]{String.valueOf(commentId)});
        } catch (SQLException e) {
            Log.e("Database", "删除动态评论失败: " + e.getMessage());
        }
    }

    // 获取点赞歌曲数
    public int getLikedSongsCountByUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM user_likes WHERE email = ?", new String[]{email});
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count;
        }
        return 0;
    }

    // 获取评论数
    public int getCommentsCountByUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM comment WHERE email = ?", new String[]{email});
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count;
        }
        return 0;
    }
    // 获取评论数，包括动态评论
    public int getTotalCommentsCountByUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        // 查询评论表中的评论数
        Cursor cursorComment = db.rawQuery("SELECT COUNT(*) FROM comment WHERE email = ?", new String[]{email});
        int commentCount = 0;
        if (cursorComment != null) {
            cursorComment.moveToFirst();
            commentCount = cursorComment.getInt(0);
            cursorComment.close();
        }

        // 查询动态评论表中的评论数
        Cursor cursorDynamicComment = db.rawQuery("SELECT COUNT(*) FROM dycomment WHERE email = ?", new String[]{email});
        int dynamicCommentCount = 0;
        if (cursorDynamicComment != null) {
            cursorDynamicComment.moveToFirst();
            dynamicCommentCount = cursorDynamicComment.getInt(0);
            cursorDynamicComment.close();
        }

        // 返回评论总数
        return commentCount + dynamicCommentCount;
    }
    // 获取动态数
    public int getDynamicsCountByUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM dynamic WHERE userEmail = ?", new String[]{email});
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count;
        }
        return 0;
    }
    // 检查用户是否存在
    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE email=?";
        try (Cursor cursor = db.rawQuery(query, new String[]{email})) {
            return cursor.getCount() > 0;
        }
    }

    // 验证用户登录
    public boolean validateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE email=? AND password=?";
        try (Cursor cursor = db.rawQuery(query, new String[]{email, password})) {
            return cursor.getCount() > 0;
        }
    }
    void updateUsername(String newUsername, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", newUsername);  // 假设表中有 `username` 字段

        // 使用传入的 email 更新用户名
        db.update("users", values, "email = ?", new String[]{email});
        db.close();
    }


    boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{username});
        boolean isTaken = cursor.getCount() > 0;
        cursor.close();
        return isTaken;
    }
    // 添加歌曲
    public void addSong(String name, String artist, String album, int likes, String filePath, String songImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("artist", artist);
        values.put("album", album);
        values.put("likes", likes);
        values.put("filePath", filePath);
        values.put("songImage", songImage); // 插入图片路径或 URL
        long result = db.insert(TABLE_SONGS, null, values);
        if (result == -1) {
            Log.e("Database", "插入歌曲失败");
        } else {
            Log.d("Database", "插入歌曲成功");
        }
    }

    // 获取所有歌曲
    public List<Song> getAllSongs() {
        List<Song> songList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SONGS;
        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                    @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex("artist"));
                    @SuppressLint("Range") String album = cursor.getString(cursor.getColumnIndex("album"));
                    @SuppressLint("Range") int likes = cursor.getInt(cursor.getColumnIndex("likes"));
                    @SuppressLint("Range") String filePath = cursor.getString(cursor.getColumnIndex("filePath"));
                    @SuppressLint("Range") String songImage = cursor.getString(cursor.getColumnIndex("songImage")); // 新增
                    Song song = new Song(id, name, artist, album, likes, filePath, songImage);
                    songList.add(song);
                } while (cursor.moveToNext());
            }
        }
        return songList;
    }

    // 获取歌曲点赞数
    @SuppressLint("Range")
    public int getLikeCount(int songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT likes FROM " + TABLE_SONGS + " WHERE id=?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(songId)})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex("likes"));
            }
        }
        return 0;
    }

    // 删除这部分代码，不再需要
// 取消点赞
    public boolean unlikeSong(String email, int songId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // 删除特定用户对特定歌曲的点赞记录
        int rowsAffected = db.delete(TABLE_USER_LIKES, "email=? AND song_id=?",
                new String[]{String.valueOf(email), String.valueOf(songId)});
        // 如果删除操作成功（至少删除了一行数据），返回 true
        return rowsAffected > 0;
    }

    public boolean likeSong(String email, int songId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email); // 用户
        values.put("song_id", songId); // 歌曲ID
        long result = db.insert(TABLE_USER_LIKES, null, values);
        // 如果插入成功，返回 true
        return result != -1;
    }
    public int getLikesCountByDynamic(int dynamicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM dynamic_likes WHERE dynamic_id = ?", new String[]{String.valueOf(dynamicId)});
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count;
        }
        return 0;
    }

    // 删除这部分代码，不再需要
    public void removeLike(String email, int dynamicId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("dynamic_likes", "email = ? AND dynamic_id = ?", new String[]{email, String.valueOf(dynamicId)});
    }

    public void addLike(String email, int dynamicId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("dynamic_id", dynamicId);
        db.insert("dynamic_likes", null, contentValues);
    }

    public boolean isLikedByUser(String email, int dynamicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM dynamic_likes WHERE email=? AND dynamic_id=?";
        try (Cursor cursor = db.rawQuery(query, new String[]{email, String.valueOf(dynamicId)})) {
            return cursor.getCount() > 0;  // 如果查到记录，则表示已点赞
        }
    }

    public void updateLikesCount(int dynamicId, int likesCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("likes_count", likesCount);  // 更新点赞数
        db.update("dynamic", contentValues, "id = ?", new String[]{String.valueOf(dynamicId)});
    }


    //    // 在 UserDatabaseHelper 类中添加方法
//    public Song getSongById(int songId) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.query("song", // 假设数据库表名为 "song"
//                new String[]{"id", "name", "artist", "album", "file_path", "songImage"}, // 列出所有你需要的字段
//                "id = ?", // 查询条件
//                new String[]{String.valueOf(songId)}, // 参数
//                null, null, null);
//
//        if (cursor != null && cursor.moveToFirst()) {
//            // 获取数据
//            int id = cursor.getInt(cursor.getColumnIndex("id"));
//            String name = cursor.getString(cursor.getColumnIndex("name"));
//            String artist = cursor.getString(cursor.getColumnIndex("artist"));
//            String album = cursor.getString(cursor.getColumnIndex("album"));
//            String filePath = cursor.getString(cursor.getColumnIndex("file_path"));
//            String songImage = cursor.getString(cursor.getColumnIndex("songImage"));
//
//            // 创建并返回 Song 对象
//            Song song = new Song(id, name, artist, album, filePath, songImage);
//            cursor.close();
//            return song;
//        } else {
//            cursor.close();
//            return null;
//        }
//    }
    // 插入歌曲到数据库
    public void insertSong(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // 如果 name 为 null
        if (song.getName() == null) {
            song.setName("未知");
        }
        values.put(COLUMN_NAME, song.getName());
        values.put(COLUMN_ARTIST, song.getArtist());
        values.put(COLUMN_ALBUM, song.getAlbum());
        values.put(COLUMN_FILE_PATH, song.getFilePath());
        values.put(COLUMN_IMAGE, song.getSongImage());
        long result = db.insert(TABLE_SONGS, null, values);

        if (result == -1) {
            Log.e("Database", "插入失败: " + song.getName());
        } else {
            Log.d("Database", "插入成功: " + song.getName());
        }
    }
//
//    public void deleteSong(int songId) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        // 删除记录，指定 id
//        int rowsDeleted = db.delete(TABLE_SONGS, COLUMN_ID + " = ?", new String[]{String.valueOf(songId)});
//
//        if (rowsDeleted > 0) {
//            // 删除成功时显示 Toast
//            Toast.makeText(context, "删除歌曲成功", Toast.LENGTH_SHORT).show();
//        } else {
//            // 删除失败时显示 Toast
//            Toast.makeText(context, "未找到歌曲，删除失败", Toast.LENGTH_SHORT).show();
//        }
//
//        db.close();
//    }

    // 检查歌曲是否已经存在
    public boolean isSongExist(String filePath) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SONGS, null, COLUMN_FILE_PATH + " = ?", new String[]{filePath}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // 根据歌曲 ID 获取对应的图片资源 ID
    public int getSongImageResource(int songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("songs", new String[]{"id"}, "id = ?", new String[]{String.valueOf(songId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int imageResId = cursor.getInt(cursor.getColumnIndex("id"));
            cursor.close();
            return imageResId;
        } else {
            cursor.close();
            return -1; // 如果没有找到图片，返回 -1
        }
    }

    // 更新歌曲点赞数
    public void updateLikesInDatabase(int songId, int likeCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("likes", likeCount);
        db.update(TABLE_SONGS, values, "id=?", new String[]{String.valueOf(songId)});
    }

    public boolean isSongLikedByUser(String email, int songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USER_LIKES + " WHERE email=? AND song_id=?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(email), String.valueOf(songId)})) {
            return cursor.getCount() > 0; // 如果查到记录，则表示已点赞
        }
    }

    // 插入评论到数据库
    public void addComment(int songId, String email, String commentText) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("song_id", songId);
        values.put("email", email);  // 使用用户的 email
        values.put("comment_text", commentText);

        db.insert("comment", null, values);
    }

    // 获取某首歌的所有评论
    public List<Comment> getCommentsForSong(int songId) {
        List<Comment> comments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT comment.id, comment.song_id, comment.comment_text, comment.timestamp, comment.email " +
                "FROM comment " +
                "WHERE song_id = ? ORDER BY timestamp DESC";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(songId)})) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String commentText = cursor.getString(cursor.getColumnIndex("comment_text"));
                @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex("email"));
                comments.add(new Comment(id, songId, commentText, timestamp, email));
            }
        }
        return comments;
    }

    public void addDynamic(Dynamic dynamic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // 获取 Dynamic 对象的各字段值
        String userEmail = dynamic.getUserEmail();
        String content = dynamic.getContent();
        int MusicId = dynamic.getMusicid();
        String songName = dynamic.getSongName();
        String artistName = dynamic.getArtistName();
        String songImage = dynamic.getSongImage();
        String timestamp = dynamic.getTimestamp();

        // 填充到 ContentValues 中
        values.put("userEmail", userEmail); // 替换为实际的列名
        values.put("content", content);     // 替换为实际的列名
        values.put("musicid", MusicId); // 替换为实际的列名
        values.put("timestamp", timestamp); // 替换为实际的列名
        values.put("songName", songName);
        values.put("artistName",artistName);
        values.put("songImage",songImage);

        // 插入数据
        long result = db.insert(TABLE_DYNAMIC, null, values);
        if (result == -1) {
            Log.e("Database", "插入动态失败");
        } else {
            Log.d("Database", "插入动态成功");
        }
    }

    public List<Dynamic> getAllDynamics() {
        List<Dynamic> dynamics = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 查询动态信息并关联歌曲表获取歌曲信息
        String query = "SELECT dynamic.id, dynamic.userEmail, dynamic.content, dynamic.musicId, dynamic.timestamp,dynamic.likes_count, " +
                "songs.name AS songName, songs.artist AS songArtist, songs.songImage AS songImage " +
                "FROM dynamic " +
                "LEFT JOIN songs ON dynamic.musicId = songs.id " +
                "ORDER BY dynamic.timestamp DESC";

        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String userEmail = cursor.getString(cursor.getColumnIndex("userEmail"));
                @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex("content"));
                @SuppressLint("Range") int musicId = cursor.getInt(cursor.getColumnIndex("musicId"));
                @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));
                @SuppressLint("Range") int likesCount = cursor.getInt(cursor.getColumnIndex("likes_count"));
                // 获取歌曲的名称、艺术家和歌曲图片
                @SuppressLint("Range") String songName = cursor.getString(cursor.getColumnIndex("songName"));
                @SuppressLint("Range") String songArtist = cursor.getString(cursor.getColumnIndex("songArtist"));
                @SuppressLint("Range") String songImage = cursor.getString(cursor.getColumnIndex("songImage"));

                // 创建 Dynamic 对象并将信息传入
                Dynamic dynamic = new Dynamic(id, userEmail, content, musicId, timestamp,songName, songArtist, songImage);
                dynamics.add(dynamic);
            }
        }
        return dynamics;
    }

    // 在 UserDatabaseHelper 类中添加方法
    public List<Song> getLikedSongsByUser(String email) {
        List<Song> likedSongs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // 查询所有点赞记录
        String query = "SELECT songs.id, songs.name, songs.artist, songs.album,songs.likes,songs.filePath, songs.songImage " +
                "FROM user_likes AS lt " + // 假设点赞记录表名为 like_table
                "JOIN songs AS songs ON lt.song_id = songs.id " +
                "WHERE lt.email = ?";

        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int songId = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range")String songName = cursor.getString(cursor.getColumnIndex("name"));
                @SuppressLint("Range")String artist = cursor.getString(cursor.getColumnIndex("artist"));
                @SuppressLint("Range")  String album = cursor.getString(cursor.getColumnIndex("album"));
                @SuppressLint("Range") String filePath = cursor.getString(cursor.getColumnIndex("filePath"));
                @SuppressLint("Range")  String songImage = cursor.getString(cursor.getColumnIndex("songImage"));
                @SuppressLint("Range")  int likes = cursor.getInt(cursor.getColumnIndex("likes"));

                // 将查询到的歌曲信息封装成 Song 对象
                Song song = new Song(songId, songName, artist, album, likes, filePath, songImage);
                likedSongs.add(song);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return likedSongs;
    }
    @SuppressLint("Range")
    public List<Comment> getUserComments(String email) {
        List<Comment> comments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM comment WHERE email = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        while (cursor.moveToNext()) {
            // 从Cursor中提取数据并传递给构造器
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            int song_id = cursor.getInt(cursor.getColumnIndex("song_id"));
            String userEmail = cursor.getString(cursor.getColumnIndex("email"));
            String content = cursor.getString(cursor.getColumnIndex("comment_text"));
            String time = cursor.getString(cursor.getColumnIndex("timestamp"));

            // 使用带参数的构造函数
            Comment comment = new Comment(id, song_id,content, time,userEmail);
            comments.add(comment);
        }
        cursor.close();
        return comments;
    }
    // 删除评论的方法
    public void deleteComment(int commentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("comment", "id = ?", new String[]{String.valueOf(commentId)});
        db.close();
    }

    @SuppressLint("Range")
    public List<Dynamic> getUserDynamics(String email) {
        List<Dynamic> dynamics = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 查询动态信息并关联歌曲表获取歌曲信息
        String query = "SELECT dynamic.id, dynamic.userEmail, dynamic.content, dynamic.musicId, dynamic.timestamp,dynamic.likes_count,  " +
                "songs.name AS songName, songs.artist AS songArtist, songs.songImage AS songImage " +
                "FROM dynamic " +
                "LEFT JOIN songs ON dynamic.musicId = songs.id " +
                "WHERE dynamic.userEmail = ? " +
                "ORDER BY dynamic.timestamp DESC";  // 按时间降序排列

        Cursor cursor = db.rawQuery(query, new String[]{email});
        while (cursor.moveToNext()) {
            // 提取动态信息
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String userEmail = cursor.getString(cursor.getColumnIndex("userEmail"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            int musicId = cursor.getInt(cursor.getColumnIndex("musicId"));
            String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));
            int likesCount = cursor.getInt(cursor.getColumnIndex("likes_count"));
            // 提取歌曲信息
            String songName = cursor.getString(cursor.getColumnIndex("songName"));
            String songArtist = cursor.getString(cursor.getColumnIndex("songArtist"));
            String songImage = cursor.getString(cursor.getColumnIndex("songImage"));

            // 创建 Dynamic 对象并加入列表
            dynamics.add(new Dynamic(
                    id, userEmail, content, musicId, timestamp,songName, songArtist, songImage));
        }
        cursor.close();
        return dynamics;
    }
    @SuppressLint("Range")
    public Song getSongById(int songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM songs WHERE id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(songId)});

        Song song = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String album = cursor.getString(cursor.getColumnIndex("album"));
            int likes = cursor.getInt(cursor.getColumnIndex("likes"));
            String filePath = cursor.getString(cursor.getColumnIndex("filePath"));
            String songImage = cursor.getString(cursor.getColumnIndex("songImage"));

            // 使用带参数的构造器
            song = new Song(id, name, artist, album, likes, filePath, songImage);
        }
        cursor.close();
        return song;
    }
    public void deleteDynamicById(int dynamicId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // 删除动态记录，按 ID 来删除
        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(dynamicId)};
        db.delete("dynamic", whereClause, whereArgs);
        db.close();
    }
    public void deletelikeById(int likeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // 删除动态记录，按 ID 来删除
        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(likeId)};
        db.delete("user_likes", whereClause, whereArgs);
        db.close();
    }

    public void deleteSong(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // 删除动态记录，按 ID 来删除
        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(id)};
        db.delete("songs", whereClause, whereArgs);
        db.close();
    }
}
