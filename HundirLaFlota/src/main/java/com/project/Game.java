package com.project;

public class Game {

    private Game instancia;

    private Player player0;

    private Player player1;

    private Game(Player player0, Player player1) {
        this.player0 = player0;
        this.player1 = player1; 
    }
    
    public Game getInstance(Player player0, Player player1) {
        if (instancia == null) {
            instancia = new Game(player0, player1);
        }
        return instancia;
    }

    public Player getPlayer0() {
        return player0;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer0(Player player0) {
        this.player0 = player0;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public void setInstancia(Game instancia) {
        this.instancia = instancia;
    }

    @Override
    public String toString() {
        return super.toString();
    }


    
}
