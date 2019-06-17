package com.example.lanchessgame.helper;

import com.example.lanchessgame.dataClass.Room;

import java.net.DatagramPacket;
import java.net.InetAddress;


public class DataPacket {
    public static final String CHECK = "check";
    public static final String RETN = "return";
    public static final String ADD = "addIn";
    public static final String READY = "ready";
    public static final String NOT_READY = "notReady";
    public static final String OFF_ROOM = "offR";
    public static final String OFF_PLAYER = "offP";
    public static final String PLAY_DATA = "data";

    public interface IClassify {
        void signCheck(String[] message);
        void signRetn(String[] message);
        void signAdd(String[] message);
        void signReady(String[] message);
        void signNotReady(String[] message);
        void signOffRoom(String[] message);
        void signOffPlayer(String[] message);
        void signPlayData(String[] message);
    }
    public static DatagramPacket getCheckPacket(String userName, String broadip){
        try {
            InetAddress addr = InetAddress.getByName(broadip);
            byte[] b = (CHECK+"@"+userName).getBytes();
            DatagramPacket packet = new DatagramPacket(b,b.length,addr,5000);
            return packet;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static DatagramPacket getRetnRoomMessagePacket(Room room){
        DatagramPacket packet;
        byte[] bytes;
        StringBuilder strb = new StringBuilder();
        strb.append(RETN+"@"+room.Owner+"@"+room.Name+"@"+room.address+"@"+room.State+"@"+room.players.size());
        for(int i = 0;i<room.players.size();i++){
            strb.append("@"+room.players.get(i).name+"@"+room.players.get(i).State);
        }
        bytes = strb.toString().getBytes();
        try {
            packet = new DatagramPacket(bytes,bytes.length, InetAddress.getByName(room.address),5000);
            return packet;
        }catch (Exception e){
            e.printStackTrace();
        }
       return null;
    }
    private static DatagramPacket simpleCreate(String userName, String broadip, String type){
        try{
            InetAddress addr = InetAddress.getByName(broadip);
            byte[] b = (type+"@"+userName).getBytes();
            DatagramPacket packet = new DatagramPacket(b,b.length,addr,5000);
            return packet;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static DatagramPacket getPlayData(String userName, String broadip, int x, int y){
        try{
            InetAddress addr = InetAddress.getByName(broadip);
            byte[] b = (PLAY_DATA+"@"+userName+"@"+x+"@"+y).getBytes();
            DatagramPacket packet = new DatagramPacket(b,b.length,addr,5000);
            return packet;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static DatagramPacket getAddPacket(String userName, String broadip){
        return simpleCreate(userName,broadip,ADD);
    }
    public static DatagramPacket getReadyPacket(String userName, String broadip){
        return simpleCreate(userName,broadip,READY);
    }
    public static DatagramPacket getNotReadyPacket(String userName, String broadip){
        return simpleCreate(userName,broadip,NOT_READY);
    }
    public static DatagramPacket getOffRoomPacket(String userName, String broadip){
        return simpleCreate(userName,broadip,OFF_ROOM);
    }
    public static DatagramPacket getOffPlayerPacket(String userName, String broadip){
        return simpleCreate(userName,broadip,OFF_PLAYER);
    }
    public static void analysePacket(DatagramPacket packet, IClassify classify){
        String[] message = new String(packet.getData(),0,packet.getLength()).split("@");
        if(message[0].equals(CHECK)) classify.signCheck(message);
        else if(message[0].equals(RETN)) classify.signRetn(message);
        else if(message[0].equals(ADD)) classify.signAdd(message);
        else if(message[0].equals(READY)) classify.signReady(message);
        else if(message[0].equals(NOT_READY)) classify.signNotReady(message);
        else if(message[0].equals(OFF_ROOM)) classify.signOffRoom(message);
        else if(message[0].equals(OFF_PLAYER)) classify.signOffPlayer(message);
        else if(message[0].equals(PLAY_DATA)) classify.signPlayData(message);
    }
}
