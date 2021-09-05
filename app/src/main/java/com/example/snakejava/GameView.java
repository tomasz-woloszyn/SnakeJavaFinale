package com.example.snakejava;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.snakejava.Apple;
import com.example.snakejava.Constants;
import com.example.snakejava.Grass;
import com.example.snakejava.MainActivity;
import com.example.snakejava.R;
import com.example.snakejava.Snake;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {
    private Bitmap bmGrass1, bmGrass2, bmSnake1,bmSnake3,bmSnake5, bmApple, bmBomb;
    private ArrayList<Grass> arrGrass = new ArrayList<>();
    private int w = 12, h=21;
    public static int sizeOfMap = 75* Constants.SCREEN_WIDTH/1080;
    private Snake snake;

    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final String USERS = "users";

    private Apple apple;
    private Handler handler;
    private Runnable r;
    private boolean move = false;
    private float mx, my;
    public static boolean isPlaying = false;
    public static int score = 0, bestScore = 0;
    private Context context;
    private int soundEat, soundDie, soundBomb;
    private float volume;
    private boolean loadedsound;
    private Bomb bomb;
    private SoundPool soundPool;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        SharedPreferences sp = context.getSharedPreferences("gamesetting", Context.MODE_PRIVATE);

        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USERS);
        mAuth = FirebaseAuth.getInstance();

        if(sp!=null){
            bestScore = sp.getInt("bestscore",0);
        }
        bmGrass1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.grass_1);
        bmGrass1 = Bitmap.createScaledBitmap(bmGrass1, sizeOfMap, sizeOfMap, true);
        bmGrass2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.grass_2);
        bmGrass2 = Bitmap.createScaledBitmap(bmGrass2, sizeOfMap, sizeOfMap, true);

        bmSnake1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.snake2);
        bmSnake3 = BitmapFactory.decodeResource(this.getResources(), R.drawable.snake3);
        bmSnake5 = BitmapFactory.decodeResource(this.getResources(), R.drawable.snake4);

        bmSnake1 = Bitmap.createScaledBitmap(bmSnake1, 14*sizeOfMap, sizeOfMap, true);
        bmApple = BitmapFactory.decodeResource(this.getResources(), R.drawable.apple_good);
        bmApple = Bitmap.createScaledBitmap(bmApple, sizeOfMap, sizeOfMap, true);
        bmBomb = BitmapFactory.decodeResource(this.getResources(), R.drawable.bomb);
        bmBomb = Bitmap.createScaledBitmap(bmBomb, sizeOfMap, sizeOfMap, true);
        bmSnake3 = Bitmap.createScaledBitmap(bmSnake3, 14*sizeOfMap, sizeOfMap, true);
        bmSnake5 = Bitmap.createScaledBitmap(bmSnake5, 14*sizeOfMap, sizeOfMap, true);

        for(int i = 0; i < h; i++){
            for (int j = 0; j < w; j++){
                if((j+i)%2==0){
                    arrGrass.add(new Grass(bmGrass1, j*bmGrass1.getWidth() + Constants.SCREEN_WIDTH/2 - (w/2)*bmGrass1.getWidth(), i*bmGrass1.getHeight()+50*Constants.SCREEN_HEIGHT/1920, bmGrass1.getWidth(), bmGrass1.getHeight()));
                }else{
                    arrGrass.add(new Grass(bmGrass2, j*bmGrass2.getWidth() + Constants.SCREEN_WIDTH/2 - (w/2)*bmGrass2.getWidth(), i*bmGrass2.getHeight()+50*Constants.SCREEN_HEIGHT/1920, bmGrass2.getWidth(), bmGrass2.getHeight()));
                }
            }
        }
        snake = new Snake(bmSnake1,arrGrass.get(126).getX(),arrGrass.get(126).getY(), 4);
        apple = new Apple(bmApple, arrGrass.get(randomApple()[0]).getX(), arrGrass.get(randomApple()[1]).getY());
        bomb = new Bomb(bmBomb, arrGrass.get(randomBomb()[0]).getX(), arrGrass.get(randomBomb()[1]).getY());

        handler = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };
        if(Build.VERSION.SDK_INT>=21){
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(audioAttributes).setMaxStreams(5);
            this.soundPool = builder.build();
        }else{
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loadedsound = true;
            }
        });
        soundEat = this.soundPool.load(context, R.raw.eat, 1);
        soundDie = this.soundPool.load(context, R.raw.die, 1);
        soundBomb = this.soundPool.load(context,R.raw.bomb,1);
    }

    private int[] randomApple(){
        int []xy = new int[2];
        Random r = new Random();
        xy[0] = r.nextInt(arrGrass.size()-1);
        xy[1] = r.nextInt(arrGrass.size()-1);
        Rect rect = new Rect(arrGrass.get(xy[0]).getX(), arrGrass.get(xy[1]).getY(), arrGrass.get(xy[0]).getX()+sizeOfMap, arrGrass.get(xy[1]).getY()+sizeOfMap);
        boolean check = true;
        while (check){
            check = false;
            for (int i = 0; i < snake.getArrPartSnake().size(); i++){
                if(rect.intersect(snake.getArrPartSnake().get(i).getrBody())){
                    check = true;
                    xy[0] = r.nextInt(arrGrass.size()-1);
                    xy[1] = r.nextInt(arrGrass.size()-1);
                    rect = new Rect(arrGrass.get(xy[0]).getX(), arrGrass.get(xy[1]).getY(), arrGrass.get(xy[0]).getX()+sizeOfMap, arrGrass.get(xy[1]).getY()+sizeOfMap);
                }
            }
        }
        return xy;
    }


    private int[] randomBomb(){
        int []xy = new int[2];
        Random r = new Random();
        xy[0] = r.nextInt(arrGrass.size()-1);
        xy[1] = r.nextInt(arrGrass.size()-1);
        Rect rect = new Rect(arrGrass.get(xy[0]).getX(), arrGrass.get(xy[1]).getY(), arrGrass.get(xy[0]).getX()+sizeOfMap, arrGrass.get(xy[1]).getY()+sizeOfMap);
        boolean check = true;
        while (check){
            check = false;
            for (int i = 0; i < snake.getArrPartSnake().size(); i++){
                if(rect.intersect(snake.getArrPartSnake().get(i).getrBody())){
                    check = true;
                    xy[0] = r.nextInt(arrGrass.size()-1);
                    xy[1] = r.nextInt(arrGrass.size()-1);
                    rect = new Rect(arrGrass.get(xy[0]).getX(), arrGrass.get(xy[1]).getY(), arrGrass.get(xy[0]).getX()+sizeOfMap, arrGrass.get(xy[1]).getY()+sizeOfMap);
                }
            }
        }
        return xy;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int a = event.getActionMasked();
        switch (a){
            case  MotionEvent.ACTION_MOVE:{
                if(move==false){
                    mx = event.getX();
                    my = event.getY();
                    move = true;
                }else{
                    if(mx - event.getX() > 100 && !snake.isMove_right()){
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMove_left(true);
                        isPlaying = true;
                        MainActivity.img_swipe.setVisibility(INVISIBLE);
                    }else if(event.getX() - mx > 100 &&!snake.isMove_left()){
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMove_right(true);
                        isPlaying = true;
                        MainActivity.img_swipe.setVisibility(INVISIBLE);
                    }else if(event.getY() - my > 100 && !snake.isMove_top()){
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMove_bottom(true);
                        isPlaying = true;
                        MainActivity.img_swipe.setVisibility(INVISIBLE);
                    }else if(my - event.getY() > 100 && !snake.isMove_bottom()){
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMove_top(true);
                        isPlaying = true;
                        MainActivity.img_swipe.setVisibility(INVISIBLE);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:{
                mx = 0;
                my = 0;
                move = false;
                break;
            }
        }
        return true;
    }

    public void draw(Canvas canvas){
        super.draw(canvas);
        canvas.drawColor(0xFF065700);
        for(int i = 0; i < arrGrass.size(); i++){
            canvas.drawBitmap(arrGrass.get(i).getBm(), arrGrass.get(i).getX(), arrGrass.get(i).getY(), null);
        }
        if(isPlaying){


            if(score == 5)
            {
                snake.updateskin(bmSnake3);
            }
            if(score == 10)
            {
                snake.updateskin(bmSnake5);
            }


            snake.update();
            if(snake.getArrPartSnake().get(0).getX() < this.arrGrass.get(0).getX()
                    ||snake.getArrPartSnake().get(0).getY() < this.arrGrass.get(0).getY()
                    ||snake.getArrPartSnake().get(0).getY()+sizeOfMap>this.arrGrass.get(this.arrGrass.size()-1).getY() + sizeOfMap
                    ||snake.getArrPartSnake().get(0).getX()+sizeOfMap>this.arrGrass.get(this.arrGrass.size()-1).getX() + sizeOfMap){
                gameOver();
            }
            for (int i = 1; i < snake.getArrPartSnake().size(); i++) {
                if (snake.getArrPartSnake().get(0).getrBody().intersect(snake.getArrPartSnake().get(i).getrBody())) {
                    gameOver();
                }
            }
            if (score == -1)
            {
                gameOver();
            }

        }
        snake.draw(canvas);
        apple.draw(canvas);
        bomb.draw(canvas);

        if(snake.getArrPartSnake().get(0).getrBody().intersect(bomb.getR())){
            if(loadedsound){
                int streamId = this.soundPool.play(this.soundBomb, (float)0.5, (float)0.5, 1, 0, 1f);
            }
            bomb.reset(arrGrass.get(randomBomb()[0]).getX(), arrGrass.get(randomBomb()[1]).getY());
            snake.removePart();
            score--;
            MainActivity.txt_score.setText(score+"");
            if(score > bestScore){
                bestScore = score;
                SharedPreferences sp = context.getSharedPreferences("gamesetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("bestscore", bestScore);
                editor.apply();
                MainActivity.txt_best_score.setText(bestScore+"");
            }

        }


        if(snake.getArrPartSnake().get(0).getrBody().intersect(apple.getR())){
            if(loadedsound){
                int streamId = this.soundPool.play(this.soundEat, (float)2, (float)2, 1, 0, 1f);
            }
            apple.reset(arrGrass.get(randomApple()[0]).getX(), arrGrass.get(randomApple()[1]).getY());
            snake.addPart();
            score++;
            MainActivity.txt_score.setText(score+"");
            if(score > bestScore){
                bestScore = score;
                SharedPreferences sp = context.getSharedPreferences("gamesetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("bestscore", bestScore);
                editor.apply();
                MainActivity.txt_best_score.setText(bestScore+"");
            }
        }
        handler.postDelayed(r, 100);
    }

    private void gameOver() {
        isPlaying = false;
        MainActivity.dialogScore.show();
        MainActivity.txt_dialog_best_score.setText(bestScore+"");
        MainActivity.txt_dialog_score.setText(score+"");
        FirebaseUser curr_user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = database.getReference(USERS).child(curr_user.getUid()).child("score");
        mDatabase.setValue(bestScore);


        if(loadedsound){
            int streamId = this.soundPool.play(this.soundDie, (float)0.5, (float)0.5, 1, 0, 1f);
        }
    }

    public void reset(){
        for(int i = 0; i < h; i++){
            for (int j = 0; j < w; j++){
                if((j+i)%2==0){
                    arrGrass.add(new Grass(bmGrass1, j*bmGrass1.getWidth() + Constants.SCREEN_WIDTH/2 - (w/2)*bmGrass1.getWidth(), i*bmGrass1.getHeight()+50*Constants.SCREEN_HEIGHT/1920, bmGrass1.getWidth(), bmGrass1.getHeight()));
                }else{
                    arrGrass.add(new Grass(bmGrass2, j*bmGrass2.getWidth() + Constants.SCREEN_WIDTH/2 - (w/2)*bmGrass2.getWidth(), i*bmGrass2.getHeight()+50*Constants.SCREEN_HEIGHT/1920, bmGrass2.getWidth(), bmGrass2.getHeight()));
                }
            }
        }
        snake = new Snake(bmSnake1,arrGrass.get(126).getX(),arrGrass.get(126).getY(), 4);
        apple = new Apple(bmApple, arrGrass.get(randomApple()[0]).getX(), arrGrass.get(randomApple()[1]).getY());
        bomb = new Bomb(bmBomb, arrGrass.get(randomBomb()[0]).getX(), arrGrass.get(randomBomb()[1]).getY());

        score = 0;
    }
}
