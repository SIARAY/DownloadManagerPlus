package ir.siaray.downloadmanagerplussample;

public class Log {
    public static <T> void print(T message) {
        android.util.Log.i("DmpSample", "" + message);
    }
}
