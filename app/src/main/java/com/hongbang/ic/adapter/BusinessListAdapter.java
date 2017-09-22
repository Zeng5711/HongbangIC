package com.hongbang.ic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hongbang.ic.R;
import com.hongbang.ic.model.OneBusinessInfo;
import com.hongbang.ic.util.ViewUtils;

import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;

import java.util.List;

/**
 * 社区选择
 * <p/>
 * Created by xionghf on 16/4/9.
 */
public class BusinessListAdapter extends BaseAdapter {

    private List<OneBusinessInfo> mDataList = null;

    private ImageOptions options;

    public BusinessListAdapter() {
        options = new ImageOptions.Builder()
                .setSize(DensityUtil.dip2px(70), DensityUtil.dip2px(70))
                .setLoadingDrawableId(R.drawable.icon_default_business)
                .setFailureDrawableId(R.drawable.icon_default_business)
                .setCrop(true)
                .build();
    }

    public BusinessListAdapter(List<OneBusinessInfo> list) {
        this();
        this.mDataList = list;
    }

    public void setData(List<OneBusinessInfo> list) {
        this.mDataList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mDataList == null) {
            return 0;
        } else {
            return mDataList.size();
        }
    }

    @Override
    public OneBusinessInfo getItem(int position) {
        if (mDataList == null || mDataList.size() - 1 < position || position < 0) {
            return null;
        } else {
            return mDataList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View layout = null;
        ViewHolder holder = null;
        OneBusinessInfo data = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = inflater.inflate(R.layout.business_list_item, null);

            holder = new ViewHolder();
            holder.photoView = (ImageView) layout.findViewById(R.id.image_business_photo);
            holder.nameView = (TextView) layout.findViewById(R.id.text_business_name);
            holder.descView = (TextView) layout.findViewById(R.id.text_business_desc);
            holder.addressView = (TextView) layout.findViewById(R.id.text_business_address);
            holder.sepLine = layout.findViewById(R.id.sep_line);
            layout.setTag(holder);
        } else {
            layout = convertView;
            holder = (ViewHolder) layout.getTag();
        }

        ViewUtils.loadImage(holder.photoView, data.imageUrl, options);
        holder.nameView.setText(data.title);
        holder.descView.setText(data.shortContent);
        holder.addressView.setText(data.address);

        return layout;
    }

    private class ViewHolder {
        ImageView photoView;

        TextView nameView;

        TextView descView;

        TextView addressView;

        View sepLine;
    }

}
