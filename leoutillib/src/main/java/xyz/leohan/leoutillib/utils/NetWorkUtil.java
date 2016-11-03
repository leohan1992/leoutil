package xyz.leohan.leoutillib.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Leo on 2016/5/10.
 */
public class NetWorkUtil {
    /**
     * wifi网络
     */
    public static final int NETWORK_TYPE_WIFI=1;
    /**
     * 无网络
     */
    public static final int NETWORK_TYPE_NONE=0;
    /**
     * 手机网络
     */
    public static final int NETWORK_TYPE_PHONE=2;

    /**
     * 判断网络是否可用
     * @param context 上下文
     * @return 网络状态是否可用
     */
    public  static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断手机网络状态
     * @param context 上下文
     * @return  NETWORK_TYPE_NONE=0表示没有网络； NETWORK_TYPE_WIFI=1表示wifi；NETWORK_TYPE_PHONE=2;表示手机数据网络
     */
    public static int getNetworkType(Context context) {//0无网 1wifi 2流量
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return NETWORK_TYPE_WIFI;
        }
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return NETWORK_TYPE_PHONE;
        }
        return NETWORK_TYPE_NONE;
    }
}
