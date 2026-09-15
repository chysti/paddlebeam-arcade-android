package com.chystialex.paddlebeamarcade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public final class GameView extends View {
    private static final int READY=0, PLAYING=1, PAUSED=2, WON=3, GAME_OVER=4;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path heartPath = new Path();
    private final Random random = new Random();
    private final ArrayList<Brick> bricks = new ArrayList<>();
    private final SharedPreferences prefs;
    private final SoundPool sounds;
    private final int wallSound, brickSound, winSound, loseSound;
    private long previousFrame;
    private int state = READY, score, highScore, lives = 3, level = 1;
    private float paddleX, paddleY, paddleW, paddleH;
    private float ballX, ballY, ballVX, ballVY, ballR, baseBallR;
    private float arenaLeft, arenaRight, arenaTop, arenaBottom;
    private boolean laidOut;
    private boolean dayTheme, muted;

    private static final int[][] PATTERNS = {
        {0x1fff,0x1fff,0x1fff,0x1fff,0x1fff,0x1fff},
        {0x0404,0x0a0a,0x1111,0x1b1b,0x0e0e,0x0404,0x0404},
        {0x1001,0x1803,0x1c07,0x1e0f,0x1fff,0x0ffe,0x07fc,0x03f8}
    };
    private static final int[] COLORS = {0xfff43f5e,0xffff8a1f,0xffffd43b,0xff35d77a,0xff29b6f6,0xff9b5de5,0xffff4fd8};

    GameView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        AudioAttributes attributes=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        sounds=new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attributes).build();
        wallSound=sounds.load(context,R.raw.wall_hit,1);
        brickSound=sounds.load(context,R.raw.brick_hit,1);
        winSound=sounds.load(context,R.raw.win,1);
        loseSound=sounds.load(context,R.raw.lose,1);
        prefs = context.getSharedPreferences("scores", Context.MODE_PRIVATE);
        highScore = prefs.getInt("high", 0);
        reloadSettings();
        textPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        setKeepScreenOn(true);
    }

    @Override protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        float d = getResources().getDisplayMetrics().density;
        arenaLeft = 14*d; arenaRight = w-14*d; arenaTop = 78*d; arenaBottom = h-18*d;
        paddleW = Math.min(w*.25f, 120*d); paddleH = 14*d;
        // Keep the paddle far enough above the control area that a dragging
        // finger does not cover the paddle or hide either of its edges.
        paddleX = w/2f; paddleY = arenaBottom-92*d;
        baseBallR = Math.max(9*d, w*.024f);
        laidOut = true;
        makeLevel(); resetBall(); previousFrame = SystemClock.uptimeMillis();
    }

    private void makeLevel() {
        bricks.clear();
        int[] pattern = PATTERNS[(level-1)%PATTERNS.length];
        int cols = level==1?7:level==2?9:level==3?11:Math.min(17,13+(level-4)/2);
        ballR=baseBallR*Math.max(.62f,1f-(level-1)*.055f);
        float gap = getWidth()*.006f;
        float bw = (arenaRight-arenaLeft-gap*(cols+1))/cols;
        float heightScale=Math.max(.70f,1f-(level-1)*.035f);
        float bh=Math.max(14*getResources().getDisplayMetrics().density,getHeight()*.038f*heightScale);
        float start = arenaTop+35*getResources().getDisplayMetrics().density;
        for (int row=0; row<pattern.length; row++) for (int col=0; col<cols; col++) {
            int sourceCol=Math.min(12,(int)((col+.5f)*13f/cols));
            if ((pattern[row] & (1 << (12-sourceCol))) == 0) continue;
            float left = arenaLeft+gap+col*(bw+gap);
            bricks.add(new Brick(left,start+row*(bh+gap),left+bw,start+row*(bh+gap)+bh,COLORS[(row+level-1)%COLORS.length], row%3==0 && level>1 ? 2 : 1));
        }
    }

    private void resetBall() {
        ballX=paddleX; ballY=paddleY-ballR-4;
        float speed = Math.min(getWidth(),getHeight())*(.48f+.035f*Math.min(level-1,9));
        ballVX=speed*(random.nextBoolean()?.55f:-.55f); ballVY=-speed*.84f;
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        drawBackground(c); drawHud(c); drawBricks(c); drawPaddle(c); drawBall(c);
        if (state != PLAYING) drawOverlay(c);
        if (state == PLAYING) update();
        postInvalidateOnAnimation();
    }

    private void drawBackground(Canvas c) {
        c.drawColor(dayTheme ? 0xffdbeafe : 0xff030712);
        paint.setShader(new RadialGradient(getWidth()/2f,getHeight()*.38f,getWidth()*.75f,dayTheme?0xfff8fafc:0xff172554,dayTheme?0xffbfdbfe:0xff02030a,Shader.TileMode.CLAMP));
        c.drawRect(0,0,getWidth(),getHeight(),paint); paint.setShader(null);
        paint.setColor(dayTheme?0x30547569:0x1829b6f6); paint.setStrokeWidth(1);
        float step=32*getResources().getDisplayMetrics().density;
        for(float x=arenaLeft;x<arenaRight;x+=step)c.drawLine(x,arenaTop,x,arenaBottom,paint);
        for(float y=arenaTop;y<arenaBottom;y+=step)c.drawLine(arenaLeft,y,arenaRight,y,paint);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(3); paint.setColor(dayTheme?0xff0369a1:0xff22d3ee); paint.setShadowLayer(12,0,0,dayTheme?0xff38bdf8:0xff06b6d4);
        c.drawRoundRect(arenaLeft,arenaTop,arenaRight,arenaBottom,12,12,paint);
        paint.clearShadowLayer(); paint.setStyle(Paint.Style.FILL);
    }

    private void drawHud(Canvas c) {
        textPaint.setTextAlign(Paint.Align.LEFT); textPaint.setTextSize(getWidth()*.046f); textPaint.setColor(dayTheme?0xff0f172a:0xfff8fafc);
        c.drawText(String.format("%06d",score),arenaLeft,50*getResources().getDisplayMetrics().density,textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER); textPaint.setTextSize(getWidth()*.034f); textPaint.setColor(dayTheme?0xff0369a1:0xff67e8f9);
        c.drawText(I18n.t(getContext(),"level")+" "+level,getWidth()/2f,48*getResources().getDisplayMetrics().density,textPaint);
        textPaint.setTextSize(getWidth()*.025f); textPaint.setColor(dayTheme?0xff475569:0xff94a3b8);
        c.drawText("HELP",getWidth()*.18f,68*getResources().getDisplayMetrics().density,textPaint);
        c.drawText(I18n.t(getContext(),"settings"),getWidth()*.40f,68*getResources().getDisplayMetrics().density,textPaint);
        c.drawText(I18n.t(getContext(),"about"),getWidth()*.62f,68*getResources().getDisplayMetrics().density,textPaint);
        textPaint.setColor(state==PAUSED?0xffffd43b:(dayTheme?0xff0369a1:0xff67e8f9));
        c.drawText("⏸ "+I18n.t(getContext(),"pause"),getWidth()*.84f,68*getResources().getDisplayMetrics().density,textPaint);
        drawLives(c);
    }

    private void drawLives(Canvas c){
        float d=getResources().getDisplayMetrics().density,size=13*d,gap=5*d;
        float start=arenaRight-size*3-gap*2, top=31*d;
        for(int i=0;i<3;i++){
            float x=start+i*(size+gap),y=top;
            heartPath.reset();heartPath.moveTo(x+size*.5f,y+size);
            heartPath.cubicTo(x+size*.42f,y+size*.86f,x,y+size*.60f,x,y+size*.30f);
            heartPath.cubicTo(x,y-size*.02f,x+size*.42f,y-size*.08f,x+size*.5f,y+size*.22f);
            heartPath.cubicTo(x+size*.58f,y-size*.08f,x+size,y-size*.02f,x+size,y+size*.30f);
            heartPath.cubicTo(x+size,y+size*.60f,x+size*.58f,y+size*.86f,x+size*.5f,y+size);heartPath.close();
            if(i<lives){paint.setStyle(Paint.Style.FILL);paint.setColor(0xffff315f);paint.setShadowLayer(10,0,0,0xffff174f);c.drawPath(heartPath,paint);paint.clearShadowLayer();}
            else{paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2*d);paint.setColor(dayTheme?0x66564768:0x66718096);c.drawPath(heartPath,paint);paint.setStyle(Paint.Style.FILL);}
        }
    }

    private void drawBricks(Canvas c) {
        for (Brick b:bricks) {
            paint.setColor(b.color); paint.setShadowLayer(9,0,0,b.color); c.drawRoundRect(b.r,5,5,paint); paint.clearShadowLayer();
            paint.setColor(0x99ffffff); c.drawRoundRect(b.r.left+4,b.r.top+3,b.r.right-4,b.r.top+6,3,3,paint);
            if(b.hp>1){ paint.setColor(0x99000000); c.drawRect(b.r.centerX()-10,b.r.centerY()-2,b.r.centerX()+10,b.r.centerY()+2,paint); }
        }
    }

    private void drawPaddle(Canvas c) {
        RectF r=new RectF(paddleX-paddleW/2,paddleY-paddleH/2,paddleX+paddleW/2,paddleY+paddleH/2);
        paint.setShader(new LinearGradient(r.left,r.top,r.right,r.bottom,new int[]{0xffe2e8f0,0xff22d3ee,0xff155e75},null,Shader.TileMode.CLAMP));
        paint.setShadowLayer(15,0,0,0xff22d3ee); c.drawRoundRect(r,paddleH/2,paddleH/2,paint); paint.clearShadowLayer(); paint.setShader(null);
    }

    private void drawBall(Canvas c) {
        paint.setColor(0xffffffff); paint.setShadowLayer(18,0,0,0xff67e8f9); c.drawCircle(ballX,ballY,ballR,paint); paint.clearShadowLayer();
        paint.setColor(0xffbae6fd); c.drawCircle(ballX-ballR*.25f,ballY-ballR*.25f,ballR*.35f,paint);
    }

    private void drawOverlay(Canvas c) {
        paint.setColor(0xb8000000); c.drawRoundRect(arenaLeft+25, getHeight()*.50f-140, arenaRight-25,getHeight()*.50f+140,25,25,paint);
        String title = state==READY?I18n.t(getContext(),"title"):state==PAUSED?I18n.t(getContext(),"pause"):state==WON?I18n.t(getContext(),"won"):I18n.t(getContext(),"over");
        String hint = state==READY?I18n.t(getContext(),"start"):state==PAUSED?I18n.t(getContext(),"continue"):state==WON?I18n.t(getContext(),"next"):I18n.t(getContext(),"new");
        textPaint.setTextAlign(Paint.Align.CENTER); textPaint.setColor(state==GAME_OVER?0xffff4f6d:0xff67e8f9); textPaint.setTextSize(getWidth()*.066f);
        textPaint.setShadowLayer(12,0,0,textPaint.getColor()); c.drawText(title,getWidth()/2f,getHeight()*.50f-55,textPaint); textPaint.clearShadowLayer();
        if(state==WON||state==GAME_OVER){textPaint.setColor(state==WON?0xffffd43b:0xffcbd5e1);textPaint.setTextSize(getWidth()*.030f);String message=state==WON?I18n.encouragement(getContext(),level):I18n.consolation(getContext(),score);drawCenteredFit(c,message,getHeight()*.50f+2,arenaRight-arenaLeft-70);}
        textPaint.setColor(0xffffffff); textPaint.setTextSize(getWidth()*.035f); c.drawText(hint,getWidth()/2f,getHeight()*.50f+65,textPaint);
        if(state==READY){ textPaint.setColor(0xff94a3b8); textPaint.setTextSize(getWidth()*.028f); c.drawText(I18n.t(getContext(),"drag"),getWidth()/2f,getHeight()*.50f+105,textPaint); }
    }

    private void drawCenteredFit(Canvas c,String value,float y,float maxWidth){float width=textPaint.measureText(value);if(width>maxWidth)textPaint.setTextSize(textPaint.getTextSize()*maxWidth/width);c.drawText(value,getWidth()/2f,y,textPaint);}

    private void update() {
        long now=SystemClock.uptimeMillis();
        float remaining=Math.min((now-previousFrame)/1000f,.050f);
        previousFrame=now;
        while(remaining>0f && state==PLAYING){float step=Math.min(remaining,1f/120f);updatePhysics(step);remaining-=step;}
    }

    private void updatePhysics(float dt) {
        ballX+=ballVX*dt; ballY+=ballVY*dt;
        if(ballX-ballR<arenaLeft){ballX=arenaLeft+ballR;ballVX=Math.abs(ballVX);ping(false);}
        if(ballX+ballR>arenaRight){ballX=arenaRight-ballR;ballVX=-Math.abs(ballVX);ping(false);}
        if(ballY-ballR<arenaTop){ballY=arenaTop+ballR;ballVY=Math.abs(ballVY);ping(false);}
        RectF paddle=new RectF(paddleX-paddleW/2,paddleY-paddleH/2,paddleX+paddleW/2,paddleY+paddleH/2);
        if(ballVY>0 && circleRect(ballX,ballY,ballR,paddle)){
            ballY=paddle.top-ballR; float offset=(ballX-paddleX)/(paddleW/2); float speed=(float)Math.hypot(ballVX,ballVY);
            ballVX=speed*.78f*offset; ballVY=-(float)Math.sqrt(Math.max(speed*speed-ballVX*ballVX,speed*speed*.35f)); ping(false);
        }
        Iterator<Brick> it=bricks.iterator();
        while(it.hasNext()){
            Brick b=it.next(); if(!circleRect(ballX,ballY,ballR,b.r))continue;
            float overlapL=ballX+ballR-b.r.left, overlapR=b.r.right-(ballX-ballR), overlapT=ballY+ballR-b.r.top, overlapB=b.r.bottom-(ballY-ballR);
            if(Math.min(overlapL,overlapR)<Math.min(overlapT,overlapB))ballVX=-ballVX;else ballVY=-ballVY;
            b.hp--; score+=b.hp==0?100:25; if(b.hp==0)it.remove(); saveHigh(); ping(true); break;
        }
        if(bricks.isEmpty()){state=WON;play(winSound,.55f);}
        if(ballY-ballR>arenaBottom){ lives--; play(loseSound,.55f); if(lives<=0){state=GAME_OVER;saveHigh();} else {state=READY;resetBall();} }
    }

    private boolean circleRect(float x,float y,float radius,RectF r){float nx=Math.max(r.left,Math.min(x,r.right)),ny=Math.max(r.top,Math.min(y,r.bottom));float dx=x-nx,dy=y-ny;return dx*dx+dy*dy<=radius*radius;}
    private void ping(boolean brick){play(brick?brickSound:wallSound,brick?.45f:.32f);}
    private void play(int sound,float volume){if(!muted)sounds.play(sound,volume,volume,1,0,1f);}
    private void saveHigh(){if(score>highScore){highScore=score;prefs.edit().putInt("high",score).apply();}}

    @Override public boolean onTouchEvent(MotionEvent e) {
        if(!laidOut)return true;
        if(e.getAction()==MotionEvent.ACTION_DOWN){
            if(e.getY()<arenaTop && e.getX()<getWidth()*.25f){
                if(state==PLAYING)state=PAUSED;
                new android.app.AlertDialog.Builder(getContext()).setTitle("How to play").setMessage("Drag your finger left or right in the lower control area. The paddle stays above your finger so both edges remain visible.\n\nKeep the ball in play and break every block. Tap PAUSE at the top-right to pause or continue.").setPositiveButton("Got it",null).show();
                return true;
            }
            if(e.getY()<arenaTop && e.getX()>=getWidth()*.75f){
                if(state==PLAYING)state=PAUSED;
                else if(state==PAUSED){state=PLAYING;previousFrame=SystemClock.uptimeMillis();}
                invalidate();return true;
            }
            if(e.getY()<arenaTop && e.getX()>getWidth()*.50f && e.getX()<getWidth()*.75f){
                if(state==PLAYING) state=PAUSED;
                getContext().startActivity(new Intent(getContext(),AboutActivity.class));
                return true;
            }
            if(e.getY()<arenaTop && e.getX()>getWidth()*.25f && e.getX()<=getWidth()*.50f){
                if(state==PLAYING) state=PAUSED;
                getContext().startActivity(new Intent(getContext(),SettingsActivity.class));
                return true;
            }
            if(state==GAME_OVER){score=0;lives=3;level=1;makeLevel();resetBall();state=READY;}
            else if(state==WON){level++;makeLevel();resetBall();state=READY;}
            else if(state==READY||state==PAUSED){state=PLAYING;previousFrame=SystemClock.uptimeMillis();}
        }
        if(e.getAction()==MotionEvent.ACTION_DOWN||e.getAction()==MotionEvent.ACTION_MOVE){paddleX=Math.max(arenaLeft+paddleW/2,Math.min(e.getX(),arenaRight-paddleW/2));if(state==READY){ballX=paddleX;}invalidate();return true;}
        return true;
    }

    void pauseGame(){if(state==PLAYING)state=PAUSED;}
    void reloadSettings(){dayTheme=prefs.getBoolean("day_theme",false);muted=prefs.getBoolean("muted",false);invalidate();}
    @Override protected void onDetachedFromWindow(){sounds.release();super.onDetachedFromWindow();}
    private static final class Brick { final RectF r; final int color; int hp; Brick(float l,float t,float rr,float b,int c,int h){r=new RectF(l,t,rr,b);color=c;hp=h;} }
}
