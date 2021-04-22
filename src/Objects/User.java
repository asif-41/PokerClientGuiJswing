package Objects;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Math.sqrt;

public class User {


    //========================================================
    //          THIS IS SERVER SIDE USER OBJECT
    //     SOME UNNECESSARY FUNCTIONS ARE OMITTED HERE
    //========================================================


    //============================================================================
    //
    //              INITIALIZING PARAMETERS
    //
    //============================================================================

    private static final long expIncrease = 25;
    public static final String rankString[] = {"Newbie", "Beginner", "Amateur", "Semi-Pro", "Pro", "World-Class", "Legendary"};
    private static final long ranksValue[] = {0, 500, 2000, 7500, 20000, 50000};

    private String username;            //      USERNAME
    private int id;                     //      USER ID
    private Date DOB;                   //      DATE OF BIRTH
    private int age;                    //      AGE
    private String fb_id;               //      USER FB ID
    private String gmail_id;            //      USER GMAIL ID
    private String loginMethod;         //      LOGIN METHOD OF USER
    private boolean isLoggedIn;         //      IS CURRENTLY LOGGED IN
    private long exp;                    //      EXP
    private int roundsWon;              //      ROUNDS WON IN WHOLE CAREER
    private int roundsPlayed;           //      ROUNDS PLAYED IN WHOLE CAREER
    private long currentCoin;            //      CURRENT COINS
    private int winStreak;              //      CURRENT WIN STREAK
    private int level;                  //      LEVEL GENERATED FROM COIN WON IN WHOLE CAREER
    private long coinWon;                //      COIN WON IN WHOLE CAREER
    private long coinLost;               //      COIN LOST IN WHOLE CAREER
    private double winPercentage;       //      WIN PERCENTAGE GENERATED FROM ROUNDS WON, ROUNDS PLAYED
    private String rank;                //      RANK OF CURRENT USER
    private int totalCallCount;         //      TOTAL CALL COUNT OF USER
    private int callCount;              //      CALL COUNT OF USER
    private int raiseCount;             //      RAISE COUNT OF USER
    private int foldCount;              //      FOLD COUNT OF USER
    private int allInCount;             //      ALL IN COUNT OF USER
    private int checkCount;             //      CHECK COUNT OF USER
    private int coinVideoCount;        //      COUNT OF COIN VIDEO AVAILABILITY
    private Date lastCoinVideoAvailableTime;    //  LAST COIN VIDEO AVAILABLE IN TIME
    private Date lastLoggedInTime;      //      LAST LOGGED IN TIME
    private Date lastFreeCoinTime;      //      LAST FREE COIN AVAIABLE IN TIME
    private Date currentLoginTime;      //      CURRENT SESSION LOGIN TIME

    private long biggestWin;            //      BIGGEST POT WIN
    private String bestHand;            //      BEST HAND

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
    private int gameId;                 //      GAME ROOM ID
    private int gameCode;               //      GAME ROOM CODE
    private String boardType;           //      ROOM TYPE
    private int maxPlayerCount;         //      MAX PLAYER COUNT IN A GAME
    private long minEntryValue;          //      MIN ENTRY COIN AMOUNT
    private int seatPosition;           //      SEAT POSITION IN GAME
    private long minCallValue;           //      MIN CALL VALUE
    private int owner_id;               //      OWNER ID OF CURRENT GAME OR ROOM

    private long boardCoin;              //      ENTRY TO BOARD WITH THIS AMOUNT OF COIN
    private long callValue;              //      CALL VALUE OF THIS USER
    private long totalCallValue;        //      TOTAL CALL VALUE OF THIS USER
    private String call;                //      CALL FOR USER IN GAME

    private long foldCost;               //      FOLD COST FOR THIS USER
    private int cycleCount;             //      CYCLE COUNT OF GAME
    private int roundCount;             //      ROUND COUNT OF GAME
    private int turnCount;              //      TURN COUNT IN CURRENT ROUND
    private long roundCoins;             //      ROUND COINS GIVEN IN THE BOARD
    private long roundCall;              //      MINIMUM CALL VALUE OF CURRENT ROUND
    private int roundIteratorSeat;      //      CURRENT PLAYER ER SEAT
    private int roundStarterSeat;       //      ROUND STARTER SEAT
    private int smallBlindSeat;         //      LOCATION OF PLAYER WHO WILL HAVE SMALL BLIND
    private int bigBlindSeat;           //      LOCATION OF PLAYER WHO WILL HAVE BIG BLIND
    private boolean gameRunning;        //      GAME RUNNING OR NOT
    private int playerCount;            //      PLAYER COUNT IN GAME

    private User[] inGamePlayers;         //      IN GAME PLAYERS DATA STORED HERE
    private User[] inWaitingRoomPlayers;    //      IN WAITING ROOM PLAYERS

    private int tempId;                 //      INVITATION ID
    private int tempCode;               //      INVITATION CODE
    private String tempBoardType;       //      INVITATION BOARD
    private long tempMinEntryValue;      //      INVITATION ENTRY VALUE
    private long tempMinCallValue;       //      INTVITATION CALL VALUE


    //============================================================================
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

    public User(int id, String username, Date DOB, int age, String fb_id, String gmail_id, String loginMethod,
                long exp,
                long currentCoin, long coinWon, long coinLost, int roundsWon, int roundsPlayed,
                int winStreak, int totalCallCount, int callCount, int raiseCount,
                int foldCount, int allInCount, int checkCount, long biggestWin, String bestHand, int coinVideoCount, Date lastCoinVideoAvailableTime,
                Date lastLoggedInTime, Date lastFreeCoinTime, Date currentLoginTime) {

        this.id = id;
        this.username = username;
        this.DOB = DOB;
        this.age = age;
        this.fb_id = fb_id;
        this.gmail_id = gmail_id;
        this.loginMethod = loginMethod;
        this.exp = exp;
        this.currentCoin = currentCoin;
        this.coinWon = coinWon;
        this.coinLost = coinLost;
        this.roundsWon = roundsWon;
        this.roundsPlayed = roundsPlayed;
        this.winPercentage = win_percentage(roundsPlayed, roundsWon);
        this.winStreak = winStreak;
        this.level = level(exp);
        this.rank = getRank(coinWon, coinLost);
        this.totalCallCount = totalCallCount;
        this.callCount = callCount;
        this.raiseCount = raiseCount;
        this.foldCount = foldCount;
        this.allInCount = allInCount;
        this.checkCount = checkCount;
        this.coinVideoCount = coinVideoCount;
        this.lastCoinVideoAvailableTime = lastCoinVideoAvailableTime;
        this.lastLoggedInTime = lastLoggedInTime;
        this.lastFreeCoinTime = lastFreeCoinTime;
        this.currentLoginTime = currentLoginTime;
        this.biggestWin = biggestWin;
        this.bestHand = bestHand;

        inGame = false;
        gameRunning = false;
        boardCoin = 0;
        inWaitingRoomPlayers = null;
        deInitializeGameData();
    }







    public void initializeGameData(int gameId, int gameCode, String boardType, long minEntryValue, long minCallValue, int owner_id, int seatPosition, long boardCoin) {

        playerCards.clear();
        boardCards.clear();

        this.gameId = gameId;
        this.gameCode = gameCode;
        this.boardType = boardType;
        this.minEntryValue = minEntryValue;
        this.minCallValue = minCallValue;
        this.owner_id = owner_id;
        this.seatPosition = seatPosition;

        this.boardCoin = boardCoin;
        currentCoin = currentCoin - boardCoin;
    }

    public void joinedAGame(int gameId, int gameCode, int owner_id, int seatPosition, int maxPlayerCount, int playerCount) {

        this.playerCount = playerCount;
        this.maxPlayerCount = maxPlayerCount;
        inGamePlayers = new User[maxPlayerCount];

        inGame = true;
        this.gameId = gameId;
        this.gameCode = gameCode;
        this.owner_id = owner_id;
        this.seatPosition = seatPosition;
    }

    public void deInitializeGameData() {

        if(inGame == true) setExp(getExp() + expIncrease);

        if(inGame == true && gameRunning == true){

            if(seatPosition == roundIteratorSeat) {
                boardCoin -= foldCost;
                totalCallValue += foldCost;
            }
            else if(seatPosition == smallBlindSeat && foldCost > 0) {
                boardCoin -= foldCost/2;
                totalCallValue += foldCost/2;
            }
            totalCallCount++;
            foldCount++;

            setRoundsPlayed(getRoundsPlayed() + 1);
            setCoinLost(getCoinLost() + totalCallValue);
        }

        inGame = false;
        playerCards = new ArrayList<Card>();
        boardCards = new ArrayList<Card>();

        playerCount = -1;
        maxPlayerCount = -1;
        inGamePlayers = null;

        gameId = -1;
        gameCode = -1;
        boardType = "";
        minEntryValue = -1;
        minCallValue = -1;
        owner_id = -1;
        seatPosition = -1;

        currentCoin = currentCoin + boardCoin;
        boardCoin = 0;
        foldCost = 0;
        cycleCount = 0;
        roundCount = 0;
        turnCount = 0;
        call = "";

        roundCoins = 0;
        roundCall = 0;
        roundIteratorSeat = -1;
        roundStarterSeat = -1;
        smallBlindSeat = -1;
        bigBlindSeat = -1;

        totalCallValue = 0;
        callValue = 0;

        gameRunning = false;
    }







    public void initializeInvitationData(int gameId, int gameCode, int maxPlayerCount, String boardType, long minEntryValue, long minCallValue, int owner_id, int seatPosition, long boardCoin) {

        this.maxPlayerCount = maxPlayerCount;
        this.gameId = gameId;
        this.gameCode = gameCode;
        this.boardType = boardType;
        this.minEntryValue = minEntryValue;
        this.minCallValue = minCallValue;
        this.owner_id = owner_id;
        this.seatPosition = seatPosition;
        this.boardCoin = boardCoin;
        currentCoin = currentCoin - boardCoin;

        inWaitingRoomPlayers = new User[maxPlayerCount];
    }

    public void deInitializeInvitationData() {

        gameId = -1;
        gameCode = -1;
        boardType = "";
        minEntryValue = 0;
        minCallValue = 0;
        owner_id = -1;
        seatPosition = -1;
        currentCoin = currentCoin + boardCoin;
        boardCoin = 0;
        inWaitingRoomPlayers = null;
    }
















    public static void loadOwnSelfFromInGamePlayers(User user) {

        if (user == null) return;

        int loc = user.seatPosition;
        User data = user.inGamePlayers[loc];

        user.exp = data.exp;
        user.currentCoin = data.currentCoin;
        user.coinWon = data.coinWon;
        user.coinLost = data.coinLost;
        user.roundsWon = data.roundsWon;
        user.roundsPlayed = data.roundsPlayed;
        user.winPercentage = data.winPercentage;
        user.winStreak = data.winStreak;
        user.level = data.level;
        user.rank = data.rank;
        user.totalCallCount = data.totalCallCount;
        user.callCount = data.callCount;
        user.raiseCount = data.raiseCount;
        user.allInCount = data.allInCount;
        user.checkCount = data.checkCount;
        user.foldCount = data.foldCount;
        user.biggestWin = data.biggestWin;
        user.bestHand = data.bestHand;

        user.boardCoin = data.boardCoin;

        user.gameRunning = data.gameRunning;
        user.roundCount = data.roundCount;
        user.cycleCount = data.cycleCount;
        user.turnCount = data.turnCount;
        user.roundCall = data.roundCall;
        user.roundCoins = data.roundCoins;
        user.roundIteratorSeat = data.roundIteratorSeat;
        user.roundStarterSeat = data.roundStarterSeat;
        user.smallBlindSeat = data.smallBlindSeat;
        user.bigBlindSeat = data.bigBlindSeat;
        user.foldCost = data.foldCost;
        user.call = data.call;
        user.callValue = data.callValue;
        user.totalCallValue = data.totalCallValue;
    }

    public static JSONObject UserToJson(User user) {

        JSONObject temp = new JSONObject();

        temp.put("id", user.getId());
        temp.put("username", user.getUsername());
        temp.put("DOB", user.getDOB());
        temp.put("age", user.getAge());
        temp.put("fb_id", user.getFb_id());
        temp.put("gmail_id", user.getGmail_id());
        temp.put("loginMethod", user.getLoginMethod());
        temp.put("exp", user.getExp());
        temp.put("currentCoin", user.getCurrentCoin());
        temp.put("coinWon", user.getCoinWon());
        temp.put("coinLost", user.getCoinLost());
        temp.put("roundsWon", user.getRoundsWon());
        temp.put("roundsPlayed", user.getRoundsPlayed());
        temp.put("winPercentage", user.getWinPercentage());
        temp.put("winStreak", user.getWinStreak());
        temp.put("level", user.getLevel());
        temp.put("rank", user.getRank());
        temp.put("totalCallCount", user.getTotalCallCount());
        temp.put("callCount", user.getCallCount());
        temp.put("raiseCount", user.getRaiseCount());
        temp.put("foldCount", user.getFoldCount());
        temp.put("allInCount", user.getAllInCount());
        temp.put("checkCount", user.getCheckCount());
        temp.put("coinVideoCount", user.getCoinVideoCount());
        temp.put("lastCoinVideoAvailableTime", user.getLastCoinVideoAvailableTime());
        temp.put("lastLoggedInTime", user.getLastLoggedInTime());
        temp.put("lastFreeCoinTime", user.getLastFreeCoinTime());
        temp.put("currentLoginTime", user.getCurrentLoginTime());
        temp.put("biggestWin", user.biggestWin);
        temp.put("bestHand", user.bestHand);

        return temp;
    }

    public static void loadGameRoomData(User user, JSONObject jsonObject) {

        JSONObject data = jsonObject.getJSONObject("data");

        user.roundCount = data.getInt("roundCount");
        user.roundCall = data.getLong("roundCall");
        user.roundStarterSeat = data.getInt("roundStarterSeat");
        user.bigBlindSeat = data.getInt("bigBlindSeat");
        user.smallBlindSeat = data.getInt("smallBlindSeat");

        user.gameRunning = data.getBoolean("gameRunning");
        user.roundCoins = data.getLong("roundCoins");

        user.foldCost = data.getLong("foldCost");
        user.turnCount = data.getInt("turnCount");
        user.cycleCount = data.getInt("cycleCount");
        user.roundIteratorSeat = data.getInt("roundIteratorSeat");

        user.call = data.getString("call");
        user.callValue = data.getLong("callValue");
        user.totalCallValue = data.getLong("totalCallValue");
    }

    public static User JSONToUser(JSONObject temp) {

        User user = new User(temp.getInt("id"),
                temp.getString("username"),
                User.stringToDate(temp.getString("DOB")),
                temp.getInt("age"),
                temp.getString("fb_id"),
                temp.getString("gmail_id"),
                temp.getString("loginMethod"),
                temp.getLong("exp"),
                temp.getLong("currentCoin"),
                temp.getLong("coinWon"),
                temp.getLong("coinLost"),
                temp.getInt("roundsWon"),
                temp.getInt("roundsPlayed"),
                temp.getInt("winStreak"),
                temp.getInt("totalCallCount"),
                temp.getInt("callCount"),
                temp.getInt("raiseCount"),
                temp.getInt("foldCount"),
                temp.getInt("allInCount"),
                temp.getInt("checkCount"),
                temp.getLong("biggestWin"),
                temp.getString("bestHand"),
                temp.getInt("coinVideoCount"),
                User.stringToDate(temp.getString("lastCoinVideoAvailableTime")),
                User.stringToDate(temp.getString("lastLoggedInTime")),
                User.stringToDate(temp.getString("lastFreeCoinTime")),
                User.stringToDate(temp.getString("currentLoginTime"))
        );
        return user;
    }

    public static User JSONToUserInGame(JSONObject temp) {

        User user = new User(temp.getInt("id"),
                temp.getString("username"),
                User.stringToDate(temp.getString("DOB")),
                temp.getInt("age"),
                temp.getString("fb_id"),
                temp.getString("gmail_id"),
                temp.getString("loginMethod"),
                temp.getLong("exp"),
                temp.getLong("currentCoin"),
                temp.getLong("coinWon"),
                temp.getLong("coinLost"),
                temp.getInt("roundsWon"),
                temp.getInt("roundsPlayed"),
                temp.getInt("winStreak"),
                temp.getInt("totalCallCount"),
                temp.getInt("callCount"),
                temp.getInt("raiseCount"),
                temp.getInt("foldCount"),
                temp.getInt("allInCount"),
                temp.getInt("checkCount"),
                temp.getLong("biggestWin"),
                temp.getString("bestHand"),
                temp.getInt("coinVideoCount"),
                User.stringToDate(temp.getString("lastCoinVideoAvailableTime")),
                User.stringToDate(temp.getString("lastLoggedInTime")),
                User.stringToDate(temp.getString("lastFreeCoinTime")),
                User.stringToDate(temp.getString("currentLoginTime"))
        );

        user.gameId = temp.getInt("gameId");
        user.gameCode = temp.getInt("gameCode");
        user.owner_id = temp.getInt("ownerId");
        user.seatPosition = temp.getInt("seatPosition");
        user.boardType = temp.getString("boardType");
        user.minEntryValue = temp.getLong("minEntryValue");
        user.minCallValue = temp.getLong("minCallValue");
        user.boardCoin = temp.getLong("boardCoin");

        user.gameRunning = temp.getBoolean("gameRunning");
        user.roundCount = temp.getInt("roundCount");
        user.cycleCount = temp.getInt("cycleCount");
        user.turnCount = temp.getInt("turnCount");
        user.roundCall = temp.getInt("roundCall");
        user.roundCoins = temp.getLong("roundCoins");
        user.roundIteratorSeat = temp.getInt("roundIteratorSeat");
        user.roundStarterSeat = temp.getInt("roundStarterSeat");
        user.smallBlindSeat = temp.getInt("smallBlindSeat");
        user.bigBlindSeat = temp.getInt("bigBlindSeat");
        user.foldCost = temp.getLong("foldCost");
        user.call = temp.getString("playerCall");
        user.callValue = temp.getLong("callValue");
        user.totalCallValue = temp.getLong("totalCallValue");

        return user;
    }

    public static JSONObject UserToJsonInGame(User user) {

        JSONObject temp = new JSONObject();

        temp.put("gameId", user.gameId);
        temp.put("gameCode", user.gameCode);
        temp.put("ownerId", user.owner_id);
        temp.put("seatPosition", user.seatPosition);
        temp.put("boardType", user.boardType);
        temp.put("minEntryValue", user.minEntryValue);
        temp.put("minCallValue", user.minCallValue);
        temp.put("boardCoin", user.boardCoin);

        temp.put("id", user.getId());
        temp.put("username", user.getUsername());
        temp.put("DOB", user.getDOB());
        temp.put("age", user.getAge());
        temp.put("fb_id", user.getFb_id());
        temp.put("gmail_id", user.getGmail_id());
        temp.put("loginMethod", user.getLoginMethod());
        temp.put("exp", user.getExp());
        temp.put("currentCoin", user.getCurrentCoin());
        temp.put("coinWon", user.getCoinWon());
        temp.put("coinLost", user.getCoinLost());
        temp.put("roundsWon", user.getRoundsWon());
        temp.put("roundsPlayed", user.getRoundsPlayed());
        temp.put("winPercentage", user.getWinPercentage());
        temp.put("winStreak", user.getWinStreak());
        temp.put("level", user.getLevel());
        temp.put("rank", user.getRank());
        temp.put("totalCallCount", user.getTotalCallCount());
        temp.put("callCount", user.getCallCount());
        temp.put("raiseCount", user.getRaiseCount());
        temp.put("foldCount", user.getFoldCount());
        temp.put("allInCount", user.getAllInCount());
        temp.put("checkCount", user.getCheckCount());
        temp.put("coinVideoCount", user.getCoinVideoCount());
        temp.put("lastCoinVideoAvailableTime", user.getLastCoinVideoAvailableTime());
        temp.put("lastLoggedInTime", user.getLastLoggedInTime());
        temp.put("lastFreeCoinTime", user.getLastFreeCoinTime());
        temp.put("currentLoginTime", user.getCurrentLoginTime());
        temp.put("biggestWin", user.getBiggestWin());
        temp.put("bestHand", user.getBestHand());

        temp.put("gameRunning", user.gameRunning);
        temp.put("roundCount", user.roundCount);
        temp.put("cycleCount", user.cycleCount);
        temp.put("turnCount", user.turnCount);
        temp.put("roundCall", user.roundCall);
        temp.put("roundCoins", user.roundCoins);
        temp.put("roundIteratorSeat", user.roundIteratorSeat);
        temp.put("roundStarterSeat", user.roundStarterSeat);
        temp.put("smallBlindSeat", user.smallBlindSeat);
        temp.put("bigBlindSeat", user.bigBlindSeat);
        temp.put("foldCost", user.foldCost);
        temp.put("playerCall", user.call);
        temp.put("callValue", user.callValue);
        temp.put("totalCallValue", user.totalCallValue);

        return temp;
    }

    //============================================================================
    //              CONSTRUCTING DONE
    //============================================================================














    //============================================================================
    //
    //          NECESSARY FUNCTIONS
    //
    //============================================================================

    public static String getRank(long coins_won, long coins_lost) {

        long diff = coins_won - coins_lost;
        String res = rankString[0];

        for (int i = 0; i < ranksValue.length - 1; i++) {

            if (diff >= ranksValue[i] && diff < ranksValue[i + 1]) {
                res = rankString[i + 1];
            }
        }
        if (diff >= ranksValue[ranksValue.length - 1]) res = rankString[ranksValue.length];

        return res;
    }

    public static int level(long exp) {
        return (int) (1 + sqrt(1 + 8 * exp / 50)) / 2;
    }

    public static double win_percentage(int played, int won) {
        if (played == 0) {
            return 0;
        } else {
            double perc;
            perc = won * 1.0 / played;
            perc = perc * 100;
            return perc;
        }
    }

    public static int getAge(String date_of_birth) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date d = sdf.parse(date_of_birth);
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int date = c.get(Calendar.DATE);
            LocalDate l1 = LocalDate.of(year, month, date);
            LocalDate now1 = LocalDate.now();
            Period diff1 = Period.between(l1, now1);
            return diff1.getYears();
        } catch (ParseException e) {
            System.out.println("Invalid date format");
            return -1;
        }
    }

    public static Date stringToDate(String t) {

        Date d = null;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

        try {
            d = sdf.parse(t);
            return d;
        } catch (ParseException e) {

            return null;
        }
    }

    /*
        Ranking system definition:
        Diff = Coins won - Coins lost
        **Diff can't be negative
        Diff between 0 and 500 -> Beginner
        Diff between 500 and 2000 -> Amateur
        Diff between 2000 and 7500 -> Semi-Pro
        Diff between 7500 and 20000 -> Pro
        Diff between 20000 and 50000 -> World-class
        Diff more than 50000 -> Legendary

    */

    public static long daysBetween(Date d1, Date d2) {

        if (d1.after(d2)) {
            Date temp = d1;
            d1 = d2;
            d2 = temp;
        }

        long days = (long) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));

        return days;
    }

    public static long hoursBetween(Date d1, Date d2) {

        if (d1.after(d2)) {
            Date temp = d1;
            d1 = d2;
            d2 = temp;
        }

        long hours = (long) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60));

        return hours;
    }

    /*
    Given the number of rounds played and number of rounds won, calculates the win perentage of a player
     */


    /*
    Calculates age given the date of birth as string in "YYYY/MM/DD" format
     */

    //============================================================================
    //
    //============================================================================













    //============================================================================
    //
    //          GETTERS AND SETTERS
    //
    //============================================================================

    public static String[] getRankString() {
        return rankString;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDOB() {
        return DOB;
    }

    public void setDOB(Date DOB) {
        this.DOB = DOB;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public static long getExpIncrease() {
        return expIncrease;
    }

    public static long[] getRanksValue() {
        return ranksValue;
    }

    public User[] getInWaitingRoomPlayers() {
        return inWaitingRoomPlayers;
    }

    public void setInWaitingRoomPlayers(User[] inWaitingRoomPlayers) {
        this.inWaitingRoomPlayers = inWaitingRoomPlayers;
    }

    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
    }

    public User[] getInGamePlayers() {
        return inGamePlayers;
    }

    public void setInGamePlayers(User[] inGamePlayers) {
        this.inGamePlayers = inGamePlayers;
    }

    public String getFb_id() {
        return fb_id;
    }

    public void setFb_id(String fb_id) {
        this.fb_id = fb_id;
    }

    public long getTotalCallValue() {
        return totalCallValue;
    }

    public void setTotalCallValue(long totalCallValue) {
        this.totalCallValue = totalCallValue;
    }

    public String getGmail_id() {
        return gmail_id;
    }

    public void setGmail_id(String gmail_id) {
        this.gmail_id = gmail_id;
    }

    public String getLoginMethod() {
        return loginMethod;
    }

    public void setLoginMethod(String loginMethod) {
        this.loginMethod = loginMethod;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
        this.level = level(this.exp);
    }

    public long getCurrentCoin() {
        return currentCoin;
    }

    public void setCurrentCoin(long currentCoin) {
        this.currentCoin = currentCoin;
    }

    public long getCoinWon() {
        return coinWon;
    }

    public void setCoinWon(long coinWon) {
        this.coinWon = coinWon;
        this.rank = getRank(coinWon, coinLost);
    }

    public long getCoinLost() {
        return coinLost;
    }

    public void setCoinLost(long coinLost) {
        this.coinLost = coinLost;
        this.rank = getRank(coinWon, coinLost);
    }

    public long getBiggestWin() {
        return biggestWin;
    }

    public void setBiggestWin(long biggestWin) {
        this.biggestWin = biggestWin;
    }

    public int getRoundsWon() {
        return roundsWon;
    }

    public void setRoundsWon(int roundsWon) {
        this.roundsWon = roundsWon;
        this.winPercentage = win_percentage(roundsPlayed, roundsWon);
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public void setRoundsPlayed(int roundsPlayed) {
        this.roundsPlayed = roundsPlayed;
        this.winPercentage = win_percentage(roundsPlayed, roundsWon);
    }

    public String getBestHand() {
        return bestHand;
    }

    public void setBestHand(String bestHand) {
        this.bestHand = bestHand;
    }

    public int getWinStreak() {
        return winStreak;
    }

    public void setWinStreak(int winStreak) {
        this.winStreak = winStreak;
    }

    public double getWinPercentage() {
        return winPercentage;
    }

    public void setWinPercentage(double winPercentage) {
        this.winPercentage = winPercentage;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getTotalCallCount() {
        return totalCallCount;
    }

    public void setTotalCallCount(int totalCallCount) {
        this.totalCallCount = totalCallCount;
    }

    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }

    public int getRaiseCount() {
        return raiseCount;
    }

    public void setRaiseCount(int raiseCount) {
        this.raiseCount = raiseCount;
    }

    public int getFoldCount() {
        return foldCount;
    }

    public void setFoldCount(int foldCount) {
        this.foldCount = foldCount;
    }

    public int getAllInCount() {
        return allInCount;
    }

    public void setAllInCount(int allInCount) {
        this.allInCount = allInCount;
    }

    public int getCheckCount() {
        return checkCount;
    }

    public void setCheckCount(int checkCount) {
        this.checkCount = checkCount;
    }

    public int getCoinVideoCount() {
        return coinVideoCount;
    }

    public void setCoinVideoCount(int coinVideoCount) {
        this.coinVideoCount = coinVideoCount;
    }

    public Date getLastCoinVideoAvailableTime() {
        return lastCoinVideoAvailableTime;
    }

    public void setLastCoinVideoAvailableTime(Date lastCoinVideoAvailableTime) {
        this.lastCoinVideoAvailableTime = lastCoinVideoAvailableTime;
    }

    public Date getLastLoggedInTime() {
        return lastLoggedInTime;
    }

    public void setLastLoggedInTime(Date lastLoggedInTime) {
        this.lastLoggedInTime = lastLoggedInTime;
    }

    public Date getLastFreeCoinTime() {
        return lastFreeCoinTime;
    }

    public void setLastFreeCoinTime(Date lastFreeCoinTime) {
        this.lastFreeCoinTime = lastFreeCoinTime;
    }

    public Date getCurrentLoginTime() {
        return currentLoginTime;
    }

    public void setCurrentLoginTime(Date currentLoginTime) {
        this.currentLoginTime = currentLoginTime;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
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

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getBoardType() {
        return boardType;
    }

    public void setBoardType(String boardType) {
        this.boardType = boardType;
    }

    public int getGameCode() {
        return gameCode;
    }

    public void setGameCode(int gameCode) {
        this.gameCode = gameCode;
    }

    public long getMinEntryValue() {
        return minEntryValue;
    }

    public void setMinEntryValue(long minEntryValue) {
        this.minEntryValue = minEntryValue;
    }

    public long getMinCallValue() {
        return minCallValue;
    }

    public void setMinCallValue(long minCallValue) {
        this.minCallValue = minCallValue;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(int owner_id) {
        this.owner_id = owner_id;
    }

    public long getBoardCoin() {
        return boardCoin;
    }

    public void setBoardCoin(long boardCoin) {
        this.boardCoin = boardCoin;
    }

    public int getSeatPosition() {
        return seatPosition;
    }

    public void setSeatPosition(int seatPosition) {
        this.seatPosition = seatPosition;
    }

    public long getCallValue() {
        return callValue;
    }

    public void setCallValue(long callValue) {
        this.callValue = callValue;
    }

    public String getCall() {
        return call;
    }

    public void setCall(String call) {
        this.call = call;
    }

    public long getFoldCost() {
        return foldCost;
    }

    public void setFoldCost(long foldCost) {
        this.foldCost = foldCost;
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public void setCycleCount(int cycleCount) {
        this.cycleCount = cycleCount;
    }

    public int getRoundCount() {
        return roundCount;
    }

    public void setRoundCount(int roundCount) {
        this.roundCount = roundCount;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public void setTurnCount(int turnCount) {
        this.turnCount = turnCount;
    }

    public long getRoundCoins() {
        return roundCoins;
    }

    public void setRoundCoins(long roundCoins) {
        this.roundCoins = roundCoins;
    }

    public long getRoundCall() {
        return roundCall;
    }

    public void setRoundCall(long roundCall) {
        this.roundCall = roundCall;
    }

    public int getRoundIteratorSeat() {
        return roundIteratorSeat;
    }

    public void setRoundIteratorSeat(int roundIteratorSeat) {
        this.roundIteratorSeat = roundIteratorSeat;
    }

    public int getSmallBlindSeat() {
        return smallBlindSeat;
    }

    public void setSmallBlindSeat(int smallBlindSeat) {
        this.smallBlindSeat = smallBlindSeat;
    }

    public int getBigBlindSeat() {
        return bigBlindSeat;
    }

    public void setBigBlindSeat(int bigBlindSeat) {
        this.bigBlindSeat = bigBlindSeat;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public void setGameRunning(boolean gameRunning) {
        this.gameRunning = gameRunning;
    }

    public int getTempId() {
        return tempId;
    }

    public void setTempId(int tempId) {
        this.tempId = tempId;
    }

    public int getTempCode() {
        return tempCode;
    }

    public void setTempCode(int tempCode) {
        this.tempCode = tempCode;
    }

    public String getTempBoardType() {
        return tempBoardType;
    }

    public void setTempBoardType(String tempBoardType) {
        this.tempBoardType = tempBoardType;
    }

    public long getTempMinEntryValue() {
        return tempMinEntryValue;
    }

    public void setTempMinEntryValue(long tempMinEntryValue) {
        this.tempMinEntryValue = tempMinEntryValue;
    }

    public long getTempMinCallValue() {
        return tempMinCallValue;
    }

    public void setTempMinCallValue(long tempMinCallValue) {
        this.tempMinCallValue = tempMinCallValue;
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
                "id=" + id +
                ", username='" + username + '\'' +
                ", DOB=" + DOB +
                ", age=" + age +
                ", fb_id='" + fb_id + '\'' +
                ", gmail_id='" + gmail_id + '\'' +
                ", loginMethod='" + loginMethod + '\'' +
                ", isLoggedIn=" + isLoggedIn +
                ", exp=" + exp +
                ", currentCoin=" + currentCoin +
                ", coinWon=" + coinWon +
                ", coinLost=" + coinLost +
                ", roundsWon=" + roundsWon +
                ", roundsPlayed=" + roundsPlayed +
                ", winPercentage=" + winPercentage +
                ", winStreak=" + winStreak +
                ", level=" + level +
                ", rank='" + rank + '\'' +
                ", totalCallCount=" + totalCallCount +
                ", callCount=" + callCount +
                ", raiseCount=" + raiseCount +
                ", foldCount=" + foldCount +
                ", allInCount=" + allInCount +
                ", checkCount=" + checkCount +
                ", coinVideoCount=" + coinVideoCount +
                ", lastCoinVideoAvailableTime=" + lastCoinVideoAvailableTime +
                ", lastLoggedInTime=" + lastLoggedInTime +
                ", lastFreeCoinTime=" + lastFreeCoinTime +
                ", currentLoginTime=" + currentLoginTime +
                ", biggestWin=" + biggestWin +
                ", bestHand='" + bestHand + '\'' +
                ", inGame=" + inGame +
                ", playerCards=" + playerCards +
                ", boardCards=" + boardCards +
                ", gameId=" + gameId +
                ", gameCode=" + gameCode +
                ", maxPlayerCount=" + maxPlayerCount +
                ", boardType='" + boardType + '\'' +
                ", minEntryValue=" + minEntryValue +
                ", minCallValue=" + minCallValue +
                ", owner_id=" + owner_id +
                ", boardCoin=" + boardCoin +
                ", seatPosition=" + seatPosition +
                ", callValue=" + callValue +
                ", totalCallValue=" + totalCallValue +
                ", call='" + call + '\'' +
                ", foldCost=" + foldCost +
                ", cycleCount=" + cycleCount +
                ", roundCount=" + roundCount +
                ", turnCount=" + turnCount +
                ", roundCoins=" + roundCoins +
                ", roundCall=" + roundCall +
                ", roundIteratorSeat=" + roundIteratorSeat +
                ", smallBlindSeat=" + smallBlindSeat +
                ", bigBlindSeat=" + bigBlindSeat +
                ", gameRunning=" + gameRunning +
                ", playerCount=" + playerCount +
                ", inGamePlayers=" + Arrays.toString(inGamePlayers) +
                ", tempId=" + tempId +
                ", tempCode=" + tempCode +
                ", tempBoardType='" + tempBoardType + '\'' +
                ", tempMinEntryValue=" + tempMinEntryValue +
                ", tempMinCallValue=" + tempMinCallValue +
                '}' + "\n";
    }


    //========================================================================================
    //
    //
    //=========================================================================================


}
