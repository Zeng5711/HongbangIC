package com.hongbang.ic.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hongbang.ic.R;
import com.hongbang.ic.activity.AboutActivity;
import com.hongbang.ic.activity.BaseActivity;
import com.hongbang.ic.activity.ChangePasswordActivity;
import com.hongbang.ic.activity.CommunityChooseActivity;
import com.hongbang.ic.activity.EditNicknameActivity;
import com.hongbang.ic.activity.LoginActivity;
import com.hongbang.ic.activity.SettingsActivity;
import com.hongbang.ic.api.response.BaseResponse;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.keycenter.WifiKeyService;
import com.hongbang.ic.model.MainData;
import com.hongbang.ic.model.UserInfo;
import com.hongbang.ic.support.clipimage.ClipImageActivity;
import com.hongbang.ic.util.AppUtils;
import com.hongbang.ic.util.FileUtils;
import com.hongbang.ic.util.T;
import com.hongbang.ic.util.ViewUtils;
import com.hongbang.ic.view.ChooseImageMenu;
import com.hongbang.ic.view.ShareDialog;

import org.xutils.common.Callback;
import org.xutils.common.util.DensityUtil;
import org.xutils.common.util.FileUtil;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 个人信息界面
 * <p/>
 * Created by xionghf on 16/3/27.
 */
@RuntimePermissions
public class PersonalFragment extends BaseMainFragment implements Observer {

    private static final String TAG = PersonalFragment.class.getName();

    private static final int CHOOSE_FROM_ALBUM = 100001;
    private static final int CHOOSE_FROM_CAMERA = 100002;
    private static final int CROP_RESULT_CODE = 100003;

    private static final int REQUEST_EDIT_NICKNAME = 100010;

    private String mCaptureTempPath = null;

    @ViewInject(R.id.image_head_portrait)
    private ImageView mPortraitView;

    @ViewInject(R.id.text_nickname)
    private TextView mNickNameView;

    @ViewInject(R.id.text_mobile)
    private TextView mMobileView;

    @ViewInject(R.id.text_community_name)
    private TextView mCommunityNameView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UserDefaults.defaults().addObserver(this);
        return inflater.inflate(R.layout.frag_personal, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        x.view().inject(this, this.getView());
        showData();
    }

    @Event(R.id.image_head_portrait)
    private void setHeadPortrait(View view) {
        ChooseImageMenu menu = new ChooseImageMenu(getActivity(), new ChooseImageMenu.OnMenuCallback() {
            @Override
            public void fromCamera() {
                PersonalFragmentPermissionsDispatcher.startCaptureWithCheck(PersonalFragment.this);
            }

            @Override
            public void fromAlbum() {
                PersonalFragmentPermissionsDispatcher.startAlbumWithCheck(PersonalFragment.this);
            }
        });
        menu.showAtLocation(getActivity().findViewById(R.id.container), Gravity.BOTTOM, 0, 0);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    protected void startAlbum() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            startActivityForResult(intent, CHOOSE_FROM_ALBUM);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            try {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, CHOOSE_FROM_ALBUM);
            } catch (Exception e2) {
                e.printStackTrace();
            }
        }
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    protected void startCapture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (getActivity().getExternalCacheDir()!=null) {
            mCaptureTempPath = getActivity().getExternalCacheDir().getPath()+"/capture_image.jpg";
        } else {
            mCaptureTempPath = Environment.getExternalStorageDirectory().getPath() + "/HongbangIC/caches/capture_image.jpg";
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCaptureTempPath)));
        startActivityForResult(intent, CHOOSE_FROM_CAMERA);
    }

    @OnShowRationale({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    protected void showCameraRationale(PermissionRequest request) {
        request.proceed();
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showDeniedForCamera() {
        T.showShort(getActivity(), "已拒绝应用使用相机");
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    protected void showNoSdcardPermissions(PermissionRequest request) {
        T.showShort(getActivity(), "没有授权应用读写外置存储");
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedForSdcard() {
        T.showShort(getActivity(), "已拒绝应用读写外置存储");
    }

    private void startCropImageActivity(String path) {
        Intent intent = new Intent(getActivity(), ClipImageActivity.class);
        intent.putExtra(ClipImageActivity.EXTRA_SOURCE_PATH, path);
        startActivityForResult(intent, CROP_RESULT_CODE);
    }

    private void showRationaleDialog(String messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(getActivity())
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();//请求权限
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case CROP_RESULT_CODE:
                String path = data.getStringExtra(ClipImageActivity.RESULT_SAVE_PATH);
                setUserInfo(path);
                break;
            case CHOOSE_FROM_ALBUM:
                startCropImageActivity(AppUtils.getPath(getActivity(), data.getData()));
                break;
            case CHOOSE_FROM_CAMERA:
                String temp = getActivity().getCacheDir() + "/capture_image.jpg";
                FileUtils.delete(temp);
                FileUtil.copy(mCaptureTempPath, temp);
                FileUtils.delete(mCaptureTempPath);
                startCropImageActivity(temp);
                break;
            case REQUEST_EDIT_NICKNAME:
                mNickNameView.setText(UserDefaults.defaults().getUserInfo().nickname);
                break;
        }
    }

    private void setUserInfo(String image) {
        ((BaseActivity) getActivity()).showLoadingDialog("头像上传中...");
        app.data().setUserInfo(image, null, new Callback.CommonCallback<BaseResponse<UserInfo>>() {
            @Override
            public void onSuccess(BaseResponse<UserInfo> result) {
                if (result.code == 0) {
                    T.showShort(getActivity(), "修改成功");
                    UserInfo data = result.getData();
                    UserInfo userInfo = UserDefaults.defaults().getUserInfo();
                    userInfo.headPortrait = data.headPortrait;
                    UserDefaults.defaults().saveUserInfo();
                    showData();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                T.showShort(getActivity(), ex.getMessage());
                ex.printStackTrace();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                ((BaseActivity) getActivity()).dismissLoadingDialog();
            }
        });
    }

    private void showData() {
        UserInfo userInfo = UserDefaults.defaults().getUserInfo();
        if (userInfo != null) {
            mNickNameView.setText(userInfo.nickname);
            mMobileView.setText(userInfo.mobile);
            mCommunityNameView.setText(userInfo.communityName);
            loadHeadPortrait();
        }
    }

    private void loadHeadPortrait() {
        UserInfo userInfo = UserDefaults.defaults().getUserInfo();
        if (userInfo != null && userInfo.headPortrait != null) {
            ImageOptions options = new ImageOptions.Builder()
                    .setSize(DensityUtil.dip2px(70), DensityUtil.dip2px(70))
                    .setSquare(true)
                    .setRadius(200)
                    .setFailureDrawableId(R.drawable.default_head_portrait)
                    .setLoadingDrawableId(R.drawable.default_head_portrait)
                    .build();
            ViewUtils.loadImage(mPortraitView, userInfo.headPortrait, options);
        }
    }

    @Event(R.id.text_nickname)
    private void changeNickname(View view) {
        startActivityForResult(new Intent(getActivity(), EditNicknameActivity.class), REQUEST_EDIT_NICKNAME);
    }

    @Event(R.id.btn_change_password)
    private void changePassword(View view) {
        startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
    }

    @Event(R.id.btn_share_app)
    private void shareApp(View view) {
        ShareDialog menu = new ShareDialog(getActivity());
        menu.showAtLocation(getActivity().findViewById(R.id.container), Gravity.BOTTOM, 0, 0);
    }

    @Event(R.id.btn_service_hotline)
    private void callServiceHotline(View view) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .content("呼叫客服热线\n" + AppConstants.HOTLINE_NUMBER)
                .positiveText(R.string.confirm)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + AppConstants.HOTLINE_NUMBER));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .negativeText(R.string.cancel);

        MaterialDialog dialog = builder.build();
        dialog.show();
    }

    @Event(R.id.btn_abort_app)
    private void abort(View view) {
        startActivity(new Intent(getActivity(), AboutActivity.class));
    }

    @Event(R.id.btn_settings)
    private void startSettingsView(View view) {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
    }

    @Event(R.id.btn_change_community)
    private void changeCommunity(View view) {
        Intent intent = new Intent(getActivity(), CommunityChooseActivity.class);
        intent.putExtra(CommunityChooseActivity.EXTRA_CHANGE_COMMUNITY, true);
        startActivity(intent);
    }

    @Event(R.id.btn_logout)
    private void logout(View view) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .content("注销登录将删除所有的本地数据，是否确定注销当前账号？")
                .positiveText(R.string.text_logout2)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        UserDefaults.defaults().clear();
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        BaseActivity.finishAll();
                        getActivity().sendBroadcast(new Intent(WifiKeyService.USER_INFO_CHANGED_ACTION));
                    }
                })
                .negativeText(R.string.cancel);

        MaterialDialog dialog = builder.build();
        dialog.show();
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data != null && data instanceof MainData) {
            return;
        }
        showData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PersonalFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}