package sg.edu.ntu.wa0002qie.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity : ";
    private static MainActivity instance;

    // msg type sent from the Bluetooth Service Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // key names received from the Bluetooth Service Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 2;
    private static final int REQUEST_ENABLE_BLUETOOTH = 3;

    private String mConnectedDevice= "";
    private BluetoothAdapter bluetoothAdapter = null;
    private Bluetooth chatService = null;
    private Sensor accelerometer;
    private SensorManager sensorManager;
    private Handler customerHandler = new Handler();
    private Arena arena;

    //original android environment
    private String gridString = "GRID 20 15 2 18 2 19 0 0 0 0 0 0 0";
    private int[] intArray = new int[300];

    // AMD
    private ListView tConversationView;
    private ListView fConversationView;
    private Button tSendButton;
    private ArrayAdapter<String> tConversationAA;
    private ArrayAdapter<String> fConversationAA;

    private String decodeString;
    // f1, f2 configuration
    private SharedPreferences preferences;
    private Handler mMyHandler = new Handler();
    private TextView robotStatus, exploreTime, fastestTime;
    private EditText x_coordinate, y_coordinate, direction;
    private EditText TextAMD;
    private ToggleButton autoManaul, explore, fastest;
    private ToggleButton tiltSensing;
    private Button update;
    private Button f1, f2;
    private Button setXY;

    private ImageButton up, left, right;
    private RelativeLayout arenaDisplay;

    private boolean autoUpdate = true;
    private boolean tilt = false;
    private int[][] obstacleArray = new int[20][15];
    private ArrayList obstacleSensor = new ArrayList();
    private long startTimeExplore = 0L;
    private long startTimeFastest = 0L;
    private long timeBuffExplore = 0L;
    private long timeBuffFastest = 0L;
    private long timeInMillisecondsExplore = 0L;
    private long timeInMillisecondsFastest = 0L;
    private long updateTimeExplore = 0L;
    private long updateTimeFastest = 0L;
    private StringBuffer outStringBuff;
    private JSONObject jsonObj;

    // fastest path
    private String dir = "";
    private int run = 0;
    private List<String> spSteps;
    private int[][] spArray = new int[20][15];

    // robot default position
    private int xStatus = 2;
    private int yStatus = 19;
    private int dStatus = 180;
    private int[][] arrowArray = new int[20][15];

    // counter for arrow coordinate
    private int arrow_x = 0; //global variable holding the x coordinate of arrow
    private int arrow_y = 0; //global variable holding the y coordinate of arrow
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.e(TAG, "+++ ON CREATE +++");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Instantiate instance
        instance = this;

        // Tilt Sensing
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // data recorded in the SettingActivity
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // assign UI elements
        robotStatus = (TextView)findViewById(R.id.text_robotStatus);

        exploreTime = (TextView)findViewById(R.id.timer_explore);
        fastestTime = (TextView)findViewById(R.id.timer_fastest);

        x_coordinate = (EditText)findViewById(R.id.coor_x);
        y_coordinate = (EditText)findViewById(R.id.coor_y);
        direction = (EditText)findViewById(R.id.dir);

        TextAMD = (EditText)findViewById(R.id.send_text);
        autoManaul = (ToggleButton)findViewById(R.id.btn_automanual);
        update = (Button)findViewById(R.id.btn_update);
        explore = (ToggleButton)findViewById(R.id.btn_explore);
        fastest = (ToggleButton)findViewById(R.id.btn_fastest);
        tiltSensing = (ToggleButton)findViewById(R.id.tilt_btn);

        f1 = (Button)findViewById(R.id.btn_f1);
        f2 = (Button)findViewById(R.id.btn_f2);

        up = (ImageButton)findViewById(R.id.btn_up);
        left = (ImageButton)findViewById(R.id.btn_left);
        right = (ImageButton)findViewById(R.id.btn_right);
        setXY = (Button)findViewById(R.id.btn_setXY);

        // initializing the environment
        init();

        // setup the on click listeners for each button
        f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "F1 clicked");
                sendMessage(preferences.getString("F1Command", ""));
            }
        });
        f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "F2 clicked");
                sendMessage(preferences.getString("F2Command", ""));
            }
        });

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Up arrow clicked");
                goStraight();
            }
        });
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Left arrow clicked");
                turnLeft();
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Right arrow clicked");
                turnRight();
            }
        });
        setXY.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG, "set X and Y");
                //pass the new coordinate of robot to arena and draw the new map
                setRobot();
            }

        });

        // the update button only can be used when the auto button is set to off
        update.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(obstacleArray != null){
                        autoUpdate = true;
                        arena.setObstacles(obstacleArray);
                    }
                    if(decodeString != null){
                        autoUpdate = true;
                        System.out.println(decodeString);
                        updateGridArray(toIntArray(decodeString));
                    }
                    autoUpdate = false;
                    Toast.makeText(MainActivity.this, "Map Updated", Toast.LENGTH_SHORT).show();

                } catch(Exception e){
                    Toast.makeText(MainActivity.this, "Already Updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(autoUpdate){
//            update.setBackgroundResource(R.drawable.enabled_btn);
//            Toast.makeText(MainActivity.this, "Auto Update : ON", Toast.LENGTH_SHORT).show();
            autoManaul.setChecked(true);
        }

        // 4 toggle buttons, set the toggle situations and the button style
        autoManaul.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){   // AUTO motion
                    update.setEnabled(false);
                    update.setBackgroundColor(Color.parseColor("#efa1bfc7"));
                    autoUpdate = true;
                    Toast.makeText(MainActivity.this, "Auto Update : ON", Toast.LENGTH_SHORT).show();
                }
                else{           // Manual motion
                    autoUpdate = false;
                    update.setEnabled(true);
                    update.setBackgroundResource(R.drawable.enabled_button);
                    Toast.makeText(MainActivity.this, "Auto Update : OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });

        explore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    int y = 21 - Integer.parseInt(y_coordinate.getText().toString());
                    String sendPos = x_coordinate.getText().toString() + ","
                            + y + ","
                            + direction.getText().toString();
                    sendMessage("BOT_POS " + sendPos + "\n");
                    sendMessage("EX_START" + "\n");
                    startTimeExplore = SystemClock.uptimeMillis();
                    customerHandler.post(updateTimerThreadExplore);
                }
                else{
                    timeBuffExplore += timeInMillisecondsExplore;
                    customerHandler.removeCallbacks(updateTimerThreadExplore);
                }
            }
        });

        fastest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    sendMessage("FP_START");
                    startTimeFastest = SystemClock.uptimeMillis();
                    customerHandler.post(updateTimerThreadFastest);
                }
                else{
                    timeBuffFastest += timeInMillisecondsFastest;
                    customerHandler.removeCallbacks(updateTimerThreadFastest);
                }
            }
        });

        // enable the tilting function first in case pressing it accidentally
        tiltSensing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // configure toggle button
            }
        });

    }

    public static MainActivity getInstance(){
        return instance;
    }

    // init the whole android environment
    private void init(){
        // default value for map string
        Log.d("MainActivity", "Init start");
        gridString = "GRID 20 15 2 18 2 19 0 0 0 0 0 0 0";
        // default value for robot position
        x_coordinate.setText("2", TextView.BufferType.EDITABLE);
        y_coordinate.setText("19", TextView.BufferType.EDITABLE);
        direction.setText("180");
        intArray = toIntArray(gridString);
        arena = new Arena(this, intArray);
        arena.setClickable(true);
        arena.setGridArray(intArray);
        for(int x = 0; x < 20; x++){
            for(int y = 0; y < 15; y++){
                obstacleArray[x][y] = 0;
                spArray[x][y] = 0;
                arrowArray[x][y] = 0;
            }
        }
//        drawShortestPath(new String[] {"F3", "R", "F5", "L", "F8", "R", "F7", "L", "F7"});

        arena.setObstacles(obstacleArray);
        arena.setSpArray(spArray);
        arena.setArrowArray(arrowArray);
        arenaDisplay = (RelativeLayout) findViewById(R.id.arenaView);
        arenaDisplay.addView(arena);
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.e(TAG, "++ ON START ++");
        if(!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, REQUEST_ENABLE_BLUETOOTH);
            Toast.makeText(getApplicationContext(), "Disabled bluetooth", Toast.LENGTH_SHORT).show();
        }
        else{
            if(chatService == null){
                setupChat();
            }
            Toast.makeText(getApplicationContext(),"Enabled bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupChat(){
        Log.d(TAG, "setupChat()");
        // initialize the array for the conversation thread
        tConversationAA = new ArrayAdapter<String>(this, R.layout.text);
        fConversationAA = new ArrayAdapter<String>(this, R.layout.text);

        tConversationView = (ListView) findViewById(R.id.listView_to);
        tConversationView.setAdapter(tConversationAA);
        fConversationView = (ListView) findViewById(R.id.listView_from);
        fConversationView.setAdapter(fConversationAA);

        // initiate the bluetooth service for connections
        chatService = new Bluetooth(this, mHandler);
        // initiate the buffer for outgoing msg
        outStringBuff = new StringBuffer("");
    }

    @Override
    public synchronized void onResume(){
        Log.d(TAG, "++ ON RESUME ++");
        super.onResume();
        // Resume the BT when it first fail onStart()
        if (chatService != null) {
            if (chatService.getState() == Bluetooth.STATE_IDLE) {
                // Start the Bluetooth chat services
                chatService.start();
            }
        }
        // Resume the Tilt Sensing
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        Log.d(TAG, "++ ON PAUSE ++");
        super.onPause();
        // sensor on pause
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onStop(){
        Log.d(TAG, "++ ON STOP ++");
        super.onStop();
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "++ ON DESTROY ++");
        super.onDestroy();
        if(chatService != null){
            chatService.stop();
        }
    }

    private void ensureDiscoverable(){
        Log.d(TAG, "ensure discoverable");
        if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent requireDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            requireDiscoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(requireDiscoverable);
        }
    }

    private void sendMessage(String string) {
        Log.d(TAG, "sendMessage()" + string);
        // Check that we're actually connected before trying anything
        if(chatService.getState() != Bluetooth.STATE_CONNECTED){
            Toast.makeText(this, "Bluetooth Not Connected", Toast.LENGTH_SHORT).show();
            return;
        }

        if(string.length() > 0){
            byte[] msgSend = string.getBytes();
            chatService.write(msgSend);
            // reset buffer to zero
            outStringBuff.setLength(0);
        }
    }

    private TextView.OnEditorActionListener writeListener = new TextView.OnEditorActionListener(){
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event){
            if(actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP){
                String msg = view.getText().toString();
                sendMessage(msg);
            }
            return true;
        }
    };

    private Runnable updateTimerThreadExplore = new Runnable() {
        @Override
        public void run() {
            timeInMillisecondsExplore = SystemClock.uptimeMillis() - startTimeExplore;
            updateTimeExplore = timeBuffExplore + timeInMillisecondsExplore;

            int sec = (int) (updateTimeExplore/1000);
            int min = sec/60;
            sec %= 60;
            int millisecond = (int) (updateTimeExplore % 1000);
            int milli = millisecond / 10;
            if(min < 10){
                exploreTime.setText("0" + min + ":" + sec + ":" + milli);
            }
            else{
                exploreTime.setText(min + ":" + sec + ":" + milli);
            }
            customerHandler.post(this);
        }
    };

    private Runnable updateTimerThreadFastest = new Runnable() {
        @Override
        public void run() {
            timeInMillisecondsFastest = SystemClock.uptimeMillis() - startTimeFastest;
            updateTimeFastest = timeBuffFastest + timeInMillisecondsFastest;

            int sec = (int) (updateTimeFastest/1000);
            int min = sec/60;
            sec %= 60;
            int millisecond = (int) (updateTimeFastest % 1000);
            int milli = millisecond / 10;
            if(min < 10){
                fastestTime.setText("0" + min + ":" + sec + ":" + milli);
            }
            else{
                fastestTime.setText(min + ":" + sec + ":" + milli);
            }
            customerHandler.post(this);
        }
    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            robotStatus.setText("Stopped");
        }
    };

    public void goStraight(){
        String forwardMsg = "F";
        sendMessage(forwardMsg);
        try {
            decodeString = decodeRobotString_algo(forwardMsg);
            if(decodeString != null)
                updateGridArray(toIntArray(decodeString));
        } catch (JSONException e){}
        // 1 sec later, set robot status back to stopped
        mMyHandler.postDelayed(mRunnable, 1000);
    }

    public void turnLeft(){
        String leftMsg = "L";
        sendMessage(leftMsg);
        try {
            decodeString = decodeRobotString_algo(leftMsg);
            if(decodeString != null)
                updateGridArray(toIntArray(decodeString));
        } catch (JSONException e){}
        mMyHandler.postDelayed(mRunnable, 1000);
    }

    public void turnRight(){
        Log.d(TAG, "Robot turn right");
        String rightMsg = "R";
        sendMessage(rightMsg);
        try {
            decodeString = decodeRobotString_algo(rightMsg);
            if(decodeString != null)
                updateGridArray(toIntArray(decodeString));
        } catch (JSONException e){}
        mMyHandler.postDelayed(mRunnable, 1000);
    }

    public void start(View v){
        // pastart,x,y,d\n
//        String sendPos = x_coordinate.getText().toString() + ","
//                + y_coordinate.getText().toString() + ","
//                + direction.getText().toString();
//        sendMessage("pastart," + sendPos + "\n");
    }

    /*
        parse a string into int array
     */
    public int[] toIntArray(String s){
        Log.d("toIntArray()", s);
        String[] stringArray = s.split(" ");
        int len = stringArray.length-1;
        int[] intArray = new int[len];

        for(int i = 1; i < len; i++){
            intArray[i-1] = Integer.parseInt(stringArray[i]);
        }
        return intArray;
    }


    private final void setStatus(CharSequence subTitle) {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //final ActionBar actionBar = this.getActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /*
        create handler to get info back from the Bluethooth Chat Service
     */
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case Bluetooth.STATE_CONNECTED:
                            setStatus("Connected to " + mConnectedDevice);
                            tConversationAA.clear();
                            break;
                        case Bluetooth.STATE_CONNECTING:
                            setStatus("Connecting");
                            break;
                        case Bluetooth.STATE_LISTEN:
                        case Bluetooth.STATE_IDLE:
                            setStatus("Disconnected");
                            break;
                    }
                    break;

                case MESSAGE_READ:
                    byte[] read = (byte[]) msg.obj;
                    String readMsg = new String(read, 0, msg.arg1);
                    if(readMsg.contains("grid")){
                        Log.d(TAG, "receive the map string");
                        // the readMessage is in a hex format
                        fConversationAA.add(mConnectedDevice + " : " + readMsg);
                        String map = readMsg.substring(10, 310);
                        Log.d(TAG, "Map string: "+map);
                        intArray = decodeGridString(map);
//                        obstacleArray = decodeMapString(map);
//                        updateObstacleArray(obstacleArray);
                        updateGridArray(intArray);
                        // To-do
                        // There will be 2 strings
                        // One with 300 digits to update unexplored and explored
                        // Another with 75 digits (hex) to update obstacles

                    }
                    else if(readMsg.contains("obstacle")){
                        String obstacle = readMsg.substring(4);
                        obstacleArray = decodeMapString(obstacle);
                        updateObstacleArray(obstacleArray);
                    }
                    else if(readMsg.contains("BOT_POS")){
                        Log.d(TAG, "receive robot position");
                        // set the robot position
                        setRobot(readMsg.split(" ")[1]);

                    }
                    else if(readMsg.contains("ARROW")) {
                        Log.d(TAG, "receive arrow position");
                        String a = readMsg.split(" ")[1];
                        double raw_x = Double.parseDouble(a.split(",")[0]);
                        double raw_y = Double.parseDouble(a.split(",")[1] + 1);
                        double sign_x = Math.signum(raw_x);
                        double sign_y = Math.signum(raw_y);
                        int relative_x = (int)((raw_x - sign_x * 5) / 10);
                        int relative_y = (int)((raw_y - sign_y * 5) / 10);
                        Log.d(TAG, "x received: " + relative_x + " y received: " + relative_y);
                        int[] arrowPosition = calculateArrowPosition(relative_x, relative_y);
                        int x = Integer.parseInt(x_coordinate.getText().toString()) + arrowPosition[0];
                        int y = Integer.parseInt(y_coordinate.getText().toString()) + arrowPosition[1];
                        Log.d(TAG, "robot pos: " + x_coordinate.getText().toString() + " " + y_coordinate.getText().toString());
                        Log.d(TAG,"arrow coordinate: " + x + " " + y);
                        checkArrowCoordinate(x, y);
                    }
                    else if(readMsg.contains("sp")){
                        try{
                            fConversationAA.add(mConnectedDevice + " : " + readMsg);
                            String[] fastestSteps = readMsg.replace("sp","").split(",");
                            //spSteps = Arrays.asList(fastestSteps);
                            drawShortestPath(fastestSteps);

                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    else{
                        fConversationAA.add(mConnectedDevice + " : " + readMsg);
                        decodeString = readMsg;
                    }
                    break;

                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    tConversationAA.add("Group 9:  " + writeMessage);
                    Toast.makeText(MainActivity.this, "SEND", Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_DEVICE_NAME:
                    mConnectedDevice = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected with " + mConnectedDevice, Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private int[] decodeGridString(String map) {
        String[] string_arr = map.split("");
        int[] int_arr = new int[300];
        for(int i = 1; i <= 300; i++){
            int_arr[i-1] = Integer.parseInt(string_arr[i]);
        }
        return int_arr;
    }

    private void checkArrowCoordinate(int x, int y) {
        // arrow will be printed out
        // only if the same coordinate was sent for 5 times
        if(x == arrow_x && y == arrow_y){
            counter++;
            if(counter == 5){
                setArrow();
                counter = 0;
            }
        }
        // if different, update the global arrow coordinate
        else {
            arrow_x = x;
            arrow_y = y;
        }
    }

    private void setArrow() {
        arrowArray[arrow_y][arrow_x] = 2;
        arena.setArrowArray(arrowArray);
    }

    private int[] calculateArrowPosition(int x, int y) {
        int[] result = new int[2];
        switch (dStatus){
            case 0:
                result[0] = -x;
                result[1] = y;
                break;
            case 90:
                result[0] = -y;
                result[1] = -x;
                break;
            case 180:
                result[0] = x;
                result[1] = -y;
                break;
            case 270:
                result[0] = y;
                result[1] = x;
                break;
        }
        return result;
    }

    /*
        not using for now
        decode each step received and update the map to show robot movement
     */
    public void stepMovement(String msg) throws JSONException{
        String step = msg.replace("#", "");
        move(step);
        if (dir.equals("F")) {
            Log.d("run:", String.valueOf(run));
            for (int i = 0; i < run; i++) {
                decodeString = decodeRobotString_algo("{go:[F]}");
                if(decodeString != null){
                    updateGridArray(toIntArray(decodeString));
                }
            }
        } else if (dir.equals("L")) {
            decodeString = decodeRobotString_algo("{go:[L]}");
            updateGridArray(toIntArray(decodeString));
        } else if (dir.equals("R")) {
            decodeString = decodeRobotString_algo("{go:[R]}");
            updateGridArray(toIntArray(decodeString));
        }
    }

    /*
        for each step, check its direction. if it's forward command, calculate the number followed by "F"
     */
    public void move(String s){
        String numMove = s;
        Log.d("numMove: ",numMove);

        dir = numMove.substring(0, 1);
        numMove = numMove.replace(dir, "");
        numMove = numMove.replace(",B,", "");
        if(numMove == ""){
            run = 0;
            return;
        }
        run = Integer.parseInt(numMove);

        Log.d("run:",numMove);
    }

    /*
        draw out the shortest path
    */
    public void drawShortestPath(String[] steps){
        int d = 180;
        int movement = 0;
        // set a pointer pointing to the drawing square
        int xptr = 1;
        int yptr = 18;
        for(String step : steps){
            // set the moving steps
            if (step.contains("b")){
                continue;
            }
            if (step.contains("F")){
                movement = Integer.parseInt(step.replace("F", ""));
                // update the array based on the direction
                if(d == 0){
                    for(int i = 0; i < movement; i++) {
                        yptr++;
                        spArray[yptr][xptr] = 1;
                    }
                }
                if(d == 90){
                    for(int i = 0; i < movement; i++) {
                        xptr--;
                        spArray[yptr][xptr] = 1;
                    }
                }
                if(d == 180){
                    for(int i = 0; i < movement; i++) {
                        yptr--;
                        spArray[yptr][xptr] = 1;
                    }
                }
                if(d == 270){
                    for(int i = 0; i < movement; i++) {
                        xptr++;
                        spArray[yptr][xptr] = 1;
                    }
                }
                continue;
            }
            // check and set direction
            if(d == 0){
                if (step.contains("R"))
                    d = 90;
                else if (step.contains("L"))
                    d = 270;
            }
            else if(d == 90){
                if (step.contains("R"))
                    d = 180;
                else if (step.contains("L"))
                    d = 0;
            }
            else if(d == 180){
                if (step.contains("R"))
                    d = 270;
                else if (step.contains("L"))
                    d = 90;
            }
            else if(d == 270){
                if (step.contains("R"))
                    d = 0;
                else if (step.contains("L"))
                    d = 180;
            }

        }

        arena.setSpArray(spArray);
    }

    // not necessary for now
    public void runShortestPath() throws JSONException{
        Log.d(TAG, "runShortestPath()");
        for(String step: spSteps){
            move(step);
            if(dir.equals("F")){
                Log.d("run:",String.valueOf(run));
                for(int i = 0; i < run; i++){
                    decodeString = decodeRobotString_algo(dir);
                    if(decodeString != null)
                        updateGridArray(toIntArray(decodeString));
                }
            }
            else if(dir.equals("L")){
                decodeString = decodeRobotString_algo(dir);
                updateGridArray(toIntArray(decodeString));
            }
            else if(dir.equals("R")){
                decodeString = decodeRobotString_algo(dir);
                updateGridArray(toIntArray(decodeString));
            }
            try {
                Thread.sleep(300);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /*
        update the robot status according to the instruction string
     */
    public String decodeRobotString_algo(String s)throws JSONException{
        int robotX = xStatus;
        int robotY = yStatus;
        int robotD = dStatus;

        // move forward
        if("F".equals(s)){
            switch(dStatus){
                case 0: //if head up
                    robotY = yStatus + 1;
                    break;
                case 90: //if head to right
                    robotX = xStatus - 1;
                    break;
                case 180: //if head down
                    robotY = yStatus - 1;
                    break;
                case 270: //if head to right
                    robotX = xStatus + 1;
                    break;
            }
        }
        // turn left
        if("L".equals(s)){
            switch(dStatus){
                case 0:
                    robotD = 270;
                    break;
                case 90:
                    robotD = 0;
                    break;
                case 180:
                    robotD = 90;
                    break;
                case 270:
                    robotD = 180;
                    break;
            }
        }
        // turn right
        if("R".equals(s)){
            switch(dStatus){
                case 0:
                    robotD = 90;
                    break;
                case 90:
                    robotD = 180;
                    break;
                case 180:
                    robotD = 270;
                    break;
                case 270:
                    robotD = 0;
                    break;
            }
        }
        // return null if the robot is moving out of bound
        if(robotX < 2 || robotX > 14 || robotY < 2 || robotY > 19)
            return null;
        return decodeRobotString(robotX, robotY, robotD);
    }

    /*
        used to set the robot status and update the grid string
     */
    public String decodeRobotString(int x, int y, int d){
        String hx = "";
        String hy = "";
        String bx = String.valueOf(x);
        String by = String.valueOf(y);

        x_coordinate.setText(x+"");
        y_coordinate.setText(y+"");

        if (d == 0){
            hx = String.valueOf(x);
            hy = String.valueOf(y+1);

            direction.setText("0");

            if(dStatus == 270){
                robotStatus.setText("Turn Right");
            }
            if(dStatus == 90){
                robotStatus.setText("Turn Left");
            }
            if(y < yStatus){
                robotStatus.setText("Moving Foward");
            }
            if(y > yStatus){
                robotStatus.setText("Moving Backward");
            }

        }
        else if (d == 90){
            hx = String.valueOf(x-1);
            hy = String.valueOf(y);

            direction.setText("90");
            if(dStatus == 0){
                robotStatus.setText("Turn Right");
            }
            if(dStatus == 180){
                robotStatus.setText("Turn Left");
            }
            if(x > xStatus){
                robotStatus.setText("Moving Foward");
            }
            if(x > xStatus){
                robotStatus.setText("Moving Backward");
            }

        }
        else if (d == 180){
            hx = String.valueOf(x);
            hy = String.valueOf(y-1);

            direction.setText("180");
            if(dStatus == 90){
                robotStatus.setText("Turn Right");
            }
            if(dStatus == 270){
                robotStatus.setText("Turn Left");
            }
            if(y > yStatus){
                robotStatus.setText("Moving Foward");
            }
            if(y < yStatus){
                robotStatus.setText("Moving Backward");
            }

        }
        else if (d == 270){
            hx = String.valueOf(x+1);
            hy = String.valueOf(y);
            direction.setText("270");
            if(dStatus == 180){
                robotStatus.setText("Turn Right");
            }
            if(dStatus == 0){
                robotStatus.setText("Turn Left");
            }
            if(x < xStatus){
                robotStatus.setText("Moving Foward");
            }
            if(x > xStatus){
                robotStatus.setText("Moving Backward");
            }

        }
        // delay the status updating by 1 sec
        mMyHandler.postDelayed(mRunnable, 1000);
        String decode = "GRID 20 15 " + hx + " " + hy + " " + bx + " " + by + " 0 0 0 0 0 0 0 0";

        xStatus = x;
        yStatus = y;
        dStatus = d;
        Log.d(TAG, "Grid decode: " + decode);
        return decode;
    }

    /*
        decode the grid string: add up the unexplored-explored with the empty-obstacle and invert it to get the result
     */
    private int[][] decodeMapString(String mapString){
        Log.d(TAG, "decode map string: " + mapString);
        String[] mapArray = mapString.split("");
        String[] binaryMap = hexToBinary(mapArray);
        // index representing the index of digit in the binary array
        int index = 1;
        int[][] result = new int[20][15];
        for(int i = 19; i >= 0; i--){
            for(int j = 0; j < 15; j++){
                result[i][j] = Integer.parseInt(binaryMap[index]);
                index ++;
            }
        }
        return result;
    }

    private String[] hexToBinary(String[] hexMap){
        String binaryString = "";
        for(int i = 1; i < hexMap.length; i++){
            Log.d(TAG, "hexMap[i]:" + hexMap[i]);
            String value = new BigInteger(hexMap[i], 16).toString(2);
            value = String.format("%4s", value).replace(" ", "0");
            binaryString += value;
        }
        Log.d(TAG, "binary map " + binaryString);
        return binaryString.split("");
    }

    /*
        update the obstacle array(tgt with the explored path) based on the passed in array
     */
    public void updateObstacleArray(int[][] list){
        Log.d(TAG, "updateObstacleArray()");
        if(autoUpdate == true){
            arena.setObstacles(list);
        }
    }

    /*
        update robot position
     */
    public void updateGridArray(int[] array){
        if(autoUpdate == true){
            Log.d("updateGridArray","true");
            arena.setGridArray(array);
        }
    }
    /*
        check for connection result
     */
    public void onActivityResult(int request, int result, Intent data){
        if (true)
            Log.d(TAG, "onActivityResult " + result);
        switch (request){
            case REQUEST_CONNECT_DEVICE:
                System.out.println("onActivityResult");
                if (result == Activity.RESULT_OK){
                    System.out.println("result ok");
                    connectDevice(data);
                }
                break;
            case REQUEST_ENABLE_BLUETOOTH:
                if(result == Activity.RESULT_OK){
                    setupChat();
                }
                else{
                    Toast.makeText(this, "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    /*
        connect to the device that is passed in
     */
    public void connectDevice(Intent data){
        // get the connected device's MAC address
        String addr = data.getExtras().getString(BluetoothDevicesActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(addr);
        // connect to the device
        chatService.connect(device, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        new MenuInflater(getApplication()).inflate(R.menu.top_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent = null;
        switch(item.getItemId()){
            case R.id.connect_devices:
                if (!bluetoothAdapter.isEnabled()) {
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 0);
                    Toast.makeText(getApplicationContext(), "Bluetooth Enable", Toast.LENGTH_SHORT).show();
                }
                else {
                    intent = new Intent(this, BluetoothDevicesActivity.class);
                    startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
                }
                return true;
            case R.id.reconfiguration:
                Intent reconfigure = new Intent(this, ReconfigurationActivity.class);
                startActivityForResult(reconfigure, 0);
            case R.id.discoverable:
                ensureDiscoverable();
                break;
            case R.id.exit:
                bluetoothAdapter.disable();
                System.exit(0);
                Toast.makeText(this, "exit", Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    /*
        reset the system
     */
    public void reset(View v){
        sendMessage("reset");
        for(int i = 0; i < 20; i ++){
            for(int j = 0; j < 15; j++) {
                obstacleArray[i][j] = 0;
                spArray[i][j] = 0;
            }
        }

        arena.setObstacles(obstacleArray);
        arena.setSpArray(spArray);

        xStatus = 2;
        yStatus = 19;
        dStatus = 180;

        tConversationAA.clear();
        fConversationAA.clear();

        init();
        setRobot();

        startTimeFastest = 0L;
        startTimeExplore = 0L;
        timeInMillisecondsFastest = 0L;
        timeInMillisecondsExplore = 0L;
        timeBuffExplore = 0L;
        timeBuffFastest = 0L;
        exploreTime.setText("00:00:00");
        fastestTime.setText("00:00:00");
    }

    // set the text view when click on map
    public void setCoordinate(int x, int y){
        x_coordinate.setText(x+"", TextView.BufferType.EDITABLE);
        y_coordinate.setText(y+"", TextView.BufferType.EDITABLE);
        xStatus = x;
        yStatus = y;
    }

    // set the postion of the robot on the map
    public void setRobot(){
        String newPos = "";
        newPos += x_coordinate.getText().toString() + " ";
        newPos += y_coordinate.getText().toString() + " ";
        newPos += direction.getText().toString();
        sendMessage(newPos);

        try {
            decodeString = decodeRobotString_algo("");
            if(decodeString != null)
                updateGridArray(toIntArray(decodeString));
        } catch (JSONException e){}
        mMyHandler.postDelayed(mRunnable, 1000);

        Toast.makeText(getApplicationContext(), "Robot Set", Toast.LENGTH_SHORT).show();
        Log.d("setPosition: ", newPos);
    }


    public void setRobot(String s){
        String[] temp = s.split(",");
        int x = Integer.parseInt(temp[0]);
        int y = Integer.parseInt(temp[1]);
        int h = Integer.parseInt(temp[2]);
        int d = (180 + 90 * (h-1)) % 360;
        decodeString = decodeRobotString(x, y, d);
        updateGridArray(toIntArray(decodeString));
        Toast.makeText(getApplicationContext(), "Robot Set", Toast.LENGTH_SHORT).show();
    }

    public void setCoord(View view){
        setRobot();
    }


    @Override
    public void onSensorChanged(SensorEvent event){
        float x = event.values[0];
        float y = event.values[1];
        if(tilt == false){
            onPause();
        }
        if (Math.abs(x) > Math.abs(y)) {
            if (x < -2) {
                Log.d(TAG,"You tilt the device right");
                turnRight();
            }
            if (x > 2) {
                Log.d(TAG, "You tilt the device left");
                turnLeft();
            }
        } else {
            if (y < -2) {
                Log.d(TAG, "You tilt the device up");
                goStraight();
            }
            if (y > 2) {
                Log.d(TAG, "You tilt the device down");
                turnLeft();
                turnLeft();

            }
        }
        if (x > (-2) && x < (2) && y > (-2) && y < (2)) {
            Log.d(TAG, "Not tilt device");
        }
    }
//        if(event.values[0] > 4){
//            turnLeft();
//        }
//        if(event.values[0] < -5){
//            turnRight();
//        }
//        if(event.values[1] < 0){
//            goStraight();
//        }
//        if(event.values[1] > 8){
//            // reverse the direction
//            turnLeft();
//            turnLeft();
//        }
//    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    public void visible(){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

}
