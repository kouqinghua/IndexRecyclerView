package com.xcher.indexrecyclerview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.xcher.indexablerecyclerview.R;
import com.xcher.indexrecyclerview.database.DataObserver;
import com.xcher.indexrecyclerview.database.HeaderFooterDataObserver;
import com.xcher.indexrecyclerview.database.IndexBarDataObserver;

/**
 * RecyclerView列表结合字母索引排序
 * 列表支持联系人、城市等首字母粘性吸顶
 * 列表滑动联动字母索引选中
 * 字母索引可选气泡提示与居中提示
 * 字母索引滑动联动列表活动
 * 可添加自定义字母索引与自定义RecyclerView头部尾部
 */
@SuppressWarnings("unchecked")
public class IndexRecyclerView extends FrameLayout {

    private static int PADDING_RIGHT_OVERLAY;
    static final String INDEX_SIGN = "#";

    private Context mContext;

    private ExecutorService mExecutorService;
    private Future mFuture;

    private RecyclerView mRecycler;
    private IndexBar mIndexBar;
    /**
     * 保存正在Invisible的ItemView
     * <p>
     * 使用mLastInvisibleRecyclerViewItemView来保存当前Invisible的ItemView，
     * 每次有新的ItemView需要Invisible的时候，把旧的Invisible的ItemView设为Visible。
     * 这样就修复了View复用导致的Invisible状态传递的问题。
     */
    private View mLastInvisibleRecyclerViewItemView;

    private RecyclerView.ViewHolder mStickyViewHolder;
    private String mStickyTitle;

    private RealAdapter mRealAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private IndexBaseAdapter mIndexAdapter;

    private TextView mCenter, mBubble;

    private int mBarTextColor, mBarFocusTextColor;
    private float mBarTextSize, mBarTextSpace, mBarWidth;
    private Drawable mBarBg;

    private DataObserver mDataSetObserver;
    private int mBubbleSize = 30;
    private int mCenterSize = 40;

    private int mBubbleDrawable = -1;

    private Handler mHandler;

    private final HeaderFooterDataObserver<EntityWrapper> mHeaderFooterDataSetObserver = new HeaderFooterDataObserver<EntityWrapper>() {
        @Override
        public void onChanged() {
            if (mRealAdapter == null) return;
            mRealAdapter.notifyDataSetChanged();
        }

        @Override
        public void onAdd(boolean header, EntityWrapper preData, EntityWrapper data) {
            if (mRealAdapter == null) return;
            mRealAdapter.addHeaderFooterData(header, preData, data);
        }

        @Override
        public void onRemove(boolean header, EntityWrapper data) {
            if (mRealAdapter == null) return;
            mRealAdapter.removeHeaderFooterData(header, data);
        }
    };

    private final IndexBarDataObserver mIndexBarDataSetObserver = new IndexBarDataObserver() {
        @Override
        public void onChanged() {
            mIndexBar.setData(mRealAdapter.getItems());
        }
    };

    public IndexRecyclerView(Context context) {
        this(context, null);
    }

    public IndexRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndexRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        this.mExecutorService = Executors.newSingleThreadExecutor();
        PADDING_RIGHT_OVERLAY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IndexRecyclerView);
            mBarTextColor = a.getColor(R.styleable.IndexRecyclerView_indexBar_textColor, ContextCompat.getColor(context, R.color.default_indexBar_textColor));
            mBarTextSize = a.getDimension(R.styleable.IndexRecyclerView_indexBar_textSize, getResources().getDimension(R.dimen.default_indexBar_textSize));
            mBarFocusTextColor = a.getColor(R.styleable.IndexRecyclerView_indexBar_selectedTextColor, ContextCompat.getColor(context, R.color.default_indexBar_selectedTextColor));
            mBarTextSpace = a.getDimension(R.styleable.IndexRecyclerView_indexBar_textSpace, getResources().getDimension(R.dimen.default_indexBar_textSpace));
            mBarBg = a.getDrawable(R.styleable.IndexRecyclerView_indexBar_background);
            mBarWidth = a.getDimension(R.styleable.IndexRecyclerView_indexBar_layout_width, getResources().getDimension(R.dimen.default_indexBar_layout_width));
            a.recycle();
        }

        if (mContext instanceof Activity) {
            ((Activity) mContext).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        mRecycler = new RecyclerView(context);
        mRecycler.setVerticalScrollBarEnabled(false);
        mRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
        addView(mRecycler, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mIndexBar = new IndexBar(context);
        mIndexBar.init(mBarBg, mBarTextColor, mBarFocusTextColor, mBarTextSize, mBarTextSpace);
        LayoutParams params = new LayoutParams((int) mBarWidth, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        addView(mIndexBar, params);

        mRealAdapter = new RealAdapter();
        mRecycler.setHasFixedSize(true);
        mRecycler.setAdapter(mRealAdapter);

        initListener();
    }

    public <T extends IndexEntity> void setAdapter(final IndexBaseAdapter<T> adapter) {

        if (mLayoutManager == null) {
            throw new NullPointerException("You must set the LayoutManager first");
        }

        this.mIndexAdapter = adapter;

        if (mDataSetObserver != null) {
            adapter.unregisterDataSetObserver(mDataSetObserver);
        }

        mDataSetObserver = new DataObserver() {

            @Override
            public void onInit() {
                onSetListener(IndexBaseAdapter.TYPE_ALL);
                onDataChanged();
            }

            @Override
            public void onChanged() {
                if (mRealAdapter != null) {
                    mRealAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onSetListener(int type) {
                if ((type == IndexBaseAdapter.TYPE_CLICK_CONTENT || type == IndexBaseAdapter.TYPE_ALL) && adapter.getOnItemContentClickListener() != null) {
                    mRealAdapter.setOnItemContentClickListener(adapter.getOnItemContentClickListener());
                }
                if ((type == IndexBaseAdapter.TYPE_LONG_CLICK_CONTENT || type == IndexBaseAdapter.TYPE_ALL) && adapter.getOnItemContentLongClickListener() != null) {
                    mRealAdapter.setOnItemContentLongClickListener(adapter.getOnItemContentLongClickListener());
                }
            }
        };

        adapter.registerDataSetObserver(mDataSetObserver);
        mRealAdapter.setIndexableAdapter(adapter);
        initStickyView(adapter);
    }

    public <T> void addHeaderAdapter(IndexHeaderAdapter<T> adapter) {
        adapter.registerDataSetObserver(mHeaderFooterDataSetObserver);
        adapter.registerIndexBarDataSetObserver(mIndexBarDataSetObserver);
        mRealAdapter.addIndexableHeaderAdapter(adapter);
    }

    public <T> void removeHeaderAdapter(IndexHeaderAdapter<T> adapter) {
        adapter.unregisterDataSetObserver(mHeaderFooterDataSetObserver);
        adapter.unregisterIndexBarDataSetObserver(mIndexBarDataSetObserver);
        mRealAdapter.removeIndexableHeaderAdapter(adapter);
    }

    public <T> void addFooterAdapter(IndexFooterAdapter<T> adapter) {
        adapter.registerDataSetObserver(mHeaderFooterDataSetObserver);
        adapter.registerIndexBarDataSetObserver(mIndexBarDataSetObserver);
        mRealAdapter.addIndexableFooterAdapter(adapter);
    }

    public <T> void removeFooterAdapter(IndexFooterAdapter<T> adapter) {
        adapter.unregisterDataSetObserver(mHeaderFooterDataSetObserver);
        adapter.unregisterIndexBarDataSetObserver(mIndexBarDataSetObserver);
        mRealAdapter.removeIndexableFooterAdapter(adapter);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager == null) {
            throw new NullPointerException("LayoutManager == null");
        }

        mLayoutManager = layoutManager;

        mRecycler.setLayoutManager(mLayoutManager);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        mRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recycler, int dx, int dy) {
                super.onScrolled(recycler, dx, dy);
                processScrollListener();
            }
        });

        mIndexBar.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int touchPos = mIndexBar.getPositionForPointY(event.getY());
                if (touchPos < 0) return true;

                if (!(mLayoutManager instanceof LinearLayoutManager)) return true;
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mLayoutManager;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        showOverlayView(event.getY(), touchPos);

                        if (touchPos != mIndexBar.getSelectionPosition()) {
                            mIndexBar.setSelectionPosition(touchPos);

                            if (touchPos == 0) {
                                linearLayoutManager.scrollToPositionWithOffset(0, 0);
                            } else {
                                linearLayoutManager.scrollToPositionWithOffset(mIndexBar.getFirstRecyclerViewPositionBySelection(), 0);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (mCenter != null) mCenter.setVisibility(GONE);
                        if (mBubble != null) mBubble.setVisibility(GONE);
                        break;
                }
                return true;
            }
        });
    }

    private void processScrollListener() {
        if (!(mLayoutManager instanceof LinearLayoutManager)) return;

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mLayoutManager;

        int firstItemPosition;
        firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        if (firstItemPosition == RecyclerView.NO_POSITION) return;

        mIndexBar.setSelection(firstItemPosition);

        ArrayList<EntityWrapper> list = mRealAdapter.getItems();
        if (mStickyViewHolder != null && list.size() > firstItemPosition) {
            EntityWrapper wrapper = list.get(firstItemPosition);
            String wrapperTitle = wrapper.getIndexTitle();

            if (EntityWrapper.TYPE_TITLE == wrapper.getItemType()) {
                if (mLastInvisibleRecyclerViewItemView != null && mLastInvisibleRecyclerViewItemView.getVisibility() == INVISIBLE) {
                    mLastInvisibleRecyclerViewItemView.setVisibility(VISIBLE);
                    mLastInvisibleRecyclerViewItemView = null;
                }

                mLastInvisibleRecyclerViewItemView = linearLayoutManager.findViewByPosition(firstItemPosition);

                if (mLastInvisibleRecyclerViewItemView != null) {
                    mLastInvisibleRecyclerViewItemView.setVisibility(INVISIBLE);
                }
            }

            // hide -> show
            if (wrapperTitle == null && mStickyViewHolder.itemView.getVisibility() == VISIBLE) {
                mStickyTitle = null;
                mStickyViewHolder.itemView.setVisibility(INVISIBLE);
            } else {
                stickyNewViewHolder(wrapperTitle);
            }

            if (firstItemPosition + 1 < list.size()) {
                processScroll(linearLayoutManager, list, firstItemPosition + 1, wrapperTitle);
            }
        }
    }

    private void processScroll(LinearLayoutManager layoutManager, ArrayList<EntityWrapper> list, int position, String title) {
        EntityWrapper nextWrapper = list.get(position);
        View nextTitleView = layoutManager.findViewByPosition(position);
        if (nextTitleView == null) return;
        if (nextWrapper.getItemType() == EntityWrapper.TYPE_TITLE) {
            if (nextTitleView.getTop() <= mStickyViewHolder.itemView.getHeight() && title != null) {
                mStickyViewHolder.itemView.setTranslationY(nextTitleView.getTop() - mStickyViewHolder.itemView.getHeight());
            }
            if (INVISIBLE == nextTitleView.getVisibility()) {
                //特殊情况：手指向下滑动的时候，需要及时把成为第二个可见View的TitleView设置Visible，
                // 这样才能配合StickyView制造两个TitleView切换的动画。
                nextTitleView.setVisibility(VISIBLE);
            }
        } else if (mStickyViewHolder.itemView.getTranslationY() != 0) {
            mStickyViewHolder.itemView.setTranslationY(0);
        }
    }

    private void stickyNewViewHolder(String wrapperTitle) {
        if ((wrapperTitle != null && !wrapperTitle.equals(mStickyTitle))) {

            if (mStickyViewHolder.itemView.getVisibility() != VISIBLE) {
                mStickyViewHolder.itemView.setVisibility(VISIBLE);
            }

            mStickyTitle = wrapperTitle;
            mIndexAdapter.onBindTitleViewHolder(mStickyViewHolder, wrapperTitle);
        }
    }

    private <T extends IndexEntity> void initStickyView(final IndexBaseAdapter<T> adapter) {
        mStickyViewHolder = adapter.onCreateTitleViewHolder(mRecycler);
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) == mRecycler) {
                mStickyViewHolder.itemView.setVisibility(INVISIBLE);
                addView(mStickyViewHolder.itemView, i + 1);
                return;
            }
        }
    }

    private void showOverlayView(float y, final int touchPos) {
        if (mIndexBar.getIndexList().size() <= touchPos) return;

        if (mBubble != null) {
            if (mBubble.getVisibility() != VISIBLE) {
                mBubble.setVisibility(VISIBLE);
            }

            if (y < PADDING_RIGHT_OVERLAY - mIndexBar.getTop() && y >= 0) {
                y = PADDING_RIGHT_OVERLAY - mIndexBar.getTop();
            } else if (y < 0) {
                if (mIndexBar.getTop() > PADDING_RIGHT_OVERLAY) {
                    y = 0;
                } else {
                    y = PADDING_RIGHT_OVERLAY - mIndexBar.getTop();
                }
            } else if (y > mIndexBar.getHeight()) {
                y = mIndexBar.getHeight();
            }
            mBubble.setY(mIndexBar.getTop() + y - 80);

            String index = mIndexBar.getIndexList().get(touchPos);
            if (!mBubble.getText().equals(index)) {
                if (index.length() > 1) {
                    mBubble.setTextSize(30);
                }
                mBubble.setText(index);
            }
        }
        if (mCenter != null) {
            if (mCenter.getVisibility() != VISIBLE) {
                mCenter.setVisibility(VISIBLE);
            }
            String index = mIndexBar.getIndexList().get(touchPos);
            if (!mCenter.getText().equals(index)) {
                if (index.length() > 1) {
                    mCenter.setTextSize(32);
                }
                mCenter.setText(index);
            }
        }
    }

    public void setEnableBubble() {
        if (mBubble == null) {
            initBubble();
        }
        mCenter = null;
    }

    public void setEnableCenter() {
        if (mCenter == null) {
            initCenter();
        }
        mBubble = null;
    }

    public void setEnableBar(boolean visible) {
        mIndexBar.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setBubbleSize(int size) {
        mBubbleSize = size;
    }

    public void setCenterSize(int size) {
        mCenterSize = size;
    }

    public void setBubbleDrawable(int resId) {
        mBubbleDrawable = resId;
    }

    private void initCenter() {
        mCenter = new TextView(mContext);
        mCenter.setBackgroundResource(R.drawable.indexable_bg_center_overlay);
        mCenter.setTextColor(Color.WHITE);
        mCenter.setTextSize(mCenterSize);
        mCenter.setGravity(Gravity.CENTER);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
        LayoutParams params = new LayoutParams(size, size);
        params.gravity = Gravity.CENTER;
        mCenter.setLayoutParams(params);
        mCenter.setVisibility(INVISIBLE);

        addView(mCenter);
    }

    private void initBubble() {
        mBubble = new AppCompatTextView(mContext);
        if (mBubbleDrawable == -1) {
            throw new NullPointerException("mBubbleDrawable is unset, please invoke setBubbleDrawable() before setEnableBubble()");
        }
        mBubble.setBackgroundResource(mBubbleDrawable);
        mBubble.setSingleLine();
        mBubble.setTextColor(Color.WHITE);
        mBubble.setTextSize(mBubbleSize);
        mBubble.setGravity(Gravity.CENTER);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        LayoutParams params = new LayoutParams(size, size);
        params.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
        params.gravity = Gravity.END;
        mBubble.setLayoutParams(params);
        mBubble.setVisibility(INVISIBLE);

        addView(mBubble);
    }

    private void onDataChanged() {
        if (mFuture != null) {
            mFuture.cancel(true);
        }
        mFuture = mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                final ArrayList<EntityWrapper> items = transform(mIndexAdapter.getItems());
                if (items == null) return;

                getSafeHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mRealAdapter.setDatas(items);
                        mIndexBar.setData(mRealAdapter.getItems());

                        if (mIndexAdapter.getIndexCallback() != null) {
                            mIndexAdapter.getIndexCallback().onFinished(items);
                        }

                        processScrollListener();
                    }
                });
            }
        });
    }

    private <T extends IndexEntity> ArrayList<EntityWrapper<T>> transform(final List<T> data) {
        try {
            TreeMap<String, List<EntityWrapper<T>>> map = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    if (lhs.equals(INDEX_SIGN)) {
                        return rhs.equals(INDEX_SIGN) ? 0 : 1;
                    } else if (rhs.equals(INDEX_SIGN)) {
                        return -1;
                    }
                    return lhs.compareTo(rhs);
                }
            });

            for (int i = 0; i < data.size(); i++) {
                EntityWrapper<T> entity = new EntityWrapper<>();
                T item = data.get(i);
                String indexName = item.getFieldIndexBy();
                String pinyin = PinyinUtil.getPingYin(indexName);
                entity.setPinyin(pinyin);

                if (PinyinUtil.matchingLetter(pinyin)) {
                    entity.setIndex(pinyin.substring(0, 1).toUpperCase());
                    entity.setIndexByField(item.getFieldIndexBy());
                } else if (PinyinUtil.matchingPolyphone(pinyin)) {
                    entity.setIndex(PinyinUtil.gePolyphoneInitial(pinyin).toUpperCase());
                    entity.setPinyin(PinyinUtil.getPolyphoneRealPinyin(pinyin));
                    String hanZi = PinyinUtil.getPolyphoneRealHanzi(indexName);
                    entity.setIndexByField(hanZi);
                    // 把多音字的真实indexField重新赋值
                    item.setFieldIndexBy(hanZi);
                } else {
                    entity.setIndex(INDEX_SIGN);
                    entity.setIndexByField(item.getFieldIndexBy());
                }
                entity.setIndexTitle(entity.getIndex());
                entity.setData(item);
                entity.setOriginalPosition(i);
                item.setFieldPinyinIndexBy(entity.getPinyin());

                List<EntityWrapper<T>> list;
                if (!map.containsKey(entity.getIndex())) {
                    list = new ArrayList<>();
                    list.add(new EntityWrapper<T>(entity.getIndex(), EntityWrapper.TYPE_TITLE));
                    map.put(entity.getIndex(), list);
                } else {
                    list = map.get(entity.getIndex());
                }

                if (list != null) {
                    list.add(entity);
                }
            }

            ArrayList<EntityWrapper<T>> list = new ArrayList<>();
            for (List<EntityWrapper<T>> entities : map.values()) {
                Collections.sort(entities, new InitialComparator<T>());
                list.addAll(entities);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Handler getSafeHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }
}
