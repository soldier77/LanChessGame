package com.example.lanchessgame.dataClass;

import android.os.Parcel;
import android.os.Parcelable;

public class Player implements Parcelable {
    public final static int NOT_READY = 0;
    public final static int READY = 1;
    public String name;
    public int State;
    public Player(String name, int State){
        this.name = name;
        this.State = State;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(State);
    }
    public static final Creator<Player> CREATOR = new Creator<Player>(){
        @Override
        public Player createFromParcel(Parcel source) {
            Player player = new Player(source.readString(),source.readInt());
            return player;
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };
}
