package com.laifeng.sopcastsdk.stream.sender.rtmp.io;

import android.util.Log;

import com.laifeng.sopcastsdk.entity.Frame;
import com.laifeng.sopcastsdk.stream.sender.rtmp.packets.Command;
import com.laifeng.sopcastsdk.stream.sender.rtmp.packets.Chunk;
import com.laifeng.sopcastsdk.stream.sender.sendqueue.ISendQueue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * RTMPConnection's write thread
 * 
 * @author francois, leo
 */
public class WriteThread extends Thread {

    private static final String TAG = "WriteThread";
    private OutputStream out;
    private SessionInfo sessionInfo;
    private OnWriteListener listener;
    private ISendQueue mSendQueue;

    public WriteThread(OutputStream out, SessionInfo sessionInfo) {
        this.out = out;
        this.sessionInfo = sessionInfo;
    }

    public void setWriteListener(OnWriteListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Frame<Chunk> frame = mSendQueue.takeFrame();
                if(frame != null) {
                    Chunk chunk = frame.data;
                    chunk.writeTo(out, sessionInfo);
                    if (chunk instanceof Command) {
                        Command command = (Command) chunk;
                        sessionInfo.addInvokedCommand(command.getTransactionId(), command.getCommandName());
                    }
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if(listener != null) {
                    listener.onDisconnect();
                }
            }
        }
    }

    public void setSendQueue(ISendQueue sendQueue) {
        mSendQueue = sendQueue;
    }

    public void shutdown() {
        listener = null;
        this.interrupt();
    }
}
