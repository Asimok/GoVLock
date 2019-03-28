package com.example.gov_lock.facelogin.HttpSerever;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

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

;

public class unband_server extends AppCompatActivity {

    private String EmployeeNumber, mima;
    private Context content;
    private BRDBHelper helper;
    public Context getContent() {
        return content;
    }

    public void setContent(Context content) {
        this.content = content;
    }

    public void startunband( String EmployeeNumber1, String mima1) {
        final OkHttpClient okHttpClient = new OkHttpClient();
        helper = new BRDBHelper(content);
        EmployeeNumber = EmployeeNumber1;
        mima = mima1;
        Map map = new HashMap();
        map.put("EmployeeNumber", EmployeeNumber);
        map.put("Password", mima);
        JSONObject jsonObject = new JSONObject(map);
        String jsonString = jsonObject.toString();
        RequestBody body = RequestBody.create(null, jsonString);//以字符串方式
        final Request request = new Request.Builder()
                .url("http://39.96.68.13:8080/SmartRoom/LockLoginServlet")
                .post(body)
                .build();
        //异步方法
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(unband_server.this, "连接服务器失败！", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(content, "验证失败！", Toast.LENGTH_LONG).show();
                } else if (Status.equals("0")) {
                    delete();
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
    public void insert() {

        //自定义增加数据
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        // values.put("UUID", getUUID.getUUID32());
        values.put("BuildingNumber", EmployeeNumber);
        values.put("RoomNumber", mima);
        long l = db.insert("brinfo", null, values);

        if (l == -1) {
            Toast.makeText(content, "绑定不成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(content, "绑定成功", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }
    public void delete() {

        SQLiteDatabase db = helper.getWritableDatabase();
        int i = db.delete("brinfo", null, null);
        if (i == 0) {
            Toast.makeText(content, "解除绑定不成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(content, "解除绑定成功" , Toast.LENGTH_SHORT).show();
        }
        db.close();

    }

}
