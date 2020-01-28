package kr.co.signallink.svsv2.utils;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class ToneUtil {

    private static final String TAG = "ToneUtil";

    public static ToneGenerator toneGenerator;

    public static void play(int mediaFileRawId) {
        Log.d(TAG, "playTone");
        try {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            }
            toneGenerator.startTone(mediaFileRawId, 200);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (toneGenerator != null) {
                        Log.d(TAG, "ToneGenerator released");
                        toneGenerator.release();
                        toneGenerator = null;
                    }
                }

            }, 200);
        } catch (Exception e) {
            Log.d(TAG, "Exception while playing sound:" + e);
        }
    }
}
