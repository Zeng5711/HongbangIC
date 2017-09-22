package com.hongbang.ic.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.hongbang.ic.R;
import com.hongbang.ic.api.response.BaseResponse;
import com.hongbang.ic.common.app;
import com.hongbang.ic.model.RepairType;
import com.hongbang.ic.util.AppUtils;
import com.hongbang.ic.util.T;

import org.xutils.common.Callback;
import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class RepairActivity extends BaseActivity {

    private static final int IMAGE_CHOOSE_REQUEST = 1000;

    @ViewInject(R.id.spinner_repair_type)
    private Spinner mSpinner;

    @ViewInject(R.id.btn_choose_image)
    private ImageButton mRepairImageView;

    @ViewInject(R.id.btn_commit)
    private Button mCommitButton;

    @ViewInject(R.id.edit_repair_desc)
    private TextView mContentView;

    private String mRepairImagePath = null;

    private ArrayList<RepairType> mRequestTypeList;

    private boolean isFirstResume = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair);
        setTitle(R.string.title_repair_add);

        setResult(RESULT_CANCELED);

        setCustomTitleButton(getString(R.string.confirm), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commitRepairInfo(v);
            }
        });

        x.view().inject(this);
        mSpinner.setEnabled(false);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (isFirstResume) {
            isFirstResume = false;
            app.data().getRepairTypeList(mRequestTypeCallback);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RepairActivityPermissionsDispatcher.onRequestPermissionsResult(RepairActivity.this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    protected void showNoSdcardPermissions(PermissionRequest request) {
        T.showShort(this, "没有授权应用读写外置存储");
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedForSdcard() {
        T.showShort(this, "已拒绝应用读写外置存储");
    }

    @Event(value = {R.id.btn_choose_image})
    private void onChooseImageBtnClick(View view) {
        RepairActivityPermissionsDispatcher.chooseImageWithCheck(RepairActivity.this);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    protected void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_CHOOSE_REQUEST);
    }

    private void initTypeSpinner() {
        if (mRequestTypeList.size() > 0) {
            String[] types = new String[mRequestTypeList.size()];
            for (int i = 0; i < mRequestTypeList.size(); i++) {
                types[i] = mRequestTypeList.get(i).name;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_repair_type_item, types);
            adapter.setDropDownViewResource(R.layout.spinner_repair_type_dropdown);
            mSpinner.setAdapter(adapter);
            try {
                Field field = Spinner.class.getDeclaredField("mPopup");
                field.setAccessible(true);
                Object popup = field.get(mSpinner);
                Method method = popup.getClass().getSuperclass().getDeclaredMethod("setVerticalOffset", int.class);
                method.setAccessible(true);
                method.invoke(popup, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSpinner.setEnabled(true);
        }
    }

    private Callback.CommonCallback<BaseResponse<ArrayList<RepairType>>> mRequestTypeCallback
            = new Callback.CommonCallback<BaseResponse<ArrayList<RepairType>>>() {
        @Override
        public void onSuccess(BaseResponse<ArrayList<RepairType>> result) {
            if (result.code == 0 && result.getData() != null) {
                mRequestTypeList = result.getData();
                initTypeSpinner();
            } else {
                T.showShort(getBaseContext(), "获取报修类型失败: " + result.msg);
            }
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            ex.printStackTrace();
            T.showShort(getBaseContext(), "获取报修类型失败:" + ex.getMessage());
        }

        @Override
        public void onCancelled(CancelledException cex) {

        }

        @Override
        public void onFinished() {
            dismissLoadingDialog();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            final String filePath = AppUtils.getPath(getBaseContext(), data.getData());
            ImageOptions options = new ImageOptions.Builder()
                    .setSize(DensityUtil.dip2px(90), DensityUtil.dip2px(90))
                    .build();
            x.image().bind(mRepairImageView, filePath, options,
                    new Callback.CommonCallback<Drawable>() {
                        @Override
                        public void onSuccess(Drawable result) {
                            mRepairImagePath = filePath;
                        }

                        @Override
                        public void onError(Throwable ex, boolean isOnCallback) {
                            T.showLong(RepairActivity.this, "无效的图片:"+ex.getMessage());
                        }

                        @Override
                        public void onCancelled(CancelledException cex) {
                        }

                        @Override
                        public void onFinished() {
                        }
                    });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Event(R.id.btn_commit)
    private void commitRepairInfo(View view) {
        String detail = mContentView.getText().toString();
        if (detail.length() == 0) {
            T.showShort(this, "请输入报修详情");
            return;
        }

        String type = getRepairType();
        if (type == null) {
            T.showShort(this, "请选择报修类型");
            return;
        }

        showLoadingDialog("提交中...");
        app.data().commitRepairInfo(type, detail, mRepairImagePath, new Callback.CommonCallback<BaseResponse<JsonObject>>() {
            @Override
            public void onSuccess(BaseResponse<JsonObject> result) {
                if (result.code == 0) {
                    T.showShort(getBaseContext(), "提交成功");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    if (result.msg != null) {
                        T.showShort(getBaseContext(), "提交失败: " + result.msg);
                    } else {
                        T.showShort(getBaseContext(), "提交失败");
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (ex != null) {
                    T.showShort(getBaseContext(), "提交失败: " + ex.getMessage());
                } else {
                    T.showShort(getBaseContext(), "提交失败");
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                dismissLoadingDialog();
            }
        });

    }

    private String getRepairType() {
        int position = mSpinner.getSelectedItemPosition();
        if (position < 0 || position > mRequestTypeList.size() - 1) {
            return null;
        } else {
            return mRequestTypeList.get(position).id;
        }
    }
}
