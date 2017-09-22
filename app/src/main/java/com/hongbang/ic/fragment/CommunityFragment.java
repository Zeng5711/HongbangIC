package com.hongbang.ic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hongbang.ic.R;
import com.hongbang.ic.activity.BaseActivity;
import com.hongbang.ic.activity.NoticeListActivity;
import com.hongbang.ic.activity.RepairHistoryActivity;
import com.hongbang.ic.activity.WebViewActivity;
import com.hongbang.ic.api.request.NoticeDetailParams;
import com.hongbang.ic.api.response.BaseResponse;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.constant.ResponseConstants;
import com.hongbang.ic.keycenter.KeyUtils;
import com.hongbang.ic.model.MainData;
import com.hongbang.ic.model.OneKeyInfo;
import com.hongbang.ic.model.OneKeyInfo4DB;
import com.hongbang.ic.model.OneNoticeInfo;
import com.hongbang.ic.util.StringUtils;
import com.hongbang.ic.util.T;
import com.hongbang.ic.util.ViewUtils;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * 主界面
 * <p/>
 * Created by xionghf on 16/3/27.
 */
public class CommunityFragment extends BaseMainFragment implements Observer {

    private static final int DEFAULT_IMAGE = 0;

    @ViewInject(R.id.community_ad_image)
    private ImageView mADImageView;

    @ViewInject(R.id.text_marquee)
    private TextView mMarqueeTextView;

    @ViewInject(R.id.text_top_notice)
    private TextView mTopNoticeViews;

    @ViewInject(R.id.text_community_name)
    private TextView mCommunityNameView;

    @ViewInject(R.id.text_community_phone)
    private TextView mCommunityPhoneView;

    private boolean shouldUpdate = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_community, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        x.view().inject(this, this.getView());

        UserDefaults.defaults().addObserver(this);

        requestData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden && shouldUpdate) {
            requestData();
        }
    }

    private boolean isFirstShow = true;

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstShow) {
            isFirstShow = false;
            return;
        }
        if (isVisible() && shouldUpdate) {
            requestData();
        } else {
            shouldUpdate = true;
        }
    }

    private void requestData() {
        ((BaseActivity) getActivity()).showLoadingDialog();
        app.data().getMainInfo(new Callback.CommonCallback<BaseResponse<MainData>>() {
            @Override
            public void onSuccess(BaseResponse<MainData> result) {
                shouldUpdate = false;
                if (result != null && result.code == ResponseConstants.RESULT_NORMAL) {
                    MainData data = result.getData();
                    UserDefaults.defaults().setMainData(data);
                    if (data.communityInfo != null) {
                        UserDefaults.defaults().getUserInfo().communityName = data.communityInfo.name;
                        UserDefaults.defaults().saveUserInfo();
                    }
                } else {
                    T.showShort(getActivity(), result != null ? result.msg : "获取数据失败");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
                showData(UserDefaults.defaults().getMainData());
                ((BaseActivity) getActivity()).dismissLoadingDialog();
            }
        });
    }

    private void showData(MainData data) {
        if (data != null) {
            if (data.communityInfo != null) {
                mCommunityNameView.setText(data.communityInfo.name);
                if (data.communityInfo.imageUrlLarge != null) {
                    ViewUtils.loadImage(mADImageView, data.communityInfo.imageUrl, ImageOptions.DEFAULT);
                }
                if (StringUtils.isEmpty(data.communityInfo.telephone)) {
                    mCommunityPhoneView.setVisibility(View.GONE);
                } else {
                    mCommunityPhoneView.setVisibility(View.VISIBLE);
                    mCommunityPhoneView.setText(data.communityInfo.telephone.trim());
                }
            }

            if (data.adv != null) {
                mMarqueeTextView.setText(data.adv.content);
            }

            if (data.notice != null) {
                mTopNoticeViews.setText(data.notice.content);
                mTopNoticeViews.setTag(data.notice);
            }

        }
    }

    @Event(value = {
            R.id.btn_community_notice,
            R.id.btn_service_guide,
            R.id.btn_community_news,
            R.id.btn_asset_repair,
    })
    private void onButtonClick(View view) {
        Intent intent = new Intent(getActivity(), NoticeListActivity.class);
        int type;
        switch (view.getId()) {
            case R.id.btn_community_notice:
                type = AppConstants.TYPE_COMMUNITY_NOTICE;
                intent.putExtra(NoticeListActivity.EXTRA_CONTENT_TYPE, type);
                break;
            case R.id.btn_service_guide:
                type = AppConstants.TYPE_SERVICE_GUIDE;
                intent.putExtra(NoticeListActivity.EXTRA_CONTENT_TYPE, type);
                break;
            case R.id.btn_community_news:
                type = AppConstants.TYPE_COMMUNITY_NEWS;
                intent.putExtra(NoticeListActivity.EXTRA_CONTENT_TYPE, type);
                break;
            case R.id.btn_asset_repair:
                startRepairView();
                return;
        }
        startActivity(intent);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data != null && data instanceof MainData) {
            return;
        }
        mMarqueeTextView.setText("");
        mTopNoticeViews.setText("");
        mTopNoticeViews.setTag(null);
        if (DEFAULT_IMAGE > 0) {
            mADImageView.setImageResource(DEFAULT_IMAGE);
        } else {
            mADImageView.setImageBitmap(null);
        }
        mCommunityNameView.setText(null);
        if (isVisible()) {
            requestData();
        } else {
            shouldUpdate = true;
        }
    }

    @Event(R.id.text_top_notice)
    private void onTopNoticeClick(View view) {
        if (view.getTag() == null || !(view.getTag() instanceof OneNoticeInfo)) {
            return;
        }
        OneNoticeInfo notice = (OneNoticeInfo) view.getTag();
        NoticeDetailParams params = new NoticeDetailParams(notice.id);
        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.title_community_notice));
        intent.putExtra(WebViewActivity.EXTRA_URL, params.toString());
        startActivity(intent);
    }

    private void startRepairView() {
        boolean authed = false;
        List<OneKeyInfo4DB> list2 = KeyUtils.getAll();
        if (list2 != null) {
            for (OneKeyInfo4DB keyInfo : list2) {
                if (keyInfo.type != AppConstants.KEY_TYPE_SHARED) {
                    authed = true;
                    break;
                }
            }
        }
        if (authed) {
            Intent intent = new Intent(getActivity(), RepairHistoryActivity.class);
            startActivity(intent);
            return;
        }
        app.data().getKeyList(new Callback.CommonCallback<BaseResponse<List<OneKeyInfo>>>() {
            @Override
            public void onSuccess(BaseResponse<List<OneKeyInfo>> result) {
                if (result.code == 0) {
                    List<OneKeyInfo> list = result.getData();
                    boolean authed = false;
                    if (list != null) {
                        for (OneKeyInfo keyInfo : list) {
                            if (keyInfo.type != AppConstants.KEY_TYPE_SHARED) {
                                authed = true;
                                break;
                            }
                        }
                    }

                    if (authed) {
                        Intent intent = new Intent(getActivity(), RepairHistoryActivity.class);
                        startActivity(intent);
                    } else {
                        T.showShort(getActivity(), "您的账号暂未在当前小区授权！");
                    }
                } else {
                    T.showShort(getActivity(), "权限检查失败");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                T.showShort(getActivity(), "权限检查失败: " + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                mActivity.dismissLoadingDialog();
            }
        });
    }
}
