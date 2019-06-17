package com.example.lanchessgame.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lanchessgame.R;
import com.example.lanchessgame.dataClass.Room;
import com.example.lanchessgame.helper.DataPacket;
import com.example.lanchessgame.helper.PlayManager;
import com.example.lanchessgame.myView.BroadView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class GameActivity extends AppCompatActivity implements View.OnClickListener{


    private int victory;
    String userName;
    Room room;
    int netId;
    MulticastSocket mSocket;
    PlayManager manager;
    AlertDialog.Builder dialog;
    TextView nowPlayer;
    Button confirm;
    View color;
    BroadView broadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        init();
        Log.d("GameActivity:","startGame");
    }

    public void init(){
        initRoom();
        initView();
        roomRunInThread();
    }
    public void initRoom(){
        Intent intent = getIntent();
        room = intent.getParcelableExtra("Room_data");
        netId = intent.getIntExtra("netId",-1);
        userName = intent.getStringExtra("userName");
        manager = new PlayManager();
        int[] colors = {Color.WHITE, Color.BLACK, Color.BLUE, Color.GREEN};
        victory = 7-room.players.size();
        for(int i = 0;i<room.players.size();i++){
            manager.addPlayer(room.players.get(i).name,colors[i]);
        }
        manager.startGame();
        try{
            mSocket = new MulticastSocket(5000);
            if(netId == -1){
                Toast.makeText(GameActivity.this,"地址不正确", Toast.LENGTH_SHORT).show();
                finish();
            }
            mSocket.setNetworkInterface(NetworkInterface.getByIndex(netId));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void initView(){
        nowPlayer = findViewById(R.id.nowPlayer);
        nowPlayer.setText(manager.getName());

        confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(this);

        broadView = findViewById(R.id.mBroadView);
        broadView.setBroad(15,15);

        color = findViewById(R.id.playerColor);
        color.setBackgroundColor(manager.getColor());

        if(manager.getName().equals(userName)){
            nowPlayer.setText("轮到你了");
            broadView.setChessPaint(manager.getColor());
        }else {
            confirm.setEnabled(false);
            broadView.setTouchable(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.confirm:
                confirmChess();
                break;
            default:
                break;
        }
    }

    private void confirmChess(){
        if(!broadView.hasPut()){
            Toast.makeText(GameActivity.this,"请先落子", Toast.LENGTH_SHORT).show();
            return;
        }
        final BroadView.Chess chess = broadView.getNowChess();
        confirm.setEnabled(false);
        broadView.setTouchable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    DatagramSocket sender = new DatagramSocket();
                    Log.d("GameActivity:","send a PlayData");
                    DatagramPacket outPacket = DataPacket.getPlayData(userName,room.address,chess.x,chess.y);
                    sender.send(outPacket);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void showVictory(){
        dialog = new AlertDialog.Builder(GameActivity.this);
        dialog.setTitle("游戏结束");
        dialog.setMessage(manager.getName()+"获胜！！");
        dialog.setCancelable(false);
        dialog.setPositiveButton("离开游戏", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackPressed();
            }
        });
        dialog.setNegativeButton("留在本页面", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }
    private void roomRunning(){
        try{
            Log.d("GameActivity:","roomRunning");
            final DatagramSocket sender = new DatagramSocket();
            mSocket.joinGroup(InetAddress.getByName(room.address));
            byte[] b = new byte[1024];
            DatagramPacket inpacket = new DatagramPacket(b,b.length);
            while (true){
                mSocket.receive(inpacket);
                DataPacket.analysePacket(inpacket, new DataPacket.IClassify() {
                    @Override
                    public void signCheck(String[] message) {
                        DatagramPacket outPacket = DataPacket.getRetnRoomMessagePacket(room);
                        try{
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
                    }

                    @Override
                    public void signReady(String[] message) {
                    }

                    @Override
                    public void signNotReady(String[] message) {

                    }

                    @Override
                    public void signOffRoom(String[] message) {

                    }

                    @Override
                    public void signOffPlayer(String[] message) {
                    }

                    @Override
                    public void signPlayData(String[] message) {
                        Log.d("GameActivity:","receive a PlayData");
                        int x = Integer.valueOf(message[2]);
                        int y = Integer.valueOf(message[3]);
                        Log.d("otherPutChess","X:"+x+"Y:"+y);
                        otherPutChess(x,y);

                    }
                });

            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }
    private void otherPutChess(final int x,final int y){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int value = broadView.putChess(x,y,manager.getColor());
                Log.d("数值----",""+value);
                if (value>=victory){
                    showVictory();
                    Log.d("*****游戏","胜利****");
                    confirm.setEnabled(false);
                    broadView.setTouchable(false);
                    broadView.invalidate();
                    nowPlayer.setText(manager.getName()+"胜利！！");
                    return;
                }
                broadView.invalidate();
                manager.next();
                nowPlayer.setText(manager.getName());
                color.setBackgroundColor(manager.getColor());
                broadView.initNowChess();
                broadView.setChessPaint(manager.getColor());
                if(userName.equals(manager.getName())){
                    turnToUs();
                }
            }
        });
    }
    private void turnToUs(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                confirm.setEnabled(true);
                broadView.setTouchable(true);
                nowPlayer.setText("轮到你了");
            }
        });
    }
    private void roomRunInThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                roomRunning();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        mSocket.close();
        super.onBackPressed();
    }
}
