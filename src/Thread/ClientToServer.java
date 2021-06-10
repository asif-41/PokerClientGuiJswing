package Thread;


import Objects.Card;
import Objects.TransactionNumber;
import Objects.User;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.gusavila92.websocketclient.WebSocketClient;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.InetAddress;
import java.net.URI;
import java.util.Timer;
import java.util.*;

import static java.lang.StrictMath.max;

public class ClientToServer extends JFrame {

    //========================================================
    //
    //          THIS IS CLIENT SIDE OBJECT
    //     SOME UNNECESSARY FUNCTIONS ARE OMITTED HERE
    //
    //      INITIALIZED WITH PORT AND LINK
    //      CONNECTS TO THE SERVER
    //      THEN WAITS FOR LOGIN AND OTHER STUFFS
    //
    //========================================================


    //============================================================================
    //
    //              INITIALIZING PARAMETERS
    //
    //===========================================================================

    private static double coinPricePerCrore;
    private static long coinAmountOnBuy[];
    private static double coinPriceOnBuy[];

    private int boardTypeCount;
    private long minCallValue[];
    private String boardType[];
    private long minEntryValue[];
    private long maxEntryValue[];
    private long mcr[];


    private URI webSocketLink;                                      //      SOCKET LINK
    private WebSocketClient webSocketClient;                        //      SOCKET
    private String received;

    private int port;                                       //      CONNECTION PORT
    private String host;                                    //      CONNECTION LINK
    //      UNNECESSARY
    //      USED FOR LOFFING IN
    private User user;                                      //      USER OBJECT IN CLIENT SIDE

    private JSONObject jsonIncoming;                        //      INCOMING JSON DATA
    private boolean hasConnected;                           //      IF CONNECTION IS MADE
    private Timer connectionCheckTimer;                     //      TIMER TO CHECK IF HAS CONNECTED


    private int tryCounter;
    private int tryConnectionTimeCounter;                   //      SECONDS COUNTER
    private int connectionTimeOut;                          //      WEB SOCKET CONNECTION TIMEOUT
    private int reconnectionTimeOut;                        //      WEB SOCKET RECONNECTION TIMEOUT


    private String curCommand;                              //      CURRENT USER COMMANDS IN GUI
    private String msg = "";                                //      MSG STRING

    private ArrayList<TransactionNumber> transactionNumbers;


    //========================================================================================
    //          DELETE THIS VARIABLES AT THE END

    //      FOR PRIVATE GAME THREADS

    private int gameThreadId;                               //      GAME ID
    private int gameThreadCode;                             //      GAME CODE
    private String gameRoomType;                            //      room type
    private int roomEntryValue;                             //      Entry fee of game room
    private String owner;
    private boolean gameRunning;                            //      GAME RUNNING OR NOT

    private int tempCode;
    private String tempBoard;
    private int tempEntryValue;
    private int tempMinCallValue;

    private String call;                                    //      CALL OF PLAYER IN CURRENT ROUND
    //private int minCallValue;                               //      MINIMUM VALUE TO CALL IN CURRENT ROUND
    private int foldCost;                                   //      FOLD COST, FOR SMALL BLIND, BIG BLINDS
    private int boardCoin;                                  //      CURRENT COIN IN BOARD
    private int cycleCount;                                 //      CYCLE COUNT
    private int roundCount;                                 //      ROUND COUNTS IN CURRENT GAME THREAD
    private int turnCount;

    //============================================================================
    //              INITIALIZING DONE
    //============================================================================


    //======================================================================================================
    //
    //              JFRAME GUI SHITS
    //              IGNORE AND DELETE THIS
    //
    //======================================================================================================


    private int inpCount = 0;                               //      INPUT COUNT
    //      USED THIS TO COMMUNICATE WITH SERVER
    //      IGNORE AND USE JSON OBJECT

    private JPanel gameButtons;
    private JButton foldButton;
    private JButton callButton;
    private JButton raiseButton;
    private JButton allInButton;
    private JButton checkButton;
    private JButton exitButton;

    private JTextArea textArea;
    private JTextField textField;
    private JScrollPane areaPanel;
    private JPanel centerPanel;

    private JPanel userButtons;
    private JButton logUser;
    private JButton userInfoButton;
    private JButton friendsButton;
    private JButton joinGame;
    private JButton inviteButton;
    private JButton buyButton;
    private JButton clearButton;
    private JButton closeButton;

    //=====================================================================================================
    //
    //=====================================================================================================


    //============================================================================
    //
    //              CONSTRUCTORS
    //
    //              INITIALIZED SOCKETS AND STREAMS HERE
    //
    //============================================================================


    public ClientToServer(URI link, int port, int tryCounter, int connectionTimeOut, int reconnectionTimeOut) {

        setUpGui();
        guiFunctions();

        webSocketLink = link;
        webSocketClient = null;

        this.port = port;
        try {
            host = InetAddress.getLocalHost().getHostAddress();

        } catch (Exception e) {
            addTextInGui("Exception in fetching ip -> " + e);
        }

        user = null;
        jsonIncoming = null;

        this.tryCounter = tryCounter;
        this.connectionTimeOut = connectionTimeOut;
        this.reconnectionTimeOut = reconnectionTimeOut;

        //createWebSocketClient();
        deInitializeAppData();
        tryConnection();

    }

    //=====================================================================================
    //
    //=====================================================================================


    //======================================================================
    //
    //      SETTING UP CONNECTION
    //
    //======================================================================

    private void createWebSocketClient() {

        webSocketClient = new WebSocketClient(webSocketLink) {
            @Override
            public void onOpen() {

                hasConnected = true;
                received = "";
            }

            @Override
            public void onTextReceived(String s) {

                try{

                    JSONObject jsonObject = new JSONObject(s);

                    boolean done = jsonObject.getBoolean("done");
                    String data = jsonObject.getString("data");

                    received += data;

                    if(done == true){
                        incomingMsg(received);
                        received = "";
                    }

                }catch (Exception e){
                    System.out.println("Exception in converting json in websocket, data length " + received.length());
                    System.out.println(e);
                }

            }

            @Override
            public void onBinaryReceived(byte[] bytes) {

            }

            @Override
            public void onPingReceived(byte[] bytes) {

            }

            @Override
            public void onPongReceived(byte[] bytes) {

            }

            @Override
            public void onException(Exception e) {

                addTextInGui("error ashchhe " + e);
                System.out.println("closed");

                if(hasConnected){
                    webSocketClient.close();
                    tryConnection();
                }
            }

            @Override
            public void onCloseReceived() {

                if(hasConnected){
                    webSocketClient.close();
                    tryConnection();
                }
            }
        };

        webSocketClient.setConnectTimeout(connectionTimeOut);
        webSocketClient.enableAutomaticReconnection(reconnectionTimeOut);
    }

    private void tryConnection() {

        hasConnected = false;
        tryConnectionTimeCounter = tryCounter;
        connectionCheckTimer = new Timer();

        createWebSocketClient();
        webSocketClient.connect();
        connectionCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                connectionChecker();
            }
        }, 0, 1000);
    }

    private void connectionChecker() {

        if (hasConnected) {
            tryConnectionTimeCounter = -1;
            connectionCheckTimer.cancel();

            addTextInGui("Connection established with server");

        } else tryConnectionTimeCounter--;

        if (tryConnectionTimeCounter == 0) {

            tryConnectionTimeCounter = -1;
            connectionCheckTimer.cancel();
            webSocketClient.close();

            addTextInGui("Cannot connect to server");
        }

    }

    //=====================================================================================
    //
    //=====================================================================================


    //======================================================================
    //
    //      COMMUNICATION
    //
    //======================================================================

    private JSONObject initiateJson() {

        JSONObject temp = new JSONObject();

        temp.put("sender", "Client");
        temp.put("ip", host);
        temp.put("port", port);

        return temp;
    }

    private void sendMessage(String temp) {

        try {
            System.out.println("sending length -> " + temp.toString().getBytes("UTF-8").length);

            String[] splitted = FluentIterable.from(Splitter.fixedLength(4000).split(temp)).toArray(String.class);

            for(int i=0; i<splitted.length-1; i++){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("done", false);
                jsonObject.put("data", splitted[i]);

                webSocketClient.send(jsonObject.toString());
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("done", true);
            jsonObject.put("data", splitted[splitted.length-1]);

            System.out.println("Sending -> "  + jsonObject.toString());
            webSocketClient.send(jsonObject.toString());
            System.out.println("sending done");

        } catch (Exception e) {
            addTextInGui("Error in sending msg to server -> " + e);
        }
    }

    private void incomingMsg(String temp) {
        try {

            System.out.println("received length -> " + temp.getBytes("UTF-8").length);
            System.out.println(temp);

            jsonIncoming = new JSONObject(temp);
            //System.out.println(jsonIncoming);

        } catch (Exception e) {
            System.out.println("Error in getting json in client side\n" + e);
            jsonIncoming = null;
            return;
        }

        if (jsonIncoming.get("requestType").equals("LoginResponse")) {

            loginRequestResponse(jsonIncoming.getBoolean("response"), jsonIncoming.getJSONObject("data"));
        }
        else if (jsonIncoming.get("requestType").equals("LogoutResponse")) {

            logoutRequestResponse(jsonIncoming.getBoolean("success"));
        }
        else if (jsonIncoming.get("requestType").equals("ForceLogout")) {

            forceLogout(jsonIncoming);
        }
        else if (jsonIncoming.get("requestType").equals("AddTransactionResponse")){

            transactionRequestResponse(jsonIncoming);
        }
        else if (jsonIncoming.get("requestType").equals("AllTransactionsResponse")){

            allTransactionsRequestResponse(jsonIncoming);
        }
        else if (jsonIncoming.get("requestType").equals("AddCoinVideoResponse")) {

            //requesting to buy coins

            receiveAddCoinVideoResponse(jsonIncoming);
            //gameStartIfInAGame();
        }
        else if (jsonIncoming.get("requestType").equals("AddFreeCoinResponse")) {

            //requesting to buy coins

            receiveAddFreeCoinResponse(jsonIncoming);
            //gameStartIfInAGame();
        }
        else if (jsonIncoming.get("requestType").equals("FriendsList")) {

            //requesting friends list
            JSONArray friends = jsonIncoming.getJSONArray("data");

            showFriends(friends);
        }
        else if (jsonIncoming.get("requestType").equals("JoinResponse")) {

            requestJoinResponse(jsonIncoming);
        }
        else if (jsonIncoming.get("requestType").equals("AbortResponse")) {

            requestAbortResponse(jsonIncoming);
        }
        else if (jsonIncoming.get("requestType").equals("UpdateOwnResponse")) {

            loadUpdateOwnResponse(jsonIncoming);
        }
        else if (jsonIncoming.get("requestType").equals("UpdateOwnInGameResponse")) {

            loadUpdateOwnInGameResponse(jsonIncoming);
        }
        else if (jsonIncoming.get("requestType").equals("GameRoom")) {

            JSONObject tempJson = jsonIncoming.getJSONObject("gameData");

            if (tempJson.get("gameRequest").equals("InitializeGameData")) {

                initializeGameData(jsonIncoming);
            }
            else if (tempJson.get("gameRequest").equals("LoadPlayersData")) {

                loadPlayersData(jsonIncoming);
            }
            else if (tempJson.get("gameRequest").equals("WelcomeGameMessage")) {

                showWelcomeGameMessage(jsonIncoming);
                exitButton.setEnabled(true);
            }
            else if (tempJson.get("gameRequest").equals("RoundStartMessage")) {

                gameRoundStart(jsonIncoming);
            }
            else if (tempJson.get("gameRequest").equals("LoadPlayerCards")) {

                loadCards(jsonIncoming);
            }
            else if (tempJson.get("gameRequest").equals("LoadBoardCards")) {

                loadCards(jsonIncoming);
            }
            else if (tempJson.get("gameRequest").equals("ShowBoardInfo")) {

                showBoardInfo(jsonIncoming);
            }
            else if (tempJson.get("gameRequest").equals("ShowCards")) {

                showCards();
            }
            else if (tempJson.get("gameRequest").equals("ShowNextTurnInfo")) {

                showNextTurnInfo(jsonIncoming);
            }
            else if (tempJson.get("gameRequest").equals("DisableGameButtons")) {

                disableGameButtons();
            }
            else if (tempJson.get("gameRequest").equals("EnableGameButtons")) {

                enableGameButtons(jsonIncoming);
            }
            else if (tempJson.get("gameRequest").equals("TurnInfo")) {

                showTurnInfo(jsonIncoming);
            }
            else if (tempJson.get("gameRequest").equals("CycleEnd")) {

                showEndCycle(jsonIncoming);
            }
            else if (tempJson.get("gameRequest").equals("Result")) {

                processResult(jsonIncoming);
            }
            else if (tempJson.get("gameRequest").equals("ShowAllCards")) {

                showAllCards((jsonIncoming));
            }
            else if (tempJson.get("gameRequest").equals("WaitForPlayersToBuy")) {

                waitForPlayersToBuy();
            }
            else if (tempJson.get("gameRequest").equals("AddBoardCoinResponse")) {

                addBoardCoinInGameResponse(jsonIncoming);
            }
            else if (tempJson.get("gameRequest").equals("LeaveGame")) {

                leaveGameRoom();

                disableGameButtons();
                exitButton.setEnabled(false);
                joinGame.setText("Join");
                addTextInGui("Leaving game");
            }
            else if (tempJson.get("gameRequest").equals("AskJoinGameThreadByCodeResponse")){

                joinGameThreadByCodeResponse(jsonIncoming);
            }
            else if(tempJson.get("gameRequest").equals("AskBoardCoin")){

                askBoardCoinForGame(jsonIncoming);
            }
            else if(tempJson.get("gameRequest").equals("DeductBlindCoins")){

                deductBlindCoins(jsonIncoming);
            }
        }
        else if (jsonIncoming.get("requestType").equals("WaitingRoom")) {

            JSONObject tempJson = jsonIncoming.getJSONObject("waitingRoomData");

            if(tempJson.get("requestType").equals("CreateWaitingRoomResponse")){

                createWaitingRoomResponse(jsonIncoming);
            }
            else if(tempJson.get("requestType").equals("AskBoardCoin")){

                askBoardCoin(jsonIncoming);
            }
            else if(tempJson.get("requestType").equals("InitializeUser")){

                initializeWaitingRoomData(jsonIncoming);
            }
            else if(tempJson.get("requestType").equals("AskJoinWaitingRoomByCodeResponse")) {

                askJoinWaitingRoomByCodeResponse(jsonIncoming);
            }
            else if(tempJson.get("requestType").equals("RemoveFromWaitingRoomResponse")) {

                removedFromWaitingRoom(jsonIncoming);
            }
            else if(tempJson.get("requestType").equals("StartGameResponse")) {

                showStartGameResponse(jsonIncoming);
            }
            else if(tempJson.get("requestType").equals("LoadPlayersData")){

                loadPlayersDataWaitingRoom(jsonIncoming);
            }
            else if(tempJson.get("requestType").equals("EditBoardCoinResponse")){

                editBoardCoinInWaitingRoomResponse(jsonIncoming);
            }
        }
        else if (jsonIncoming.get("requestType").equals("CheckConnection")){

            sendConnectionCheckResponse();
        }
        else if (jsonIncoming.get("requestType").equals("LoadBoardData")){

            loadBoardData(jsonIncoming);
        }
        else if (jsonIncoming.get("requestType").equals("LoadShopData")){

            loadShopData(jsonIncoming);
        }
        else if (jsonIncoming.get("requestType").equals("ShowNotifications")){

            showNotifications(jsonIncoming);
        }
        else if (jsonIncoming.get("requestType").equals("TokenRequestResponse")){

            showToken(jsonIncoming);
        }

    }

    //=====================================================================================
    //
    //=====================================================================================








    //=============================================================================
    //
    //          LOGIN FUNCTIONS, UPDATE OWN FUNCTION
    //
    //          (FB_ID, "facebook"), (GMAIL_ID, "google") ( empty, "guest")
    //
    //=============================================================================

    private void requestLogin(String account_data, String account_type, String username, String imageLink) {

        JSONObject send = initiateJson();
        send.put("requestType", "LoginRequest");

        JSONObject tempJson = new JSONObject();

        tempJson.put("account_id", account_data);
        tempJson.put("account_type", account_type);
        tempJson.put("account_username", username);
        tempJson.put("imageLink", imageLink);

        send.put("data", tempJson);
        sendMessage(send.toString());
    }

    private void loginRequestResponse(boolean success, JSONObject data) {

        if (success) {

            //logged in successfully
            //editing gui buttons

            logUser.setText("Logout");
            userInfoButton.setEnabled(true);
            friendsButton.setEnabled(true);
            joinGame.setEnabled(true);
            inviteButton.setEnabled(true);
            buyButton.setEnabled(true);

            //making new users

            initializeAppData(data.getJSONObject("appData"));
            loadUser(data);
            addTextInGui("Welcome " + user.getUsername());
        } else {
            addTextInGui("Login failed! No More guests allowed");
        }
    }

    private void loadUser(JSONObject temp) {

        user = User.JSONToUser(temp);
        user.setLoggedIn(true);
    }

    private void initializeAppData(JSONObject jsonObject){

        JSONArray array;

        coinPricePerCrore = jsonObject.getDouble("coinPricePerCrore");

        array = jsonObject.getJSONArray("coinAmountOnBuy");
        coinAmountOnBuy = new long[array.length()];
        for(int i=0; i<array.length(); i++) coinAmountOnBuy[i] = array.getLong(i);


        array = jsonObject.getJSONArray("coinPriceOnBuy");
        coinPriceOnBuy = new double[array.length()];
        for(int i=0; i<array.length(); i++) coinPriceOnBuy[i] = array.getDouble(i);

        boardTypeCount = jsonObject.getInt("boardTypeCount");

        array = jsonObject.getJSONArray("minCallValue");
        minCallValue = new long[boardTypeCount];
        for(int i=0; i<boardTypeCount; i++) minCallValue[i] = array.getLong(i);

        array = jsonObject.getJSONArray("boardType");
        boardType = new String[boardTypeCount];
        for(int i=0; i<boardTypeCount; i++) boardType[i] = array.getString(i);

        array = jsonObject.getJSONArray("minEntryValue");
        minEntryValue = new long[boardTypeCount];
        for(int i=0; i<boardTypeCount; i++) minEntryValue[i] = array.getLong(i);

        array = jsonObject.getJSONArray("maxEntryValue");
        maxEntryValue = new long[boardTypeCount];
        for(int i=0; i<boardTypeCount; i++) maxEntryValue[i] = array.getLong(i);

        array = jsonObject.getJSONArray("mcr");
        mcr = new long[boardTypeCount];
        for(int i=0; i<boardTypeCount; i++) mcr[i] = array.getLong(i);

        User.setExpIncrease(jsonObject.getInt("expIncrease"));

        array = jsonObject.getJSONArray("rankString");
        String[] temp = new String[array.length()];
        for(int i=0; i<array.length(); i++) temp[i] = array.getString(i);
        User.setRankString(temp);

        array = jsonObject.getJSONArray("ranksValue");
        long[] temp2 = new long[array.length()];
        for(int i=0; i<array.length(); i++) temp2[i] = array.getLong(i);
        User.setRanksValue(temp2);
    }

    private void deInitializeAppData(){

        coinPricePerCrore = 0.0;
        coinAmountOnBuy = null;
        coinPriceOnBuy = null;

        boardTypeCount = 0;
        minCallValue = null;
        boardType = null;
        minEntryValue = null;
        maxEntryValue = null;
        mcr = null;

        User.setExpIncrease(0);
        User.setRankString(null);
        User.setRanksValue(null);
    }



    private void requestUpdateOwn() {

        JSONObject send = initiateJson();
        send.put("requestType", "UpdateOwn");

        sendMessage(send.toString());
    }

    private void loadUpdateOwnResponse(JSONObject jsonObject) {

        boolean response = jsonObject.getBoolean("response");

        if (response) user = User.JSONToUser(jsonObject.getJSONObject("data"));
    }

    private void requestUpdateOwnInGame() {

        JSONObject send = initiateJson();
        send.put("requestType", "UpdateOwnInGame");

        sendMessage(send.toString());
    }

    private void loadUpdateOwnInGameResponse(JSONObject jsonObject) {

        boolean response = jsonObject.getBoolean("response");

        if (response) user = User.JSONToUserInGame(jsonObject.getJSONObject("data"));
    }




    private void logoutUser() {

        //logged out successfully
        //gui options
        logUser.setText("Login");
        userInfoButton.setEnabled(false);
        friendsButton.setEnabled(false);
        joinGame.setEnabled(false);
        inviteButton.setEnabled(false);
        buyButton.setEnabled(false);

        addTextInGui("Logged out user " + user.getUsername());
        user = null;
    }

    private void logoutRequestResponse(boolean success) {

        if (success) {

            logoutUser();
        } else addTextInGui("Logout request failed");
    }

    private void sendLogoutRequest() {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "LogoutRequest");

        sendMessage(send.toString());
    }

    private void forceLogout(JSONObject jsonObject){

        System.out.println("ekhon force logout korbe!");
        System.out.println("Reason "  + jsonObject.getString("message"));

    }

    public void closeEverything() {

        try {
            user = null;
            this.dispose();
            webSocketClient.close();
        } catch (Exception e) {
            addTextInGui("Error in closing connection in Client side, error -> " + e);
        }
    }

    //=====================================================================================
    //
    //=====================================================================================




    //=====================================================================================
    //
    //                  COIN ADDING FUNCTIONS
    //
    //=====================================================================================

    private void addCoinByVideoRequest() {

        JSONObject send = initiateJson();

        send.put("id", user.getId());
        send.put("username", user.getUsername());
        send.put("requestType", "AddCoinVideoRequest");
        send.put("requestTime", Calendar.getInstance().getTime());

        sendMessage(send.toString());
    }

    private void receiveAddCoinVideoResponse(JSONObject temp) {

        boolean success = jsonIncoming.getBoolean("success");
        long value = jsonIncoming.getLong("currentCoin");
        Date d = User.stringToDate(jsonIncoming.getString("lastCoinVideoAvailableTime"));
        long added = jsonIncoming.getLong("coinAdded");
        int count = jsonIncoming.getInt("coinVideoCount");

        if (success) {
            user.setCurrentCoin(value);
            user.setLastCoinVideoAvailableTime(d);
            user.setCoinVideoCount(count);
        }
        addTextInGui(jsonIncoming.getString("message"));
    }

    private void addFreeCoinRequest() {

        JSONObject send = initiateJson();

        send.put("id", user.getId());
        send.put("username", user.getUsername());
        send.put("requestType", "AddFreeCoinRequest");
        send.put("requestTime", Calendar.getInstance().getTime());

        sendMessage(send.toString());

    }

    private void receiveAddFreeCoinResponse(JSONObject temp) {

        boolean success = jsonIncoming.getBoolean("success");
        long value = jsonIncoming.getLong("currentCoin");
        Date d = User.stringToDate(jsonIncoming.getString("lastFreeCoinTime"));
        long coinAdded = jsonIncoming.getLong("coinAdded");

        if (success) {
            user.setCurrentCoin(value);
            user.setLastFreeCoinTime(d);
        }
        addTextInGui(jsonIncoming.getString("message"));
    }

    //=====================================================================================
    //
    //=====================================================================================






    //=====================================================================================
    //
    //                  JOIN/EXIT GAME FUNCTIONS
    //
    //=====================================================================================

    private void requestJoin(int gameId, int gameCode, String boardType, long minEntryValue, long minCallValue, int owner_id, int seatPosition, long boardCoin) {

        user.initializeGameData(gameId, gameCode, boardType, minEntryValue, minCallValue, owner_id, seatPosition, boardCoin);
        sendJoinRequest();
    }

    private void sendJoinRequest() {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "JoinRequest");

        JSONObject tempJson = new JSONObject();

        tempJson.put("gameId", user.getGameId());
        tempJson.put("gameCode", user.getGameCode());
        tempJson.put("boardType", user.getBoardType());
        tempJson.put("minEntryValue", user.getMinEntryValue());
        tempJson.put("minCallValue", user.getMinCallValue());
        tempJson.put("owner_id", user.getOwner_id());
        tempJson.put("seatPosition", user.getSeatPosition());
        tempJson.put("boardCoin", user.getBoardCoin());

        send.put("data", tempJson);

        sendMessage(send.toString());
    }

    private void requestJoinResponse(JSONObject temp) {

        addTextInGui(jsonIncoming.get("data").toString());
        joinGame.setText("Abort");
    }


    private void requestAbort() {

        user.deInitializeGameData();
        sendRequestAbort();
    }

    private void sendRequestAbort() {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "AbortRequest");

        sendMessage(send.toString());
    }

    private void requestAbortResponse(JSONObject temp) {

        addTextInGui(jsonIncoming.get("data").toString());
        joinGame.setText("Join");
    }



    private void requestExit() {

        sendExitRequest();
        leaveGameRoom();

        disableGameButtons();
        exitButton.setEnabled(false);
        joinGame.setText("Join");
        addTextInGui("Leaving game");
    }

    private void sendExitRequest() {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("gameId", gameThreadId);
        tempJson.put("gameCode", gameThreadCode);
        tempJson.put("requestType", "ExitGame");

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }

    private void leaveGameRoom() {

        user.deInitializeGameData();
    }

    //=====================================================================================
    //
    //=====================================================================================








    //=======================================================================================
    //
    //              GAME THREAD FUNCTIONALITIES
    //
    //=======================================================================================



    //=======================================================================================
    //              INITIALIZING GAME DATA
    //=======================================================================================

    private void initializeGameData(JSONObject jsonObject) {

        JSONObject gameData = jsonObject.getJSONObject("gameData");

        int playerCount = gameData.getInt("playerCount");
        int gameId = gameData.getInt("gameId");
        int gameCode = gameData.getInt("gameCode");
        int ownerId = gameData.getInt("ownerId");
        int seatPosition = gameData.getInt("seatPosition");
        int maxPlayerCount = gameData.getInt("maxPlayerCount");

        user.joinedAGame(gameId, gameCode, ownerId, seatPosition, maxPlayerCount, playerCount);
    }

    private void loadPlayersData(JSONObject temp) {

        JSONArray data = temp.getJSONArray("data");

        for (int i = 0; i < data.length(); i++) {

            User tempUser = User.JSONToUserInGame((JSONObject) data.get(i));
            user.getInGamePlayers()[tempUser.getSeatPosition()] = tempUser;
        }
        User.loadOwnSelfFromInGamePlayers(user);
    }

    private void showWelcomeGameMessage(JSONObject jsonObject) {             //GAME STARTING E

        JSONObject gameData = jsonObject.getJSONObject("gameData");

        String show = gameData.getString("message");
        addTextInGui(show);
    }

    //=======================================================================================
    //              INITIALIZING GAME DATA
    //=======================================================================================


    //=======================================================================================
    //              INOMINGS FROM GAME THREAD
    //=======================================================================================

    private void gameRoundStart(JSONObject jsonObject) {                     //ROUND STARTING E

        String message = jsonObject.getString("message");
        addTextInGui(message);
    }

    private void deductBlindCoins(JSONObject jsonObject){

        JSONArray array = jsonObject.getJSONArray("data");
        String msg = jsonObject.getString("message");

        long roundCoin = jsonObject.getLong("roundCoins");

        JSONObject temp = array.getJSONObject(0);
        int smallBlindSeat = temp.getInt("seatPosition");
        long smallBlindDeduct = temp.getLong("amount");

        temp = array.getJSONObject(1);
        int bigBlindSeat = temp.getInt("seatPosition");
        long bigBlindDeduct = temp.getLong("amount");

        System.out.println("roundCoin: " + roundCoin + " small blind: " + smallBlindSeat + " " + smallBlindDeduct + " big blind: " + bigBlindSeat + " " + bigBlindDeduct + " " + msg);
    }

    private void loadCards(JSONObject jsonObject) {

        int loc = -1;
        JSONArray array = jsonObject.getJSONArray("data");
        String req = jsonObject.getJSONObject("gameData").getString("gameRequest");

        if (req.equals("LoadPlayerCards")) loc = 1;
        else if (req.equals("LoadBoardCards")) loc = 0;


        ArrayList location;
        if (loc == 1) {
            location = user.getPlayerCards();
            location.clear();
        }
        else {
            location = user.getBoardCards();
            if(array.length() == 0) location.clear();
        }

        for (int i = 0; i < array.length(); i++) {

            String x[] = array.getString(i).split("\\.");

            Card card = new Card(Integer.valueOf(x[0]) - 1, Integer.valueOf(x[1]) - 2, loc);
            location.add(card);
        }
    }




    //  BOTH OF THEM UNNECESSARY, SHOW BOARD INFO CALLED FROM
    //  AROUND LINE 443

    private void showCards() {

        String bleh = "";

        bleh = "Board: ";
        for (int i = 0; i < user.getBoardCards().size(); i++)
            bleh += ((Card) user.getBoardCards().get(i)).toStringWithoutType() + "\n";
        addTextInGui(bleh);


        bleh = "Player: ";
        for (int i = 0; i < user.getPlayerCards().size(); i++)
            bleh += ((Card) user.getPlayerCards().get(i)).toStringWithoutType() + "\n";
        addTextInGui(bleh);
    }

    private void showBoardInfo(JSONObject jsonObject) {

        JSONObject data = jsonObject.getJSONObject("data");

        int roundCount = data.getInt("roundCount");
        long roundCoins = data.getLong("roundCoins");
        int turnCount = data.getInt("turnCount");
        int cycleCount = data.getInt("cycleCount");
        long roundCall = data.getLong("roundCall");

        String show = "round " + roundCount + " cycle " + cycleCount + " turn " + turnCount + "\n";
        show += "round coin " + roundCoins + " round minimum call " + roundCall;

        addTextInGui(show);
    }

    private void showNextTurnInfo(JSONObject jsonObject) {

        JSONObject temp = jsonObject.getJSONObject("data");

        String username = temp.getString("username");
        int pos = temp.getInt("seatPosition");

        String show = username + "'s turn, seat position " + pos;
        addTextInGui(show);
    }



    //===================================================================================
    //
    //      GAME BUTTONS ENABLING
    //
    //===================================================================================

    private void enableGameButtons(JSONObject JsonObject) {

        JSONArray temp = JsonObject.getJSONArray("data");

        String show = "Your current coin: " + user.getBoardCoin() + "\n";
        long cost;

        for (int i = 0; i < temp.length(); i++) {

            JSONObject jsonObject = temp.getJSONObject(i);

            if (jsonObject.getString("name").equals("Fold")) {

                foldButton.setEnabled(true);

                show += "You can fold";
                show += "\n";

            }
            else if (jsonObject.getString("name").equals("Call")) {

                cost = jsonObject.getLong("cost");
                show += "You can call, minimum value: " + cost + "\n";
                callButton.setEnabled(true);
            }
            else if (jsonObject.getString("name").equals("Raise")) {

                cost = jsonObject.getLong("cost");

                show += "You can raise, minimum value: " + cost + "\n";
                raiseButton.setEnabled(true);
                textField.setEditable(true);
            }
            else if (jsonObject.getString("name").equals("Check")) {

                show += "You can check\n";
                checkButton.setEnabled(true);
            }
            else if (jsonObject.getString("name").equals("AllIn")) {

                show += "You can go all in\n";
                allInButton.setEnabled(true);
            }
        }
        addTextInGui(show);
    }

    private void disableGameButtons() {
        allInButton.setEnabled(false);
        callButton.setEnabled(false);
        foldButton.setEnabled(false);
        raiseButton.setEnabled(false);
        checkButton.setEnabled(false);

        textField.setText("");
        textField.setEditable(false);
    }

    //=================================================================================





    //=====================================================================================
    //
    //              GAMETHREAD OUTCOMING
    //
    //
    //=====================================================================================

    public void sendGameThreadCallRequest() {

        user.setTotalCallCount(user.getTotalCallCount()+1);
        user.setCallCount(user.getCallCount()+1);
        user.setBoardCoin(user.getBoardCoin() - user.getRoundCall());
        user.setCallValue(user.getRoundCall());
        user.setTotalCallValue(user.getTotalCallValue() + user.getRoundCall());
        user.setCall("Call");
        user.setRoundCoins(user.getRoundCoins() + user.getRoundCall());

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "GameCall");
        tempJson.put("call", "Call");

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }

    public void sendGameThreadRaiseRequest(long value) {

        //      RAISE COIN SLIDER BETWEEN
        //      user.getRoundCall()     to      user.getBoardCoin()

        if( !(value >= user.getRoundCall() && value <= user.getBoardCoin()) ){

            addTextInGui("Invalid amount");
            return ;
        }

        user.setTotalCallCount(user.getTotalCallCount()+1);
        user.setRaiseCount(user.getRaiseCount()+1);
        user.setBoardCoin(user.getBoardCoin()-value);
        user.setCallValue(value);
        user.setTotalCallValue(user.getTotalCallValue() + value);
        user.setCall("Raise");
        user.setRoundCoins(user.getRoundCoins() + value);
        user.setRoundCall(value);

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "GameCall");
        tempJson.put("call", "Raise");
        tempJson.put("cost", value);

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }

    public void sendGameThreadAllInRequest() {

        user.setTotalCallCount(user.getTotalCallCount()+1);
        user.setAllInCount(user.getAllInCount()+1);
        user.setCallValue(user.getBoardCoin());
        user.setTotalCallValue(user.getTotalCallValue()+user.getBoardCoin());
        user.setCall("AllIn");
        user.setRoundCoins(user.getRoundCoins()+user.getBoardCoin());
        if(user.getBoardCoin() > user.getRoundCall()) user.setRoundCall(user.getBoardCoin());
        user.setBoardCoin(0);

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();
        tempJson.put("requestType", "GameCall");
        tempJson.put("call", "AllIn");

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }

    public void sendGameThreadCheckRequest() {

        user.setTotalCallCount(user.getTotalCallCount()+1);
        user.setCheckCount(user.getCheckCount()+1);
        user.setCall("Check");

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();
        tempJson.put("requestType", "GameCall");
        tempJson.put("call", "Check");

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }

    public void sendGameThreadFoldRequest() {

        user.setTotalCallCount(user.getTotalCallCount() + 1);
        user.setFoldCount(user.getFoldCount()+1);
        user.setBoardCoin(user.getBoardCoin() - user.getFoldCost());
        user.setCall("Fold");
        user.setRoundCoins(user.getRoundCoins() + user.getFoldCost());

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();
        tempJson.put("requestType", "GameCall");
        tempJson.put("call", "Fold");

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }


    private void showTurnInfo(JSONObject jsonObject) {

        JSONObject temp = jsonObject.getJSONObject("data");

        String username = temp.getString("username");
        int seatPosition = temp.getInt("seatPosition");
        String call = temp.getString("call");
        long value = temp.getLong("value");

        String show = username + " " + call + "ed with value " + value + ", seatPosition " + seatPosition;
        addTextInGui(show);
    }

    //=====================================================================================
    //
    //
    //=====================================================================================
















    //=====================================================================================
    //
    //              ROUND ENDING RELATED STUFF
    //
    //
    //=====================================================================================

    private void showEndCycle(JSONObject jsonObject) {

        String msg = jsonObject.getString("data");
        addTextInGui(msg);
    }

    private void showAllCards(JSONObject jsonObject) {

        String show;
        JSONObject data = jsonObject.getJSONObject("data");

        JSONArray temp;

        show = "All board cards: \n";
        temp = data.getJSONArray("allBoardCards");

        for (int i = 0; i < temp.length(); i++) show += temp.getString(i) + "\n";
        show += "\n";


        show += "All player cards: \n\n";
        temp = data.getJSONArray("allPlayerCards");

        for (int i = 0; i < temp.length(); i++) {

            JSONObject tempObject = temp.getJSONObject(i);
            JSONArray tempArray = tempObject.getJSONArray("cards");

            show += "username: " + tempObject.getString("username") + " seat: " + tempObject.getInt("seatPosition") + "\n";
            show += tempArray.getString(0) + "\n";
            show += tempArray.getString(1) + "\n\n";
        }

        addTextInGui(show);
    }


    private void loadWinnerData(JSONObject jsonObject){

        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray eachPlayerData = data.getJSONArray("eachPlayerData");

        boolean showCardsAtEnd = data.getBoolean("showCards");

        for(int i=0; i<eachPlayerData.length(); i++){

            JSONObject jsonObject1 = eachPlayerData.getJSONObject(i);

            int seat = jsonObject1.getInt("seat");
            long winAmount = jsonObject1.getLong("winAmount");
            boolean hasWon = jsonObject1.getBoolean("hasWon");
            long coinBack = jsonObject1.getLong("coinBack");
            String power = jsonObject1.getString("power");


            User temp = user.getInGamePlayers()[seat];

            if(temp == null) continue;
            temp.setRoundsPlayed(temp.getRoundsPlayed() + 1);
            temp.setBoardCoin(temp.getBoardCoin() + coinBack);

            if(hasWon){
                temp.setRoundsWon(temp.getRoundsWon() + 1);
                temp.setWinStreak(temp.getWinStreak() + 1);
                temp.setCoinWon(temp.getCoinWon() + winAmount);
                temp.setBiggestWin( max(temp.getBiggestWin() , winAmount) );
                if(showCardsAtEnd) temp.setBestHand( Card.compareHand(temp.getBestHand(), power) );
                temp.setBoardCoin(temp.getBoardCoin() + winAmount);
            }
            else{
                temp.setWinStreak(0);
                temp.setCoinLost(temp.getCoinLost() + temp.getTotalCallValue());
            }
        }
        User.loadOwnSelfFromInGamePlayers(user);






        /*
        long winAmount = data.getLong("winAmount");

        int kk = 0;
        int winners[] = new int[user.getMaxPlayerCount()];
        String[] results = new String[winnerData.length()];

        for(int i=0; i<user.getMaxPlayerCount(); i++) winners[i] = 0;
        for(int i=0; i<winnerData.length(); i++){

            int k = winnerData.getJSONObject(i).getInt("seatPosition");
            winners[k] = 1;
            results[i] = winnerData.getJSONObject(i).getString("resultString");
        }

        for(int i=0; i<user.getMaxPlayerCount(); i++){

            if(user.getInGamePlayers()[i] == null) continue;

            User temp = user.getInGamePlayers()[i];
            temp.setRoundsPlayed(temp.getRoundsPlayed() + 1);

            if(winners[i] == 1){
                temp.setRoundsWon(temp.getRoundsWon() + 1);
                temp.setWinStreak(temp.getWinStreak() + 1);
                temp.setCoinWon(temp.getCoinWon() + winAmount);
                temp.setBiggestWin( max(temp.getBiggestWin() , winAmount) );
                if(showCardsAtEnd) temp.setBestHand( Card.compareHand(temp.getBestHand(), results[kk]) );
                temp.setBoardCoin(temp.getBoardCoin() + winAmount);

                kk++;
            }
            else{
                temp.setWinStreak(0);
                temp.setCoinLost(temp.getCoinLost() + temp.getTotalCallValue());
            }
        }
        User.loadOwnSelfFromInGamePlayers(user);*/
    }

    private void processResult(JSONObject jsonObject) {


        System.out.println("Winner result -> " + jsonObject);

        loadWinnerData(jsonObject);

        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray eachPlayerData = data.getJSONArray("eachPlayerData");

        boolean showCardsAtEnd = data.getBoolean("showCards");

        for(int i=0; i<eachPlayerData.length(); i++){

            JSONObject jsonObject1 = eachPlayerData.getJSONObject(i);

            int seat = jsonObject1.getInt("seat");
            long winAmount = jsonObject1.getLong("winAmount");
            boolean hasWon = jsonObject1.getBoolean("hasWon");
            long coinBack = jsonObject1.getLong("coinBack");
            String power = jsonObject1.getString("power");

            if( ! hasWon ) continue;

            JSONArray winningData = jsonObject1.getJSONArray("winLevel");
            for(int j=0; j<winningData.length(); j++){

                JSONObject jsonObject2 = winningData.getJSONObject(j);

                long amount = jsonObject2.getLong("amount");
                int level = jsonObject2.getInt("level");
                int resultFoundAtLevel = jsonObject2.getInt("resultFoundAtLevel");

                if(showCardsAtEnd) System.out.println( Card.suitMessage(power, resultFoundAtLevel) );
            }
        }

        /*
        loadWinnerData(jsonObject);

        JSONObject data = jsonObject.getJSONObject("data");

        String show = "\nWinners: ";

        int winnerCount = data.getJSONArray("winnerData").length();
        long winAmount = data.getLong("winAmount");


        if (winnerCount == 0) {
            addTextInGui("Everyone folded, no winners!");
            return;
        }

        String[] winners = new String[winnerCount];
        String[] winnerCards = new String[winnerCount];

        for (int i = 0; i < winnerCount; i++) {

            JSONObject temp = data.getJSONArray("winnerData").getJSONObject(i);

            winners[i] = temp.getString("username");
            winnerCards[i] = temp.getString("resultString");
        }

        boolean showCards = data.getBoolean("showCards");
        show += "Winning amount each: " + winAmount;
        show += winnerCount + " winners\n";

        for (int i = 0; i < winnerCount; i++) {
            show += winners[i];
            show += "\n";

            if (showCards == true) show += " -> " + Card.suitMessage(winnerCards[i], data.getInt("showWinLevel"));
        }
        if (winnerCount > 1) show += "\nTied between " + winnerCount + " players";

        addTextInGui(show);*/
    }

    //======================================================================================
    //
    //======================================================================================







    //======================================================================================
    //
    //              WAIT FOR PLAYERS
    //              OR
    //              WAIT FOR PLAYERS TO BUY COINS
    //
    //======================================================================================

    private void waitForPlayersToBuy() {

        disableGameButtons();

        user.setGameRunning(false);

        String show = "Waiting for players to buy coins" ;
        addTextInGui(show);
    }

    private void sendAddBoardCoinInGameRequest(long value){

        if(value > user.getCurrentCoin()) {
            addTextInGui("Invalid Request");
            return ;
        }

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "AddBoardCoin");
        tempJson.put("amount", value);

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }

    private void addBoardCoinInGameResponse(JSONObject jsonObject){

        JSONObject gameData = jsonObject.getJSONObject("gameData");
        boolean success = gameData.getBoolean("success");
        long amount = gameData.getLong("amount");

        if(success){
            user.setCurrentCoin(user.getCurrentCoin() - amount);
            user.setBoardCoin(user.getBoardCoin() + amount);

            tryStartCurrentGame();
        }
    }


    private void tryStartCurrentGame(){

        if(user.isInGame() == false) return ;
        else if(user.isGameRunning() == true) return ;
        else sendTryStartCurrentGameRequest();
    }

    private void sendTryStartCurrentGameRequest() {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("gameId", gameThreadId);
        tempJson.put("gameCode", gameThreadCode);
        tempJson.put("requestType", "StartNewRound");

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }

    //==================================================================================
    //
    //==================================================================================
















    //=================================================================================
    //
    //          WAITING ROOM
    //
    //=================================================================================



    //=================================================================================
    //
    //          CREATE WAITING ROOM
    //
    //=================================================================================

    private void createWaitingRoom(String boardType, long minEntryValue, long minCallValue){

        // OWNER ER CURRENT COIN minEntryValue
        // ER BESHI HOILEI CREATE KORTE PARBE

        sendCreateWaitingRoomRequest(boardType, minEntryValue, minCallValue);
    }

    private void sendCreateWaitingRoomRequest(String boardType, long minEntryValue, long minCallValue) {

        JSONObject send = initiateJson();

        send.put("ownerId", user.getId());
        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "Create");

        tempJson.put("boardType", boardType);
        tempJson.put("minEntryValue", minEntryValue);
        tempJson.put("minCallValue", minCallValue);

        send.put("waitingRoomData", tempJson);
        sendMessage(send.toString());
    }

    private void createWaitingRoomResponse(JSONObject jsonObject){

        String message = jsonObject.getString("data");
        addTextInGui(message);
    }


    //=================================================================================
    //
    //=================================================================================





    //=================================================================================
    //
    //          JOIN REQUESTS
    //
    //=================================================================================

    private void askBoardCoin(JSONObject jsonObject){

        JSONObject waitingRoomData = jsonObject.getJSONObject("waitingRoomData");

        long minEntryValue = waitingRoomData.getLong("minEntryValue");
        long minCallValue = waitingRoomData.getLong("minCallValue");

        String show = "Min entry value " + minEntryValue + " min call value " + minCallValue + "\n";
        show += "Enter entry amount: " ;

        addTextInGui(show);
    }

    private void sendJoinAmount(long value){

        JSONObject send = initiateJson();
        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "JoinAmount");
        tempJson.put("amount", value);

        send.put("waitingRoomData", tempJson);
        sendMessage(send.toString());
    }


    private void initializeWaitingRoomData(JSONObject jsonObject){

        JSONObject waitingRoomData = jsonObject.getJSONObject("waitingRoomData");

        int id = waitingRoomData.getInt("gameId");
        int code = waitingRoomData.getInt("gameCode");
        String boardType = waitingRoomData.getString("boardType");
        long minEntryValue = waitingRoomData.getLong("minEntryValue");
        long minCallValue = waitingRoomData.getLong("minCallValue");
        long boardCoin = waitingRoomData.getLong("boardCoin");
        int seatPosition = waitingRoomData.getInt("seatPosition");
        int owner_id = waitingRoomData.getInt("ownerId");
        int maxPlayerCount = waitingRoomData.getInt("maxPlayerCount");

        user.initializeInvitationData(id, code, maxPlayerCount, boardType, minEntryValue, minCallValue, owner_id, seatPosition, boardCoin);
    }



    private void joinWaitingRoomByCode(int code) {

        sendJoinWaitingRoomByCodeRequest(code);
    }

    private void sendJoinWaitingRoomByCodeRequest(int code) {

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "AskJoinWaitingRoomByCode");
        tempJson.put("gameCode", code);

        send.put("waitingRoomData", tempJson);
        sendMessage(send.toString());
    }

    private void askJoinWaitingRoomByCodeResponse(JSONObject temp) {

        JSONObject waitingRoomData = temp.getJSONObject("waitingRoomData");
        String msg = waitingRoomData.getString("message");
        int code = waitingRoomData.getInt("gameCode");
        boolean success = waitingRoomData.getBoolean("success");

        addTextInGui(msg);

        waitingRoomJoinDone(success, code);
    }


    private void cancelJoiningRequest(){

        sendCancelJoinRequest();
    }

    private void sendCancelJoinRequest(){

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "CancelJoinRequest");

        send.put("waitingRoomData", tempJson);
        sendMessage(send.toString());

    }

    //=================================================================================
    //
    //=================================================================================




    //=================================================================================
    //
    //          ADD BOARD COIN IN WAITING ROOM
    //
    //=================================================================================

    private void sendEditBoardCoinInWaitingRoomRequest(long value){

        if(value > user.getCurrentCoin() + user.getBoardCoin()) {
            addTextInGui("Invalid Request");
            return ;
        }

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "EditBoardCoin");
        tempJson.put("amount", value);

        send.put("waitingRoomData", tempJson);

        sendMessage(send.toString());
    }

    private void editBoardCoinInWaitingRoomResponse(JSONObject jsonObject){

        JSONObject waitingRoomData = jsonObject.getJSONObject("waitingRoomData");
        boolean success = waitingRoomData.getBoolean("success");
        long boardCoin = waitingRoomData.getLong("boardCoin");
        long currentCoin = waitingRoomData.getLong("currentCoin");

        if(success){
            user.setCurrentCoin(currentCoin);
            user.setBoardCoin(boardCoin);
        }
    }

    //=================================================================================
    //
    //=================================================================================








    //=================================================================================
    //
    //          ALL PLAYERS DATA
    //
    //=================================================================================

    private void showPlayersData(JSONObject jsonObject){

        JSONArray array = jsonObject.getJSONArray("data");
        System.out.println("in user " + user.getUsername() + " -> " + array);
    }

    //=================================================================================
    //
    //=================================================================================










    //=================================================================================
    //
    //          REMOVE FROM WAITING ROOM
    //
    //=================================================================================

    private void removeMeFromWaitingRoom(){

        int loc = user.getSeatPosition();
        user.deInitializeInvitationData();
        sendRemoveMeFromWaitingRoom(loc);
    }

    private void sendRemoveMeFromWaitingRoom(int loc){

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();
        tempJson.put("requestType", "RemoveMeFromWaitingRoom");
        tempJson.put("seatPosition", loc);

        send.put("waitingRoomData", tempJson);
        sendMessage(send.toString());

    }

    //  OWNER ONLY
    private void removeFromWaitingRoom(int[] seat){

        JSONObject send = initiateJson();

        send.put("owner", user.getUsername());
        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();
        tempJson.put("requestType", "RemoveFromWaitingRoom");

        JSONArray array = new JSONArray();
        for(int x : seat) array.put(x);

        send.put("data", array);

        send.put("waitingRoomData", tempJson);
        sendMessage(send.toString());
    }


    private void removedFromWaitingRoom(JSONObject jsonObject) {

        JSONObject waitingRoomData = jsonObject.getJSONObject("waitingRoomData");
        String message = waitingRoomData.getString("message");

        if(message != null) addTextInGui(message);

        user.deInitializeInvitationData();
    }

    //=================================================================================
    //
    //=================================================================================













    //=================================================================================
    //
    //          START GAME FROM WAITING ROOM
    //
    //          LOAD PLAYERS DATA AND SHOW
    //
    //=================================================================================

    private void sendStartGameRequest() {

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();
        tempJson.put("requestType", "StartGame");

        send.put("waitingRoomData", tempJson);
        sendMessage(send.toString());
    }

    private void showStartGameResponse(JSONObject temp) {

        JSONObject waitingRoomData = temp.getJSONObject("waitingRoomData");
        String msg = waitingRoomData.getString("message");

        addTextInGui(msg);
    }


    private void loadPlayersDataWaitingRoom(JSONObject temp) {

        JSONArray data = temp.getJSONArray("data");

        for (int i = 0; i < data.length(); i++) {

            User tempUser = User.JSONToUserInGame((JSONObject) data.get(i));
            user.getInWaitingRoomPlayers()[tempUser.getSeatPosition()] = tempUser;
        }
    }


    //=================================================================================
    //
    //=================================================================================









    //=================================================================================
    //
    //=================================================================================













    //=================================================================================
    //
    //          JOIN GAME BY CODE
    //
    //=================================================================================

    private void joinGameThreadByCode(int code) {

        sendJoinGameThreadByCodeRequest(code);
    }

    private void sendJoinGameThreadByCodeRequest(int code) {

        JSONObject send = initiateJson();

        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "AskJoinGameThreadByCode");
        tempJson.put("gameCode", code);

        send.put("gameData", tempJson);
        sendMessage(send.toString());
    }


    private void joinGameThreadByCodeResponse(JSONObject temp) {

        JSONObject gameData = temp.getJSONObject("gameData");
        String msg = gameData.getString("message");
        boolean success = gameData.getBoolean("success");

        if(success == false) user.deInitializeGameData();

        addTextInGui(msg);
    }


    private void cancelJoiningGameRequest(){

        user.deInitializeGameData();
        sendCancelJoiningGameRequest();
    }

    private void sendCancelJoiningGameRequest(){

        JSONObject send = initiateJson();

        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "CancelJoinRequest");

        send.put("gameData", tempJson);
        sendMessage(send.toString());

    }




    private void askBoardCoinForGame(JSONObject jsonObject){

        JSONObject gameData = jsonObject.getJSONObject("gameData");

        int gameId = gameData.getInt("gameId");
        int gameCode = gameData.getInt("gameCode");
        int ownerId = gameData.getInt("ownerId");
        long minEntryValue = gameData.getLong("minEntryValue");
        long minCallValue = gameData.getLong("minCallValue");
        String boardType = gameData.getString("boardType");

        user.initializeGameData(gameId, gameCode, boardType, minEntryValue, minCallValue, ownerId, -1, 0 );

        String show = "Min entry value " + minEntryValue + " min call value " + minCallValue + " boardType " + boardType + "\n";
        show += "Enter entry amount: " ;

        addTextInGui(show);
    }



    private void joinAmountForGame(long value){

        user.setBoardCoin(value);
        sendJoinAmountForGame(value);
    }

    private void sendJoinAmountForGame(long value){

        JSONObject send = initiateJson();
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("gameCode", user.getGameCode());
        tempJson.put("requestType", "JoinAmount");
        tempJson.put("amount", value);

        send.put("gameData", tempJson);
        sendMessage(send.toString());
    }

    //=================================================================================
    //
    //=================================================================================










    //=================================================================================
    //
    //          JOIN GAME BY CODE
    //
    //=================================================================================

    private void joinByCode(int code){

        joinWaitingRoomByCode(code);
    }

    private void waitingRoomJoinDone(boolean success, int code){

        if(success == false) joinGameThreadByCode(code);
    }

    //=================================================================================
    //
    //=================================================================================













    //=================================================================================
    //
    //              DATA LOADINGS, CONNECTION CHECK
    //
    //=================================================================================

    private void sendConnectionCheckResponse(){

        JSONObject send = initiateJson();
        send.put("requestType", "CheckConnection");
        send.put("isConnected", true);
        sendMessage(send.toString());
    }

    private void sendBoardDataRequest(){

        JSONObject send = initiateJson();
        send.put("requestType", "BoardData");

        sendMessage(send.toString());
    }

    private void loadBoardData(JSONObject data){

        JSONArray array;
        JSONObject jsonObject = data.getJSONObject("data");

        int boardTypeCount = jsonObject.getInt("boardTypeCount");

        array = jsonObject.getJSONArray("minCallValue");
        long[] minCallValue = new long[boardTypeCount];
        for(int i=0; i<boardTypeCount; i++) minCallValue[i] = array.getLong(i);

        array = jsonObject.getJSONArray("boardType");
        String[] boardType = new String[boardTypeCount];
        for(int i=0; i<boardTypeCount; i++) boardType[i] = array.getString(i);

        array = jsonObject.getJSONArray("minEntryValue");
        long[] minEntryValue = new long[boardTypeCount];
        for(int i=0; i<boardTypeCount; i++) minEntryValue[i] = array.getLong(i);

        array = jsonObject.getJSONArray("maxEntryValue");
        long[] maxEntryValue = new long[boardTypeCount];
        for(int i=0; i<boardTypeCount; i++) maxEntryValue[i] = array.getLong(i);

        array = jsonObject.getJSONArray("mcr");
        long[] mcr = new long[boardTypeCount];
        for(int i=0; i<boardTypeCount; i++) mcr[i] = array.getLong(i);
    }

    private void shopDataRequest(){

        JSONObject send = initiateJson();
        send.put("requestType", "ShopData");

        sendMessage(send.toString());
    }

    private void loadShopData(JSONObject data) {

        JSONObject jsonObject = data.getJSONObject("data");
        JSONArray array;

        double coinPricePerCrore = jsonObject.getDouble("coinPricePerCrore");

        array = jsonObject.getJSONArray("coinAmountOnBuy");
        long[] coinAmountOnBuy = new long[array.length()];
        for(int i=0; i<array.length(); i++) coinAmountOnBuy[i] = array.getLong(i);

        array = jsonObject.getJSONArray("coinPriceOnBuy");
        double[] coinPriceOnBuy = new double[array.length()];
        for(int i=0; i<array.length(); i++) coinPriceOnBuy[i] = array.getDouble(i);

        array = data.getJSONArray("Numbers");
        ArrayList<TransactionNumber> transactionNumbers = new ArrayList<TransactionNumber>();
        for(int i=0; i<array.length(); i++) transactionNumbers.add(new TransactionNumber(array.getJSONObject(i)));

        int requestLeft = data.getInt("requestLeft");

        this.transactionNumbers = transactionNumbers;

    }

    //=================================================================================
    //
    //=================================================================================









    //=================================================================================
    //
    //              TRANSACTION FUNCTIONS
    //
    //=================================================================================

    public double getCurrencyAmount(long coinAmount, String req){

        double price = 0.0;

        if(req.equals("buy")) {

            for(int i=0; i<coinAmountOnBuy.length; i++){
                if(coinAmount == coinAmountOnBuy[i]){
                    price = coinPriceOnBuy[i];
                    break;
                }
            }
        }
        else if(req.equals("withdraw")){

            price = coinPricePerCrore * ( coinAmount / 10000000 );
        }
        return price;
    }

    private void coinBuyRequest(long coinAmount, String method, String trId, String sender, String receiver) {

        JSONObject send = initiateJson();

        send.put("id", user.getId());
        send.put("username", user.getUsername());
        send.put("requestType", "Transaction");

        JSONObject tempJson = new JSONObject();

        tempJson.put("request", "BuyCoin");
        tempJson.put("method", method);
        tempJson.put("transactionId", trId);
        tempJson.put("coinAmount", coinAmount);
        tempJson.put("requestTime", Calendar.getInstance().getTime());
        tempJson.put("sender", sender);
        tempJson.put("receiver", receiver);

        send.put("data", tempJson);
        sendMessage(send.toString());
    }

    private void withdrawCoinRequest(long coinAmount, String method, String receiver){

        JSONObject send = initiateJson();

        send.put("id", user.getId());
        send.put("username", user.getUsername());
        send.put("requestType", "Transaction");

        JSONObject tempJson = new JSONObject();

        tempJson.put("request", "WithdrawCoin");
        tempJson.put("method", method);
        tempJson.put("transactionId", "");
        tempJson.put("coinAmount", coinAmount);
        tempJson.put("requestTime", Calendar.getInstance().getTime());
        tempJson.put("sender", "");
        tempJson.put("receiver", receiver);
        send.put("data", tempJson);

        sendMessage(send.toString());
    }

    private void transactionRequestResponse(JSONObject jsonObject){

        String msg = "";

        boolean success = jsonObject.getBoolean("success");
        long currentCoin = jsonObject.getLong("currentCoin");
        int reqLeft = jsonObject.getInt("requestLeft");

        user.setCurrentCoin(currentCoin);


        msg += jsonObject.getString("message");
        msg += "\n";
        msg += "Request left: " + reqLeft + "\n";
        addTextInGui(msg);
    }



    private void getAllTransactionsRequest(){

        JSONObject send = initiateJson();

        send.put("id", user.getId());
        send.put("username", user.getUsername());
        send.put("requestType", "Transaction");

        JSONObject tempJson = new JSONObject();

        tempJson.put("request", "ShowTransactions");
        send.put("data", tempJson);

        sendMessage(send.toString());
    }

    private void allTransactionsRequestResponse(JSONObject jsonObject){

        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray transactions = data.getJSONArray("transactions");
        JSONArray pendingTransactions = data.getJSONArray("pendingTransactions");
        JSONArray pendingRefunds = data.getJSONArray("pendingRefunds");
        JSONArray refunds = data.getJSONArray("refunds");

        for(int i=0; i<pendingTransactions.length(); i++){

            JSONObject j = pendingTransactions.getJSONObject(i);

            int id = j.getInt("id");
            int account_id = j.getInt("account_id");
            String type = j.getString("type");
            String method = j.getString("method");
            String transactionId = j.getString("transactionId");
            long coinAmount = j.getLong("coinAmount");
            double price = j.getDouble("price");
            String receiver = j.getString("receiver");
            String sender = j.getString("sender");
            Date requestTime = User.stringToDate(j.getString("requestTime"));
        }

        for(int i=0; i<transactions.length(); i++){

            JSONObject j = transactions.getJSONObject(i);

            int id = j.getInt("id");
            int account_id = j.getInt("account_id");
            String type = j.getString("type");
            String method = j.getString("method");
            String transactionId = j.getString("transactionId");
            long coinAmount = j.getLong("coinAmount");
            double price = j.getDouble("price");
            String receiver = j.getString("receiver");
            String sender = j.getString("sender");
            Date requestTime = User.stringToDate(j.getString("requestTime"));
            Date approvalTime = User.stringToDate(j.getString("approvalTime"));
        }

        for(int i=0; i<pendingRefunds.length(); i++){

            JSONObject j = pendingRefunds.getJSONObject(i);

            int id = j.getInt("id");
            int account_id = j.getInt("account_id");
            String type = j.getString("type");
            String method = j.getString("method");
            String transactionId = j.getString("transactionId");
            long coinAmount = j.getLong("coinAmount");
            double refundAmount = j.getDouble("refundAmount");
            String receiver = j.getString("receiver");
            String sender = j.getString("sender");
            Date requestTime = User.stringToDate(j.getString("requestTime"));
            Date refundRequestTime = User.stringToDate(j.getString("refundRequestTime"));
            String reason = j.getString("reason");
        }

        for(int i=0; i<refunds.length(); i++){

            JSONObject j = refunds.getJSONObject(i);

            int id = j.getInt("id");
            int account_id = j.getInt("account_id");
            String type = j.getString("type");
            String method = j.getString("method");
            String prevTransactionId = j.getString("prevTransactionId");
            String refundTransactionId = j.getString("refundTransactionId");
            long coinAmount = j.getLong("coinAmount");
            double refundAmount = j.getDouble("refundAmount");
            String receiver = j.getString("receiver");
            String sender = j.getString("sender");
            Date requestTime = User.stringToDate(j.getString("requestTime"));
            Date refundRequestTime = User.stringToDate(j.getString("refundRequestTime"));
            Date refundTime = User.stringToDate(j.getString("refundTime"));
            String reason = j.getString("reason");
            String prevReceiver = j.getString("prevReceiver");
        }
    }


    private void getNotifications(){

        JSONObject send = initiateJson();

        send.put("id", user.getId());
        send.put("username", user.getUsername());
        send.put("requestType", "NotificationRequest");

        sendMessage(send.toString());
    }

    private void showNotifications(JSONObject jsonObject){

        JSONArray notifications = jsonObject.getJSONArray("data");
        for(int i=0; i<notifications.length(); i++){

            JSONObject jsonObject1 = notifications.getJSONObject(i);

            long coinAdded = jsonObject1.getLong("coinAdded");
            user.setCurrentCoin(user.getCurrentCoin() + coinAdded);

            System.out.println("Notification " + i + " -> ");
            System.out.println(jsonObject1);
        }
        System.out.println();
    }




    //=================================================================================
    //
    //=================================================================================




    private void requestToken(){

        JSONObject send = initiateJson();

        send.put("id", user.getId());
        send.put("username", user.getUsername());
        send.put("requestType", "TokenRequest");

        sendMessage(send.toString());
    }

    private void showToken(JSONObject jsonObject){

        String token = jsonObject.getString("token");
        String link = "http://66.42.55.46:1112/request?id=" + user.getId() + "&token=" + token ;

        addTextInGui("Token: " + token + "\nlink: " + link + "\n\n");
    }




    //===========================================================================================
    //
    //      GUI SHITS, IGNORE
    //
    //===========================================================================================


    private void logUserClick() {

        msg = "";
        if (user != null) {
            msg += "Logout";
            sendLogoutRequest();
            curCommand = "Logout";
        } else {
            msg += "Login ";
            requestLogin("a", "guest", "nai", "nai");
            /*textField.setEditable(true);
            inpCount = 4;
            curCommand = "Login";

            addTextInGui("Enter username and then password");*/
        }
    }

    private void infoUserClick() {
        //curCommand = "UserInfo";
        //addTextInGui(User.UserToJsonInGame(user).toString());

        requestToken();

        //removeFromWaitingRoom(new int[]{1,2});
    }

    private void joinClick() {

        try{
            shopDataRequest();
            Thread.sleep(500);
            coinBuyRequest(100000, "bkash", "haha", "01783942932", transactionNumbers.get(0).getJSON().toString());
            Thread.sleep(500);

        }catch (Exception e){

        }


        if (joinGame.getText().equals("Join")) {
            requestJoin(-1, -1, boardType[0], minEntryValue[0], minCallValue[0], -1, -1, 100000);
            curCommand = "Join";
        } else if (joinGame.getText().equals("Abort")) {
            requestAbort();
            curCommand = "Abort";
        }

    }

    private void inviteButtonClick() {

        try{
            shopDataRequest();
            Thread.sleep(500);
            withdrawCoinRequest(100000, "bkash", "01783942932");

        }catch (Exception e){

        }
        //addFreeCoinRequest();
        //getCoinBuyWithdrawDataRequest();
        //withdrawCoinRequest(100000, "bkash", "01783942932");
        //getTransactionsRequest();
        //addFreeCoinRequest();
        //addCoinByVideoRequest();

        //createWaitingRoom("board1", 100000, 10000);
        //sendJoinAmount(200000);

    }

    private void friendsButtonClicked() {

        JSONObject send = initiateJson();

        send.put("id", user.getId());
        send.put("username", user.getUsername());
        send.put("requestType", "Haha");

        String hehe = "";
        for(int i=0; i<9000; i++) hehe += "1";

        send.put("hehe", hehe);

        sendMessage(send.toString());

        //getNotifications();
        //getAllTransactionsRequest();

        //curCommand = "Friends";
        //requestFriendsList();
    }

    private void closeButtonClicked() {
        String ret = "";
        ret = "Close";
        closeEverything();
    }

    private void buyCoinClick() {
        getNotifications();
        curCommand = "Buy";
        textField.setEditable(true);
    }

    private void guiFunctions() {

        logUser.addActionListener(e -> logUserClick());
        userInfoButton.addActionListener(e -> infoUserClick());
        friendsButton.addActionListener(e -> friendsButtonClicked());
        joinGame.addActionListener(e -> joinClick());
        buyButton.addActionListener(e -> buyCoinClick());
        inviteButton.addActionListener(e -> inviteButtonClick());
        clearButton.addActionListener(e -> textArea.setText(""));
        closeButton.addActionListener(e -> closeButtonClicked());

        callButton.addActionListener(e -> clickedCallButton());
        foldButton.addActionListener(e -> clickedFoldButton());
        raiseButton.addActionListener(e -> clickedRaiseButton());
        checkButton.addActionListener(e -> clickedCheckButton());
        allInButton.addActionListener(e -> clickedAllInButton());
        exitButton.addActionListener(e -> clickedExitButton());


        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                    if (curCommand == "Login") {
                        msg += textField.getText() + " ";
                        addTextInGui(textField.getText());
                        textField.setText("");
                        inpCount--;

                        if (inpCount == 0) {
                            textField.setEditable(false);

                            String[] temp = msg.split(" ");
                            String account_data = temp[1];
                            String account_type = temp[2];
                            String account_username = temp[3];
                            String link = temp[4];

                            requestLogin(account_data, account_type, account_username, link);
                            //String account_data, String account_type, String username,)
                            //requestLogin("hello" + username, "google", "Asif_"+username , "https://www.pngitem.com/pimgs/m/279-2799324_transparent-guest-png-become-a-member-svg-icon.png");
                            //requestLogin("hello" + (int) Math.random(), "facebook", "Asif", "https://www.pngitem.com/pimgs/m/279-2799324_transparent-guest-png-become-a-member-svg-icon.png");
                            curCommand = "";
                        }
                    } else if (curCommand == "Buy") {
                        int v = Integer.valueOf(textField.getText());

                        textField.setText("");
                        textField.setEditable(false);
                        curCommand = "";

                        //coinBuyRequest(v, "bkash", "lol");

                        //joinWaitingRoomByCode(v);
                        //joinGameThreadByCode(v);
                        //joinByCode(v);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    private void setUpGui() {

        setTitle("User GUI");

        gameButtons = new JPanel();
        foldButton = new JButton("Fold");
        callButton = new JButton("Call");
        raiseButton = new JButton("Raise");
        allInButton = new JButton("All In");
        checkButton = new JButton("Check");
        exitButton = new JButton("Exit");

        gameButtons.setLayout(new BoxLayout(gameButtons, BoxLayout.X_AXIS));
        gameButtons.setPreferredSize(new Dimension(100, 50));

        foldButton.setBorder(new CompoundBorder(new LineBorder(gameButtons.getBackground(), 10), foldButton.getBorder()));
        callButton.setBorder(new CompoundBorder(new LineBorder(gameButtons.getBackground(), 10), callButton.getBorder()));
        raiseButton.setBorder(new CompoundBorder(new LineBorder(gameButtons.getBackground(), 10), raiseButton.getBorder()));
        allInButton.setBorder(new CompoundBorder(new LineBorder(gameButtons.getBackground(), 10), allInButton.getBorder()));
        checkButton.setBorder(new CompoundBorder(new LineBorder(gameButtons.getBackground(), 10), checkButton.getBorder()));
        exitButton.setBorder(new CompoundBorder(new LineBorder(gameButtons.getBackground(), 10), exitButton.getBorder()));

        allInButton.setEnabled(false);
        callButton.setEnabled(false);
        foldButton.setEnabled(false);
        raiseButton.setEnabled(false);
        checkButton.setEnabled(false);
        exitButton.setEnabled(false);

        gameButtons.add(allInButton);
        gameButtons.add(callButton);
        gameButtons.add(foldButton);
        gameButtons.add(raiseButton);
        gameButtons.add(checkButton);
        gameButtons.add(exitButton);


        textArea = new JTextArea();
        textArea.setEditable(false);
        addTextInGui("\n        Welcome to the User gui");

        areaPanel = new JScrollPane(textArea);
        areaPanel.setPreferredSize(new Dimension(100, 385));

        textField = new JTextField();
        textField.setEditable(false);


        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        centerPanel.add(areaPanel);
        centerPanel.add(gameButtons);
        centerPanel.add(textField);


        userButtons = new JPanel();
        logUser = new JButton("Login");
        userInfoButton = new JButton("Info");
        friendsButton = new JButton("Friends");
        joinGame = new JButton("Join");
        inviteButton = new JButton("Invite");
        buyButton = new JButton("Buy coins");
        clearButton = new JButton("Clear");
        closeButton = new JButton("Close");

        userButtons.setLayout(new BoxLayout(userButtons, BoxLayout.Y_AXIS));
        userButtons.setPreferredSize(new Dimension(100, 50));

        logUser.setBorder(new CompoundBorder(new LineBorder(userButtons.getBackground(), 10), logUser.getBorder()));
        userInfoButton.setBorder(new CompoundBorder(new LineBorder(userButtons.getBackground(), 10), userInfoButton.getBorder()));
        friendsButton.setBorder(new CompoundBorder(new LineBorder(userButtons.getBackground(), 10), friendsButton.getBorder()));
        joinGame.setBorder(new CompoundBorder(new LineBorder(userButtons.getBackground(), 10), joinGame.getBorder()));
        inviteButton.setBorder(new CompoundBorder(new LineBorder(userButtons.getBackground(), 10), inviteButton.getBorder()));
        buyButton.setBorder(new CompoundBorder(new LineBorder(userButtons.getBackground(), 10), buyButton.getBorder()));
        clearButton.setBorder(new CompoundBorder(new LineBorder(userButtons.getBackground(), 10), clearButton.getBorder()));
        closeButton.setBorder(new CompoundBorder(new LineBorder(userButtons.getBackground(), 10), closeButton.getBorder()));

        userInfoButton.setEnabled(false);
        friendsButton.setEnabled(false);
        joinGame.setEnabled(false);
        inviteButton.setEnabled(false);
        buyButton.setEnabled(false);

        userButtons.add(logUser);
        userButtons.add(userInfoButton);
        userButtons.add(friendsButton);
        userButtons.add(joinGame);
        userButtons.add(inviteButton);
        userButtons.add(buyButton);
        userButtons.add(clearButton);
        userButtons.add(closeButton);

        add(centerPanel, BorderLayout.CENTER);
        add(userButtons, BorderLayout.EAST);

        setResizable(false);
        setSize(600, 500);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void addTextInGui(String text) {
        String temp = textArea.getText();
        temp += "        " + text + "\n\n";
        textArea.setText(temp);
    }

    //=====================================================================================


    //=====================================================================================
    //
    //              JSON CODES
    //
    //
    //=====================================================================================

    private void requestFriendsList() {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "FriendsListRequest");

        sendMessage(send.toString());
    }

    private void showFriends(JSONArray friends) {

        String bleh = "Friends: \n";

        for (int i = 0; i < friends.length(); i++) {

            JSONObject tempJson = friends.getJSONObject(i);
            String tempString = "";

            tempString += tempJson.getString("username") + " ";
            tempString += tempJson.getInt("coinWon") + " ";
            tempString += tempJson.getInt("currentCoin") + " ";
            tempString += tempJson.getInt("level") + " ";


            bleh += tempString + "\n";
        }

        addTextInGui(bleh);
    }

    //=====================================================================================
    //
    //              JSON CODES
    //              FOR GAME THREAD
    //
    //=====================================================================================







    //====================================================================================
    //
    //      GAME BUTTON ON CLICK FUNCTIONS
    //
    //      SENDS GAME TURN REQUESTS TO SERVER
    //
    //      WHICH BUTTONS WILL BE ENABLED IS SENT BY THE SERVER
    //
    //      CHECKED IF VALID INPUTS WERE GIVEN IN RAISE BUTTON CALL
    //      OTHERWISE ALL BASIC
    //
    //====================================================================================

    private void clickedCallButton() {
        //String send = "";
        //call = "Call";
        //user.setCurrentCoin(user.getCurrentCoin() - minCallValue);

        sendGameThreadCallRequest();
    }

    private void clickedFoldButton() {
        String send = "";
        call = "Fold";
        //user.setCurrentCoin(user.getCurrentCoin() - foldCost);

        sendGameThreadFoldRequest();
    }

    private void clickedRaiseButton() {

        long y = -1, temp;

        String str = textField.getText();
        textField.setText("");

        try {
            temp = Long.valueOf(str);
            y = temp;
            //if (temp >= minCallValue && temp <= user.getCurrentCoin()) y = temp;
            //else addTextInGui("Integer must be between " + minCallValue + " " + user.getCurrentCoin());
        } catch (Exception e) {
            addTextInGui("Enter a valid number");
        }
        if (y == -1) return;

        //user.setCurrentCoin(user.getCurrentCoin() - y);

        call = "Raise";
        sendGameThreadRaiseRequest(y);
    }

    private void clickedCheckButton() {
        call = "Check";
        sendGameThreadCheckRequest();
    }

    private void clickedAllInButton() {
        call = "AllIn";
        long v = user.getCurrentCoin();

        //user.setCurrentCoin(0);
        sendGameThreadAllInRequest();
    }

    private void clickedExitButton() {

        requestExit();
    }


    //=====================================================================================
    //
    //
    //=====================================================================================


    public void check(int i){
        requestLogin("a", "guest", "nai", "bleh");

        try{
            Thread.sleep(500);
        }catch (Exception e){

        }
        //requestJoin(-1, -1, boardType[0], minEntryValue[0], minCallValue[0], -1, -1, 100000);
    }

}
