package helper.scxml.scxml2.t1_stopWatch;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

public class StopWatchEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    //时分秒
    private int hr;
    private int min;
    private int sec;
    //100毫秒
    private int fract;

    private transient Timer timer;

    /**
     * 重置当前状态机
     */
    public synchronized void reset() {
        hr = min = sec = fract = 0;
    }

    /**
     * 运行秒表
     */
    public synchronized void run() {
        if (timer == null) {
            timer = new Timer(true);
            //使用timer来定时执行，秒表计数，每100毫秒，执行一次increment方法
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    increment();
                }
            }, 100, 100);
        }
    }

    /**
     * 停止秒表
     */
    public synchronized void stop() {
        timer.cancel();
        timer = null;
    }

    /**
     * 得到当前秒表的时间
     * @return
     */
    public synchronized String getDisplay() {
        return String.format("%d:%02d:%02d,%d", hr, min, sec, fract);
    }

    /**
     * 自增方法
     */
    private synchronized void increment() {
        if (fract < 9) {
            fract++;
        } else {
            fract = 0;
            if (sec < 59) {
                sec++;
            } else {
                sec = 0;
                if (min < 59) {
                    min++;
                } else {
                    min = 0;
                    hr++;
                }
            }
        }
    }
}