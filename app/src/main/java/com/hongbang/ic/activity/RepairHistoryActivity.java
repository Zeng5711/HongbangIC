package com.hongbang.ic.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hongbang.ic.R;
import com.hongbang.ic.adapter.RepairListAdapter;
import com.hongbang.ic.api.response.BaseResponse;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.model.OneRepairInfo;
import com.hongbang.ic.util.T;

import org.xutils.common.Callback;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;

public class RepairHistoryActivity extends BaseActivity implements
        Callback.CommonCallback<BaseResponse<ArrayList<OneRepairInfo>>>,
        SwipeRefreshLayout.OnRefreshListener {

    public static final int REQUEST_COMMIT_REPAIR = 10001;

    @ViewInject(R.id.list_view)
    private ListView mListView;

    @ViewInject(R.id.swipe_layout)
    private SwipeRefreshLayout mSwipeLayout;

    private RepairListAdapter mListAdapter;

    private int mCurrentPage = 1;

    private int mTotalCount = 0;

    private Callback.Cancelable mCurrentRequest = null;

    private ArrayList<OneRepairInfo> mDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setTitle(R.string.title_repair_history);
        if (!UserDefaults.defaults().isPropertyUser()) {
            setCustomTitleButton("报修", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(
                            new Intent(RepairHistoryActivity.this, RepairActivity.class),
                            REQUEST_COMMIT_REPAIR);
                }
            });
        }

        x.view().inject(this);

        mListAdapter = new RepairListAdapter(mDataList);
        mListView.setAdapter(mListAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (mSwipeLayout.isRefreshing()) {
                    return;
                }
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
                        if (mTotalCount > mDataList.size()) {
                            requestData(mCurrentPage + 1);
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        mSwipeLayout.setColorSchemeResources(R.color.green);
        mSwipeLayout.setOnRefreshListener(this);
    }

    protected boolean shouldUpdate = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldUpdate) {
            showLoadingDialog();
            mDataList.clear();
            mListAdapter.notifyDataSetChanged();
            requestData(1);
            shouldUpdate = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_COMMIT_REPAIR) {
            if (resultCode == Activity.RESULT_OK) {
                shouldUpdate = true;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestData(int pageNo) {
        mCurrentRequest = app.data().getRepairList(pageNo, this);
    }

    @Event(value = R.id.list_view, type = ListView.OnItemClickListener.class)
    private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OneRepairInfo info = mListAdapter.getItem(position);
        if (info != null) {
            Intent intent = new Intent(RepairHistoryActivity.this, RepairDetailActivity.class);
            intent.putExtra(RepairDetailActivity.EXTRA_REPAIR_INFO_ID, info.id);
            startActivity(intent);
        }
    }

    @Override
    public void onSuccess(BaseResponse<ArrayList<OneRepairInfo>> result) {
        if (result.code == 0) {
            if (mSwipeLayout.isRefreshing()) {
                mDataList.clear();
            }
            mCurrentPage = result.pageNo;
            mTotalCount = result.total;
            mDataList.addAll(result.getData());
            mListAdapter.notifyDataSetChanged();
            if (mCurrentPage == 1 && mTotalCount > 0) {
                mListView.setSelection(0);
            }
        } else {
            T.showShort(getBaseContext(), result.msg == null ? "数据加载失败" : result.msg);
        }
    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {
        T.showShort(getBaseContext(), "数据加载失败: " + ex.getMessage());
    }

    @Override
    public void onCancelled(CancelledException cex) {

    }

    @Override
    public void onFinished() {
        dismissLoadingDialog();
        mSwipeLayout.setRefreshing(false);
        mCurrentRequest = null;
    }

    @Override
    public void onRefresh() {
        if (mCurrentRequest != null && !mCurrentRequest.isCancelled()) {
            mCurrentRequest.cancel();
            mCurrentRequest = null;
        }
        requestData(1);
    }

}
