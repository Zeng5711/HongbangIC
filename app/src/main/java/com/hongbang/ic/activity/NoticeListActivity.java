package com.hongbang.ic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hongbang.ic.R;
import com.hongbang.ic.adapter.NoticeListAdapter;
import com.hongbang.ic.api.request.NoticeDetailParams;
import com.hongbang.ic.common.DataManager;
import com.hongbang.ic.api.response.BaseResponse;
import com.hongbang.ic.common.app;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.model.OneNoticeInfo;
import com.hongbang.ic.util.T;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;

public class NoticeListActivity extends BaseActivity implements
        Callback.CommonCallback<BaseResponse<ArrayList<OneNoticeInfo>>>,
        SwipeRefreshLayout.OnRefreshListener {

    public static final String EXTRA_CONTENT_TYPE = "content_type";

    @ViewInject(R.id.list_view)
    private ListView mListView;

    @ViewInject(R.id.swipe_layout)
    private SwipeRefreshLayout mSwipeLayout;

    @ViewInject(R.id.tip_no_data)
    private View mNoDataView;

    private NoticeListAdapter mListAdapter;

    private int mCurrentPage = 1;

    private int mTotalCount = 0;

    private Callback.Cancelable mCurrentRequest = null;

    private int mContentType;

    private ArrayList<OneNoticeInfo> mDataList = new ArrayList<>();

    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mContentType = getIntent().getIntExtra(EXTRA_CONTENT_TYPE, AppConstants.TYPE_COMMUNITY_NOTICE);

        int resId = R.string.title_community_notice;
        switch (mContentType) {
            case AppConstants.TYPE_COMMUNITY_NOTICE:
                resId = R.string.title_community_notice;
                break;
            case AppConstants.TYPE_SERVICE_GUIDE:
                resId = R.string.title_service_guide;
                break;
            case AppConstants.TYPE_COMMUNITY_NEWS:
                resId = R.string.title_community_news;
                break;
        }
        mTitle = getString(resId);
        setTitle(resId);

        x.view().inject(this);

        mListAdapter = new NoticeListAdapter(mDataList);
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

        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.green);
    }

    protected boolean isFirstResume = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstResume) {
            showLoadingDialog();
            requestData(1);
            isFirstResume = false;
        }
    }

    private void requestData(int pageNo) {
        mCurrentRequest = app.data().getNoticeList(mContentType, pageNo, this);
    }

    @Event(value = R.id.list_view, type = ListView.OnItemClickListener.class)
    private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OneNoticeInfo info = mListAdapter.getItem(position);
        if (info != null) {
            NoticeDetailParams params = new NoticeDetailParams(info.id);
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_TITLE, mTitle);
            intent.putExtra(WebViewActivity.EXTRA_URL, params.toString());
            startActivity(intent);
        }
    }

    @Override
    public void onSuccess(BaseResponse<ArrayList<OneNoticeInfo>> result) {
        if (result.code == 0) {
            if (mSwipeLayout.isRefreshing()) {
                mDataList.clear();
            }
            mCurrentPage = result.pageNo;
            mTotalCount = result.total;
            mDataList.addAll(result.getData());
            mListAdapter.notifyDataSetChanged();

            if (mListAdapter.getCount() > 0) {
                mNoDataView.setVisibility(View.GONE);
            } else {
                mNoDataView.setVisibility(View.VISIBLE);
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
