package com.hongbang.ic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hongbang.ic.activity.CommunityChooseActivity;
import com.hongbang.ic.activity.LoginActivity;
import com.hongbang.ic.activity.MainActivity;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.model.UserInfo;
import com.hongbang.ic.util.AppUtils;
import com.hongbang.ic.view.PagerDotView;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;

public class GuideActivity extends Activity {

    @ViewInject(R.id.view_pager)
    private ViewPager mViewPager;

    @ViewInject(R.id.pager_dot_view)
    private PagerDotView mDotView;

    private ArrayList<View> viewContainer = new ArrayList<>();

    private int[] mGuideImageId = new int[]{
            R.drawable.guide_image_1, R.drawable.guide_image_2, R.drawable.guide_image_3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        x.view().inject(this);

        mDotView.setCount(mGuideImageId.length);
        mDotView.setCurrentSelected(0);

        for (int i = 0; i < mGuideImageId.length; i++) {
            View layout = LayoutInflater.from(this).inflate(R.layout.layout_guide_image, null);
            ImageView view = (ImageView) layout.findViewById(R.id.image_view);
            view.setImageResource(mGuideImageId[i]);
            view.setOnClickListener(new OnPageClickListener(i));
            viewContainer.add(layout);
        }

        mViewPager.setAdapter(new GuidePagerAdapter());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int pos) {
                mDotView.setCurrentSelected(pos);
            }

        });
    }

    class GuidePagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return viewContainer.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(viewContainer.get(position));
            return viewContainer.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(viewContainer.get(position));
        }
    }

    class OnPageClickListener implements View.OnClickListener {

        private int position;

        OnPageClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (position < mGuideImageId.length - 1) {
                mViewPager.setCurrentItem(position + 1, true);
            } else {
                if (UserDefaults.defaults().getToken() == null ||
                        UserDefaults.defaults().getUserInfo() == null) {
                    startActivity(new Intent(GuideActivity.this, LoginActivity.class));
                } else {
                    UserInfo userInfo = UserDefaults.defaults().getUserInfo();
                    if (userInfo.communityId == null) {
                        startActivity(new Intent(GuideActivity.this, CommunityChooseActivity.class));
                    } else {
                        startActivity(new Intent(GuideActivity.this, MainActivity.class));
                    }
                }
                AppUtils.appFirstStarted(GuideActivity.this);
                GuideActivity.this.finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }
    }
}
