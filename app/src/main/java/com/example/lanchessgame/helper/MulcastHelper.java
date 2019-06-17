package com.example.lanchessgame.helper;

import android.util.Log;

import com.example.lanchessgame.dataClass.Player;
import com.example.lanchessgame.dataClass.Room;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MulcastHelper {
    private static NetworkInterface lanNet;
    public interface Isearch{
        void getRooms(List<Room> rooms);
    }
    public static void SearchRoom(final Isearch isearch){
        final List<Room> rooms = new ArrayList<>();
        final MulticastSocket socket;
        final String ip = "230.0.0.";
        InetAddress next;
        try{
            socket = new MulticastSocket(5000);
            getLanNet();
            if(lanNet!=null) socket.setNetworkInterface(lanNet);
            debugprint("MulcastHelper ", "SearchRoom_MulticastSocket: "+socket.getNetworkInterface().getName());
            for(int i = 0;i<10;i++){
                String addr = ip+i;
                next = InetAddress.getByName(addr);
                socket.joinGroup(next);
            }
            debugprint("MulticastHelper","join_over");
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] b = new byte[1024];
                    DatagramPacket inPacket = new DatagramPacket(b,b.length);
                    try{
                        while(true){
                            socket.receive(inPacket);
                            DataPacket.analysePacket(inPacket, new DataPacket.IClassify() {
                                @Override
                                public void signCheck(String[] message) {

                                }
                                @Override
                                public void signRetn(String[] message) {
                                    debugprint("method__SearchRoom:","find a room");
                                    int j = 0;
                                    for(String str:message){
                                        debugprint("SearchRoom:【"+j+"】 ",str);
                                        j++;
                                    }

                                    int size = Integer.valueOf(message[5]);
                                    List<Player> players = new ArrayList<>();
                                    for(int i = 6;i<message.length;){
                                        int state = Integer.valueOf(message[i+1]);
                                        String name = message[i];
                                        Player player = new Player(name,state);
                                        i += 2;
                                        players.add(player);
                                    }
                                    String owner = message[1];
                                    String roomName = message[2];
                                    String addr = message[3];
                                    int state = Integer.valueOf(message[4]);
                                    Room room = new Room(roomName,owner,addr,state,players);
                                    rooms.add(room);
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

                                }
                            });
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }finally {
                        socket.close();
                    }

                }
            });
            thread.start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        DatagramSocket sender = new DatagramSocket();
                        for(int i = 0;i<10;i++){
                            String addr = ip+i;
                            DatagramPacket outPacket = DataPacket.getCheckPacket("null",addr);
                            sender.send(outPacket);
                        }
                        sender.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                        isearch.getRooms(rooms);
                        thread.interrupt();
                        socket.close();
                        debugprint("Method__SearchRoom","return rooms");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }catch (Exception e){
        e.printStackTrace();
        }
    }


    public static int getNetworkInterfaceId(){
        try{
            if(lanNet != null&&lanNet.isUp()){
                return lanNet.getIndex();
            }

        }catch ( Exception e){
            e.printStackTrace();
        }
        return -1;
    }
    private static NetworkInterface getLanNet(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                    Enumeration<NetworkInterface> nifs;
                    try{
                        nifs = NetworkInterface.getNetworkInterfaces();
                        while(nifs.hasMoreElements()){
                            NetworkInterface nf = nifs.nextElement();
                            if(nf.isUp()){
                                Enumeration<InetAddress> addrs = nf.getInetAddresses();
                                while (addrs.hasMoreElements()){
                                    InetAddress addr = addrs.nextElement();
                                    if(addr.isSiteLocalAddress()){
                                        lanNet = nf;
                                    }
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
        }).start();
        try{
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        return lanNet;
    }
    private static void debugprint(String tag, String str){
        Log.d(tag,str);
    }
}
