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
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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

    URI webSocketLink;                                      //      SOCKET LINK
    WebSocketClient webSocketClient;                        //      SOCKET
    private int port;                                       //      CONNECTION PORT
    private String host;                                    //      CONNECTION LINK
    //      UNNECESSARY
    //      USED FOR LOFFING IN
    private User user;                                      //      USER OBJECT IN CLIENT SIDE
    private int inpCount = 0;                               //      INPUT COUNT
    //      USED THIS TO COMMUNICATE WITH SERVER
    //      IGNORE AND USE JSON OBJECT


    //======================================================================================
    //
    //                  IN GAME DATA
    //
    //======================================================================================
    private String curCommand;                              //      CURRENT USER COMMANDS IN GUI
    private String msg = "";                                //      MSG STRING
    //      FOR PRIVATE GAME THREADS
    private int gameThreadId;                               //      GAME ID
    private int gameThreadCode;                             //      GAME CODE
    private String call;                                    //      CALL OF PLAYER IN CURRENT ROUND
    private int minCallValue;                               //      MINIMUM VALUE TO CALL IN CURRENT ROUND
    private int foldCost;                                   //      FOLD COST, FOR SMALL BLIND, BIG BLINDS
    private int boardCoin;                                  //      CURRENT COIN IN BOARD
    private int cycleCount;                                 //      CYCLE COUNT
    private int roundCount;                                 //      ROUND COUNTS IN CURRENT GAME THREAD

    //===============================================================================
    //
    //      NEW VARIABLES
    //
    //===============================================================================
    //      TO SHOW ANIMATION AT START OF THE ROUND
    private int turnCount;                                  //      TURN COUNT IN CURRENT ROUND
    //      EXAMPLE:    PLAYER A, PLAYER B
    //                  ROUND START
    //                  A CALLED, THEN B ER TURN
    //                  NOW TURNCOUNT = 2;
    private JSONObject jsonIncoming;
    private boolean hasConnected;                           //      IF CONNECTION IS MADE
    private int timeCounter;                            //      SECONDS COUNTER
    private Timer connectionCheckTimer;                     //      TIMER TO CHECK IF HAS CONNECTED


    //============================================================================
    //              INITIALIZING DONE
    //============================================================================


    //======================================================================================================
    //
    //              JFRAME GUI SHITS
    //              IGNORE AND DELETE THIS
    //
    //======================================================================================================

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


    public ClientToServer(URI link, int port) {


        setUpGui();
        guiFunctions();

        user = null;
        jsonIncoming = null;
        gameThreadId = -1;

        webSocketLink = link;
        webSocketClient = null;

        this.port = port;
        try {
            host = InetAddress.getLocalHost().getHostAddress();

        } catch (Exception e) {
            addTextInGui("Exception in fetching ip -> " + e);
        }

        createWebSocketClient();
        tryConnection();
    }

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
        webSocketClient.setConnectTimeout(1000);
        webSocketClient.enableAutomaticReconnection(1000);

    }


    //======================================================================
    //
    //      CONNECTION TIMER
    //
    //======================================================================

    private void tryConnection() {

        timeCounter = 60;
        hasConnected = false;
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
            timeCounter = -1;
            connectionCheckTimer.cancel();

            addTextInGui("Connection established with server");
        } else timeCounter--;

        if (timeCounter == 0) {
            timeCounter = -1;
            connectionCheckTimer.cancel();
            webSocketClient.close();

            addTextInGui("Cannot connect to server");
        }

    }

    //=====================================================================================
    //
    //
    //=====================================================================================


    //======================================================================
    //
    //      COMMUNICATION
    //
    //======================================================================

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


            if (jsonIncoming.getBoolean("response")) {

                //logged in successfully
                //editing gui buttons

                logUser.setText("Logout");
                userInfoButton.setEnabled(true);
                friendsButton.setEnabled(true);
                joinGame.setEnabled(true);
                inviteButton.setEnabled(true);
                buyButton.setEnabled(true);

                //making new users

                JSONObject tempJSON = jsonIncoming.getJSONObject("data");
                loadUser(tempJSON);
                addTextInGui("Welcome " + user.getUsername());
            } else {
                addTextInGui("Login failed!");
            }
        } else if (jsonIncoming.get("requestType").equals("LogoutResponse")) {
            if (jsonIncoming.getBoolean("response")) {

                //logged out successfully
                //gui options

                logUser.setText("Login");
                userInfoButton.setEnabled(false);
                friendsButton.setEnabled(false);
                joinGame.setEnabled(false);
                inviteButton.setEnabled(false);
                buyButton.setEnabled(false);

                addTextInGui("Logged out user " + user.getUsername());
                logoutUser();
            } else addTextInGui("Logout request failed");
        } else if (jsonIncoming.get("requestType").equals("BuyCoin")) {

            //requesting to buy coins

            receiveBuyCoinResponse(jsonIncoming);
        } else if (jsonIncoming.get("requestType").equals("FriendsList")) {

            //requesting friends list
            JSONArray friends = jsonIncoming.getJSONArray("data");

            showFriends(friends);
        } else if (jsonIncoming.get("requestType").equals("Join")) {

            //join in a game request

            addTextInGui(jsonIncoming.get("data").toString());
            joinGame.setText("Abort");
        } else if (jsonIncoming.get("requestType").equals("Abort")) {

            //abort game join request

            addTextInGui(jsonIncoming.get("data").toString());
            joinGame.setText("Join");
        } else if (jsonIncoming.get("requestType").equals("GameRoom")) {

            JSONObject tempJson = jsonIncoming.getJSONObject("gameData");

            if (tempJson.get("gameRequest").equals("LoadRoomData")) {

                loadRoomData(jsonIncoming.getJSONArray("data"));
            } else if (tempJson.get("gameRequest").equals("RoundStartMessage")) {

                roundInitialize();
                showRoundStartMessage(jsonIncoming.getString("data"));
            } else if (tempJson.get("gameRequest").equals("CheckCoinBothEnd")) {

                checkIfCoinMatch(jsonIncoming.getInt("data"));
            } else if (tempJson.get("gameRequest").equals("LoadPlayerCards")) {

                //cards came from game threads
                //load cards in user objects
                //decoded card data from strings

                decodeCards(jsonIncoming.getJSONArray("data"), 1);
            } else if (tempJson.get("gameRequest").equals("LoadBoardCards")) {

                //cards came from game threads
                //load cards in user objects
                //decoded card data from strings

                decodeCards(jsonIncoming.getJSONArray("data"), 0);
            } else if (tempJson.get("gameRequest").equals("BoardData")) {

                setBoardInfo(jsonIncoming.getJSONObject("data"));
            } else if (tempJson.get("gameRequest").equals("EnableGameButtons")) {

                enableGameButtons(jsonIncoming.getJSONArray("data"));
            } else if (tempJson.get("gameRequest").equals("DisableGameButtons")) {

                disableGameButtons();
            } else if (tempJson.get("gameRequest").equals("TurnInfo")) {

                showTurnInfo(jsonIncoming.getJSONObject("data"));
            } else if (tempJson.get("gameRequest").equals("CycleEnd")) {

                showEndCycle(jsonIncoming.getString("data"));
            } else if (tempJson.get("gameRequest").equals("WelcomeMessage")) {

                showWelcomeMessage(tempJson);
            } else if (tempJson.get("gameRequest").equals("Result")) {

                processResult(jsonIncoming.getJSONObject("data"));
            } else if (tempJson.get("gameRequest").equals("AllCards")) {

                showAllCards(jsonIncoming.getJSONObject("data"));
            } else if (tempJson.get("gameRequest").equals("WaitForPlayers")) {

                waitForPlayers();
            } else if (tempJson.get("gameRequest").equals("NextTurnInfo")) {

                showNextTurnInfo(jsonIncoming.getJSONObject("data"));
            }
        }

    }

    //=====================================================================================
    //
    //
    //=====================================================================================


    //===================================================================================
    //
    //      GAMETHREAD FUNCTIONS
    //
    //===================================================================================


    //================================================================================================
    //
    //      ENABLES/DISABLES GAME BUTTONS FROM INFORMATION SENT BY SERVER
    //      INFO CAME FROM
    //                          incomingMsg() function
    //                          gameThread YourTurn:
    //                          or
    //                          gameThread Disable:
    //                          types
    //
    //      ENABLING BUTTON EXAMPLE:
    //      STRING:             GameThread YourTurn :5 Fold bigBlind 10000 Call Raise Check AllIn
    //
    //      enableButtons( { 5, Fold, bigBlind, 10000, Call, Raise, Check, AllIn } )
    //
    //                      5 -> button count
    //
    //                      fold blinds came right after fold command
    //                      fold cost follows it
    //
    //                      Call value are set by GameThread BoardInfo in incomingMsg functions.
    //
    //===============================================================================================


    private void enableGameButtons(JSONArray temp) {

        String show = "";
        for (int i = 0; i < temp.length(); i++) {

            JSONObject jsonObject = temp.getJSONObject(i);

            if (jsonObject.getString("name").equals("Fold")) {

                foldButton.setEnabled(true);
                foldCost = jsonObject.getInt("cost");

                show += "You can fold";

                if (jsonObject.getString("blindType").equals("SmallBlind")) {
                    show += ", small blind, folding will cost " + foldCost;
                } else if (jsonObject.getString("blindType").equals("BigBlind")) {
                    show += ", big blind, folding will cost " + foldCost;
                }
                show += "\n";

            } else if (jsonObject.getString("name").equals("Call")) {
                show += "You can call, minimum value: " + minCallValue + "\n";
                callButton.setEnabled(true);
            } else if (jsonObject.getString("name").equals("Raise")) {
                show += "You can raise, minimum value: " + minCallValue + "\n";
                raiseButton.setEnabled(true);
                textField.setEditable(true);
            } else if (jsonObject.getString("name").equals("Check")) {
                show += "You can check\n";
                checkButton.setEnabled(true);
            } else if (jsonObject.getString("name").equals("AllIn")) {
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

    private void decodeCards(JSONArray temp, int cardLocation) {

        ArrayList location;
        if (cardLocation == 0) location = user.getBoardCards();
        else location = user.getPlayerCards();

        for (int i = 0; i < temp.length(); i++) {
            String x[] = temp.getString(i).split("\\.");

            Card card = new Card(Integer.valueOf(x[0]) - 1, Integer.valueOf(x[1]) - 2, cardLocation);
            location.add(card);
        }
    }

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

    private void checkIfWon(int winnerCount, int winAmount, String[] winners) {

        boolean didWin = false;

        for (int i = 0; i < winnerCount; i++) {
            if (winners[i].equals(user.getUsername())) {
                didWin = true;
                break;
            }
        }
        if (didWin == false) return;
        user.setCurrentCoin(user.getCurrentCoin() + winAmount);
    }

    private void processResult(JSONObject data) {


        String show = "Winners: ";

        int winnerCount = data.getJSONArray("winnerData").length();
        int winAmount = data.getInt("winAmount");


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
        checkIfWon(winnerCount, winAmount, winners);

        boolean showCards = data.getBoolean("showCards");

        show += winnerCount + " winners\n";
        for (int i = 0; i < winnerCount; i++) {
            show += winners[i];
            show += "\n";

            if (showCards == true) show += " -> " + Card.suitMessage(winnerCards[i], data.getInt("showWinLevel"));
        }
        if (winnerCount > 1) show += "\nTied between " + winnerCount + " players";

        addTextInGui(show);
    }

    //====================================================================================


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
        user.setCurrentCoin(user.getCurrentCoin() - minCallValue);

        sendGameThreadCallRequest();
    }

    private void clickedFoldButton() {
        String send = "";
        call = "Fold";
        user.setCurrentCoin(user.getCurrentCoin() - foldCost);

        sendGameThreadFoldRequest();
    }

    private void clickedRaiseButton() {

        int y = -1, temp;

        String str = textField.getText();
        textField.setText("");

        try {
            temp = Integer.valueOf(str);
            if (temp >= minCallValue && temp <= user.getCurrentCoin()) y = temp;
            else addTextInGui("Integer must be between " + minCallValue + " " + user.getCurrentCoin());
        } catch (Exception e) {
            addTextInGui("Enter a valid number");
        }
        if (y == -1) return;

        user.setCurrentCoin(user.getCurrentCoin() - y);

        call = "Raise";
        sendGameThreadRaiseRequest(y);
    }

    private void clickedCheckButton() {
        call = "Check";
        sendGameThreadCheckRequest();
    }

    private void clickedAllInButton() {
        call = "AllIn";
        int v = user.getCurrentCoin();

        user.setCurrentCoin(0);
        sendGameThreadAllInRequest(v);
    }


    //=====================================================================================
    //
    //
    //=====================================================================================


    //===================================================================================
    //
    //              GUI ON CLICK BUTTONS
    //
    //===================================================================================

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
        addTextInGui(user.toString());
    }


    //=====================================================================================
    //      REQUESTS FRIENDLIST TO SERVER
    //
    //      SAVE FRIENDLIST FROM SERVER WHEN LOGGING IN
    //      THEN IF ANY CHANGE MADE, SEND DATA TO SERVER
    //
    //=====================================================================================

    private void friendListRequest() {
        curCommand = "Friends";
        requestFriendsList();
    }


    //====================================================================================
    //      REQUEST TO JOIN A GAME
    //
    //
    //===================================================================================

    private void joinClick() {
        if (joinGame.getText().equals("Join")) {
            joinRequest(true);
            curCommand = "Join";
        } else if (joinGame.getText().equals("Abort")) {
            joinRequest(false);
            curCommand = "Abort";
        }
    }


    //======================================================================================
    //
    //      BUY COIN REQUEST
    //      REQUEST COMPLETED IN guiFunctions() {}
    //                           using addActionListener on textFields
    //
    //=======================================================================================

    private void buyCoinClick() {
        curCommand = "Buy";
        textField.setEditable(true);
    }

    //
    //
    //=======================================================================================


    //======================================================================================
    //
    //
    //              CLOSING CONNECTION
    //
    //
    //======================================================================================

    private void closeEverything() {

        try {
            this.dispose();
        } catch (Exception e) {
            addTextInGui("Error in closing connection in Client side, error -> " + e);
        }
    }

    private void closeButtonClicked() {
        String ret = "";
        ret = "Close";
        requestClose();
    }

    //=======================================================================================
    //
    //=======================================================================================


    //===========================================================================================
    //
    //      GUI SHITS, IGNORE
    //
    //===========================================================================================

    private void guiFunctions() {

        logUser.addActionListener(e -> logUserClick());
        userInfoButton.addActionListener(e -> infoUserClick());
        friendsButton.addActionListener(e -> friendListRequest());
        joinGame.addActionListener(e -> joinClick());
        buyButton.addActionListener(e -> buyCoinClick());
        clearButton.addActionListener(e -> textArea.setText(""));
        closeButton.addActionListener(e -> closeButtonClicked());

        callButton.addActionListener(e -> clickedCallButton());
        foldButton.addActionListener(e -> clickedFoldButton());
        raiseButton.addActionListener(e -> clickedRaiseButton());
        checkButton.addActionListener(e -> clickedCheckButton());
        allInButton.addActionListener(e -> clickedAllInButton());

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
                            requestLogin(msg);
                            curCommand = "";
                        }
                    } else if (curCommand == "Buy") {
                        int v = Integer.valueOf(textField.getText());

                        textField.setText("");
                        textField.setEditable(false);
                        curCommand = "";

                        coinBuyRequest(v, "Bkash", "lol");

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

    private JSONObject initiateRequest() {

        JSONObject temp = new JSONObject();

        temp.put("sender", "Client");
        temp.put("ip", host);
        temp.put("port", port);

        return temp;
    }

    private void loadUser(JSONObject tempJSON) {

        user = new User(tempJSON.getString("username"), tempJSON.getString("password"), tempJSON.getInt("currentCoin"), tempJSON.getInt("coinWon"));
    }

    private void logoutUser() {
        user = null;
    }

    private void receiveBuyCoinResponse(JSONObject temp) {
        int value = jsonIncoming.getInt("currentCoin");

        user.setCurrentCoin(value);
        addTextInGui(jsonIncoming.getString("responseMsg"));
    }


    private void requestLogin(String data) {

        JSONObject send = initiateRequest();
        send.put("requestType", "LoginRequest");


        String[] temp = data.split(" ");
        String username = temp[1];
        String password = temp[2];

        JSONObject tempJson = new JSONObject();

        tempJson.put("username", username);
        tempJson.put("password", password);

        send.put("data", tempJson);

        sendMessage(send.toString());
    }

    private void coinBuyRequest(int value, String method, String trId) {

        JSONObject send = initiateRequest();

        send.put("username", user.getUsername());
        send.put("requestType", "BuyCoin");

        JSONObject tempJson = new JSONObject();

        tempJson.put("method", method);
        tempJson.put("transactionId", trId);
        tempJson.put("value", value);

        send.put("data", tempJson);

        sendMessage(send.toString());
    }

    private void sendLogoutRequest() {

        JSONObject send = initiateRequest();

        send.put("username", user.getUsername());
        send.put("requestType", "LogoutRequest");

        sendMessage(send.toString());
    }

    private void requestFriendsList() {

        JSONObject send = initiateRequest();

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

    private void joinRequest(boolean isJoin) {

        JSONObject send = initiateRequest();

        send.put("username", user.getUsername());
        send.put("requestType", "JoinRequest");


        JSONObject tempJson = new JSONObject();

        tempJson.put("requestIn", isJoin);
        tempJson.put("roomCode", -1);

        send.put("data", tempJson);

        sendMessage(send.toString());
    }

    private void requestClose() {

        /*
        JSONObject send = new JSONObject();

        send.put("sender", "Client");
        send.put("ip", socket.getLocalAddress().getHostAddress());
        send.put("port", socket.getPort());

        if(user != null) send.put("username", user.getUsername());
        else send.put("username", "");

        send.put("requestType", "closeRequest");

        sendMessage(send.toString());
        */
    }


    //=====================================================================================
    //
    //              JSON CODES
    //              FOR GAME THREAD
    //
    //=====================================================================================


    //=======================================================================================
    //
    //
    //
    //=======================================================================================

    private void loadRoomData(JSONArray data) {

        System.out.println("Loading room data");
        System.out.println(data);


    }

    private void showRoundStartMessage(String msg) {                     //ROUND STARTING E

        addTextInGui(msg);


    }

    private void checkIfCoinMatch(int value) {

        int serverSideCoin = value;

        if (user.getCurrentCoin() == serverSideCoin) addTextInGui("Coin count in both end, okay");
        else if (user.getCurrentCoin() != serverSideCoin) addTextInGui("Something is wrong, you will be banned");
    }

    private void setBoardInfo(JSONObject data) {

        boardCoin = data.getInt("boardCoin");
        roundCount = data.getInt("roundCount");
        turnCount = data.getInt("turnCount");
        cycleCount = data.getInt("cycleCount");
        minCallValue = data.getInt("minimumCallValue");

        String show = "round " + roundCount + " board-coin " + boardCoin + " turn: " + turnCount + " cycle: " + cycleCount + " round-call: " + minCallValue;
        addTextInGui(show);
        showCards();
    }

    private void roundInitialize() {

        user.getPlayerCards().clear();
        user.getBoardCards().clear();
    }

    private void showTurnInfo(JSONObject temp) {

        String username = temp.getString("username");
        int seatPosition = temp.getInt("seatPosition");
        String call = temp.getString("call");
        int value = temp.getInt("value");

        String show = username + " " + call + "ed with value " + value + ", seatPosition " + seatPosition;
        addTextInGui(show);
    }

    private void showEndCycle(String msg) {

        addTextInGui(msg);
    }

    private void showWelcomeMessage(JSONObject jsonObject) {             //GAME STARTING E

        String show;

        gameThreadId = jsonObject.getInt("id");
        gameThreadCode = jsonObject.getInt("code");

        show = "Welcome to game room!\n";
        show += "Game id: " + jsonObject.getInt("id") + " game code: " + jsonObject.getInt("code");

        addTextInGui(show);
    }

    private void showAllCards(JSONObject jsonObject) {

        String show;
        JSONArray temp;

        show = "All board cards: \n";
        temp = jsonObject.getJSONArray("allBoardCards");

        for (int i = 0; i < temp.length(); i++) show += temp.getString(i) + "\n";
        show += "\n";


        show += "All player cards: \n\n";
        temp = jsonObject.getJSONArray("allPlayerCards");

        for (int i = 0; i < temp.length(); i++) {

            JSONObject tempObject = temp.getJSONObject(i);
            JSONArray tempArray = tempObject.getJSONArray("cards");

            show += "username: " + tempObject.getString("username") + " seat: " + tempObject.getInt("seatPosition") + "\n";
            show += tempArray.getString(0) + "\n";
            show += tempArray.getString(1) + "\n\n";
        }

        addTextInGui(show);
    }

    private void waitForPlayers() {

        disableGameButtons();

        String show = "Wait for players to buy coins";
        addTextInGui(show);

    }

    private void showNextTurnInfo(JSONObject jsonObject) {

        String username = jsonObject.getString("username");
        int pos = jsonObject.getInt("seatPosition");

        String show = username + "'s turn, seat position " + pos;
        addTextInGui(show);

    }


    //=====================================================================================
    //
    //              GAMETHREAD JSON CODES
    //
    //
    //=====================================================================================

    public void sendGameThreadCallRequest() {

        JSONObject send = initiateRequest();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("gameId", gameThreadId);
        tempJson.put("gameCode", gameThreadCode);
        tempJson.put("roundCount", roundCount);
        tempJson.put("turnCount", turnCount);

        JSONObject tempJson2 = new JSONObject();

        tempJson2.put("call", call);
        tempJson2.put("cost", minCallValue);

        tempJson.put("callData", tempJson2);

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }

    public void sendGameThreadRaiseRequest(int value) {

        JSONObject send = initiateRequest();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("gameId", gameThreadId);
        tempJson.put("gameCode", gameThreadCode);
        tempJson.put("roundCount", roundCount);
        tempJson.put("turnCount", turnCount);

        JSONObject tempJson2 = new JSONObject();

        tempJson2.put("call", call);
        tempJson2.put("cost", value);

        tempJson.put("callData", tempJson2);

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }

    public void sendGameThreadAllInRequest(int value) {

        JSONObject send = initiateRequest();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("gameId", gameThreadId);
        tempJson.put("gameCode", gameThreadCode);
        tempJson.put("roundCount", roundCount);
        tempJson.put("turnCount", turnCount);

        JSONObject tempJson2 = new JSONObject();

        tempJson2.put("call", call);
        tempJson2.put("cost", value);

        tempJson.put("callData", tempJson2);

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }

    public void sendGameThreadCheckRequest() {

        JSONObject send = initiateRequest();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("gameId", gameThreadId);
        tempJson.put("gameCode", gameThreadCode);
        tempJson.put("roundCount", roundCount);
        tempJson.put("turnCount", turnCount);

        JSONObject tempJson2 = new JSONObject();

        tempJson2.put("call", call);
        tempJson2.put("cost", 0);

        tempJson.put("callData", tempJson2);

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }

    public void sendGameThreadFoldRequest() {

        JSONObject send = initiateRequest();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("gameId", gameThreadId);
        tempJson.put("gameCode", gameThreadCode);
        tempJson.put("roundCount", roundCount);
        tempJson.put("turnCount", turnCount);

        JSONObject tempJson2 = new JSONObject();

        tempJson2.put("call", call);
        tempJson2.put("cost", foldCost);

        tempJson.put("callData", tempJson2);

        send.put("gameData", tempJson);

        sendMessage(send.toString());
    }


}
