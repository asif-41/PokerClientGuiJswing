package Objects;

import java.util.ArrayList;

public class User {


    //========================================================
    //          THIS IS CLIENT SIDE USER OBJECT
    //     SOME UNNECESSARY FUNCTIONS ARE OMITTED HERE
    //========================================================


    //============================================================================
    //
    //              INITIALIZING PARAMETERS
    //
    //

    private String username;            //      USERNAME
    private String password;            //      PASSWORD
    private int currentCoin;            //      CURRENT COINS
    private int coinWon;                //      COINT WON IN WHOLE CAREER
    private int roundsWon;              //      ROUNDS WON IN WHOLE CAREER
    private int roundsPlayed;           //      ROUNDS PLAYED IN WHOLE CAREER
    private int winPercentage;          //      WIN PERCENTAGE GENERATED FROM ROUNDS WON, ROUNDS PLAYED
    private int winStreak;              //      CURRENT WIN STREAK
    private int level;                  //      LEVEL GENERATED FROM COIN WON IN WHOLE CAREER

    private boolean inGame;             //      IDENTIFIES IF USER IS IN GAME
    private ArrayList playerCards;      //      PLAYER CARDS IN ROUND
    private ArrayList boardCards;       //      BOARD CARDS IN A ROUND,     NOT STORED IN CLIENT SIDE, SENT BY
    //                                  SERVER ONE AFTER ONE

    private int prevLevelCoin;          //      NEEDED TO DETERMINE LEVEL
    private int nextLevelCoin;          //      NEEDED TO DETERMINE LEVEL
    private int seatPosition;           //      SEAT POSITION IN GAME
    //      UNNECESSARY IF LEVEL IS SENT FROM DATABASE

    //              INITIALIZING DONE
    //============================================================================


    //============================================================================
    //              CONSTRUCTORS
    //
    //       THESE DATA WILL COME FROM DATABASE
    //       AFTER SUCCESSFUL LOGIN
    //       OR AS GUEST USER
    //
    //============================================================================


    public User(String username, String password, int currentCoin, int coinWon) {

        this.username = username;
        this.password = password;
        this.currentCoin = currentCoin;
        this.coinWon = coinWon;

        seatPosition = -1;
        inGame = false;

        boardCards = new ArrayList<Card>();
        boardCards.clear();

        playerCards = new ArrayList<Card>();
        playerCards.clear();

        initializeOtherValues();
    }

    //
    //              CONSTRUCTORINT DONE
    //============================================================================


    //============================================================================
    //
    //      INITIALIZE OTHER VALUES
    //      NEEDS TO BE IMPLEMENTED
    //
    //============================================================================

    private void initializeOtherValues() {

        //algo to set level

        level = 3;
        prevLevelCoin = 1000000;
        nextLevelCoin = 10000000;
    }

    //===========================================================================
    //
    //===========================================================================


    //============================================================================
    //
    //          GETTERS AND SETTERS
    //
    //============================================================================

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCurrentCoin() {
        return currentCoin;
    }

    public void setCurrentCoin(int currentCoin) {
        this.currentCoin = currentCoin;
    }

    public int getCoinWon() {
        return coinWon;
    }

    public void setCoinWon(int coinWon) {
        this.coinWon = coinWon;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPrevLevelCoin() {
        return prevLevelCoin;
    }

    public void setPrevLevelCoin(int prevLevelCoin) {
        this.prevLevelCoin = prevLevelCoin;
    }

    public int getNextLevelCoin() {
        return nextLevelCoin;
    }

    public void setNextLevelCoin(int nextLevelCoin) {
        this.nextLevelCoin = nextLevelCoin;
    }

    public ArrayList getPlayerCards() {
        return playerCards;
    }

    public void setPlayerCards(ArrayList playerCards) {
        this.playerCards = playerCards;
    }

    public ArrayList getBoardCards() {
        return boardCards;
    }

    public void setBoardCards(ArrayList boardCards) {
        this.boardCards = boardCards;
    }

    public int getSeatPosition() {
        return seatPosition;
    }

    public void setSeatPosition(int seatPosition) {
        this.seatPosition = seatPosition;
    }


    //===========================================================================
    //
    //============================================================================


    //======================================================================================
    //
    //              REDUNDANT UNNECESSARY FUNCTIONS
    //              USED FOR DEBUGGING
    //              DELETE WHEN FINALIZING
    //
    //=======================================================================================


    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", currentCoin=" + currentCoin +
                ", inGame=" + inGame +
                ", level=" + level +
                "}\n";
    }


    //========================================================================================
    //
    //
    //=========================================================================================


}

