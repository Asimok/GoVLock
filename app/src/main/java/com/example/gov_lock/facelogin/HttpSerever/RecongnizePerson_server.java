package com.example.gov_lock.facelogin.HttpSerever;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.arcsoft.face.FaceFeature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class RecongnizePerson_server extends AppCompatActivity {

    private String BuildingNumber,RoomNumber;
    private Context content;
    private BRDBHelper helper;
    private List<FaceMessage> models;
    public Context getContent() {
        return content;
    }

    public void setContent(Context content) {
        this.content = content;
    }

    public void  startRecongizePerson() {
        final OkHttpClient okHttpClient = new OkHttpClient();
        helper = new BRDBHelper(content);
        models = new ArrayList<>();
        select();
        Map map = new HashMap();
        map.put("BuildingNumber", BuildingNumber);
        map.put("RoomNumber", RoomNumber);
        JSONObject jsonObject = new JSONObject(map);
        String jsonString = jsonObject.toString();
        RequestBody body = RequestBody.create(null, jsonString);//以字符串方式
        final Request request = new Request.Builder()
                .url("http://39.96.68.13:8080/SmartRoom/CompareFaceServlet")
                .post(body)
                .build();
        //异步方法
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(RecongnizePerson_server.this, "连接服务器失败！", Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String res = response.body().string();//获取到传过来的字符串
              //  Log.d("aa", "res    " + res);
                try {
                    JSONArray jsonArray = new JSONArray(res);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = jsonArray.getJSONObject(i);
                        String Name = jsonObj.getString("name");
                        String employeeNumber = jsonObj.getString("employeeNumber");
                        String FaceFeature = jsonObj.getString("PFaceFeature");

                        Log.d("cc", "Name   " + i+ "   "+Name);
                        Log.d("cc", "employeeNumber   " +i+ "   "+ employeeNumber);
                        showRequestResult(FaceFeature, Name,employeeNumber);
                        if (FaceFeature.equals("-1")&&Name.equals("-1")) {
                            break;
                        }
                    }
                    Log.d("cc", "  models  getFaceInfo()   大小   " + models.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void showRequestResult(final String FaceFeature,final String Name,String employeeNumber) {

                if (FaceFeature.equals("-1")&&Name.equals("-1")) {
                    Looper.prepare();
                    Toast.makeText(content, "查询失败！", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
                else {
                    FaceMessage model = new FaceMessage(Base64.getDecoder().decode(FaceFeature.trim().toString()),Name,employeeNumber);
                    model.setFaceFeature(Base64.getDecoder().decode(FaceFeature.trim().toString()));
                    model.setName(Name);
                    models.add(model);
                    Log.d("cc", "models  插入成功 ");
                }
    }

    public  List<FaceMessage> getFaceInfo()
    {
        Log.d("cc", "  models  getFaceInfo()   大小" + models.size());
        for(int i=0;i<models.size();i++)
        {

            Log.d("cc", "models  Name   "+i+"   " +  models.get(i).getName());
        }
        return models;
    }
    //查找
    public  void select() {

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from brinfo", null);
        while (cursor.moveToNext()) {
             BuildingNumber = cursor.getString(cursor.getColumnIndex("BuildingNumber"));
             RoomNumber = cursor.getString(cursor.getColumnIndex("RoomNumber"));
        }
        db.close();

    }

}
