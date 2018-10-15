package sg.edu.ntu.wa0002qie.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class Arena extends View {
    private int col = Constants.MAP_COL;
    private Robot robot;
    private ArenaThread thread;
    private int gridSize;

    // array holding information of the map
    private int[][] grid = new int[Constants.MAP_ROW][Constants.MAP_COL]; // array of 300 digits
    private int[][] obstacles = new int[Constants.MAP_ROW][Constants.MAP_COL];
    private int[][] spArray = new int[Constants.MAP_ROW][Constants.MAP_COL];
    private int[][] arrowArray = new int[Constants.MAP_ROW][Constants.MAP_COL];
    private int[] headPos = new int[2];
    private int[] robotPos = new int[2];

    private Drawable upArrow;
    private Drawable leftArrow;
    private Drawable rightArrow;
    private Drawable waypoint;

    public Arena(Context context){
        super(context);
        robot = new Robot();
        thread = new ArenaThread(this);
        thread.startThread();
        upArrow = context.getResources().getDrawable(R.drawable.up);
        leftArrow = context.getResources().getDrawable(R.drawable.left);
        rightArrow = context.getResources().getDrawable(R.drawable.right);
        waypoint = context.getResources().getDrawable(R.drawable.candy);
        robot.setUpArrow(upArrow);
        robot.setLeftArrow(leftArrow);
        robot.setRightArrow(rightArrow);
        robot.setWayPoint(waypoint);
    }

    @Override
    public void onDraw(Canvas canvas){
//        Log.d("Arena", "onDraw");
        RelativeLayout arenaView = (RelativeLayout) getRootView().findViewById(R.id.arenaView);
        gridSize = ((arenaView.getMeasuredWidth()) - (arenaView.getMeasuredWidth() / col)) / col;;
        robot.drawArena(canvas, gridSize);
    }

    public void setGridArray(int[][] gridArray){
        Log.d("Arena", "set grid array");
        this.grid = gridArray;
    }

    public void setSpArray(int[][] spArray){
        this.spArray = spArray;
    }
    public void setObstacles(int[][] obstacles){
        this.obstacles = obstacles;
    }
    public void setArrowArray(int[][] arrowArray){ this.arrowArray = arrowArray; }
    public void setHeadPos(int[] headPos) {
        this.headPos[0] = headPos[0];
        this.headPos[1] = Constants.MAP_ROW - headPos[1] + 1;
    }
    public void setRobotPos(int[] robotPos) {
        this.robotPos[0] = robotPos[0];
        this.robotPos[1] = Constants.MAP_ROW - robotPos[1 + 1];
    }
    public void setWayPoint(int x, int y){
        this.robot.setWayPoint(x, y);
    }

    public void update(){
        robot.setGridSettings(grid);
        robot.setObstacles(obstacles);
        robot.setSpArray(spArray);
        robot.setArrowArray(arrowArray);
        robot.setHeadPos(headPos);
        robot.setRobotPos(robotPos);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        int column = Math.round(event.getX() / gridSize);
        //invert "ROW"

        int row = Constants.MAP_ROW - Math.round(event.getY() / gridSize) +1;
        Log.d("Arena",  "clicked");
        Log.d("Column: ",  column+"");
        Log.d("Row: ",  row+"");
        MainActivity.getInstance().setCoordinate(column, row);
        return true;
    }
}

