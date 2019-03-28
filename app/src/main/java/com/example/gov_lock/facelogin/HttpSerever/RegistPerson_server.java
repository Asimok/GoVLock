package com.example.gov_lock.facelogin.HttpSerever;

import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistPerson_server extends AppCompatActivity {

    private String FaceFeature, EmployeeNumber;
    private Context content;

    public Context getContent() {
        return content;
    }

    public void setContent(Context content) {
        this.content = content;
    }

    public void startRegistPerson( String FaceFeature1, String EmployeeNumber1) {
        final OkHttpClient okHttpClient = new OkHttpClient();

        EmployeeNumber = EmployeeNumber1;
        FaceFeature = FaceFeature1;
        Map map = new HashMap();
        map.put("EmployeeNumber", EmployeeNumber);
        map.put("FaceFeature", FaceFeature);
        JSONObject jsonObject = new JSONObject(map);
        String jsonString = jsonObject.toString();
        RequestBody body = RequestBody.create(null, jsonString);//以字符串方式
        final Request request = new Request.Builder()
                .url("http://39.96.68.13:8080/SmartRoom/FaceRegisterServlet")
                .post(body)
                .build();
        //异步方法
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(RegistPerson_server.this, "连接服务器失败！", Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String res = response.body().string();//获取到传过来的字符串
                Log.d("aa", "res    " + res);
                try {
                    JSONObject jsonObj = new JSONObject(res);
                    String status = jsonObj.getString("status");
                    Log.d("aa", "res   " + status);
                    showRequestResult(status);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showRequestResult(final String Status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (Status.equals("-1")) {
                    Toast.makeText(content, "写入注册信息失败！", Toast.LENGTH_LONG).show();
                } else if (Status.equals("0")) {

                    Toast.makeText(content, "写入注册信息成功！", Toast.LENGTH_LONG).show();
                }
                else if (Status.equals("-2")) {

                    Toast.makeText(content, "员工号非法！", Toast.LENGTH_LONG).show();
                }
                else if (Status.equals("-3")) {

                    Toast.makeText(content, "您不是该公司员工！", Toast.LENGTH_LONG).show();
                }

            }
        });

    }


}
