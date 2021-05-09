/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulgame;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Fauzan
 */
public class Game extends Canvas implements Runnable{
    Window window;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    private int score = 0;
    private String username;
    private String level;
    public int speed;
    private int time = 10;
    private String GameMode;
    private Thread thread;
    private boolean running = false;
    private Random random = new Random();
    private Handler handler;
    
    
    public enum STATE{
        Game,
        GameOver
    };
    
    public STATE gameState = STATE.Game;
    
    public Game(String username, String level, String GameMode){
        window = new Window(WIDTH, HEIGHT, "1901280 - IraFitri Yani - C2", this);
        
        handler = new Handler();
        this.addKeyListener(new KeyInput(handler, this));
        this.username = username;
        this.GameMode = GameMode;
        this.level = level;
        playSound("/Kalimba.wav");
        if(gameState == STATE.Game)
        {
            switch (level) {
                case "Easy":
                    time = 20;
                    speed = 10;
                    break;
                case "Medium":
                    time = 10;
                    speed = 7;
                    break;
                case "Hard":
                    time = 5;
                    speed = 4;
                    break;
            }
        }
        handler.addObject(new Items(100,150, ID.Item));
        handler.addObject(new Items(200,350, ID.Item));
        handler.addObject(new Player(200,200, ID.Player));
        handler.addObject(new Enemy(300,0, ID.Musuh));
        if (this.GameMode == "MultiPlayer") {
            handler.addObject(new Player(300,300, ID.Playerr));
        }
    }

    public int getScore(){
        return this.score;
    }
    public String getUsername(){
        return this.username;
    }
    
    public synchronized void start(){
        thread = new Thread(this);
        thread.start();
        running = true;
    }
    
    public synchronized void stop(){
        try{
            thread.join();
            running = false;
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        
        while(running){
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            while(delta >= 1){
                tick();
                delta--;
            }
            if(running){
                render();
                frames++;
            }
            
            if(System.currentTimeMillis() - timer > 1000){
                timer += 1000;
                //System.out.println("FPS: " + frames);
                frames = 0;
                if(gameState == STATE.Game){
                    if(time > 0)
                    {
                        time--;
                    }
                    else
                    {
                        gameState = STATE.GameOver;
                    }
                }
            }
        }
        stop();
    }
    
    private void tick(){
        handler.tick();
        if(gameState == STATE.Game){
            GameObject playerObject = null;
            GameObject playerObjectt = null;
            for(int i=0;i< handler.object.size(); i++){
                if(handler.object.get(i).getId() == ID.Player){
                   playerObject = handler.object.get(i);
                }
                if(handler.object.get(i).getId() == ID.Playerr){
                   playerObjectt = handler.object.get(i);
                }
            }
            if(playerObject != null){
                for(int i=0;i< handler.object.size(); i++){
                    if(handler.object.get(i).getId() == ID.Item){
                        if(checkCollision(playerObject, handler.object.get(i))){
                            playSound("/Eat.wav");
                            handler.removeObject(handler.object.get(i));
                            handler.addObject(new Items(random.nextInt(WIDTH),random.nextInt(HEIGHT), ID.Item));
                            score = score + 10;
                            time = time + 5;
                            break;
                        }
                    }
                    if(handler.object.get(i).getId() == ID.Musuh){
                        if(checkCollision(playerObject, handler.object.get(i))){
                            playSound("/GameOver.wav");
                            handler.removeObject(handler.object.get(i));
                            time = 0;
                            gameState = STATE.GameOver;
                            break;
                        }
                    }
                }
            }
            if(playerObjectt != null){
                for(int i=0;i< handler.object.size(); i++){
                    if(handler.object.get(i).getId() == ID.Item){
                        if(checkCollision(playerObjectt, handler.object.get(i))){
                            playSound("/Eat.wav");
                            handler.removeObject(handler.object.get(i));
                            handler.addObject(new Items(random.nextInt(WIDTH),random.nextInt(HEIGHT), ID.Item));
                            score = score + 10;
                            time = time + 5;
                            break;
                        }
                    }
                    if(handler.object.get(i).getId() == ID.Musuh){
                        if(checkCollision(playerObjectt, handler.object.get(i))){
                            playSound("/GameOver.wav");
                            handler.removeObject(handler.object.get(i));
                            time = 0;
                            gameState = STATE.GameOver;
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public static boolean checkCollision(GameObject player, GameObject item){
        boolean result = false;
        
        int sizePlayer = 50;
        int sizeItem = 20;
        
        int playerLeft = player.x;
        int playerRight = player.x + sizePlayer;
        int playerTop = player.y;
        int playerBottom = player.y + sizePlayer;
        
        int itemLeft = item.x;
        int itemRight = item.x + sizeItem;
        int itemTop = item.y;
        int itemBottom = item.y + sizeItem;
        
        if((playerRight > itemLeft ) &&
        (playerLeft < itemRight) &&
        (itemBottom > playerTop) &&
        (itemTop < playerBottom)
        ){
            result = true;
        }
        
        return result;
    }
    
    private void render(){
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null){
            this.createBufferStrategy(3);
            return;
        }
        
        Graphics g = bs.getDrawGraphics();
        
        g.setColor(Color.decode("#F1f3f3"));
        g.fillRect(0, 0, WIDTH, HEIGHT);
                
        
        
        if(gameState ==  STATE.Game){
            handler.render(g);
            
            Font currentFont = g.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.4F);
            g.setFont(newFont);

            g.setColor(Color.BLACK);
            g.drawString("Score: " +Integer.toString(score), 20, 20);

            g.setColor(Color.BLACK);
            g.drawString("Time: " +Integer.toString(time), WIDTH-120, 20);
        }else{
            Font currentFont = g.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * 3F);
            g.setFont(newFont);

            g.setColor(Color.BLACK);
            g.drawString("GAME OVER", WIDTH/2 - 120, HEIGHT/2 - 30);

            currentFont = g.getFont();
            Font newScoreFont = currentFont.deriveFont(currentFont.getSize() * 0.5F);
            g.setFont(newScoreFont);

            g.setColor(Color.BLACK);
            g.drawString("Score: " +Integer.toString(score), WIDTH/2 - 50, HEIGHT/2 - 10);
            
            g.setColor(Color.BLACK);
            g.drawString("Press Space to Continue", WIDTH/2 - 100, HEIGHT/2 + 30);
        }
                
        g.dispose();
        bs.show();
    }
    
    public static int clamp(int var, int min, int max){
        if(var >= max){
            return var = max;
        }else if(var <= min){
            return var = min;
        }else{
            return var;
        }
    }
    
    public void close(){
        window.CloseWindow();
    }
    
    public void playSound(String filename){
        try {
            // Open an audio input stream.
            URL url = this.getClass().getResource(filename);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            // Get a sound clip resource.
            Clip clip = AudioSystem.getClip();
            // Open audio clip and load samples from the audio input stream.
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        } catch (LineUnavailableException e) {
           e.printStackTrace();
        }
    
    }
}
