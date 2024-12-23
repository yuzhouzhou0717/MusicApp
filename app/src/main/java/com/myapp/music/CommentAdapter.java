package com.myapp.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;
    private String userName;
    private UserDatabaseHelper dbHelper;
    private Context context;
    // 修改构造函数，接受长按监听器
    public CommentAdapter(Context context, List<Comment> comments, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
        this.context = context;
        this.comments = comments;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        dbHelper = new UserDatabaseHelper(context);  // 初始化 dbHelper
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

//        holder.emailTextView.setText(comment.getEmail());
        userName = dbHelper.getUserNameByEmail(comment.getEmail());
        holder.emailTextView.setText(userName);
        holder.commentTextView.setText(comment.getCommentText());
        holder.timestampTextView.setText(comment.getTimestamp());

        // 单击事件：点击评论跳转到歌曲详情
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(comment);
            }
        });
        // 设置长按监听器
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(comment, position);
            }
            return true; // 返回true表示已处理长按事件
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView emailTextView, commentTextView, timestampTextView;

        public CommentViewHolder(View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
    // 定义单击回调接口
    public interface OnItemClickListener {
        void onItemClick(Comment comment);
    }
    // 定义一个长按回调接口
    public interface OnItemLongClickListener {
        void onItemLongClick(Comment comment, int position);
    }
}

