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


    //==================================================================================
    //
    //          IN GAME DATA
    //
    //==================================================================================

    private boolean inGame;             //      IDENTIFIES IF USER IS IN GAME
    private ArrayList playerCards;      //      PLAYER CARDS IN ROUND
    private ArrayList boardCards;       //      BOARD CARDS IN A ROUND,     NOT STORED IN CLIENT SIDE, SENT BY
    //                                  SERVER ONE AFTER ONE
    //      NOT USED IN SERVER SIDE
    //      BECAUSE, FOR ALL PLAYERS, SAME BOARD CARDS
    //      RATHER USED AN ARRAYLIST FOR BOARD CARDS

    private int roomId;                 //      GAME ROOM ID
    private int roomCode;               //      GAME ROOM CODE
    private String boardType;           //      ROOM TYPE
    private int boardCoin;              //      ENTRY COIN AMOUNT
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

        roomId = -1;
        roomCode = -1;
        boardType = "";
        boardCoin = -1;
        seatPosition = -1;
        inGame = false;

        boardCards = new ArrayList<Card>();
        playerCards = new ArrayList<Card>();

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

    public int getRoundsWon() {
        return roundsWon;
    }

    public void setRoundsWon(int roundsWon) {
        this.roundsWon = roundsWon;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public void setRoundsPlayed(int roundsPlayed) {
        this.roundsPlayed = roundsPlayed;
    }

    public int getWinPercentage() {
        return winPercentage;
    }

    public void setWinPercentage(int winPercentage) {
        this.winPercentage = winPercentage;
    }

    public int getWinStreak() {
        return winStreak;
    }

    public void setWinStreak(int winStreak) {
        this.winStreak = winStreak;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(int roomCode) {
        this.roomCode = roomCode;
    }

    public String getBoardType() {
        return boardType;
    }

    public void setBoardType(String boardType) {
        this.boardType = boardType;
    }

    public int getBoardCoin() {
        return boardCoin;
    }

    public void setBoardCoin(int boardCoin) {
        this.boardCoin = boardCoin;
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
                ", password='" + password + '\'' +
                ", currentCoin=" + currentCoin +
                ", coinWon=" + coinWon +
                ", roundsWon=" + roundsWon +
                ", roundsPlayed=" + roundsPlayed +
                ", winPercentage=" + winPercentage +
                ", winStreak=" + winStreak +
                ", level=" + level +
                ", inGame=" + inGame +
                ", playerCards=" + playerCards +
                ", boardCards=" + boardCards +
                ", roomId=" + roomId +
                ", roomCode=" + roomCode +
                ", boardType='" + boardType + '\'' +
                ", boardCoin=" + boardCoin +
                ", seatPosition=" + seatPosition +
                '}';
    }


    //========================================================================================
    //
    //
    //=========================================================================================


}

