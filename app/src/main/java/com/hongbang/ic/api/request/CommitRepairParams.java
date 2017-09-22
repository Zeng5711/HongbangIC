package com.hongbang.ic.api.request;

import com.hongbang.ic.constant.HttpConstants;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;

import java.io.File;

/**
 * 提交新报修
 * <p>
 * Created by xionghf on 16/5/1.
 */
@HttpRequest(
        path = HttpConstants.REPAIR_COMMIT_PATH,
        builder = SessionParamsBuilder.class)
public class CommitRepairParams extends RequestParams {

    public String type;

    public String content;

    public File file;

    public CommitRepairParams() {
        this.setMultipart(true);
    }

}