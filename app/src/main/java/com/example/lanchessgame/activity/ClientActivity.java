package com.example.lanchessgame.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lanchessgame.R;
import com.example.lanchessgame.dataClass.Player;
import com.example.lanchessgame.dataClass.Room;
import com.example.lanchessgame.helper.DataPacket;
import com.example.lanchessgame.helper.PlayerAdapter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.List;

public class ClientActivity extends AppCompatActivity implements View.OnClickListener{

    private MulticastSocket msocket;
    private TextView roomName;
    private Button ready,leave;
    private RecyclerView recyclerView;
    private PlayerAdapter adapter;
    private Room room;
    private int NetId;
    private String userName;
    private int userState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        init();
        roomRunInThread();
    }


    private void init(){
        initRoom();
        initView();
        initConfig();
    }
    private void initRoom(){
        Intent intent = getIntent();
        room = intent.getParcelableExtra("Room_data");
        userName = intent.getStringExtra("userName");
        NetId = intent.getIntExtra("netId",-1);
        if(NetId == -1){
            Toast.makeText(this,"未找到可用局域网", Toast.LENGTH_SHORT).show();
            onDestroy();
        }
        userState = Player.NOT_READY;
        room.players.add(new Player(userName,userState));
        PrintRoom();
    }
    private void initView(){
        recyclerView = findViewById(R.id.playerList2);
        ready = findViewById(R.id.readyGame);
        leave = findViewById(R.id.leaveRoom);
        roomName = findViewById(R.id.RoomName);
        roomName.setText(room.Name);
        ready.setOnClickListener(this);
        leave.setOnClickListener(this);

    }
    private void initConfig(){
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        adapter = new PlayerAdapter(room.players);
        recyclerView.setAdapter(adapter);
        try{
            msocket = new MulticastSocket(5000);
            msocket.setNetworkInterface(NetworkInterface.getByIndex(NetId));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        DatagramSocket sender = new DatagramSocket();
                        DatagramPacket outPacket = DataPacket.getAddPacket(userName,room.address);
                        sender.send(outPacket);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void roomRunning(){
        try{
            Log.d("ClientActivity","roomRunning");
            msocket.joinGroup(InetAddress.getByName(room.address));
            byte[] b = new byte[1024];
            final DatagramPacket inpacket = new DatagramPacket(b,b.length);
            while (true){
                msocket.receive(inpacket);
                DataPacket.analysePacket(inpacket, new DataPacket.IClassify() {
                    @Override
                    public void signCheck(String[] message) {

                    }

                    @Override
                    public void signRetn(String[] message) {

                    }

                    @Override
                    public void signAdd(String[] message) {
                        if(message[1].equals(userName)) return;
                        Player player = new Player(message[1],Player.NOT_READY);
                        room.addPlay(player);
                        adapterChange();
                    }

                    @Override
                    public void signReady(String[] message) {
                        room.changePlayer(message[1],Player.READY);
                        adapterChange();
                    }

                    @Override
                    public void signNotReady(String[] message) {
                        room.changePlayer(message[1],Player.NOT_READY);
                        adapterChange();
                    }

                    @Override
                    public void signOffRoom(String[] message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"房间已关闭", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                        });
                    }

                    @Override
                    public void signOffPlayer(String[] message) {
                        room.removePlay(message[1]);
                        adapterChange();
                    }

                    @Override
                    public void signPlayData(String[] message) {
                        Intent intent = new Intent(ClientActivity.this,GameActivity.class);
                        intent.putExtra("Room_data",room);
                        intent.putExtra("userName",userName);
                        intent.putExtra("netId",NetId);
                        startActivity(intent);
                        Log.d("ClientActivity", "signPlayData: startActivity");
                        msocket.close();
                        finish();
                    }
                });

            }

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("ClientActivityThreadEnd");
        }

    }
    private void roomRunInThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                roomRunning();
            }
        }) .start();
    }
    private void adapterChange(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void PrintRoom(){
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.readyGame:
                readyGame();
                break;
            case R.id.leaveRoom:
                leaveRoom();
                break;
            default:
        }
    }
    private void readyGame(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramPacket outPacket = null;
                    if(userState == Player.NOT_READY){
                        userState = Player.READY;
                        outPacket = DataPacket.getReadyPacket(userName,room.address);
                        ready.post(new Runnable() {
                            @Override
                            public void run() {
                                ready.setText("取消");
                            }
                        });
                    }else if(userState == Player.READY){
                        userState = Player.NOT_READY;
                        outPacket = DataPacket.getNotReadyPacket(userName,room.address);
                        ready.post(new Runnable() {
                            @Override
                            public void run() {
                                ready.setText("准备");
                            }
                        });
                    }else Log.d("Player_State","state value is irregular");
                    DatagramSocket sender = new DatagramSocket();
                    if(outPacket != null) {
                        Log.d("perform readyGame :","OutPacket :");
                        Log.d("address:",""+outPacket.getAddress());
                        Log.d("value:",new String(outPacket.getData(),0,outPacket.getLength()));
                        sender.send(outPacket);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void leaveRoom(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket outPacket = DataPacket.getOffPlayerPacket(userName,room.address);
                try{
                    DatagramSocket sender = new DatagramSocket();
                    sender.send(outPacket);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
        onBackPressed();
    }
    @Override
    protected void onDestroy() {
        msocket.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        msocket.close();
        super.onBackPressed();
    }
}
