package com.hongbang.ic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hongbang.ic.keycenter.WifiKeyService;
import com.hongbang.ic.util.AppUtils;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (!AppUtils.isServiceWork(context, WifiKeyService.class.getName())) {
                Intent startServiceIntent = new Intent(context, WifiKeyService.class);
                startServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(startServiceIntent);
            }
        }
    }
}
