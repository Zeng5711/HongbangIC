package com.hongbang.ic.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hongbang.ic.R;
import com.hongbang.ic.api.response.BaseResponse;
import com.hongbang.ic.common.app;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.model.OneRepairInfo;
import com.hongbang.ic.util.StringUtils;
import com.hongbang.ic.util.T;
import com.hongbang.ic.util.ViewUtils;

import org.xutils.common.Callback;
import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class RepairDetailActivity extends BaseActivity implements Callback.CommonCallback<BaseResponse<OneRepairInfo>> {

    public static final String EXTRA_REPAIR_INFO_ID = "repair_info_id";

    @ViewInject(R.id.corner_process_state)
    private ImageView mProcessStateView;

    @ViewInject(R.id.label_repair_title)
    private TextView mRepairTitleView;

    @ViewInject(R.id.label_repair_time)
    private TextView mRepairTimeView;

    @ViewInject(R.id.label_repair_desc)
    private EditText mRepairDescView;

    @ViewInject(R.id.image_repair_photo)
    private ImageView mRepairPhoto;

    @ViewInject(R.id.label_feedback_time)
    private TextView mFeedbackTimeView;

    @ViewInject(R.id.label_feedback_detail)
    private EditText mFeedbackDatailView;

    private OneRepairInfo mRepairInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair_detail);

        setTitle(R.string.title_repair_detail);

        x.view().inject(this);

        String id = getIntent().getStringExtra(EXTRA_REPAIR_INFO_ID);

        app.data().getRepairDetail(id, this);

    }

    private void loadViewData() {
        mProcessStateView.setVisibility(View.VISIBLE);
        mProcessStateView.setImageResource(AppConstants.PROCESS_STATE_ICON_MAP.get(mRepairInfo.processState));

        mRepairTitleView.setText(mRepairInfo.title);
        if (mRepairInfo.submitTime > 0) {
            mRepairTimeView.setText(StringUtils.formatDate(mRepairInfo.submitTime, AppConstants.DATE_FORMAT_1));
        }
        mRepairDescView.setText(mRepairInfo.content);

        if (mRepairInfo.feedbackTime > 0) {
            mFeedbackTimeView.setText(StringUtils.formatDate(mRepairInfo.feedbackTime, AppConstants.DATE_FORMAT_1));
        }

        mFeedbackDatailView.setText(mRepairInfo.feedback);

        if (mRepairInfo.imageUrl != null) {
            mRepairPhoto.setVisibility(View.VISIBLE);
            ImageOptions options = new ImageOptions.Builder()
                    .setSize(DensityUtil.dip2px(90), DensityUtil.dip2px(90))
                    .build();
            ViewUtils.loadImage(mRepairPhoto, mRepairInfo.imageUrl, options);
        } else {
            mRepairPhoto.setVisibility(View.GONE);
        }

        showLoadingDialog();
    }

    @Override
    public void onSuccess(BaseResponse<OneRepairInfo> result) {
        if (result.code == 0 && result.getData() != null) {
            mRepairInfo = result.getData();
            loadViewData();
        } else {
            T.showShort(this, "获取报修详情失败");
        }
    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {
        T.showShort(this, "获取报修详情失败");
    }

    @Override
    public void onCancelled(CancelledException cex) {

    }

    @Override
    public void onFinished() {
        dismissLoadingDialog();
    }
}
