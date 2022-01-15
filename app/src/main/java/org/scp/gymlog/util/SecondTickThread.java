package org.scp.gymlog.util;

import java.util.function.Supplier;

import lombok.Setter;

@Setter
public class SecondTickThread extends Thread {
    private final Supplier<Boolean> onTickListener;
    protected Function onFinishListener;

    public SecondTickThread(Supplier<Boolean> onTickListener) {
        this.onTickListener = onTickListener;
    }

    @Override
    public final void run() {
        try {
            while(onTickListener.get()) Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Thread interrupted
        } finally {
            if (onFinishListener != null) {
                onFinishListener.call();
            }
        }
    }
}
