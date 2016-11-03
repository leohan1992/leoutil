package xyz.leohan.leoutillib.http;


import okhttp3.Call;

/**
 * Created by Leo on 2016/3/30.
 *
 */
public abstract class OKHttpListener {
//    private LoadingDialog dialog;
//    private Context mContext;
    public OKHttpListener(){
//        this.mContext=context;
    }
    public abstract void onSuccess(String result);

    public abstract void onFailed(Call call, Exception e);
//    public void showDialog(){
//        dialog=new LoadingDialog(mContext);
////        dialog.setMessage("加载中...");
//        dialog.show();
//    }
//    public void dismissDialog(){
//        if (dialog!=null&&dialog.isShowing()){
//            dialog.dismiss();
//        }
//    }
//    public boolean isShowingDialog(){
//        if (dialog!=null&&dialog.isShowing()){
//            return true;
//        }
//        return false;
//    }
   public void update(long bytesRead, long contentLength, boolean done){

   }

}
