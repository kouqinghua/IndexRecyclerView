package com.xcher.sample.contact;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.xcher.indexrecyclerview.IndexRecyclerView;
import com.xcher.indexrecyclerview.IndexableAdapter;
import com.xcher.indexrecyclerview.SimpleHeaderAdapter;
import com.xcher.sample.R;
import com.xcher.sample.ToastUtil;

/**
 * Created by YoKey on 16/10/8.
 */
public class PickContactActivity extends AppCompatActivity {

    private ContactAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_contact);
        getSupportActionBar().setTitle("联系人");
        IndexRecyclerView indexableLayout = findViewById(R.id.indexableLayout);

        indexableLayout.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new ContactAdapter(this);
        indexableLayout.setAdapter(mAdapter);
        mAdapter.setDatas(initDatas());
        indexableLayout.setBubbleDrawable(R.drawable.icon_hint_bg);
        indexableLayout.setEnableBubble();

        mAdapter.setOnItemContentClickListener(new IndexableAdapter.OnItemContentClickListener<UserEntity>() {
            @Override
            public void onItemClick(View v, int originalPosition, int currentPosition, UserEntity entity) {
                if (originalPosition >= 0) {
                    ToastUtil.showShort(PickContactActivity.this, "选中:" + entity.getNick() + "  当前位置:" + currentPosition + "  原始所在数组位置:" + originalPosition);
                } else {
                    ToastUtil.showShort(PickContactActivity.this, "选中Header/Footer:" + entity.getNick() + "  当前位置:" + currentPosition);
                }
            }
        });

        // 添加我关心的人
        indexableLayout.addHeaderAdapter(new SimpleHeaderAdapter<>(mAdapter, "☆", "我关心的", initFavDatas()));
    }

    private List<UserEntity> initDatas() {
        List<UserEntity> list = new ArrayList<>();
        // 初始化数据
        List<String> contactStrings = Arrays.asList(getResources().getStringArray(R.array.contact_array));
        List<String> mobileStrings = Arrays.asList(getResources().getStringArray(R.array.mobile_array));
        for (int i = 0; i < contactStrings.size(); i++) {
            UserEntity contactEntity = new UserEntity(contactStrings.get(i), mobileStrings.get(i));
            list.add(contactEntity);
        }
        return list;
    }

    private List<UserEntity> initFavDatas() {
        List<UserEntity> list = new ArrayList<>();
        list.add(new UserEntity("张三", "10000"));
        list.add(new UserEntity("李四", "10001"));
        return list;
    }
}
