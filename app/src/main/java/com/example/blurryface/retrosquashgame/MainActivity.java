package com.example.blurryface.retrosquashgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    //canvas
    Canvas canvas;
    SquashCourtView courtView;
    //sound
    private SoundPool soundPool;
    private int sample1,sample2,sample3,sample4;

    //normalising pixels
    Display display;
    int screenWidth,screenHeight;
    //game objects
    int racketWidth,racketHeight;
    Point racketPosition;

    int ballWidth;
    Point ballPosition;
    //variables to keep track of movements
    //racket movement
    boolean racketIsMovingLeft,racketIsMovingRight;
    //ball movement
    public boolean ballIsMovingLeft,ballIsMovingRight,ballIsMovingUp,ballIsMovingDown;

    //Score tracker
    int score,lives,speed;
    //variable to keep track of fps..Game is running the same on all platforms
    int fps;
    long lastFrameTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        courtView = new SquashCourtView(this);
        setContentView(courtView);
        //initializing sound
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        sample1 = soundPool.load(this,R.raw.sample1,0);
        sample2 = soundPool.load(this,R.raw.sample2,0);
        sample3 = soundPool.load(this,R.raw.sample3,0);
        sample4 = soundPool.load(this,R.raw.sample4,0);

        //initializing Display
        display = getWindowManager().getDefaultDisplay();
        //getting the height and width of the screen
        Point size = new Point();
        display.getSize(size);
        screenHeight = size.y;
        screenWidth = size.x;
        //game objects
        racketPosition = new Point();
        racketPosition.x = screenWidth/2;
        racketPosition.y = screenHeight-20;
        racketWidth = screenWidth/6;
        racketHeight =20;

        ballWidth = screenWidth/35;
        ballPosition = new Point();
        ballPosition.x = screenWidth/2;
        ballPosition.y = 1+ballWidth;

        //initialize lives
        lives = 3;
        score =0;
        speed =10;

    }

    @Override
    protected void onResume() {
        super.onResume();
        courtView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        courtView.pause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if (event.getX()>=(screenWidth/2))
                {
                    racketIsMovingRight=true;
                    racketIsMovingLeft=false;
                }
                else
                {
                    racketIsMovingLeft=true;
                    racketIsMovingRight=false;
                }
                break;
            case MotionEvent.ACTION_UP:
                racketIsMovingLeft=false;
                racketIsMovingRight =false;
                break;
        }
        return true;
    }

    public class SquashCourtView extends SurfaceView implements Runnable {
        Thread logicThread;
        SurfaceHolder holder;
        volatile boolean playingSquash;
        Paint paint;

        public SquashCourtView(Context context) {
            super(context);
            //initialize variables
            holder = getHolder();
            paint = new Paint();
            ballIsMovingDown = true;
            Random ran = new Random();
            int direction = ran.nextInt(3);
            switch (direction)
            {
                //moves at the center
                case 0:
                    ballIsMovingLeft = false;
                    ballIsMovingRight = false;
                    break;
                //moves left
                case 1:
                    ballIsMovingLeft = true;
                    ballIsMovingRight = false;
                    break;
                //moves right
                case 2:
                    ballIsMovingLeft = false;
                    ballIsMovingRight = true;
                    break;
            }
        }

        @Override
        public void run() {
            while (playingSquash) {
                updateLogic();
                drawCourt();
                controlFPS();
            }
        }
        public void updateLogic()
        {
            if(racketIsMovingRight)
            {
                if(racketPosition.x +(racketWidth/2)<screenWidth)
                {
                    racketPosition.x+=20;
                }
            }
            if(racketIsMovingLeft)
            {
                if(racketPosition.x - (racketWidth/2)>0)
                    racketPosition.x -=20;
            }


            //ball movement
            //right
            if(ballPosition.x + ballWidth > screenWidth)
            {
                ballIsMovingLeft = true;
                ballIsMovingRight =false;
                soundPool.play(sample1,1,1,0,0,1);
            }
            //left
            if(ballPosition.x <0)
            {
                ballIsMovingLeft = false;
                ballIsMovingRight =true;
                soundPool.play(sample1,1,1,0,0,1);
            }
            //buttom
            if(ballPosition.y>screenHeight-ballWidth) {
                lives -= 1;
                if (lives == 0) {
                    lives = 3;
                    score = 0;
                    soundPool.play(sample4, 1, 1, 0, 0, 1);
                }
                //return ball to top
                ballPosition.y = 1 + ballWidth;
                Random ran = new Random();
                int direction = ran.nextInt(3);
                switch (direction) {
                    //moves at the center
                    case 0:
                        ballIsMovingLeft = false;
                        ballIsMovingRight = false;
                        break;
                    //moves left
                    case 1:
                        ballIsMovingLeft = true;
                        ballIsMovingRight = false;
                        break;
                    //moves right
                    case 2:
                        ballIsMovingLeft = false;
                        ballIsMovingRight = true;
                        break;
                }
            }
                //top
            if(ballPosition.y<=0){
                    ballIsMovingDown = true;
                    ballIsMovingUp = false;
                    ballPosition.y = 1;
                    soundPool.play(sample2,1,1,0,0,1);
                }
            //move ball
            if(ballIsMovingDown) {
                ballPosition.y +=6;
            }
            if (ballIsMovingUp) {
                ballPosition.y -= 10;
            }
            if(ballIsMovingLeft) {
                ballPosition.x -= 12;
            }
            if (ballIsMovingRight) {
                ballPosition.x += 12;
            }
            //has ball hit the racket
            if(ballPosition.y + ballWidth>=(racketPosition.y-racketHeight/2)){
                int halfRacket = racketWidth/2;
                if(ballPosition.x + ballWidth>(racketPosition.x - halfRacket)&& ballPosition.x - ballWidth<(racketPosition.x + halfRacket))
                {
                    soundPool.play(sample3,1,1,0,0,1);
                    score++;
                    speed+=4;
                    ballIsMovingUp = true;
                    ballIsMovingDown = false;
                    //go up lefty or righty
                    if(ballPosition.x>racketPosition.x)
                    {
                        ballIsMovingRight = true;
                        ballIsMovingLeft = false;
                    }
                    else
                    {
                        ballIsMovingRight = false;
                        ballIsMovingLeft = true;
                    }
                }
            }


        }
        //give android the items to draw
        public void drawCourt()
        {
            //check if the android system is busy
            if(!holder.getSurface().isValid())
                return;
            canvas = holder.lockCanvas();
            //backgroundcolor
            canvas.drawColor(Color.BLACK);
            //title
            paint.setColor(Color.BLUE);
            paint.setTextSize(45);
            String title = "Score: "+score +" Lives:"+ lives+" fps:"+fps;
            canvas.drawText(title,45,45,paint);
            //racket
            int left = racketPosition.x-(racketWidth/2);
            int top = racketPosition.y -(racketHeight/2);
            int right = racketPosition.x +(racketWidth/2);
            int bottom = racketPosition.y +(racketHeight/2);
            canvas.drawRect(left,top,right,bottom,paint);
            //ball
            canvas.drawCircle(ballPosition.x,ballPosition.y,ballWidth,paint);
            holder.unlockCanvasAndPost(canvas);

        }
        private void controlFPS()
        {
            long timeThisFrame = System.currentTimeMillis() - lastFrameTime;
            long timeToSleep = 15 - timeThisFrame;
            if(timeThisFrame>0)
            {
                fps = (int) (100/timeThisFrame);
            }
            if(timeToSleep>0)
            {
                try {
                    Thread.sleep(timeToSleep);
                }catch (InterruptedException e){}
            }
            lastFrameTime = System.currentTimeMillis();
        }

        public void resume()
        {
            playingSquash = true;
            logicThread = new Thread(this);
            logicThread.start();
        }
        public void pause()
        {
            playingSquash = false;
            try {
                logicThread.join();
            }catch (InterruptedException e){}

        }

    }
}
