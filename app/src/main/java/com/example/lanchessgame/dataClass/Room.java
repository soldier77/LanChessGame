package com.example.lanchessgame.dataClass;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Room implements Parcelable {
    public static final int IN_GAME = 1;
    public static final int NO_GAME = 0;

    public String Name;
    public String Owner;
    public String address;
    public int State;
    public List<Player> players;

    public Room(String Name, String Owner, String address, int State, List<Player> players){
        this.Name = Name;
        this.Owner = Owner;
        this.address = address;
        this.State = State;
        this.players = players;
    }
    public void addPlay(Player player){
        players.add(player);
    }
    public void removePlay(String playerName){
        Iterator<Player> iterator = players.iterator();
        while (iterator.hasNext()){
            if(iterator.next().name.equals(playerName)) iterator.remove();
        }
    }
    public void changePlayer(String name, int state){
        Iterator<Player> iterator = players.iterator();
        for(Player player:players){
            if (player.name.equals(name)) player.State = state;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Name);
        dest.writeString(Owner);
        dest.writeString(address);
        dest.writeInt(State);
        dest.writeTypedList(players);
    }
    public static final Creator<Room> CREATOR = new Creator<Room>(){
        @Override
        public Room createFromParcel(Parcel source) {
            String name = source.readString();
            String owner = source.readString();
            String address = source.readString();
            int state = source.readInt();
            List<Player> players = new ArrayList<>();
            source.readTypedList(players,Player.CREATOR);
            return new Room(name,owner,address,state,players);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };
}
