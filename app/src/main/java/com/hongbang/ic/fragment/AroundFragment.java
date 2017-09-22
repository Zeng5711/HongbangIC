package com.hongbang.ic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hongbang.ic.R;
import com.hongbang.ic.activity.WebViewActivity;
import com.hongbang.ic.adapter.BusinessListAdapter;
import com.hongbang.ic.api.request.AroundDetailParams;
import com.hongbang.ic.api.response.BaseResponse;
import com.hongbang.ic.common.app;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.model.OneBusinessInfo;
import com.hongbang.ic.util.T;
import com.hongbang.ic.view.BusinessCategoryView;

import org.xutils.common.Callback;
import org.xutils.common.util.DensityUtil;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;

/**
 * 周边
 * <p/>
 * Created by xionghf on 16/3/27.
 */
public class AroundFragment extends BaseMainFragment implements SwipeRefreshLayout.OnRefreshListener,
        Callback.CommonCallback<BaseResponse<ArrayList<OneBusinessInfo>>> {

    @ViewInject(R.id.business_list_view)
    private ListView mListView;

    @ViewInject(R.id.swipe_layout)
    private SwipeRefreshLayout mSwipeLayout;

    private TextView mNoDataView;

    private BusinessListAdapter mListAdapter;

    private ArrayList<OneBusinessInfo> mDataList = new ArrayList<>();

    private int mCategoryId = AppConstants.BUSINESS_CATEGORY_ID[0];

    private int mCurrentPage = 1;

    private int mTotalCount = 0;

    private BusinessCategoryView mCategoryView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_around, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        x.view().inject(this, this.getView());

        mCategoryView = new BusinessCategoryView(getActivity(),
                AppConstants.AROUND_CATEGORY_ICONS, AppConstants.AROUND_CATEGORY_NAMES,
                AppConstants.AROUND_CATEGORY_ID);
        mCategoryView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.WRAP_CONTENT));
        mCategoryView.setOnCategorySelectedListener(new BusinessCategoryView.OnCategorySelectedListener() {
            @Override
            public void onCategorySelected(int categoryId) {
                if (mCategoryId == categoryId) {
                    return;
                }

                mDataList.clear();
                mListAdapter.notifyDataSetChanged();

                if (mCurrentRequest != null) {
                    mCurrentRequest.cancel();
                }
                mSwipeLayout.setRefreshing(false);
                requestData(categoryId, 1);
            }
        });

        mListView.addHeaderView(mCategoryView);

        mNoDataView = new TextView(getActivity());
        mNoDataView.setLayoutParams(new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.WRAP_CONTENT));
        mNoDataView.setTextSize(15);
        mNoDataView.setText(R.string.tip_no_business);
        mNoDataView.setPadding(0, DensityUtil.dip2px(40), 0, 0);
        mNoDataView.setTextColor(getResources().getColor(R.color.gray));
        mNoDataView.setGravity(Gravity.CENTER_HORIZONTAL);
        mNoDataView.setVisibility(View.GONE);
        mListView.addFooterView(mNoDataView);

        mListView.setFooterDividersEnabled(false);

        mListAdapter = new BusinessListAdapter(mDataList);
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
                            requestData(mCategoryId, mCurrentPage + 1);
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

        requestData(AppConstants.AROUND_CATEGORY_ID[0], 1);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            mDataList.clear();
            mListAdapter.notifyDataSetChanged();
            mCurrentPage = 1;
            requestData(AppConstants.AROUND_CATEGORY_ID[0], mCurrentPage);
        } else if (mCurrentRequest != null) {
            mCurrentRequest.cancel();
            mSwipeLayout.setRefreshing(false);
        }
    }

    private Callback.Cancelable mCurrentRequest = null;

    private void requestData(int categoryId, int pageNo) {
        mCategoryView.setSelection(categoryId);

        mCategoryId = categoryId;
        mCurrentRequest = app.data().getAroundList(mCategoryId, pageNo, this);
    }

    @Event(value = R.id.business_list_view, type = ListView.OnItemClickListener.class)
    private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OneBusinessInfo info = mListAdapter.getItem((int) id);
        if (info != null) {
            AroundDetailParams params = new AroundDetailParams(info.id);
            Intent intent = new Intent(getActivity(), WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.module_around));
            intent.putExtra(WebViewActivity.EXTRA_URL, params.toString());
            startActivity(intent);
        }
    }

    @Override
    public void onRefresh() {
        if (mCurrentRequest != null && !mCurrentRequest.isCancelled()) {
            mCurrentRequest.cancel();
            mCurrentRequest = null;
        }
        requestData(mCategoryId, 1);
    }

    @Override
    public void onSuccess(BaseResponse<ArrayList<OneBusinessInfo>> result) {
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
            T.showShort(getActivity(), result.msg == null ? "数据加载失败" : result.msg);
        }
    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {
        T.showShort(getActivity(), "数据加载失败: " + ex.getMessage());
    }

    @Override
    public void onCancelled(CancelledException cex) {

    }

    @Override
    public void onFinished() {
        mSwipeLayout.setRefreshing(false);
        mCurrentRequest = null;
    }
}
