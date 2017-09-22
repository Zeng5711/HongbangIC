package com.hongbang.ic.keycenter;

/**
 * 最基本报文
 * <p/>
 * Created by xionghf on 16/5/9.
 */
public class SimplePacket extends AbstractPacket {

    protected byte[] getSocketData() {
        int len;
        if (data == null) {
            len = 0;
        } else {
            len = (byte) data.length;
        }
        length = (byte) (len + 6);
        byte[] result = new byte[len + 10];

        result[0] = head;
        result[1] = cmd;
        result[2] = length;

        if (mac != null) {
            System.arraycopy(mac, 0, result, 3, Math.min(6, mac.length));
        }

        if (len > 0) {
            System.arraycopy(data, 0, result, 9, len);
        }

        int checksum = 0;
        for (int i = 0; i < result.length - 1; i++) {
            checksum += result[i] & 0xff;
            checksum &= 0xff;
        }
        checksum ^= 0xff;
        result[result.length - 1] = (byte) (checksum & 0xff);

        return result;
    }
}
