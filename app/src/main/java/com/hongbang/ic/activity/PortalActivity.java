package com.hongbang.ic.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.hongbang.ic.R;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortalActivity extends BaseActivity {

    @ViewInject(value = R.id.activity_list)
    private ListView mListView;

    private String[] activities = {
            "登录", "com.hongbang.ic.activity.LoginActivity",
            "注册", "com.hongbang.ic.activity.RegisterActivity",
            "社区选择", "com.hongbang.ic.activity.CommunityChooseActivity",
            "主界面", "com.hongbang.ic.activity.MainActivity",
            "报修", "com.hongbang.ic.activity.RepairActivity",
            "我的报修", "com.hongbang.ic.activity.RepairHistoryActivity",
            "我的报修详情", "com.hongbang.ic.activity.RepairDetailActivity",
    };

    private List<Map<String, String>> mListData = new ArrayList<>();

    {
        for (int i = 0; i < activities.length / 2; i++) {
            Map<String, String> item = new HashMap<>();
            item.put("name", activities[2 * i]);
            item.put("package", activities[2 * i + 1]);
            mListData.add(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal);
        setTitle("Activity入口");
        x.view().inject(this);

        mListView.setAdapter(new SimpleAdapter(this, mListData,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "package"},
                new int[]{android.R.id.text1, android.R.id.text2}));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ProgressDialog dialog = new ProgressDialog(PortalActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
//                dialog.setMessage("登录中");
////                dialog.setIndeterminateDrawable(new ColorDrawable(Color.TRANSPARENT));
//                dialog.setCancelable(true);
//                dialog.show();
                Intent intent = new Intent();
                intent.setClassName(PortalActivity.this, activities[2 * position + 1]);
                startActivity(intent);
            }
        });
    }

}
