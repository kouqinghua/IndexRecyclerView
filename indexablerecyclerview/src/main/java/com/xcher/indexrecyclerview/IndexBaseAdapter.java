package com.xcher.indexrecyclerview;

import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import com.xcher.indexrecyclerview.database.DataObservable;
import com.xcher.indexrecyclerview.database.DataObserver;

public abstract class IndexBaseAdapter<T extends IndexEntity> {

    static final int TYPE_ALL = 0;
    static final int TYPE_CLICK_CONTENT = 2;
    static final int TYPE_LONG_CLICK_CONTENT = 4;

    private final DataObservable mDataSetObservable = new DataObservable();

    private List<T> mData;

    private IndexCallback<T> mCallback;
    private OnItemContentClickListener mContentClickListener;
    private OnItemContentLongClickListener mContentLongClickListener;

    public abstract RecyclerView.ViewHolder onCreateTitleViewHolder(ViewGroup parent);

    public abstract RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent);

    public abstract void onBindTitleViewHolder(RecyclerView.ViewHolder holder, String indexTitle);

    public abstract void onBindContentViewHolder(RecyclerView.ViewHolder holder, T entity);

    public void setData(List<T> data) {
        setData(data, null);
    }

    public void setData(List<T> data, IndexCallback<T> callback) {
        this.mCallback = callback;
        mData = data;
        notifyInit();
    }

    public void setOnItemContentClickListener(OnItemContentClickListener<T> listener) {
        this.mContentClickListener = listener;
        notifySetListener(TYPE_CLICK_CONTENT);
    }

    public void setOnItemContentLongClickListener(OnItemContentLongClickListener<T> listener) {
        this.mContentLongClickListener = listener;
        notifySetListener(TYPE_LONG_CLICK_CONTENT);
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyInit();
    }

    private void notifyInit() {
        mDataSetObservable.notifyInit();
    }

    private void notifySetListener(int type) {
        mDataSetObservable.notifySetListener(type);
    }

    public List<T> getItems() {
        return mData;
    }

    IndexCallback<T> getIndexCallback() {
        return mCallback;
    }

    OnItemContentClickListener getOnItemContentClickListener() {
        return mContentClickListener;
    }

    OnItemContentLongClickListener getOnItemContentLongClickListener() {
        return mContentLongClickListener;
    }

    public void registerDataSetObserver(DataObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public interface IndexCallback<T> {
        void onFinished(List<EntityWrapper<T>> data);
    }

    public interface OnItemTitleClickListener {
        void onItemClick(View v, int currentPosition, String indexTitle);
    }

    public interface OnItemContentClickListener<T> {
        void onItemClick(View v, int originalPosition, int currentPosition, T entity);
    }

    public interface OnItemTitleLongClickListener {
        boolean onItemLongClick(View v, int currentPosition, String indexTitle);
    }

    public interface OnItemContentLongClickListener<T> {
        boolean onItemLongClick(View v, int originalPosition, int currentPosition, T entity);
    }
}
