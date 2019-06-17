package com.example.lanchessgame.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lanchessgame.R;
import com.example.lanchessgame.dataClass.Player;
import com.example.lanchessgame.dataClass.Room;
import com.example.lanchessgame.helper.DataPacket;
import com.example.lanchessgame.helper.PlayerAdapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class RoomActivity extends AppCompatActivity implements View.OnClickListener{
    private MulticastSocket msocket;
    private RecyclerView playList;
    private int NetId;
    private Room room;
    private EditText RoomName;
    private Button start,leave;
    private PlayerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        RoomName = findViewById(R.id.RoomNameEdit);
        leave = findViewById(R.id.exitRoom);
        start = findViewById(R.id.start);
        leave.setOnClickListener(this);
        start.setOnClickListener(this);
        playList = findViewById(R.id.playerList);
        initRoom();
        roomRunInThread();
    }
    private void initRoom(){
        Intent intent = getIntent();
        List<Player> players = new ArrayList<>();
        String address = intent.getStringExtra("address");
        String Owner = intent.getStringExtra("userName");
        NetId = intent.getIntExtra("netId",-1);
        if(NetId == -1){
            onDestroy();
            Toast.makeText(getApplicationContext(),"房间已关闭，未找到可用局域网", Toast.LENGTH_SHORT).show();
        }
        Player player = new Player(Owner,Player.READY);
        players.add(player);
        room = new Room(RoomName.getText().toString(),Owner,address,Room.NO_GAME,players);
        try {
            msocket = new MulticastSocket(5000);
            msocket.setNetworkInterface(NetworkInterface.getByIndex(NetId));
        }catch (Exception e){
            e.printStackTrace();
        }
        LinearLayoutManager manager = new LinearLayoutManager(this);
        playList.setLayoutManager(manager);
        adapter = new PlayerAdapter(room.players);
        PrintRoom();
        playList.setAdapter(adapter);
    }
    private void roomRunning(){
        try{
            System.out.println("RoomActivityThreadStart");
            final DatagramSocket sender = new DatagramSocket();
            msocket.joinGroup(InetAddress.getByName(room.address));
            byte[] b = new byte[1024];
            DatagramPacket inpacket = new DatagramPacket(b,b.length);
            while (true){
                msocket.receive(inpacket);
                DataPacket.analysePacket(inpacket, new DataPacket.IClassify() {
                    @Override
                    public void signCheck(String[] message) {
                        room.Name = RoomName.getText().toString();
                        DatagramPacket outPacket = DataPacket.getRetnRoomMessagePacket(room);
                        try{
                            Log.d("Send Packet","ReturnRoomPacket");
                            sender.send(outPacket);
                        }catch (IOException e){
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void signRetn(String[] message) {

                    }

                    @Override
                    public void signAdd(String[] message) {
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

                    }

                    @Override
                    public void signOffPlayer(String[] message) {
                        room.removePlay(message[1]);
                        adapterChange();
                    }

                    @Override
                    public void signPlayData(String[] message) {
                        Intent intent = new Intent(RoomActivity.this,GameActivity.class);
                        intent.putExtra("Room_data",room);
                        intent.putExtra("userName",room.Owner);
                        intent.putExtra("netId",NetId);
                        startActivity(intent);
                        msocket.close();
                        finish();
                    }
                });

            }

        }catch(Exception e){
            System.out.println("RoomActivityThreadEnd");
            e.printStackTrace();
        }

    }
    private void roomRunInThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                roomRunning();
            }
        }).start();
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
        try{
            NetworkInterface inter = msocket.getNetworkInterface();
            Enumeration<InetAddress> addrs = inter.getInetAddresses();
            while (addrs.hasMoreElements()){
                InetAddress addr = addrs.nextElement();
                Log.d("MulticastSocket_Address:",addr.getHostAddress());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
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
            case R.id.start:
                StartGame();
                break;
            case R.id.exitRoom:
                ExitRoom();
                break;
            default:
        }
    }
    private void StartGame(){

        if(room.players.size() >= 2){
            boolean flag = true;
            for(Player player:room.players){
                if(player.State == Player.NOT_READY){
                    flag = false;
                    break;
                }
            }
            if (flag) {
                //TODO:startGame
                Toast.makeText(getApplicationContext(),"开始游戏", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DatagramPacket outPacket = DataPacket.getPlayData(room.Owner,room.address,-1,-1);
                        try{
                            DatagramSocket sender = new DatagramSocket();
                            sender.send(outPacket);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
                Log.d("StartGame","start");
                return;
            }
        }
        Toast.makeText(getApplicationContext(),"请等待人满或全员准备", Toast.LENGTH_SHORT).show();
    }
    private void ExitRoom(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket outPacket = DataPacket.getOffRoomPacket(room.Owner,room.address);
                try{
                    DatagramSocket sender = new DatagramSocket();
                    sender.send(outPacket);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
        try{
            onBackPressed();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) RoomName.requestFocus();
    }

    @Override
    public void onBackPressed() {
        msocket.close();
        super.onBackPressed();
    }
}
