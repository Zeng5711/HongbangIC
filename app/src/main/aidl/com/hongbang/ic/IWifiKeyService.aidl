// IWifiKeyService.aidl
package com.hongbang.ic;

interface IWifiKeyService {
    void reloadKeyData();

    void setShakeEnabled(boolean enabled);

    void setEnabled(boolean enabled);

    void onCommunityChanged();

    void setScreenOnEnabled(boolean enabled);

    void setAutoConn(boolean enabled);
}
