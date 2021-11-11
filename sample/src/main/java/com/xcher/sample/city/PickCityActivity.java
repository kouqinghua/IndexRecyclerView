package com.xcher.sample.city;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.widget.FrameLayout;


import java.util.List;

import com.xcher.indexrecyclerview.SimpleHeaderAdapter;
import com.xcher.sample.R;

/**
 * 选择城市
 * Created by YoKey on 16/10/7.
 */
public class PickCityActivity extends AppCompatActivity {
    private List<CityEntity> mDatas;
    private SearchFragment mSearchFragment;
    private SearchView mSearchView;
    private FrameLayout mProgressBar;
    private SimpleHeaderAdapter mHotCityAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_city);
        getSupportActionBar().setTitle("选择城市");

//        mSearchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);
//        IndexableLayout indexableLayout = (IndexableLayout) findViewById(R.id.indexableLayout);
//        mSearchView = (SearchView) findViewById(R.id.searchview);
//        mProgressBar = (FrameLayout) findViewById(R.id.progress);
//
//
////        indexableLayout.setLayoutManager(new LinearLayoutManager(this));
//        indexableLayout.setLayoutManager(new GridLayoutManager(this, 2));
//
//        // 多音字处理
//        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(this)));
//
//
//
//        // setAdapter
//        CityAdapter adapter = new CityAdapter(this);
//        indexableLayout.setAdapter(adapter);
//        // set Datas
//        mDatas = initDatas();
//
//        adapter.setDatas(mDatas, new IndexableAdapter.IndexCallback<CityEntity>() {
//            @Override
//            public void onFinished(List<EntityWrapper<CityEntity>> datas) {
//                // 数据处理完成后回调
//                mSearchFragment.bindDatas(mDatas);
//                mProgressBar.setVisibility(View.GONE);
//            }
//        });
//
//        // set Center OverlayView
//        indexableLayout.setEnableCenter();
//
//        // set Listener
//        adapter.setOnItemContentClickListener(new IndexableAdapter.OnItemContentClickListener<CityEntity>() {
//            @Override
//            public void onItemClick(View v, int originalPosition, int currentPosition, CityEntity entity) {
//                if (originalPosition >= 0) {
//                    ToastUtil.showShort(PickCityActivity.this, "选中:" + entity.getName() + "  当前位置:" + currentPosition + "  原始所在数组位置:" + originalPosition);
//                } else {
//                    ToastUtil.showShort(PickCityActivity.this, "选中Header:" + entity.getName() + "  当前位置:" + currentPosition);
//                }
//            }
//        });
//
//        adapter.setOnItemTitleClickListener(new IndexableAdapter.OnItemTitleClickListener() {
//            @Override
//            public void onItemClick(View v, int currentPosition, String indexTitle) {
//                ToastUtil.showShort(PickCityActivity.this, "选中:" + indexTitle + "  当前位置:" + currentPosition);
//            }
//        });
//
//        // 添加 HeaderView DefaultHeaderAdapter接收一个IndexableAdapter, 使其布局以及点击事件和IndexableAdapter一致
//        // 如果想自定义布局,点击事件, 可传入 new IndexableHeaderAdapter
//
//        mHotCityAdapter = new SimpleHeaderAdapter<>(adapter, "热", "热门城市", iniyHotCityDatas());
//        // 热门城市
//        indexableLayout.addHeaderAdapter(mHotCityAdapter);
//        // 定位
//        final List<CityEntity> gpsCity = iniyGPSCityDatas();
//        final SimpleHeaderAdapter gpsHeaderAdapter = new SimpleHeaderAdapter<>(adapter, "定", "当前城市", gpsCity);
//        indexableLayout.addHeaderAdapter(gpsHeaderAdapter);
//
//        // 显示真实索引
////        indexableLayout.showAllLetter(false);
//
//        // 模拟定位
//        indexableLayout.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                gpsCity.get(0).setName("杭州市");
//                gpsHeaderAdapter.notifyDataSetChanged();
//            }
//        }, 3000);
//
//        // 搜索Demo
//        initSearch();
    }

//    // 更新数据点击事件
//    public void update(View view) {
//        List<CityEntity> list = new ArrayList<>();
//        list.add(new CityEntity("杭州市"));
//        list.add(new CityEntity("北京市"));
//        list.add(new CityEntity("上海市"));
//        list.add(new CityEntity("广州市"));
//        mHotCityAdapter.addDatas(list);
//        Toast.makeText(this, "更新数据", Toast.LENGTH_SHORT).show();
//    }
//
//    private List<CityEntity> initDatas() {
//        List<CityEntity> list = new ArrayList<>();
//        List<String> cityStrings = Arrays.asList(getResources().getStringArray(R.array.city_array));
//        for (String item : cityStrings) {
//            CityEntity cityEntity = new CityEntity();
//            cityEntity.setName(item);
//            list.add(cityEntity);
//        }
//        return list;
//    }
//
//    private List<CityEntity> iniyHotCityDatas() {
//        List<CityEntity> list = new ArrayList<>();
//        list.add(new CityEntity("杭州市"));
//        list.add(new CityEntity("北京市"));
//        list.add(new CityEntity("上海市"));
//        list.add(new CityEntity("广州市"));
//        return list;
//    }
//
//    private List<CityEntity> iniyGPSCityDatas() {
//        List<CityEntity> list = new ArrayList<>();
//        list.add(new CityEntity("定位中..."));
//        return list;
//    }
//
//    private void initSearch() {
//        getSupportFragmentManager().beginTransaction().hide(mSearchFragment).commit();
//
//        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                if (newText.trim().length() > 0) {
//                    if (mSearchFragment.isHidden()) {
//                        getSupportFragmentManager().beginTransaction().show(mSearchFragment).commit();
//                    }
//                } else {
//                    if (!mSearchFragment.isHidden()) {
//                        getSupportFragmentManager().beginTransaction().hide(mSearchFragment).commit();
//                    }
//                }
//
//                mSearchFragment.bindQueryText(newText);
//                return false;
//            }
//        });
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (!mSearchFragment.isHidden()) {
//            // 隐藏 搜索
//            mSearchView.setQuery(null, false);
//            getSupportFragmentManager().beginTransaction().hide(mSearchFragment).commit();
//            return;
//        }
//        super.onBackPressed();
//    }
}
