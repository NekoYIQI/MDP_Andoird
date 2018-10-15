package sg.edu.ntu.wa0002qie.myapplication;

public class Constants {
    //Map Constants
    public static final int MAP_ROW = 20;
    public static final int MAP_COL = 15;

    //Robot Constants

    public static final int NORTH = 180;
    public static final int EAST = 270;
    public static final int SOUTH = 0;
    public static final int WEST = 90;

    public static final int LEFT = 4;
    public static final int RIGHT = 5;
    public static final int MIDDLE = 6;

    public static final int ROBOT_SENSEOR_AMOUNT = 6;

    public static final int START_POS_X = 0;
    public static final int START_POS_Y = MAP_ROW - 3;
    public static final int GOAL_POS_X = MAP_COL - 3;
    public static final int GOAL_POS_Y = 0;

    public static final int PAINT_PIXEL_OFFSET = 10;
    public static final int ROBOT_SIZE = 3;
    public static final int HEADING_PIXEL_SIZE = 8;


/*
 	M1 - 10cm
    M2 - 20cm
    M3 - 30cm

    v - exploration
    w - fastestPath
    x - right
    y - left
    z - recalibrateStart
*/

    //Communication Constants
    public static final String TARGET_ARDUINO = "";

    public static final String TARGET_ANDROID = "MDF ";

    public static final String MOVE_ONE_GRID = "F";
    public static final String MOVE_TWO_GRID = "2";
    public static final String MOVE_THREE_GRID = "3";
    public static final String MOVE_FOUR_GRID = "4";
    public static final String MOVE_FIVE_GRID = "5";
    public static final String MOVE_SIX_GRID = "6";
    public static final String MOVE_SEVEN_GRID = "7";
    public static final String MOVE_EIGHT_GRID = "8";
    public static final String MOVE_NINE_GRID = "9";

    public static final String TURN_LEFT = "L";
    public static final String TURN_RIGHT = "R";
    public static final String RECALIBRATE_START= "C";
    public static final String UTURN = "B";
    public static final String START_READY = "P";


}

