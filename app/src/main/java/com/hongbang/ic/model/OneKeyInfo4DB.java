package com.hongbang.ic.model;

import com.google.gson.annotations.SerializedName;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * 一条钥匙信息, DB使用
 * <p/>
 * Created by xionghf on 16/5/21.
 */
@Table(name = "user_key_info")
public class OneKeyInfo4DB implements Serializable {

    @Column(name = "_id", isId = true)
    public String id;

    @Column(name = "community_id")
    public String communityId;

    @Column(name = "phone_num")
    public String mobile;

    @Column(name = "private_key")
    public String defaultKey;

    @Column(name = "private_roll")
    public String defaultRoll;

    @Column(name = "temporary_key")
    public String tempKey;

    @Column(name = "temporary_roll")
    public String tempRoll;

    @Column(name = "type")
    public int type;

    @Column(name = "period")
    public long validityPeriod = -1;

    @Column(name = "timestamp")
    public long timestamp;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj != null && obj instanceof OneKeyInfo4DB) {
            OneKeyInfo4DB key = (OneKeyInfo4DB) obj;
            return this.id != null && this.id.equalsIgnoreCase(key.id)
                    || this.type == key.type && this.defaultKey != null && this.defaultKey.equalsIgnoreCase(key.defaultKey);
        } else {
            return false;
        }
    }
}
