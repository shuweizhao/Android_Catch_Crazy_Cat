package com.example.shuweizhao.catchcrazycat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by shuweizhao on 1/2/16.
 */
public class PlayGround extends SurfaceView implements View.OnTouchListener{

    private static int WIDTH = 60;
    private static final int ROWLEN = 10;
    private static final int COLLEN = 10;
    private static final int NUMBLOCKS = 20;
    private int score;
    private int highestScore = Integer.MAX_VALUE;

    Dot[][] matrix;
    Dot cat;
    public PlayGround(Context context) {
        super(context);
        getHolder().addCallback(callback);
        matrix = new Dot[ROWLEN][COLLEN];
        for(int i = 0; i < ROWLEN; i++) {
            for (int j = 0; j < COLLEN; j++){
                matrix[i][j] = new Dot(j, i);
            }
        }
        initGame();
        setOnTouchListener(this);
    }

    private Dot getDot(int x, int y) {
        if (x < 0 || x >= COLLEN || y < 0 || y >= ROWLEN) {
            return null;
        }
        return matrix[y][x];
    }

    private boolean isAtEdge(Dot d) {
        if (d.getX() * d.getY() == 0 || d.getX() + 1 == COLLEN || d.getY() + 1 == ROWLEN) {
            return true;
        }
        return false;
    }

    private Dot getNeighbor(Dot d, int dir) {
        if (d == null) {
            return null;
        }
        int srcX = d.getX();
        int srcY = d.getY();
        switch (dir) {
            case 1:
                return getDot(srcX - 1, srcY);
            case 2:
                if (srcY % 2 == 0) {
                    return getDot(srcX - 1, srcY - 1);
                }
                else {
                    return getDot(srcX, srcY - 1);
                }
            case 3:
                if (srcY % 2 == 0) {
                    return getDot(srcX, srcY - 1);
                }
                else {
                    return getDot(srcX + 1, srcY - 1);
                }
            case 4:
                return getDot(srcX + 1, srcY);
            case 5:
                if (srcY % 2 == 0) {
                    return getDot(srcX, srcY + 1);
                }
                else {
                    return getDot(srcX + 1, srcY + 1);
                }
            case 6:
                if (srcY % 2 == 0) {
                    return getDot(srcX - 1, srcY + 1);
                }
                else {
                    return getDot(srcX, srcY + 1);
                }
            default:
        }
        return null;
    }

    private int getDistance(Dot d, int dir) {
        int distance = 0;
        if (isAtEdge(d)) {
            return 1;
        }
        Dot origin = d, next;
        while(true) {
            next = getNeighbor(origin, dir);
            if (isAtEdge(next)) {
                distance++;
                return distance;
            }
            if (next.getStatus() == Dot.STATUS_ON) {
                return -distance;
            }

            distance++;
            origin = next;
        }
    }

    private void moveTo(Dot d) {
        d.setStatus(Dot.STATUS_IN);
        getDot(cat.getX(), cat.getY()).setStatus(Dot.STATUS_OFF);
        cat.setXY(d.getX(), d.getY());
        score++;
    }

    private void move() {
        if (isAtEdge(cat)) {
            alert(false);
            return;
        }

        List<Dot> avalNeighbour = new ArrayList<>();
        HashMap<Dot, Integer> positive = new HashMap<>();
        HashMap<Dot, Integer> negative = new HashMap<>();
        for (int i = 1; i < 7; i++) {
            Dot neighbor = getNeighbor(cat, i);
            if (neighbor.getStatus() == Dot.STATUS_OFF) {
                avalNeighbour.add(neighbor);
                if (getDistance(neighbor, i) > 0) {
                    positive.put(neighbor, i);
                }
                else {
                    negative.put(neighbor, i);
                }
            }
        }
        if (avalNeighbour.size() == 0) {
            alert(true);
            return;
        }
        else if (avalNeighbour.size() == 1) {
            moveTo(avalNeighbour.get(0));
        }
        else {
            Dot best = null;
            if (positive.size() != 0) {
                int min = Integer.MAX_VALUE;
                for (Dot d : positive.keySet()) {
                    int a = getDistance(d, positive.get(d));
                    if (a < min) {
                        min = a;
                        best = d;
                    }
                }
                moveTo(best);
            }
            else {
                int max = 1;
                for (int i = 0; i < avalNeighbour.size(); i++) {
                    int b = getDistance(avalNeighbour.get(i), negative.get(avalNeighbour.get(i)));
                    if (b < max) {
                        max = b;
                        best = avalNeighbour.get(i);
                    }
                }
                moveTo(best);
            }
        }
    }

    private void alert(boolean win) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String text = "";
        if (win) {
            text = "Congratulations, You Win!";
            highestScore = highestScore < score ? highestScore : score;
        }
        else {
            text = "Sorry, You Lose!";
        }
        builder.setTitle(text);
        if (highestScore == Integer.MAX_VALUE) {
            builder.setMessage("Your score is " + score + "\n" + "highest score is NONE");
        }
        else {
            builder.setMessage("Your score is " + score + "\n" + "highest score is " + highestScore);
        }
            builder.setPositiveButton("Restart again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initGame();
                redraw();
            }
        });
        builder.show();
        //Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }



    private void redraw() {
        Canvas c = getHolder().lockCanvas();
        c.drawColor(Color.CYAN);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        for (int i = 0; i < ROWLEN; i++) {
            int offset = 0;
            if (i % 2 != 0) {
                offset = WIDTH / 2;
            }
            for (int j = 0; j < COLLEN; j++) {
                Dot d = getDot(j, i);
                switch (d.getStatus()) {
                    case Dot.STATUS_OFF:
                        paint.setColor(Color.WHITE);
                        break;
                    case Dot.STATUS_ON:
                        paint.setColor(Color.BLUE);
                        break;
                    case Dot.STATUS_IN:
                        paint.setColor(Color.RED);
                        break;
                    default:
                        break;
                }
                c.drawOval(new RectF(d.getX()*WIDTH + offset, d.getY()*WIDTH, (d.getX()+1)*WIDTH + offset, (d.getY()+1)*WIDTH), paint);
            }
        }
        getHolder().unlockCanvasAndPost(c);
    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            redraw();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            WIDTH = width / (COLLEN + 1);
            redraw();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    public void initGame() {
        score = 0;
        for (int i = 0; i < ROWLEN; i++) {
            for (int j = 0; j < COLLEN; j++) {
                matrix[i][j].setStatus(Dot.STATUS_OFF);
            }
        }
        cat = new Dot(4,5);
        getDot(4,5).setStatus(Dot.STATUS_IN); // set cat's status
        for (int i = 0; i < NUMBLOCKS;) {
            int xIndex = (int) ((Math.random() * 1000) % COLLEN);
            int yIndex = (int) ((Math.random() * 1000) % ROWLEN);
            if (getDot(xIndex, yIndex).getStatus() == Dot.STATUS_OFF) {
                getDot(xIndex, yIndex).setStatus(Dot.STATUS_ON);
                i++;
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            //Toast.makeText(getContext(), event.getX() + ":"+ event.getY(), Toast.LENGTH_SHORT).show();
            int x = 0, y = 0;
            y = (int) (event.getY() / WIDTH);
            if (y % 2 == 0) {
                x = (int) (event.getX() / WIDTH);
            }
            else {
                x = (int) ((event.getX() - WIDTH / 2) / WIDTH);
            }
            if ((x + 1 > COLLEN) || (y + 1> ROWLEN)) {
                Toast.makeText(getContext(), "Invalid Touch", Toast.LENGTH_SHORT).show();
                initGame();
            }
            else if (getDot(x, y).getStatus() == Dot.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS_ON);
                move();
            }
            redraw();
        }
        return true;
    }
}

