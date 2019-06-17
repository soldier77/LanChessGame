package com.example.lanchessgame.helper;


public class PlayManager {
    private static final int MAX_COUNT = 10;
    private String[] playerNames;
    private int[] playerColor;
    private int size;
    private int point;
    public PlayManager(){
        size = 0;
        playerNames = new String[MAX_COUNT];
        playerColor = new int[MAX_COUNT];
    }
    public void addPlayer(String player, int color){
        playerNames[size] = player;
        playerColor[size] = color;
        size++;
    }
    public void startGame(){
        point = 0;
    }
    public void next(){
        point++;
        point = point%size;
    }
    public String getName(){
        return playerNames[point];
    }
    public int getColor(){
        return playerColor[point];
    }
}
