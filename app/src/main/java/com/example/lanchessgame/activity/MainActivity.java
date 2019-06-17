package com.example.lanchessgame.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.lanchessgame.R;
import com.example.lanchessgame.dataClass.Player;
import com.example.lanchessgame.dataClass.Room;
import com.example.lanchessgame.helper.MulcastHelper;
import com.example.lanchessgame.helper.RoomAdapter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button setUp,searchRoom;
    private WifiManager.MulticastLock lock;
    private EditText userName;
    private RoomAdapter adapter;
    private ProgressBar load;
    private RecyclerView recyclerView;
    private List<Room> rooms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        adapter.setiRoomAdapter(new RoomAdapter.IRoomAdapter() {
            @Override
            public void start(Intent intent) {
                intent.putExtra("userName",userName.getText().toString());
                intent.putExtra("netId",MulcastHelper.getNetworkInterfaceId());
                startActivity(intent);
                rooms.clear();
                adapter.notifyDataSetChanged();
            }
        });
        searchRoom.setOnClickListener(this);
        setUp.setOnClickListener(this);

        try{
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            lock = wifiManager.createMulticastLock("multicastLock");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void initView(){
        setUp = findViewById(R.id.setupRoom);
        searchRoom = findViewById(R.id.searchRoom);
        userName = findViewById(R.id.UserName);
        int i = (int)(Math.random()*1000);
        userName.setText("play"+i);
        load = findViewById(R.id.loadRoom);
        load.setVisibility(View.INVISIBLE);
        recyclerView = findViewById(R.id.roomDisplay);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        adapter = new RoomAdapter(rooms);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
//        Log.d("lock", "onClick: "+lock.isHeld());
        if(lock.isHeld()) lock.acquire();
        switch(v.getId()){
            case R.id.setupRoom:
                setUpRoom();
                break;
            case R.id.searchRoom:
                searchRoom();
                break;
        }
    }

    private void setUpRoom(){
        setEnable(3500,setUp);
        Log.d("perform SetUp Room:","开始");
        new Thread(new Runnable() {
            @Override
            public void run() {
                MulcastHelper.SearchRoom(new MulcastHelper.Isearch() {
                    @Override
                    public void getRooms(List<Room> rooms) {
                        mainThreadSetUp();
                    }
                });
            }
        }).start();

    }
    private void mainThreadSetUp(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this,RoomActivity.class);
                String ip = getNoManRoom(rooms);
                if(ip!=null&&MulcastHelper.getNetworkInterfaceId() != -1){
                    intent.putExtra("userName",userName.getText().toString());
                    intent.putExtra("address",ip);
                    intent.putExtra("netId",MulcastHelper.getNetworkInterfaceId());
                    Log.d("perform Search Room:","准备启动活动");
                    startActivity(intent);
                }
            }
        });
    }
    private void searchRoom(){
        loadRoom(3500);
        setEnable(3500,searchRoom);
        rooms.clear();
        adapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                MulcastHelper.SearchRoom(new MulcastHelper.Isearch() {
                    @Override
                    public void getRooms(List<Room> rooms) {
                        showSearchResult(rooms);
                    }
                });
            }
        }).start();
    }

    private String getNoManRoom(List<Room> rooms){
        String ip = "230.0.0.";
        if (rooms.size()<1) return ip+"1";
        for(int i = 0;i<rooms.size();i++){
            String str = ip+i;
            Log.d("Search a no man room:","Find ---"+str);
            for(Room room:rooms){
                if(room.address.equals(str)) {
                    str = null;
                    break;
                }
            }
            if (str != null) return str;
        }
        return null;
    }
    private void printInThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                printNetworkInterface();
            }
        }).start();
    }
    private void printNetworkInterface(){
        try{
            Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
            while(nifs.hasMoreElements()){
                NetworkInterface nf = nifs.nextElement();
                if(nf.isUp()){
                    Enumeration<InetAddress> addrs = nf.getInetAddresses();
                    while (addrs.hasMoreElements()){
                        InetAddress addr = addrs.nextElement();
                        if(addr.isSiteLocalAddress()){
                            System.out.println("**********SiteLocal***********");
                            Log.d("    HostAddress",addr.getHostAddress());
                            Log.d("    HostName:",addr.getHostName());
                            Log.d("*       HostAddress",addr.getHostAddress());
                            Log.d("*       HostName:",addr.getHostName());
                            System.out.println("**********SiteLocal***********");
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void showSearchResult(final List<Room> newRooms){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(newRooms.size() != 0){
                    PrintRoom(newRooms);
                    rooms.clear();
                    rooms.addAll(newRooms);
                    adapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(getApplicationContext(),"没有搜索到房间", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void loadRoom(final long time){
        load.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(time);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            load.setVisibility(View.INVISIBLE);
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void setEnable(final long time, final View view){
        view.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(time);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            view.setEnabled(true);
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void PrintRoom(List<Room> rooms){
        for(Room room:rooms){
            Log.d("RoomName:",room.Name);
            Log.d("RoomOwner:",room.Owner);
            Log.d("RoomAddress:",room.address);
            Log.d("RoomPlayer:",""+room.players.size());
            List<Player> players = room.players;
            for (Player player:players){
                Log.d("    PlayerName:",player.name);
                Log.d("    PlayerState:",""+player.State);
            }
        }
    }
}
