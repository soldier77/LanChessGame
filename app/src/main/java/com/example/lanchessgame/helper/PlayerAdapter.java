package com.example.lanchessgame.helper;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lanchessgame.R;
import com.example.lanchessgame.dataClass.Player;

import java.util.List;


public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.MyHolder> {
    List<Player> players;
    public PlayerAdapter(List<Player> players){
        this.players = players;
    }
    public static class MyHolder extends RecyclerView.ViewHolder{
        TextView playerName;
        TextView playerState;
        public MyHolder(View view){
            super(view);
            playerName = view.findViewById(R.id.playerName);
            playerState = view.findViewById(R.id.playerState);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Player player = players.get(position);
        holder.playerName.setText(player.name);
        if(player.State == Player.READY) holder.playerState.setText("准备");
        else holder.playerState.setText("未准备");
    }

    @Override
    public int getItemCount() {
        return players.size();
    }


    @Override
    public MyHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.play_item,parent,false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

}
