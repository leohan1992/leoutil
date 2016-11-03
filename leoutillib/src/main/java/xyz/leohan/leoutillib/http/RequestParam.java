package xyz.leohan.leoutillib.http;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Leo on 2016/3/31.
 * OKHttpUtils 参数类
 */
public class RequestParam {
    private HashMap<String, String> stringParam;
    private List<File> fileParam;
    private HashMap<String, String> header;
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");
    private String mUrl;

    public RequestParam(String url) {
        stringParam = new HashMap<>();
        fileParam = new ArrayList<>();
        header = new HashMap<>();
        this.mUrl = url;
    }

    public void addStringParam(String key, String value) {
        stringParam.put(key, value);
    }

    public void addFile(File file) {
        fileParam.add(file);
    }

    public void addHeader(String key, String value) {
        header.put(key, value);
    }

    private RequestBody getRequestBody() {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        if (!stringParam.isEmpty()) {//构造Form部分

            for (Object o : stringParam.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                builder.addFormDataPart(key, val);
            }

        }
        if (!fileParam.isEmpty()) {//构造文件部分
            for (File f : fileParam) {
                builder.addFormDataPart("file",f.getName(),RequestBody.create(MEDIA_TYPE_MARKDOWN, f));
            }
        }
        return builder.build();
    }

    public Request getRequest() {
        Request.Builder builder = new Request.Builder();
        builder.url(mUrl);
        if (!header.isEmpty()) {//构造header部分
            for (Object o : header.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                builder.addHeader(key, val);
            }

        }
        builder.post(getRequestBody());
        return builder.build();
    }
    private RequestBody getFormRequestBody(){
        FormBody.Builder builder=new FormBody.Builder();
        if (!stringParam.isEmpty()) {//构造Form部分

            for (Object o : stringParam.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                builder.add(key, val);
            }

        }
        return builder.build();
    }
    public Request getFormRequest(){
        Request.Builder builder = new Request.Builder();
        builder.url(mUrl);
        if (!header.isEmpty()) {//构造header部分
            for (Object o : header.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                builder.addHeader(key, val);
            }
        }
        builder.post(getFormRequestBody());
        return builder.build();
    }
}
