package com.hongbang.ic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hongbang.ic.R;
import com.hongbang.ic.model.BaseCommunityChooseItem;
import com.hongbang.ic.model.OneCityInfo;
import com.hongbang.ic.model.OneCommunityInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xionghf on 16/4/2.
 */
public class CommunityChooseAdapter extends SectionedBaseAdapter {

    private List<SectionedItemData> mDataList = new ArrayList<>();

    {
        mDataList.add(new SectionedItemData());
    }

    public void setData(List<SectionedItemData> list) {
        mDataList.clear();
        if (list != null && list.size() > 0) {
            mDataList.addAll(list);
        } else {
            mDataList.add(new SectionedItemData());
        }
        notifyDataSetChanged();
    }

    @Override
    public BaseCommunityChooseItem getItem(int section, int position) {
        if (mDataList.size() - 1 < section) {
            return null;
        } else if (mDataList.get(section).itemsInSection.size() - 1 < position) {
            return null;
        } else {
            return mDataList.get(section).itemsInSection.get(position);
        }
    }

    @Override
    public long getItemId(int section, int position) {
        return 0;
    }

    @Override
    public int getSectionCount() {
        return mDataList.size();
    }

    @Override
    public int getCountForSection(int section) {
        if (mDataList.size() == 0) {
            return 0;
        } else {
            return mDataList.get(section).itemsInSection.size();
        }
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup parent) {
        LinearLayout layout;
        Object obj = getItem(section, position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = (LinearLayout) inflater.inflate(R.layout.community_list_item, null);
            holder.nameView = (TextView) layout.findViewById(R.id.text_community_name);
            holder.addrView = (TextView) layout.findViewById(R.id.text_community_addr);
            holder.sepLine = layout.findViewById(R.id.list_item_border_line);
            layout.setTag(holder);
        } else {
            layout = (LinearLayout) convertView;
            holder = (ViewHolder) layout.getTag();
        }
        if (position == getCountForSection(section) - 1 && section != getSectionCount() - 1) {
            holder.sepLine.setVisibility(View.INVISIBLE);
        } else {
            holder.sepLine.setVisibility(View.VISIBLE);
        }

        if (obj instanceof OneCityInfo) {
            OneCityInfo cityInfo = (OneCityInfo) obj;
            holder.nameView.setText(cityInfo.name);
            holder.addrView.setVisibility(View.GONE);
        } else if (obj instanceof OneCommunityInfo) {
            OneCommunityInfo communityInfo = (OneCommunityInfo) obj;
            holder.nameView.setText(communityInfo.name);
            holder.addrView.setText(communityInfo.addr);
            holder.addrView.setVisibility(View.VISIBLE);
        }

        return layout;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        LinearLayout layout = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = (LinearLayout) inflater.inflate(R.layout.community_list_section, null);
        } else {
            layout = (LinearLayout) convertView;
        }
        ((TextView) layout.findViewById(R.id.text_first_letter)).setText(mDataList.get(section).firstLetter);
        return layout;
    }

    public int gotoSection(String firstLetter) {
        if (firstLetter == null || firstLetter.length() == 0) {
            return -1;
        }
        int selection = 0;

        int temp = -1;
        for (SectionedItemData data : mDataList) {
            if (data.firstLetter == null || data.firstLetter.length() == 0) {
                selection += 1;
                continue;
            } else if (data.firstLetter.charAt(0) <= firstLetter.charAt(0)) {
                selection += temp + 1;
                temp = data.itemsInSection.size();
            } else {
                break;
            }
        }
        return Math.max(0, selection);
    }

    class ViewHolder {
        TextView nameView;

        TextView addrView;

        View sepLine;
    }

    public static class SectionedItemData {

        public String firstLetter = null;

        public List<BaseCommunityChooseItem> itemsInSection = new ArrayList<>();

    }
}
