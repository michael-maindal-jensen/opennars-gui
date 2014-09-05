package nars.nario;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import nars.nario.sonar.FakeSoundEngine;
import nars.nario.sonar.SonarSoundEngine;
import nars.nario.sprites.Mario;


public class MarioComponent extends JComponent implements Runnable, KeyListener, FocusListener
{
    private static final long serialVersionUID = 739318775993206607L;
    public int TICKS_PER_SECOND = 24;

    private boolean running = false;
    GraphicsConfiguration graphicsConfiguration;
    protected Scene scene;
    SonarSoundEngine sound;
    private boolean focused = false;
    protected MapScene mapScene;
    private BufferedImage image;
    private int tick;
    private int renderedFrames;
    boolean antialias = false;


    public MarioComponent()
    {
        this.setFocusable(true);
        this.setEnabled(true);


        try
        {
            sound = new SonarSoundEngine(16);
        }
        catch (LineUnavailableException e)
        {
            e.printStackTrace();
            sound = new FakeSoundEngine();
        }

        setFocusable(true);
    }

    private void toggleKey(int keyCode, boolean isPressed)
    {
        if (keyCode == KeyEvent.VK_LEFT)
        {
            scene.toggleKey(Mario.KEY_LEFT, isPressed);
        }
        if (keyCode == KeyEvent.VK_RIGHT)
        {
            scene.toggleKey(Mario.KEY_RIGHT, isPressed);
        }
        if (keyCode == KeyEvent.VK_DOWN)
        {
            scene.toggleKey(Mario.KEY_DOWN, isPressed);
        }
        if (keyCode == KeyEvent.VK_UP)
        {
            scene.toggleKey(Mario.KEY_UP, isPressed);
        }
        if (keyCode == KeyEvent.VK_A)
        {
            scene.toggleKey(Mario.KEY_SPEED, isPressed);
        }
        if (keyCode == KeyEvent.VK_S)
        {
            scene.toggleKey(Mario.KEY_JUMP, isPressed);
        }
    }

    public void paint(Graphics g)    {
            
            /*          drawString(og, "FPS: " + fps, 5, 5, 0);
             drawString(og, "FPS: " + fps, 4, 4, 7);*/


             if (antialias) {
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,  
                                  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
             }
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);            

            renderedFrames++;


    }

    public void update(Graphics g)
    {
    }

    public void start()     {
        if (!running)
        {
            running = true;
            new Thread(this, "Game Thread").start();
        }
    }

    public void stop()     {
        Art.stopMusic();
        running = false;
    }

    private boolean updateDoubleBuffer(int w, int h)     {
        if ((w == 0) || (h == 0))
            return false;
        
            /*
             * if image is already compatible and optimized for current system 
             * settings, simply return it
             */
            if ((image!=null) /*&& (image.getColorModel().equals(gfx_config.getColorModel()))*/ && (image.getWidth()==w) && (image.getHeight()==h)) {
                    //use existing image
            }
            else {
                // obtain the current system graphical settings
                GraphicsConfiguration gfx_config = GraphicsEnvironment.
                    getLocalGraphicsEnvironment().getDefaultScreenDevice().
                    getDefaultConfiguration();
                        
                image = gfx_config.createCompatibleImage(w, h);
            }
            
            return true;
    }
    
    final Runnable repaintRun = new Runnable() {

                @Override
                public void run() {
                    repaint();
                }
                
    };
    
    public void ready() {
        toTitle();        
    }
    
    public void run()     {
        graphicsConfiguration = getGraphicsConfiguration();

        //      scene = new LevelScene(graphicsConfiguration);
        mapScene = new MapScene(graphicsConfiguration, this, new Random().nextLong());
        scene = mapScene;
        scene.setSound(sound);

        Art.init(graphicsConfiguration, sound);

        updateDoubleBuffer(320,240);
        Graphics og = image.getGraphics();

        int lastTick = -1;
        //        double lastNow = 0;
        renderedFrames = 0;
        int fps = 0;

        //        double now = 0;
        //        double startTime = System.nanoTime() / 1000000000.0; 
        //        double timePerFrame = 0; 
        double time = System.nanoTime() / 1000000000.0;
        double now = time;
        double averagePassedTime = 0;

        addKeyListener(this);
        addFocusListener(this);

        boolean naiveTiming = true;

        ready();

        while (running)
        {
            double lastTime = time;
            time = System.nanoTime() / 1000000000.0;
            double passedTime = time - lastTime;

            if (passedTime < 0) naiveTiming = false; // Stop relying on nanotime if it starts skipping around in time (ie running backwards at least once). This sometimes happens on dual core amds.
            averagePassedTime = averagePassedTime * 0.9 + passedTime * 0.1;

            if (naiveTiming)
            {
                now = time;
            }
            else
            {
                now += averagePassedTime;
            }

            tick = (int) (now * TICKS_PER_SECOND);
            if (lastTick == -1) lastTick = tick;
            while (lastTick < tick)
            {
                scene.tick();
                lastTick++;

                if (lastTick % TICKS_PER_SECOND == 0)
                {
                    fps = renderedFrames;
                    renderedFrames = 0;
                }
            }

            float alpha = (float) (now * TICKS_PER_SECOND - tick);
            sound.clientTick(alpha);

            //int x = (int) (Math.sin(now) * 16 + 160);
            //int y = (int) (Math.cos(now) * 16 + 120);

            //og.setColor(Color.WHITE);
            //og.fillRect(0, 0, 320, 240);

            scene.render(og, alpha);
            
            update();
            
            /*if (!this.hasFocus() && tick/4%2==0)             {
                String msg = "CLICK TO PLAY";

                drawString(og, msg, 160 - msg.length() * 4 + 1, 110 + 1, 0);
                drawString(og, msg, 160 - msg.length() * 4, 110, 7);
            }*/

            SwingUtilities.invokeLater(repaintRun);
            
            try             {
                Thread.sleep(5);
            }
            catch (InterruptedException e)
            {
            }
            
        }

        Art.stopMusic();
    }

    protected void update()  { }
    
    
    private void drawString(Graphics g, String text, int x, int y, int c)
    {
        char[] ch = text.toCharArray();
        for (int i = 0; i < ch.length; i++)
        {
            g.drawImage(Art.font[ch[i] - 32][c], x + i * 8, y, null);
        }
    }

    public void keyPressed(KeyEvent arg0)
    {
        toggleKey(arg0.getKeyCode(), true);
    }

    public void keyReleased(KeyEvent arg0)
    {
        toggleKey(arg0.getKeyCode(), false);
    }

    public LevelScene startLevel(long seed, int difficulty, int type)
    {
        scene = new LevelScene(graphicsConfiguration, this, seed, difficulty, type);
        scene.setSound(sound);
        scene.init();
        return (LevelScene)scene;
    }

    public void levelFailed()
    {
        scene = mapScene;
        mapScene.startMusic();
        Mario.lives--;
        if (Mario.lives == 0)
        {
            lose();
        }
    }

    public void keyTyped(KeyEvent arg0)
    {
    }

    public void focusGained(FocusEvent arg0)
    {
        focused = true;
    }

    public void focusLost(FocusEvent arg0)
    {
        focused = false;
    }

    public void levelWon()
    {
        scene = mapScene;
        mapScene.startMusic();
        mapScene.levelWon();
    }
    
    public void win()
    {
        scene = new WinScene(this);
        scene.setSound(sound);
        scene.init();
    }
    
    public void toTitle()
    {
        Mario.resetStatic();
        scene = new TitleScene(this, graphicsConfiguration);
        scene.setSound(sound);
        scene.init();
    }
    
    public void lose()
    {
        scene = new LoseScene(this);
        scene.setSound(sound);
        scene.init();
    }

    public void startGame()
    {
        scene = mapScene;
        mapScene.startMusic();
        mapScene.init();
   }
}