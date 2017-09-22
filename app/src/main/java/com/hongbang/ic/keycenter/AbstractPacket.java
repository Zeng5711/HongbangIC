package com.hongbang.ic.keycenter;

import com.hongbang.ic.keycenter.tasks.OnTaskTracker;
import com.hongbang.ic.util.Logger;
import com.hongbang.ic.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 报文基类
 * <p/>
 * Created by xionghf on 16/5/9.
 */
public abstract class AbstractPacket {

    public final byte head = 0x55;

    public byte cmd;

    public byte length;

    public static byte[] mac;

    public byte[] data;

    protected abstract byte[] getSocketData();

    public boolean send(Socket socket, OnTaskTracker tracker) {
        if (socket.isClosed()) {
            return true;
        }

        try {
            OutputStream output = socket.getOutputStream();
            byte[] data = getSocketData();

            String log = StringUtils.bytes2Hex2(data, 0, data.length);
            Logger.debug("KeyCenter", "send: " + log);
            if (tracker != null) {
                tracker.onSendData(log);
            }

            output.write(data);
            output.flush();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}