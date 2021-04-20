package Thread;


import Objects.Card;
import Objects.User;
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

    public static int boardTypeCount = 5;
    public static long minCallValue[] = {10000, 10000, 10000, 10000, 10000};
    public static String boardType[] = {"board1", "board2", "board3", "board4", "board5"};
    public static long minEntryValue[] = {100000, 100000, 100000, 100000, 100000};



    URI webSocketLink;                                      //      SOCKET LINK
    WebSocketClient webSocketClient;                        //      SOCKET
    private int port;                                       //      CONNECTION PORT
    private String host;                                    //      CONNECTION LINK
    //      UNNECESSARY
    //      USED FOR LOFFING IN
    private User user;                                      //      USER OBJECT IN CLIENT SIDE

    private JSONObject jsonIncoming;                        //      INCOMING JSON DATA
    private boolean hasConnected;                           //      IF CONNECTION IS MADE
    private Timer connectionCheckTimer;                     //      TIMER TO CHECK IF HAS CONNECTED


    private int tryConnectionTimeCounter;                   //      SECONDS COUNTER
    private int connectionTimeOut;                          //      WEB SOCKET CONNECTION TIMEOUT
    private int reconnectionTimeOut;                        //      WEB SOCKET RECONNECTION TIMEOUT


    private String curCommand;                              //      CURRENT USER COMMANDS IN GUI
    private String msg = "";                                //      MSG STRING


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


    public ClientToServer(URI link, int port, int tryConnectionTimeCounter, int connectionTimeOut, int reconnectionTimeOut) {


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

        this.tryConnectionTimeCounter = tryConnectionTimeCounter;
        this.connectionTimeOut = connectionTimeOut;
        this.reconnectionTimeOut = reconnectionTimeOut;
        hasConnected = false;

        curCommand = "";
        msg = "";

        createWebSocketClient();
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
            }

            @Override
            public void onTextReceived(String s) {

                incomingMsg(s);
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
            }

            @Override
            public void onCloseReceived() {

            }
        };
        webSocketClient.setConnectTimeout(connectionTimeOut);
        webSocketClient.enableAutomaticReconnection(reconnectionTimeOut);

    }

    private void tryConnection() {

        connectionCheckTimer = new Timer();
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

            System.out.println("Sending -> " + temp);
            webSocketClient.send(temp);

        } catch (Exception e) {
            addTextInGui("Error in sending msg to server -> " + e);
        }
    }

    private void incomingMsg(String temp) {

        try {
            jsonIncoming = new JSONObject(temp);
            System.out.println(jsonIncoming);

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
        else if (jsonIncoming.get("requestType").equals("BuyCoinResponse")) {

            //requesting to buy coins

            receiveBuyCoinResponse(jsonIncoming);
            //gameStartIfInAGame();
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
        }
        else if (jsonIncoming.getString("requestType").equals("WaitingRoom")) {

            JSONObject tempJson = jsonIncoming.getJSONObject("waitingRoomData");

            System.out.println("Waiting room er info ashchhe");
            System.out.println(jsonIncoming);

            if (tempJson.getString("requestType").equals("SetWaitingRoomData")) {

                initiateWaitingRoom(jsonIncoming);
            }
            else if (tempJson.getString("requestType").equals("JoinWaitingRoom")) {

                showInvitationInfo(tempJson);
                acceptWaitingRoomInvitation();
            }
            else if (tempJson.getString("requestType").equals("JoinWaitingRoomResponse")) {

                showWaitingRoomJoinResponse(tempJson.getBoolean("success"), tempJson.getInt("gameCode"), tempJson.getString("message"));
            }
            else if (tempJson.getString("requestType").equals("RemoveFromWaitingRoomResponse")) {

                String message = tempJson.getString("message");
                removedFromWaitingRoom(message);
            }
            else if (tempJson.getString("requestType").equals("AllRoomData")) {

                loadWaitingRoomData(jsonIncoming);
            }
            else if (tempJson.get("requestType").equals("AskJoinWaitingRoomByCodeResponse")) {

                askJoinWaitingRoomByCodeResponse(jsonIncoming);
            }
            else if (tempJson.get("requestType").equals("ApproveJoinByCodeRequest")) {

                approveJoinByCodeRequest(jsonIncoming);
            }
            else if (tempJson.get("requestType").equals("StartGameResponse")) {

                showStartGameResponse(jsonIncoming);
            }

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

    private void requestLogin(String account_data, String account_type, String username) {

        JSONObject send = initiateJson();
        send.put("requestType", "LoginRequest");

        JSONObject tempJson = new JSONObject();

        tempJson.put("account_id", account_data);
        tempJson.put("account_type", account_type);
        tempJson.put("account_username", username);

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

    private void closeEverything() {

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

    private void receiveBuyCoinResponse(JSONObject temp) {

        boolean success = jsonIncoming.getBoolean("success");
        long value = jsonIncoming.getLong("currentCoin");

        if (success) user.setCurrentCoin(value);
        addTextInGui(jsonIncoming.getString("message"));
    }

    private void coinBuyRequest(long value, String method, String trId) {

        JSONObject send = initiateJson();

        send.put("id", user.getId());
        send.put("username", user.getUsername());
        send.put("requestType", "BuyCoinRequest");

        JSONObject tempJson = new JSONObject();

        tempJson.put("method", method);
        tempJson.put("transactionId", trId);
        tempJson.put("value", value);

        send.put("data", tempJson);

        sendMessage(send.toString());
    }

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

        for (int i = 0; i < temp.length(); i++) {

            JSONObject jsonObject = temp.getJSONObject(i);

            if (jsonObject.getString("name").equals("Fold")) {

                foldButton.setEnabled(true);

                show += "You can fold";

                if (jsonObject.getString("blindType").equals("SmallBlind")) {
                    show += ", small blind, folding will cost " + user.getFoldCost();

                }
                else if (jsonObject.getString("blindType").equals("BigBlind")) {
                    show += ", big blind, folding will cost " + user.getFoldCost();

                }
                show += "\n";

            }
            else if (jsonObject.getString("name").equals("Call")) {

                show += "You can call, minimum value: " + user.getRoundCall() + "\n";
                callButton.setEnabled(true);
            }
            else if (jsonObject.getString("name").equals("Raise")) {

                show += "You can raise, minimum value: " + user.getRoundCall() + "\n";
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
        JSONArray winnerData = data.getJSONArray("winnerData");

        boolean showCardsAtEnd = data.getBoolean("showCards");
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
        User.loadOwnSelfFromInGamePlayers(user);
    }

    private void processResult(JSONObject jsonObject) {

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

        addTextInGui(show);
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

        tempJson.put("gameId", gameThreadId);
        tempJson.put("gameCode", gameThreadCode);
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

    //===================================================================================
    //
    //      showCards:
    //
    //          SHOWS LOADED BOARD CARDS, PLAYER CARDS IN GUI
    //
    //          BOARD CARDS COME FROM SERVER ONE BY ONE IN EACH TURN
    //          TO ENSURE GAME SECURITY
    //
    //      decodeCards:
    //
    //          LOADS CARDS IN USER OBJECTS FROM DATA CAME FROM SERVER
    //
    //          CARD VALUES CAME WITH ACTUAL VALUE
    //              BUT WE MAKE CARDS WITH SUIT RANGE 0-3, VALUE RANGE 0-12
    //              WHERE AS ACTUAL RANGE IN CARD OBJECT IS 1-4, VALUE RANGE 2-14
    //
    //===================================================================================

    //==================================================================================


    //==================================================================================
    //
    //          CHECK IF SERVER SIDE COIN, CLIENT COIN MATCHES
    //          AT START OF EVERY ROUND
    //
    //==================================================================================


    //=================================================================================


    //==============================================================================================================================
    //
    //      RESULTS CAME, CHECK IF THIS USER WON
    //
    //      ALSO USE THE STRING TO GENERATE RESULT MESSAGE
    //
    //
    //      RESULT STRING(INPUT OF processResultString):
    //
    //      2 20000 ASIF 3.(7,3,1).(7,2,0).(7,1,0).(13,4,0).(11,1,0) ASIF2 3.(7,3,1).(7,2,0).(7,1,0).(13,4,0).(11,1,0) true 3
    //
    //      2           ->   WINNER COUNT
    //
    //      20000       ->   COIN WON BY EACH USER
    //
    //      ASIF        ->   FIRST WINNER WITH POWER STRING     ->    3.(7,3,1).(7,2,0).(7,1,0).(13,4,0).(11,1,0)
    //
    //      ASIF2       ->   SECOND WINNER WITH POWER STRING    ->    3.(7,3,1).(7,2,0).(7,1,0).(13,4,0).(11,1,0)
    //
    //      true        ->   SHOW RESULT CARDS
    //                       DONT SHOW IF OTHERWISE         IF EVERYONE FOLDS EXCEPT ONE PLAYER OR SOME OTHER CASE
    //
    //      3           ->   WINNER FOUND AT LEVEL 3
    //                       MEANS WINNER WAS FOUND AT 3RD KEY
    //                       WHILE ITERATING THE POWER STRING
    //
    //
    //===============================================================================================================================

    //====================================================================================





    //===================================================================================
    //
    //              GUI ON CLICK BUTTONS
    //
    //===================================================================================

    //=====================================================================================
    //      REQUESTS FRIENDLIST TO SERVER
    //
    //      SAVE FRIENDLIST FROM SERVER WHEN LOGGING IN
    //      THEN IF ANY CHANGE MADE, SEND DATA TO SERVER
    //
    //=====================================================================================


    //====================================================================================
    //      REQUEST TO JOIN A GAME
    //
    //
    //===================================================================================

    private void createWaitingRoom(ArrayList<String> friends, String boardName, int minEntryCoin, int entryCoin) {

        sendCreateWaitingRoomRequest(friends, boardName, minEntryCoin, entryCoin);
    }

    private void loadWaitingRoomData(JSONObject temp) {

        showWaitingRoomData(temp);
    }

    private void addInWaitingRoom(ArrayList<String> friends) {

        sendAddInWaitingRoomRequest(friends);
    }

    private void removeFromWaitingRoom(ArrayList<String> friends) {

        sendRemoveFromWaitingRoomRequest(friends);
    }

    private void requestExitFromWaitingRoom() {

        sendRequestExitFromWaitingRoom();
        removedFromWaitingRoom(null);
        //Then exit
    }

    private void startGameFromWaitingRoom() {

        sendStartGameRequest();
    }

    private void showInvitationInfo(JSONObject data) {

        tempCode = data.getInt("gameCode");
        tempBoard = data.getString("boardType");
        tempEntryValue = data.getInt("minEntryAmount");
        tempMinCallValue = data.getInt("minCallValue");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                tempCode = -1;
                tempBoard = "";
                tempEntryValue = 0;
                tempMinCallValue = 0;

            }
        }, 10000);

        addTextInGui(data.toString());
    }

    private void acceptWaitingRoomInvitation() {

        if (tempCode == -1) return;
        sendWaitingRoomInvitationAccept();
    }

    private void joinWaitingRoomByCode(int code) {

        sendJoinWaitingRoomByCodeRequest(code);
    }

    private void askJoinWaitingRoomByCodeResponse(JSONObject temp) {

        JSONObject waitingRoomData = temp.getJSONObject("waitingRoomData");

        boolean asking = waitingRoomData.getBoolean("success");
        String msg = waitingRoomData.getString("message");

        addTextInGui(msg);

    }

    private void removedFromWaitingRoom(String msg) {

        deinitiateWaitingRoom();
        if (msg != null) addTextInGui(msg);
    }

    private void approveJoinByCodeRequest(JSONObject temp) {

        JSONObject waitingRoomData = temp.getJSONObject("waitingRoomData");

        String username = waitingRoomData.getString("username");
        String msg = waitingRoomData.getString("message");

        addTextInGui(msg);

        ArrayList tempUser = new ArrayList<String>();
        tempUser.add(username);

        addInWaitingRoom(tempUser);

    }

    //===========================================================================================
    //
    //===========================================================================================

    private void deinitiateWaitingRoom() {

        owner = "";

        gameThreadId = -1;
        gameThreadCode = -1;
        roomEntryValue = -1;
        gameRoomType = "";
        user.setSeatPosition(-1);
    }

    private void initiateWaitingRoom(JSONObject temp) {

        owner = temp.getString("owner");

        JSONObject tempJson = temp.getJSONObject("waitingRoomData");

        gameThreadId = tempJson.getInt("gameId");
        gameThreadCode = tempJson.getInt("gameCode");
        roomEntryValue = tempJson.getInt("userEntryAmount");
        gameRoomType = tempJson.getString("boardType");
        user.setSeatPosition(tempJson.getInt("seatPosition"));
    }

    private void sendCreateWaitingRoomRequest(ArrayList<String> friends, String boardName, int minEntryCoin, int entryCoin) {

        JSONObject send = initiateJson();

        send.put("owner", user.getUsername());
        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "Create");
        tempJson.put("gameId", user.getGameId());
        tempJson.put("gameCode", user.getGameCode());
        tempJson.put("boardType", boardName);
        tempJson.put("entryAmount", minEntryCoin);
        tempJson.put("userEntryAmount", entryCoin);

        JSONArray tempJson2 = new JSONArray();

        for (int i = 0; i < friends.size(); i++) tempJson2.put(friends.get(i));
        tempJson.put("roomData", tempJson2);

        send.put("waitingRoomData", tempJson);
        sendMessage(send.toString());
    }

    private void sendWaitingRoomInvitationAccept() {

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "AcceptInvitation");
        tempJson.put("gameCode", tempCode);
        tempJson.put("boardType", tempBoard);
        tempJson.put("userEntryAmount", 0);

        send.put("waitingRoomData", tempJson);

        System.out.println("Accept kortese");
        System.out.println(send.toString());
        sendMessage(send.toString());
    }

    private void showWaitingRoomJoinResponse(boolean succes, int code, String msg) {

        String show;

        show = msg + "\n";
        show += "Room code " + code;

        addTextInGui(show);
    }

    private void sendAddInWaitingRoomRequest(ArrayList<String> friends) {

        JSONObject send = initiateJson();

        send.put("owner", user.getUsername());
        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "AddInWaitingRoom");

        JSONArray tempJson2 = new JSONArray();

        for (int i = 0; i < friends.size(); i++) tempJson2.put(friends.get(i));
        tempJson.put("roomData", tempJson2);

        send.put("waitingRoomData", tempJson);
        sendMessage(send.toString());
    }

    private void sendRemoveFromWaitingRoomRequest(ArrayList<String> friends) {

        JSONObject send = initiateJson();

        send.put("owner", user.getUsername());
        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "RemoveFromWaitingRoom");

        JSONArray tempJson2 = new JSONArray();

        for (int i = 0; i < friends.size(); i++) tempJson2.put(friends.get(i));
        tempJson.put("roomData", tempJson2);

        send.put("waitingRoomData", tempJson);
        sendMessage(send.toString());
    }

    private void sendRequestExitFromWaitingRoom() {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "RemoveMeFromWaitingRoom");

        send.put("waitingRoomData", tempJson);
        sendMessage(send.toString());
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

    private void showWaitingRoomData(JSONObject temp) {

        System.out.println(temp);
    }

    private void sendStartGameRequest() {

        JSONObject send = initiateJson();

        send.put("owner", owner);
        send.put("requestType", "WaitingRoom");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "StartGame");

        send.put("waitingRoomData", tempJson);
        sendMessage(send.toString());
    }

    private void showStartGameResponse(JSONObject temp) {

        JSONObject waitingRoomData = temp.getJSONObject("waitingRoomData");

        boolean success = waitingRoomData.getBoolean("success");
        String msg = waitingRoomData.getString("message");

        addTextInGui(msg + "\nsuccess " + success);
    }

    //===========================================================================================
    //
    //===========================================================================================

















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
            textField.setEditable(true);
            inpCount = 2;
            curCommand = "Login";

            addTextInGui("Enter username and then password");
        }
    }

    private void infoUserClick() {
        curCommand = "UserInfo";
        addTextInGui(User.UserToJsonInGame(user).toString());
    }

    private void joinClick() {

        if (joinGame.getText().equals("Join")) {
            requestJoin(-1, -1, boardType[0], minEntryValue[0], minCallValue[0], -1, -1, 100000);
            curCommand = "Join";
        } else if (joinGame.getText().equals("Abort")) {
            requestAbort();
            curCommand = "Abort";
        }
    }

    private void inviteButtonClick() {

        //requestUpdateOwnInGame();

        //addFreeCoinRequest();

        /*
        ArrayList temp = new ArrayList<String>();

        temp.add("b");
        temp.add("c");
        temp.add("d");

        createWaitingRoom(temp, "board1", 100000, 100000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startGameFromWaitingRoom();
            }
        }, 15000);
        */
    }

    private void friendsButtonClicked() {
        curCommand = "Friends";
        requestFriendsList();
    }

    private void closeButtonClicked() {
        String ret = "";
        ret = "Close";
        closeEverything();
    }

    private void buyCoinClick() {
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
                            String username = temp[1];
                            String password = temp[2];

                            requestLogin(username, password, "");
                            curCommand = "";
                        }
                    } else if (curCommand == "Buy") {
                        int v = Integer.valueOf(textField.getText());

                        textField.setText("");
                        textField.setEditable(false);
                        curCommand = "";

                        sendAddBoardCoinInGameRequest(v);

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







    private void gameStartIfInAGame() {

        //KONO GAME E NAI
        if (gameThreadId == -1) return;
        if (gameRunning == true) return;

        sendTryStartCurrentGameRequest();
    }






    private void roundInitialize() {

        user.getPlayerCards().clear();
        user.getBoardCards().clear();
    }

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
        String send = "";
        call = "Call";
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






}
