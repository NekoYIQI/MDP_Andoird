package sg.edu.ntu.wa0002qie.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class Arena extends View {
    private int col = 15;
    private Robot robot;
    private ArenaThread thread;
    private int gridSize;
    private int[] grid;
    private int[][] obstacles = new int[15][20];
    private int[][] spArray = new int[15][20];

    public Arena(Context context, int[] array){
        super(context);
        robot = new Robot();
        thread = new ArenaThread(this);
        thread.startThread();
    }

    @Override
    public void onDraw(Canvas canvas){
//        Log.d("Arena", "onDraw");
        RelativeLayout arenaView = (RelativeLayout) getRootView().findViewById(R.id.arenaView);
        gridSize = ((arenaView.getMeasuredWidth()) - (arenaView.getMeasuredWidth() / col)) / col;;
        robot.drawArena(canvas, gridSize);
    }

    public void setGridArray(int[] gridArray){
        Log.d("Arena", "set grid array");
        this.grid = gridArray;
    }

    public void setSpArray(int[][] spArray){
        this.spArray = spArray;
    }
    public void setObstacles(int[][] obstacles){
        this.obstacles = obstacles;
    }
    public void update(){
        robot.setGridSettings(grid);
        robot.setObstacles(obstacles);
        robot.setSpArray(spArray);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int column = (int)(event.getX() / gridSize);
        int row = (int)(event.getY() / gridSize);
        Log.d("Arena",  "clicked");
        Log.d("Column: ",  column+"");
        Log.d("Row: ",  row+"");
        int size = grid.length;
        Log.d("Arena","length of gird array = "+size);
        MainActivity.getInstance().setCoordinate(column, row);
        return true;
    }
}

