package com.hongbang.ic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hongbang.ic.R;
import com.hongbang.ic.common.UserDefaults;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class DebugActivity extends BaseActivity {

    @ViewInject(R.id.list_view)
    private ListView listView;

    String[] items4Manager = {"刷卡", "上传白名单", "下载白名单", "上传黑名单", "下载黑名单", "上传刷卡记录", "下载刷卡记录"};

    String[] items = {"刷卡"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        setTitle("调试界面");

        x.view().inject(this);

//        if (UserDefaults.defaults().isPropertyUser()) {
        items = items4Manager;
//        }
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.debug_list_item, items));
    }

    @Event(value = R.id.list_view,
            type = AdapterView.OnItemClickListener.class)
    private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < 7) {
            Intent i = new Intent(this, DebugKeyActivity.class);
            i.putExtra(DebugKeyActivity.EXTRA_TITLE, items[position]);
            i.putExtra(DebugKeyActivity.EXTRA_ACTION, position);
            startActivity(i);
        }
    }
}
