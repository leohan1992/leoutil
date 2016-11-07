package xyz.leohan.leoutillib.http;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by Leo on 2016/3/30.
 * OKHttp工具类
 */
public class OKHttpUtils {
    private static OkHttpClient mOkHttpClient ;
    private static Handler mHandler ;

    private OKHttpUtils() {
        mOkHttpClient = new OkHttpClient();
        mHandler = new Handler(Looper.getMainLooper());
    }

    private static OKHttpUtils instance = null;

    public static OKHttpUtils getInstance() {
        if (instance == null) {
            synchronized (OKHttpUtils.class) {
                if (instance == null) {
                    instance = new OKHttpUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 要在Application中调用，
     * @param certificates
     */
    public  void setCertificates(InputStream... certificates)
    {
        try
        {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates)
            {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

                try
                {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e)
                {
                }
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            trustManagerFactory.init(keyStore);
            sslContext.init
                    (
                            null,
                            trustManagerFactory.getTrustManagers(),
                            new SecureRandom()
                    );
            mOkHttpClient =  mOkHttpClient.newBuilder().sslSocketFactory(sslContext.getSocketFactory()).build();

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    /**
     * 普通get方法
     *
     * @param url      地址
     * @param listener 回调接口，UI线程
     */
    public void getAsync(String url, final OKHttpListener listener) {
        final Request request = new Request.Builder().url(url).build();
//        if (!listener.isShowingDialog()) {
//            listener.showDialog();
//        }
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedResultCallBack(call, e, listener);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() == 200) {
                    try {
                        String result = response.body().string();
                        sendSuccessResultCallBack(result, listener);
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendFailedResultCallBack(call, e, listener);
                    }
                } else {
                    sendFailedResultCallBack(call, new Exception("发生错误"), listener);
                }
            }

        });
    }

    /**
     * post方法  application/x-www-form-urlencoded
     *
     * @param param    参数
     * @param listener 监听回调 UI线程
     */
    public void postFormAsync(RequestParam param, final OKHttpListener listener) {

        Call call = mOkHttpClient.newCall(param.getFormRequest());
//        if (!listener.isShowingDialog()){
//            listener.showDialog();}
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                sendFailedResultCallBack(call, e, listener);
            }

            @Override
            public void onResponse(Call call, Response response) {

                if (response.code() == 200) {
                    try {
                        String result = response.body().string();
                        sendSuccessResultCallBack(result, listener);
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendFailedResultCallBack(call, e, listener);
                    }
                } else {
                    sendFailedResultCallBack(call, new Exception("发生错误"), listener);
                }
            }
        });

    }

    /**
     * post方法  multipart/form-data
     *
     * @param param    参数
     * @param listener 回调接口 UI线程
     */
    public void upload(RequestParam param, final OKHttpListener listener) {

        Request request = param.getRequest().newBuilder().post(new ProgressRequestBody(param.getRequest().body(), listener)).build();
        Call call = mOkHttpClient.newCall(request);
//        if (!listener.isShowingDialog()){
//            listener.showDialog();}
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                sendFailedResultCallBack(call, e, listener);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String result = response.body().string();
                    sendSuccessResultCallBack(result, listener);
                } catch (IOException e) {
                    e.printStackTrace();
                    sendFailedResultCallBack(call, e, listener);
                }
            }
        });
    }

    /**
     * 下载方法，没有触发对话框
     *
     * @param url          下载地址
     * @param downloadPath 文件存储路径
     * @param fileName     文件名，可以为空
     * @param listener     下载回调监听
     */
    public void download(String url, String downloadPath, String fileName, final OKHttpListener listener) {
        if (TextUtils.isEmpty(fileName)) {
            fileName = url.substring(url.lastIndexOf("/") + 1);
        }
        if (!downloadPath.endsWith("/")) {
            downloadPath += "/";
        }
        final File file = new File(downloadPath + fileName);

        final Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() == 200) {
                    if (!file.getParentFile().exists()) {
                        boolean state = file.getParentFile().mkdirs();
                        if (!state) {
                            sendFailedResultCallBack(call, new EOFException("无法创建路径"), listener);
                            return;
                        }
                    }
                    InputStream is = response.body().byteStream();
                    FileOutputStream fos = null;
                    long length = response.body().contentLength();
                    long sum = 0;
                    try {
                        fos = new FileOutputStream(file);
                        byte[] b = new byte[1024];
                        while (true) {
                            int len = is.read(b);
                            if (len == -1) {
                                break;
                            }
                            fos.write(b);
                            sum += len;
                            sendProgressCallBack(sum, length, false, listener);

                        }
                        sendProgressCallBack(sum, length, true, listener);
                        sendSuccessResultCallBack(response.message(), listener);
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendFailedResultCallBack(call, e, listener);
                    } finally {
                        try {
                            is.close();
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendFailedResultCallBack(call, e, listener);
                        }

                    }
                } else {
                    try {
                        throw new ConnectException(response.code() + ":" + response.message());
                    } catch (ConnectException e) {
                        e.printStackTrace();
                        sendFailedResultCallBack(call, e, listener);
                    }
                }
            }
        });
    }

    private void sendSuccessResultCallBack(final String result, final OKHttpListener callback) {
        if (callback == null) {
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(result);
//                callback.dismissDialog();
            }
        });
    }

    private void sendFailedResultCallBack(final Call call, final Exception e, final OKHttpListener callback) {
        if (call == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailed(call, e);
//                callback.dismissDialog();
            }
        });
    }

    private void sendProgressCallBack(final long bytesRead, final long contentLength, final boolean done, final OKHttpListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.update(bytesRead, contentLength, done);
            }
        });
    }

    /**
     * 包装的请求体，处理进度
     * User:lizhangqu(513163535@qq.com)
     * Date:2015-09-02
     * Time: 17:15
     */
    public class ProgressRequestBody extends RequestBody {
        //实际的待包装请求体
        private final RequestBody requestBody;
        //进度回调接口
        private final OKHttpListener progressListener;
        //包装完成的BufferedSink
        private BufferedSink bufferedSink;

        /**
         * 构造函数，赋值
         *
         * @param requestBody      待包装的请求体
         * @param progressListener 回调接口
         */
        public ProgressRequestBody(RequestBody requestBody, OKHttpListener progressListener) {
            this.requestBody = requestBody;
            this.progressListener = progressListener;
        }

        /**
         * 重写调用实际的响应体的contentType
         *
         * @return MediaType
         */
        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        /**
         * 重写调用实际的响应体的contentLength
         *
         * @return contentLength
         * @throws IOException 异常
         */
        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        /**
         * 重写进行写入
         *
         * @param sink BufferedSink
         * @throws IOException 异常
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (bufferedSink == null) {
                //包装
                bufferedSink = Okio.buffer(sink(sink));
            }
            //写入
            requestBody.writeTo(bufferedSink);
            //必须调用flush，否则最后一部分数据可能不会被写入
            bufferedSink.flush();

        }

        /**
         * 写入，回调进度接口
         *
         * @param sink Sink
         * @return Sink
         */
        private Sink sink(Sink sink) {
            return new ForwardingSink(sink) {
                //当前写入字节数
                long bytesWritten = 0L;
                //总字节长度，避免多次调用contentLength()方法
                long contentLength = 0L;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        //获得contentLength的值，后续不再调用
                        contentLength = contentLength();
                    }
                    //增加当前写入的字节数
                    bytesWritten += byteCount;
                    //回调
                    sendProgressCallBack(bytesWritten, contentLength, bytesWritten == contentLength, progressListener);
                }
            };
        }
    }
}
