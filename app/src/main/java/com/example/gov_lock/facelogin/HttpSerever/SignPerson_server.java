package com.example.gov_lock.facelogin.HttpSerever;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.gov_lock.R;
import com.example.gov_lock.facelogin.activityTest.ChooseFunctionActivityTest;
import com.example.gov_lock.facelogin.activityTest.RecognizeActivityTest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SignPerson_server extends AppCompatActivity {
    private long lastClickTime = 0;
    private String BuildingNumber,RoomNumber, EmployeeNumber;
    private Context content;
    private BRDBHelper helper;
    public Context getContent() {
        return content;
    }
    private TextToSpeech textToSpeech,texttospeech;
    public void setContent(Context content) {
        this.content = content;
    }

    public void startSignPerson( String EmployeeNumber1) {
        Log.d("aa", "res    " + "进入1");
        // 初始化TextToSpeech对象
        texttospeech = new TextToSpeech(content, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // 如果装载TTS引擎成功
                if (status == TextToSpeech.SUCCESS) {
                    // 设置使用美式英语朗读
                    int result = texttospeech.setLanguage(Locale.US);
                    // 如果不支持所设置的语言
                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                            && result != TextToSpeech.LANG_AVAILABLE) {
                        Log.d("ff", "TTS暂时不支持这种语言的朗读！");
                    }
                }
            }
        });

        final OkHttpClient okHttpClient = new OkHttpClient();
        helper = new BRDBHelper(content);
        select();
        EmployeeNumber = EmployeeNumber1;
        Map map = new HashMap();
        map.put("EmployeeNumber", EmployeeNumber);
        map.put("BuildingNumber", BuildingNumber);
        map.put("RoomNumber", RoomNumber);
        JSONObject jsonObject = new JSONObject(map);
        String jsonString = jsonObject.toString();
        RequestBody body = RequestBody.create(null, jsonString);//以字符串方式
        final Request request = new Request.Builder()
                .url("http://39.96.68.13:8080/SmartRoom/SignServlet")
                .post(body)
                .build();
        //异步方法
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(SignPerson_server.this, "连接服务器失败！", Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String res = response.body().string();//获取到传过来的字符串
                Log.d("aa", "res    " + res);
                try {
                    JSONObject jsonObj = new JSONObject(res);
                    String Status = jsonObj.getString("status");
                 //   Log.d("gg", "res   " + status);
                   // showRequestResult(status);
                    if (Status.equals("-1")) {
                    Looper.prepare();
                        Log.d("aa", "签到失败！请重试   " );
//                        Uri uri = Uri.parse("android.resource://com.example.gov_lock/" + R.raw.signfail);
//                        Ringtone rt = RingtoneManager.getRingtone(content, uri);
//                        rt.play();
                        texttospeech.speak("签到失败！请重试！", TextToSpeech.QUEUE_ADD,
                                null);
                    Toast.makeText(content, "签到失败！请重试！", Toast.LENGTH_LONG).show();
                    Looper.loop();;
                } else if (Status.equals("0")) {
                    Looper.prepare();
                    Toast.makeText(content, "身份已认证，签到成功！", Toast.LENGTH_LONG).show();
//TODO 提示音
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                long now = System.currentTimeMillis();
                                if (now - lastClickTime > 1500) {
                                    lastClickTime = now;
                                    Log.d("aaa", "允许单次触发!!!");
//                                    Uri uri = Uri.parse("android.resource://com.example.gov_lock/" + R.raw.signsucess);
//                                    Ringtone rt = RingtoneManager.getRingtone(content, uri);
//                                    rt.play();
                                    texttospeech.speak("签到成功！", TextToSpeech.QUEUE_ADD,
                                            null);
                                } else Log.d("aaa", "阻止重复触发!!!");
                            }
                        }).start();


                        Log.d("aa", "签到成功   " );
                    Looper.loop();;
                }
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

//                if (Status.equals("-1")) {
//                    Looper.prepare();
//                    Toast.makeText(content, "签到失败！请重试！", Toast.LENGTH_LONG).show();
//                    Looper.loop();;
//                } else if (Status.equals("0")) {
//                    Looper.prepare();
//                    Toast.makeText(content, "签到成功！", Toast.LENGTH_LONG).show();
//                    Looper.loop();;
//                }


            }
        });

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
