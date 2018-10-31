package sg.edu.ntu.wa0002qie.myapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class Robot {
    // array holding information of map and robot
    private int[][] gridSettings; // array of length 300 with explore information
    private int[] headPos = new int[2];
    private int[] robotPos = new int[2];
    private int[][] obstacleArray = new int[20][15];
    private int[][] spArray = new int[20][15];
    private int[][] arrowArray = new int[20][15];

    private int X;
    private int Y;
    private int _X;
    private int _Y;

    private Paint paint;
    private Canvas canvas;
    private static int size = 0;
    private final static int ROW = 20;
    private final static int COLUMN = 15;

    private int waypoint_x = -1;
    private int waypoint_y = -1;


    public void setUpArrow(Drawable upArrow) {
        this.upArrow = upArrow;
    }
    public void setLeftArrow(Drawable leftArrow) {
        this.leftArrow = leftArrow;
    }
    public void setRightArrow(Drawable rightArrow) {
        this.rightArrow = rightArrow;
    }
    public void setWayPoint(Drawable waypoint) {this.waypoint = waypoint;}

    private Drawable upArrow;
    private Drawable leftArrow;
    private Drawable rightArrow;
    private Drawable waypoint;

    public Robot(){
        super();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    public void setCanvas(Canvas c){
        this.canvas = c;
    }

    public void drawArena(Canvas canvas, int gridSize){
        setCanvas(canvas);
        this.size = gridSize;

        int rHeadX = headPos[0];
        int rHeadY = headPos[1];
        int rRobotX = robotPos[0];
        int rRobotY = robotPos[1];

        boolean directionUD = false,
                directionLR = false;


        // BG
        int index = 0;
        for (int i = 0; i < ROW; i++){
            for (int j = 0; j < COLUMN; j++){
                if(gridSettings[i][j] == 0){
                    drawCell(j + 1, i + 1, gridSize, Color.parseColor("#FFF9C4"), canvas);
                }
                else if(gridSettings[i][j] == 1){
                    drawCell(j + 1, i + 1, gridSize, Color.parseColor("#FFE7F9"), canvas);
                }
            }
        }

        // Obstacles
        for (int i = 0; i < 20; i++){
            for(int j = 0; j < 15; j++) {
                if (this.obstacleArray[i][j] == 0)        // unexplored cell
                    drawCell(j + 1, i + 1, gridSize, Color.TRANSPARENT, canvas);

                else if (this.obstacleArray[i][j] == 1)   // empty cell
                    drawCell(j + 1, i + 1, gridSize, Color.parseColor("#333333"), canvas);

//                else                                      // obstacle
//                    drawCell(j + 1, i + 1, gridSize, Color.parseColor("#333333"), canvas);
            }


        }

        // Arrows
        for(int i = 0 ; i < 20; i++){
            for(int j = 0; j < 15; j++) {
                if (this.arrowArray[i][j] == 0)        // unexplored cell
                    drawCell(j + 1, i + 1, gridSize, Color.TRANSPARENT, canvas);

                else if (this.arrowArray[i][j] == 1)   // empty cell
                    drawImage(j + 1, i + 1, gridSize, upArrow, canvas);

                else                                   // arrow
                {
                    drawCell(j + 1, i + 1, gridSize, Color.TRANSPARENT, canvas);
                }
            }
        }
        // WayPoint
        if(waypoint_x != -1 && waypoint_y != -1){
            drawImage(waypoint_x, 21 - waypoint_y, gridSize, waypoint, canvas);
        }

        // START
        for (int i = 1; i <= 3; i++){
            for (int j = ROW-2; j <= ROW; j++){
                drawCell(i, j, gridSize, Color.parseColor("#E0E0E0"), canvas);
            }
        }

        // GOAL
        for (int i = COLUMN-2; i <= COLUMN; i++){
            for (int j = 1; j <= 3; j++){
                drawCell(i, j, gridSize, Color.parseColor("#efa1bfc7"), canvas);
            }
        }

        // ROBOT
        // See whether the robot is towards horizontal way or vertical way
        if(rHeadX == rRobotX){
            directionUD = true;
            directionLR = false;
        }
        if(rHeadY == rRobotY){
            directionUD = false;
            directionLR = true;
        }

        // Draw Robot
        if(directionLR){
            for (int i = rRobotX-1; i <= rRobotX+1; i++){
                for (int j = rRobotY-1; j < rRobotY+2; j++){
                    drawCell(i, j, gridSize, Color.parseColor("#FFD600"), canvas);
                }
            }
            //head
            for (int j = rRobotY-1; j <= rRobotY+1; j++){
                drawCell(rHeadX, j, gridSize, Color.parseColor("#FFD600"), canvas);
            }
        }
        if(directionUD){
            for (int i = rRobotX-1; i <= rRobotX+1; i++){
                for (int j = rRobotY-1; j <= rRobotY+1; j++){
                    drawCell(i, j, gridSize, Color.parseColor("#FFD600"), canvas);
                }
            }
            //head  FFFF00
            for (int i = rRobotX-1; i <= rRobotX+1; i++){
                drawCell(i, rHeadY, gridSize, Color.parseColor("#FFD600"), canvas);
            }
        }

        // shortest path
        for (int i = 0; i < 20; i++){
            for (int j = 0; j < 15; j++){
                if (this.spArray[i][j] == 1) {
                    drawCell(j + 1, i + 1, gridSize, Color.parseColor("#4DD0E1"), canvas);
                }
            }
        }
    }

    public void drawCell(int i, int j, int gridSize, int c, Canvas canvas){
        X = i * gridSize - gridSize / 2;
        Y = j * gridSize - gridSize / 2;
        _X = i * gridSize + gridSize / 2;
        _Y = j * gridSize + gridSize / 2;
        // paint the fill in color
        paint.setColor(c);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(new RectF(X,Y,_X,_Y), paint);
        // paint the stroke border
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(new RectF(X,Y,_X,_Y), paint);
    }

    public void drawImage(int i, int j, int gridSize, Drawable d, Canvas canvas){
        Drawable drawable = d;
        X = i * gridSize - gridSize / 2;
        Y = j * gridSize - gridSize / 2;
        _X = i * gridSize + gridSize / 2;
        _Y = j * gridSize + gridSize / 2;
        // paint the fill in color
        paint.setStyle(Paint.Style.FILL);
        drawable.setBounds(X, Y, _X, _Y);
        drawable.draw(canvas);
    }

    public void setGridSettings(int[][] gridArray) {
        this.gridSettings = gridArray;
    }
    public void setObstacles(int[][] obstacleArray) {
        this.obstacleArray = obstacleArray;
    }
    public void setSpArray(int[][] spArray){
        this.spArray = spArray;
    }
    public void setArrowArray(int[][] arrowArray){ this.arrowArray = arrowArray; }
    public void setHeadPos(int[] headPos) {
        this.headPos = headPos;
    }
    public void setRobotPos(int[] robotPos) {
        this.robotPos = robotPos;
    }
    public void setWayPoint(int x, int y){
        waypoint_x = x;
        waypoint_y = y;
    }
}

