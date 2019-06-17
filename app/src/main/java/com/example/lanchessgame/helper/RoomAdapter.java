package com.example.lanchessgame.helper;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lanchessgame.R;
import com.example.lanchessgame.activity.ClientActivity;
import com.example.lanchessgame.dataClass.Room;

import java.util.List;


public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.MyHolder> {
    private List<Room> rooms;
    private IRoomAdapter iRoomAdapter;
    public interface IRoomAdapter{
        void start(Intent intent);
    }
    public static class MyHolder extends RecyclerView.ViewHolder{
        TextView roomName,owner,seat,state;
        Room room;
        public MyHolder(View view){
            super(view);
            roomName = view.findViewById(R.id.roomName);
            owner = view.findViewById(R.id.ownerName);
            seat = view.findViewById(R.id.seat);
            state = view.findViewById(R.id.roomState);
        }
    }

    public RoomAdapter(List<Room> rooms){
        this.rooms = rooms;
    }
    public void setiRoomAdapter(IRoomAdapter iRoomAdapter){
        this.iRoomAdapter = iRoomAdapter;
    }
    @Override
    public int getItemCount() {
        return rooms.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Room room = rooms.get(position);
        holder.room = room;
        holder.roomName.setText(room.Name);
        holder.owner.setText(room.Owner);
        String s = room.players.size()+"/3";
        holder.seat.setText(s);
        if(room.players.size()>=3) holder.state.setText("不可加入");
        else holder.state.setText("可加入");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_item,parent,false);
        final MyHolder holder = new MyHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.room.players.size()<3){
                    Intent intent = new Intent(parent.getContext(),ClientActivity.class);
                    intent.putExtra("Room_data",holder.room);
                    iRoomAdapter.start(intent);
                }else Toast.makeText(parent.getContext(),"房间已满", Toast.LENGTH_SHORT).show();

            }
        });
        return holder;
    }
}
