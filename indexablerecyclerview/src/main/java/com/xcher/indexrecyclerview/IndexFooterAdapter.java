package com.xcher.indexrecyclerview;

import java.util.List;

/**
 * FooterView Adapter
 * Created by YoKey on 16/10/14.
 */
public abstract class IndexFooterAdapter<T> extends AbstractHeaderFooterAdapter<T> {

    public IndexFooterAdapter(String index, String indexTitle, List<T> datas) {
        super(index, indexTitle, datas);
    }

    @Override
    int getHeaderFooterType() {
        return EntityWrapper.TYPE_FOOTER;
    }

    /**
     * set Content-ItemView click listener
     */
    public void setOnItemFooterClickListener(OnItemFooterClickListener<T> listener) {
        this.mListener = listener;
    }

    /**
     * set Content-ItemView longClick listener
     */
    public void setOnItemFooterLongClickListener(OnItemFooterLongClickListener<T> listener) {
        this.mLongListener = listener;
    }

    public interface OnItemFooterClickListener<T> extends OnItemClickListener<T>{
    }

    public interface OnItemFooterLongClickListener<T> extends OnItemLongClickListener<T>{
    }
}
