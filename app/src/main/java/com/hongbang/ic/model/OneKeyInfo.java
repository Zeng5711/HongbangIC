package com.hongbang.ic.model;

import com.google.gson.annotations.SerializedName;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.util.StringUtils;

import java.util.Date;

/**
 * 一条钥匙信息
 * <p/>
 * Created by xionghf on 16/5/10.
 */
public class OneKeyInfo {
    @SerializedName("id")
    public String id;

    @SerializedName("parkId")
    public String communityId;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("key")
    public String key;

    @SerializedName("type")
    public int type = 0;

    @SerializedName("useDate")
    public String validityPeriod;

    public OneKeyInfo4DB toOneKeyInfo4DB() {
        if (this.key == null || this.key.length() < 230) {
            return null;
        }

        OneKeyInfo4DB info = new OneKeyInfo4DB();
        info.id = this.id;
        info.communityId = this.communityId;
        info.mobile = this.mobile;
        info.type = this.type;
        Date date = StringUtils.string2Date(this.validityPeriod, "yyyy-MM-dd HH:mm");
        if (date != null) {
            info.validityPeriod = date.getTime();
        }

        if ((this.type == AppConstants.KEY_TYPE_NORMAL
                || this.type == AppConstants.KEY_TYPE_ALL
                || this.type == AppConstants.KEY_TYPE_SHARED)
                && info.validityPeriod < System.currentTimeMillis()) {
            return null;
        }
        info.timestamp = System.currentTimeMillis();

        int length = this.key.length();
        if (this.type != AppConstants.KEY_TYPE_SHARED) {
            if (length < 460) {
                return null;
            }

            info.defaultKey = this.key.substring(length - 460, length - 300);
            info.defaultRoll = this.key.substring(length - 300, length - 230);
            info.tempKey = this.key.substring(length - 230, length - 70);
            info.tempRoll = this.key.substring(length - 70);
        } else {
            info.defaultKey = this.key.substring(length - 230, length - 70);
            info.defaultRoll = this.key.substring(length - 70);
        }

        return info;
    }

}
