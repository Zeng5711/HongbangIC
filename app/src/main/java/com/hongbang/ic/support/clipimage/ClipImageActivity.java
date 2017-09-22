package com.hongbang.ic.support.clipimage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;

import com.hongbang.ic.R;
import com.hongbang.ic.activity.BaseActivity;
import com.hongbang.ic.util.StringUtils;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 裁剪图片的Activity
 *
 * @author xiechengfa2000@163.com
 * @ClassName: CropImageActivity
 * @Description:
 * @date 2015-5-8 下午3:39:22
 */
public class ClipImageActivity extends BaseActivity {
    public static final String RESULT_SAVE_PATH = "save_path";
    public static final String EXTRA_SOURCE_PATH = "source_path";
    private ClipImageLayout mClipImageLayout = null;

    private String mSavePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_image);
        setTitle(R.string.title_clip_image);
        setCustomTitleButton(getString(R.string.confirm), new OnClickListener() {
            @Override
            public void onClick(View v) {
                progress();
            }
        });

        mClipImageLayout = (ClipImageLayout) findViewById(R.id.clipImageLayout);
        String path = getIntent().getStringExtra(EXTRA_SOURCE_PATH);

        mSavePath = getFilesDir() + File.separator + StringUtils.MD5(UUID.randomUUID().toString()) + ".png";

        ImageOptions options = new ImageOptions.Builder()
                .setSize(1000, 1000)
                .build();
        x.image().loadDrawable(path, options, new Callback.CacheCallback<Drawable>() {
            @Override
            public boolean onCache(Drawable result) {
                return false;
            }

            @Override
            public void onSuccess(Drawable result) {
                mClipImageLayout.setImageDrawable(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    public void progress() {
        showLoadingDialog("处理中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = mClipImageLayout.clip(500);

                saveBitmap(bitmap, mSavePath);

                Intent intent = new Intent();
                intent.putExtra(RESULT_SAVE_PATH, mSavePath);
                setResult(RESULT_OK, intent);
                x.task().post(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoadingDialog();
                        finish();
                    }
                });
            }
        }).start();
    }

    private void saveBitmap(Bitmap bitmap, String path) {
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }

        FileOutputStream fos = null;
        try {
            f.createNewFile();
            fos = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
