package com.myapp.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DyCommentAdapter extends RecyclerView.Adapter<DyCommentAdapter.CommentViewHolder> {

    private List<DynamicComment> dycommentsList;  // 使用 DyComment 类作为数据类型
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private String userName;
    private UserDatabaseHelper dbHelper;
    private Context context;
    // 点击事件接口
    public interface OnItemClickListener {
        void onItemClick(DynamicComment Dycomment);
    }

    // 长按事件接口
    public interface OnItemLongClickListener {
        void onItemLongClick(DynamicComment Dycomment, int position);
    }

    // 构造器，接收数据列表、点击和长按事件监听器
    public DyCommentAdapter(Context context,List<DynamicComment> dycommentsList, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
        this.context = context;
        this.dycommentsList = dycommentsList;
        this.onItemClickListener = onItemClickListener;
        this.onItemLongClickListener = onItemLongClickListener;
        dbHelper = new UserDatabaseHelper(context);  // 初始化 dbHelper
    }

    // 创建视图持有者
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dycomment, parent, false);
        return new CommentViewHolder(view);
    }

    // 绑定数据到视图
    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        DynamicComment dycomment = dycommentsList.get(position);  // 使用 DyComment 类型
        userName = dbHelper.getUserNameByEmail(dycomment.getEmail());
        holder.emailTextView.setText(userName);
//        holder.emailTextView.setText(dycomment.getEmail());
        holder.commentTextView.setText(dycomment.getComment());
        holder.timestampTextView.setText(dycomment.getTimestamp());

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(dycomment);  // 调用接口方法
            }
        });

        // 设置长按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(dycomment, position);  // 调用接口方法
            }
            return true;  // 返回 true 表示已处理长按事件
        });
    }

    // 获取评论列表的总数
    @Override
    public int getItemCount() {
        return dycommentsList.size();  // 返回 dycommentsList 的大小
    }

    // 更新评论列表
    public void updateComments(List<DynamicComment> newComments) {
        dycommentsList.clear();
        dycommentsList.addAll(newComments);
        notifyDataSetChanged();  // 更新适配器数据
    }

    // 视图持有者
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;
        TextView commentTextView;
        TextView timestampTextView;

        public CommentViewHolder(View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.tvCommentEmail);
            commentTextView = itemView.findViewById(R.id.tvCommentText);
            timestampTextView = itemView.findViewById(R.id.tvCommentTimestamp);
        }
    }
}
