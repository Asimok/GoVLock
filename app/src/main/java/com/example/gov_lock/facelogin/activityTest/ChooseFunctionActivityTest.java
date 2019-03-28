package com.example.gov_lock.facelogin.activityTest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.example.gov_lock.MainActivity;
import com.example.gov_lock.R;
import com.example.gov_lock.facelogin.HttpSerever.BRDBHelper;
import com.example.gov_lock.facelogin.HttpSerever.FaceMessage;
import com.example.gov_lock.facelogin.HttpSerever.RecongnizePerson_server;
import com.example.gov_lock.facelogin.HttpSerever.SignPerson_server;
import com.example.gov_lock.facelogin.HttpSerever.band_server;
import com.example.gov_lock.facelogin.HttpSerever.getUUID;
import com.example.gov_lock.facelogin.HttpSerever.unband_server;
import com.example.gov_lock.facelogin.common.Constants;
import com.example.gov_lock.facelogin.util.ConfigUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static java.lang.Math.abs;
import static java.lang.Math.pow;


public class ChooseFunctionActivityTest extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static String address;
    // 获取到蓝牙适配器
    public static BluetoothAdapter mBluetoothAdapter;
    // 用来保存搜索到的设备信息
    public List<String> bluetoothDevices = new ArrayList<String>();
    // ListView组件
    public ListView lvDevices;
    // ListView的字符串数组适配器
    public ArrayAdapter<String> arrayAdapter;
    // UUID，蓝牙建立链接需要的
    public static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    // 为其链接创建一个名称
    public  final String NAME = "Bluetooth_Socket";
    // 选中发送数据的蓝牙设备，全局变量，否则连接在方法执行完就结束了
    public static BluetoothDevice selectDevice;
    // 获取到选中设备的客户端串口，全局变量，否则连接在方法执行完就结束了
    public static BluetoothSocket clientSocket;
    // 获取到向设备写的输出流，全局变量，否则连接在方法执行完就结束了
    public static OutputStream os;
    public static InputStream is;
    // 服务端利用线程不断接受客户端信息
    //public  AcceptThread thread;
    String message="";
    //定义按钮
    //定义按钮

    public static byte[] LED_STATE_OPEN = {(byte) 0xA0,0x01,0x01, (byte) 0xA2};
    public static byte[] LED_STATE_CLOSE = {(byte) 0xA0,0x01,0x00, (byte) 0xA1};

    public  TextView re_msg;
    public static TextView select;
    private Toast toast = null;
    private String BuildingNumber="",RoomNumber="";
    private BRDBHelper helper;
    private EditText edBuildingNumber,edRoomNumber;
    private TextView bundroom;
    SignPerson_server sign;
    public static List<FaceMessage> faceRegisterInfoList;
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wty_main);
        getPremession();//获取虚拟定位权限
        edBuildingNumber=findViewById(R.id.BuildingNumber);
        edRoomNumber=findViewById(R.id.RoomNumber);
        bundroom=findViewById(R.id.bundroom);
        select=findViewById(R.id.select);
        helper = new BRDBHelper(this);
        select();
        bundroom.setText("已绑定房间："+BuildingNumber+"   "+RoomNumber);
        initView();
        initBluetooth();
        Uri uri = Uri.parse("android.resource://com.example.gov_lock/" + R.raw.welcome);
        Ringtone rt = RingtoneManager.getRingtone(ChooseFunctionActivityTest.this, uri);
        rt.play();


//        final RecongnizePerson_server recp =new RecongnizePerson_server();
//        recp.setContent(ChooseFunctionActivityTest.this);
//        recp.startRecongizePerson();
//
//        sign =new SignPerson_server();
//        sign.setContent(ChooseFunctionActivityTest.this);
//        Timer timer = new Timer();//初始化一个时间
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//
//                faceRegisterInfoList=recp.getFaceInfo();
//                Log.d("cc","RecognizeActivityTest 获取  faceRegisterInfoList()  的大小    "+faceRegisterInfoList.size() );
//                for(int i=0;i<faceRegisterInfoList.size();i++)
//                {
//                    Log.d("cc", "定时获取 faceRegisterInfoList  Name   " +i+"   " + faceRegisterInfoList.get(i).getName());
//                }
//
//            }
//
//        }, 2500);

    }

    private void initBluetooth() {
        // 获取到蓝牙默认的适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 获取到ListView组件
        lvDevices = (ListView) findViewById(R.id.lvDevices);
        // 为listview设置字符换数组适配器
        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                bluetoothDevices);
        // 为listView绑定适配器
        lvDevices.setAdapter(arrayAdapter);
        // 为listView设置item点击事件侦听
        lvDevices.setOnItemClickListener(this);

        // 用Set集合保持已绑定的设备   将绑定的设备添加到Set集合。
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();

        // 因为蓝牙搜索到设备和完成搜索都是通过广播来告诉其他应用的
        // 这里注册找到设备和完成搜索广播
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

    }

    private void initView() {
        //设置视频模式下的人脸优先检测方向
        //默认全方位
        ConfigUtil.setFtOrient(ChooseFunctionActivityTest.this, FaceEngine.ASF_OP_0_HIGHER_EXT);
    }
/**
 * 打开相机，recoface
 *
 * @param view
 */
public void jumpToFaceRecognizeActivity(View view) {
    startActivity(new Intent(this, RecognizeActivityTest.class));
}
    /**
     * 打开相机，人脸注册
     *
     * @param view
     */
    public void jumpToFaceRegisteActivity(View view) {
        startActivity(new Intent(this, RegisterActivityTest.class));
    }

    /**
     * 批量注册和删除功能
     *
     * @param view
     */
    public void jumpToBatchRegisterActivity(View view) {
        startActivity(new Intent(this, FaceManageActivityTest.class));
    }

    /**
     * initenige
     *
     * @param view
     */
    public void activeEngine(final View view) {
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }
        if (view != null) {
            view.setClickable(false);
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                FaceEngine faceEngine = new FaceEngine();
                int activeCode = faceEngine.active(ChooseFunctionActivityTest.this, Constants.APP_ID, Constants.SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            showToast(getString(R.string.active_success));
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            showToast(getString(R.string.already_activated));
                        } else {
                            showToast(getString(R.string.active_failed, activeCode));
                        }

                        if (view != null) {
                            view.setClickable(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                activeEngine(null);
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    private void showToast(String s) {
        if (toast == null) {
            toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast.setText(s);
            toast.show();
        }
    }

    public void bangding(View view) {
        //insert();

        AlertDialog.Builder builder = new AlertDialog.Builder(ChooseFunctionActivityTest.this);
        builder.setIcon(R.drawable.regoface);
        builder.setTitle("请确认您的身份");
        //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
        View view1 = LayoutInflater.from(ChooseFunctionActivityTest.this).inflate(R.layout.dialog, null);
        //    设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view1);

        final EditText username = (EditText) view1.findViewById(R.id.username);
        final EditText password = (EditText) view1.findViewById(R.id.password);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String a = username.getText().toString().trim();
                String b = password.getText().toString().trim();
                //    将输入的用户名和密码打印出来
                //Toast.makeText(ChooseFunctionActivityTest.this, "用户名: " + a + ", 密码: " + b, Toast.LENGTH_SHORT).show();
                band_server band = new band_server();
                band.setContent(ChooseFunctionActivityTest.this);
                band.startband(a, b,edBuildingNumber.getText().toString(),edRoomNumber.getText().toString());
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        if (edBuildingNumber.getText().toString().equals(""))
        {
            Toast.makeText(this, "请输入楼号", Toast.LENGTH_SHORT).show();
        }
        else  if(edRoomNumber.getText().toString().equals(""))
        {
            Toast.makeText(this, "请输入房间号", Toast.LENGTH_SHORT).show();
        }
        else {

            builder.show();
        }

    }

    public void unbangding(View view) {

        //delete();


        AlertDialog.Builder builder = new AlertDialog.Builder(ChooseFunctionActivityTest.this);
        builder.setIcon(R.drawable.regoface);
        builder.setTitle("请确认您的身份");
        //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
        View view1 = LayoutInflater.from(ChooseFunctionActivityTest.this).inflate(R.layout.dialog, null);
        //    设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view1);

        final EditText username = (EditText)view1.findViewById(R.id.username);
        final EditText password = (EditText)view1.findViewById(R.id.password);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String a = username.getText().toString().trim();
                String b = password.getText().toString().trim();
                //    将输入的用户名和密码打印出来
               // Toast.makeText(ChooseFunctionActivityTest.this, "用户名: " + a + ", 密码: " + b, Toast.LENGTH_SHORT).show();
                unband_server uband=new unband_server();
                uband.setContent(ChooseFunctionActivityTest.this);
                uband.startunband(a, b);

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        builder.show();
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
    //搜索蓝牙设备
    public  void onClick_Search(View view) {
        bluetoothDevices.clear();
        // 点击搜索周边设备，如果正在搜索，则暂停搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d("aa", "暂停搜索");
        }

        mBluetoothAdapter.startDiscovery();
        Log.d("aa", "正在扫描...");
    }
    // 注册广播接收者
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public  void onReceive(Context arg0, Intent intent) {
            // 获取到广播的action
            String action = intent.getAction();
            // 判断广播是搜索到设备还是搜索完成
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                // 找到设备后获取其设备
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 判断这个设备是否是之前已经绑定过了，如果是则不需要添加，在程序初始化的时候已经添加了
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 设备没有绑定过，则将其保持到arrayList集合中
                    Log.d("aa", "设备没有绑定过   "+device.getName() + ":"
                            + device.getAddress() + "\n");
                    short rssi = intent.getExtras().getShort(
                            BluetoothDevice.EXTRA_RSSI);
                    int iRssi = abs(rssi);
                    // 将蓝牙信号强度换算为距离
                    double power = (iRssi - 59) / 25.0;
                    String mm = new Formatter().format("%.2f", pow(10, power)).toString();

                    Log.d("aa", "距离    "+mm);


                    bluetoothDevices.add(device.getName() + ":  "
                            + device.getAddress() + "  距离:  "+mm+ "m"+"\n");
                    // 更新字符串数组适配器，将内容显示在listView中

                    arrayAdapter.notifyDataSetChanged();

                }
                else{
                    Log.d("aa", "设备绑定过   "+device.getName() + ":"
                            + device.getAddress() + "\n");
                    short rssi = intent.getExtras().getShort(
                            BluetoothDevice.EXTRA_RSSI);
                    int iRssi = abs(rssi);
                    // 将蓝牙信号强度换算为距离
                    double power = (iRssi - 59) / 25.0;
                    String mm = new Formatter().format("%.2f", pow(10, power)).toString();

                    Log.d("aa", "距离    "+mm);


                    bluetoothDevices.add(device.getName() + ":  "
                            + device.getAddress() + "  距离:  "+mm+ "m   已配对"+"\n");
                    // 更新字符串数组适配器，将内容显示在listView中

                    arrayAdapter.notifyDataSetChanged();
                }
            } else if (action
                    .equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {

                Toast.makeText(ChooseFunctionActivityTest.this, "搜索完成", Toast.LENGTH_SHORT).show();
                Log.d("aa", "搜索完成");
            }

        }
    };
    public  void getPremession() {
        //判断是否有权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("aa", "模糊定位");
//请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                    0x114);
//判断是否需要 向用户解释，为什么要申请该权限
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                Log.d("aa", "判断是否需要 向用户解释，为什么要申请该权限");
            }
        }
    }

    public static void sendOrderopen()
    {
        Log.d("aa","进入   sendOrderopen");
        // 如果选择设备为空则代表还没有选择设备
        if (selectDevice == null) {
            //通过地址获取到该设备
            selectDevice = mBluetoothAdapter.getRemoteDevice(address);
            Log.d("aa","selectDevice1   "+selectDevice);
        }
        // 这里需要try catch一下，以防异常抛出
        try {
            // 判断客户端接口是否为空
            if (clientSocket == null) {

                // 获取到客户端接口
                clientSocket = selectDevice
                        .createRfcommSocketToServiceRecord(MY_UUID);
                // 向服务端发送连接
                clientSocket.connect();
                select.setText("成功连接："+selectDevice);
                Log.d("aa","连接成功");
                ConnectedThread connectedThread = new ConnectedThread(clientSocket);
                connectedThread.start();

                // 获取到输出流，向外写数据
                os = clientSocket.getOutputStream();
                is =clientSocket.getInputStream();
            }
            else
            {
                Log.d("aa","连接蓝牙不成功");
            }

            // 判断是否拿到输出流
            if (os != null) {
                // 需要发送的信息
                Log.d("aa","拿到输出流");
                os.write(LED_STATE_OPEN);

            }
            else
            {
                Log.d("aa","没有  拿到输出流");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // 如果发生异常则告诉用户发送失败
        }
    }
    public  static  void sendOrderclose()
    {
        // 如果选择设备为空则代表还没有选择设备
        if (selectDevice == null) {
            //通过地址获取到该设备
            selectDevice = mBluetoothAdapter.getRemoteDevice(address);
            Log.d("aa","selectDevice1   "+selectDevice);
        }
        // 这里需要try catch一下，以防异常抛出
        try {
            // 判断客户端接口是否为空
            if (clientSocket == null) {

                // 获取到客户端接口
                clientSocket = selectDevice
                        .createRfcommSocketToServiceRecord(MY_UUID);
                // 向服务端发送连接
                clientSocket.connect();
                select.setText("成功连接："+selectDevice);
                Log.d("aa","连接成功");
                ConnectedThread connectedThread = new ConnectedThread(clientSocket);
                connectedThread.start();

                // 获取到输出流，向外写数据
                os = clientSocket.getOutputStream();
                is =clientSocket.getInputStream();
            }

            // 判断是否拿到输出流
            if (os != null) {
                // 需要发送的信息
                Log.d("aa","拿到输出流");
                os.write(LED_STATE_CLOSE);

            }
            else
            {
                Log.d("aa","没有  拿到输出流");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // 如果发生异常则告诉用户发送失败
        }
    }
    private static class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream input = null;
            OutputStream output = null;
            try {
                input = socket.getInputStream();
                output = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.inputStream = input;
            this.outputStream = output;
        }
        public void run() {
            Log.d("aa","进入run" );

            byte[] buff = new byte[8];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buff);
                    Log.e("aa", " bytes 长度       "+bytes);
                    String str = new String(buff, "ISO-8859-1");
                    str = str.substring(0, bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }


        }
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 获取到这个设备的信息
        String s = arrayAdapter.getItem(position);

        // 对其进行分割，获取到这个设备的地址
        address = s.substring((s.indexOf(":")+1),s.indexOf("距")).trim();
        select.setText("选择设备："+address);
        Log.d("aa","地址   "+address);
        // 判断当前是否还是正在搜索周边设备，如果是则暂停搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }
}
