package nars.grid2d;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import nars.grid2d.Cell.Material;
import nars.grid2d.gui.EditorPanel;
import nars.grid2d.particle.Particle;
import nars.grid2d.particle.ParticleSystem;
import nars.gui.Window;
import processing.core.PApplet;
import processing.core.PVector;


public class Grid2DSpace extends PApplet {

    public static boolean world_used=false;

    ///////////////HAMLIB
    //processingjs compatibility layer
    int mouseScroll = 0;
    public final Hauto cells;
    List<GridObject> objects = new ArrayList();
    ProcessingJs processingjs = new ProcessingJs();
    //Hnav 2D navigation system
    Hnav hnav = new Hnav();
    //Object
    float selection_distance = 10;
    public float maxNodeSize = 40f;
    float FrameRate = 30f;
    private int automataPeriod = 1;
    boolean drawn = false;
    Hsim hsim = new Hsim();
    Hamlib hamlib = new Hamlib();
    public Button getBack;
    public Button conceptsView;
    public Button memoryView;
    public Button fetchMemory;
    float sx = 800;
    float sy = 800;
    long lasttime = -1;
    double realtime;
    public ParticleSystem particles;
    
    public Grid2DSpace(Hauto cells) {
        super();
        this.cells = cells;
        world_used=true;
        
        init();
        
        
    }

    
    public void add(GridObject g) {
        objects.add(g);
        g.init(this);
    }
    
    public Window newWindow(int width, int height, final boolean exitOnClose) {
        
        Window j = new Window("") {
            
            @Override
            protected void close() {
                stop();
                destroy();
                getContentPane().removeAll();
                
                if (exitOnClose)
                    System.exit(0);                
            }            
        };
        
        Container content = j.getContentPane();
        content.setLayout(new BorderLayout());

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        
        /*final JCheckBox syntaxEnable = new JCheckBox("Syntax");
        syntaxEnable.addActionListener(new ActionListener() {
        @Override public void actionPerformed(ActionEvent e) {
        }
        });
        menu.add(syntaxEnable);
         */
        
        EditorPanel editor = new EditorPanel(this);
        Window editorWindow = new Window("Edit", editor);
        editorWindow.setSize(200, 400);
        editorWindow.setVisible(true);
        
        
        
        content.add(menu, BorderLayout.NORTH);
        content.add(this, BorderLayout.CENTER);
        
        
        
        j.setSize(width, height);//initial size of the window
        j.setVisible(true);
        
        return j;
    }
    

    @Override
    protected void resizeRenderer(int newWidth, int newHeight) {
        super.resizeRenderer(newWidth, newHeight); //To change body of generated methods, choose Tools | Templates.
        drawn = false;
    }

    public void mouseScrolled() {
        hamlib.mouseScrolled();
    }

    public void keyPressed() {
        hamlib.keyPressed();
    }

    public void mouseMoved() {
        hamlib.mouseMoved();
    }

    public void mouseReleased() {
        hamlib.mouseReleased();
    }

    public void mouseDragged() {
        hamlib.mouseDragged();
    }

    public void mousePressed() {
        hamlib.mousePressed();
    }

    public void automataclicked(float x, float y) {
        if (x < 0 || y < 0) {
            return;
        }
        float realx = x / rendersize;
        float realy = y / rendersize;
        if (realx >= cells.w || realy >= cells.h) {
            return;
        }
        cells.clicked(realx,realy);
    }

    @Override
    public void draw() {
        background(0, 0, 0/*, 0.001f*/);
        pushMatrix();
         
        hnav.Transform();
        hrend_DrawBegin();
        
        drawGround();        
        drawObjects();
        drawParticles();
        
        hrend_DrawEnd();
        //popMatrix();
       
        
        
        popMatrix();
        hrend_DrawGUI();

        realtime = System.nanoTime() / 1.0e9;
        
        if (time % automataPeriod == 0) {
            cells.Exec();
        }
        if (time % 10 == 0) {
            for (GridObject g : objects) {
                Effect e = (g instanceof GridAgent) ? ((GridAgent)g).perceiveNext() : null;
                g.update(e);
                
                if (g instanceof GridAgent) {
                    GridAgent b = (GridAgent)g;
                    if (b.actions.size() > 0) {
                        Action a = b.actions.pop();
                        if (a!=null) {
                            process(b, a);
                        }
                    }
                    
                }
            }
        } 
    
    }

    
    public void process(GridAgent agent, Action action) {
        Effect e = action.process(this, agent);
        if (e!=null) {
            agent.perceive(e);
        }
    }
    
    void hrend_DrawBegin() {
    }

    void hrend_DrawEnd() {
        //fill(0);
        //text("Hamlib simulation system demonstration", 0, -5);
        //stroke(255, 255, 255);
        //noStroke();
        /*if (lastclicked != null) {
        fill(255, 0, 0);
        ellipse(lastclicked.x, lastclicked.y, 10, 10);
        }*/
    }

    public void hrend_DrawGUI() {
        fill(255);
        rect(10,10,10,10);
    }

    @Override
    public void setup() {
        frameRate(FrameRate);
        
        particles = new ParticleSystem(this);
    }
    int time = 0;
    float rendersize = 1;

    public int getTime() {
        return time;
    }

    public double getRealtime() {
        return realtime;
    }
    

//    enum MotionEffect {
//        Moved, PainfullyMoved, TooHigh, TooSolid /* collision, impenetrable, bump */, Stuck /* flypaper, quicksand */, TooFar
//    }
    
    private String whyNonTraversible(GridAgent agent, int x, int y, int tx, int ty) {
        int dx = Math.abs(tx-x);
        int dy = Math.abs(ty-y);
        
        if (!((dx <= 1) && (dy <= 1)))
            return "Too far";

        Cell from = cells.at(x, y);
        Cell to = cells.at(tx, ty);
                
        //System.out.println(to + " " + to.material);
        if ((to.material == Material.StoneWall) || to.is_solid)
            return "Too solid";
        
        final float maxTraversableHeight = 8;
        float dHeight = to.height - from.height;
        if (dHeight > maxTraversableHeight)
            return "Too high";
     
        return null;
    }

    
    public Effect getMotionEffect(GridAgent agent, Action a, int x, int y, int tx, int ty) {
        String reason = whyNonTraversible(agent, x, y, tx, ty);
        if (reason == null) {
            return new Effect(a, true, getTime(), "Moved");
        }
        else {
            return new Effect(a, false, getTime(), reason);
        }
        
        
    }
    
    public void drawObjects() {
        pushMatrix();
        
        //shift half a cell down and right so that when an object draws, it's centerd in the middle of a cell.
        translate(rendersize/2f, rendersize/2f);
        
        for (GridObject g : objects)
            g.draw();
        popMatrix();
    }
    
    public void drawParticles() {
        //PImage b = particles.particleImage;
        //this.blend(b, 0, 0, getWidth(), getHeight(), 0, 0, getWidth(), getHeight(), PImage.ADD);
        
        particles.tick();
        for (Particle p : particles.p) {
            fill(p.rgba);
            rect(p.xPos, p.yPos, 0.1f,0.1f);
        }
    }
    
    public void drawGround() {
        time++;
        
        //for speed:
        //strokeCap(SQUARE);
        //strokeJoin(PROJECT);
        
        noStroke();
        //Hauto h=cells;
        
        for (int i = 0; i < cells.w; i++) {
            for (int j = 0; j < cells.h; j++) {
                Cell c = cells.readCells[i][j];
                float x = i*rendersize; 
                float y = j*rendersize;

        
                pushMatrix();
                translate(x, y);
                c.draw(this,j==0 || i==0 || i==cells.w-1 || j==cells.w-1);
                popMatrix();

            }
        }
        
    }


    class ProcessingJs {

        ProcessingJs() {
            addMouseWheelListener(new MouseWheelListener() {
                public void mouseWheelMoved(MouseWheelEvent evt) {
                    mouseScroll = -evt.getWheelRotation();
                    mouseScrolled();
                }
            });
        }
    }

    class Hnav {

        private float savepx = 0;
        private float savepy = 0;
        private int selID = 0;
        private float zoom = 43.0f;
        private float difx = 0;
        private float dify = 0;
        private int lastscr = 0;
        private boolean EnableZooming = true;
        private float scrollcamspeed = 1.1f;

        float MouseToWorldCoordX(int x) {
            return 1 / zoom * (x - difx - width / 2);
        }

        float MouseToWorldCoordY(int y) {
            return 1 / zoom * (y - dify - height / 2);
        }
        private boolean md = false;

        void mousePressed() {
            md = true;
            if (mouseButton == RIGHT) {
                savepx = mouseX;
                savepy = mouseY;
            }
            drawn = false;
        }

        void mouseReleased() {
            md = false;
        }

        void mouseDragged() {
            if (mouseButton == RIGHT) {
                difx += (mouseX - savepx);
                dify += (mouseY - savepy);
                savepx = mouseX;
                savepy = mouseY;
            }
            drawn = false;
        }
        private float camspeed = 1.0f;
        private float scrollcammult = 0.92f;
        boolean keyToo = true;

        void keyPressed() {
            if ((keyToo && key == 'w') || keyCode == UP) {
                dify += (camspeed);
            }
            if ((keyToo && key == 's') || keyCode == DOWN) {
                dify += (-camspeed);
            }
            if ((keyToo && key == 'a') || keyCode == LEFT) {
                difx += (camspeed);
            }
            if ((keyToo && key == 'd') || keyCode == RIGHT) {
                difx += (-camspeed);
            }
            if (!EnableZooming) {
                return;
            }
            if (key == '-' || key == '#') {
                float zoomBefore = zoom;
                zoom *= scrollcammult;
                difx = (difx) * (zoom / zoomBefore);
                dify = (dify) * (zoom / zoomBefore);
            }
            if (key == '+') {
                float zoomBefore = zoom;
                zoom /= scrollcammult;
                difx = (difx) * (zoom / zoomBefore);
                dify = (dify) * (zoom / zoomBefore);
            }
            drawn = false;
        }

        void Init() {
            difx = -width / 2;
            dify = -height / 2;
        }

        void mouseScrolled() {
            if (!EnableZooming) {
                return;
            }
            float zoomBefore = zoom;
            if (mouseScroll > 0) {
                zoom *= scrollcamspeed;
            } else {
                zoom /= scrollcamspeed;
            }
            difx = (difx) * (zoom / zoomBefore);
            dify = (dify) * (zoom / zoomBefore);
            drawn = false;
        }

        void Transform() {
            translate(difx + 0.5f * width, dify + 0.5f * height);
            scale(zoom, zoom);
        }
    }

    ////Object management - dragging etc.
    class Hsim {

        ArrayList obj = new ArrayList();

        void Init() {
            smooth();
        }

        void mousePressed() {
            if (mouseButton == LEFT) {
                checkSelect();
                float x = hnav.MouseToWorldCoordX(mouseX);
                float y = hnav.MouseToWorldCoordY(mouseY);
                automataclicked(x, y);
            }
        }
        boolean dragged = false;

        void mouseDragged() {
            if (mouseButton == LEFT) {
                dragged = true;
                dragElems();
            }
            mousePressed();
        }

        void mouseReleased() {
            dragged = false;
            //selected = null;
        }

        void dragElems() {
            /*
            if (dragged && selected != null) {
            selected.x = hnav.MouseToWorldCoordX(mouseX);
            selected.y = hnav.MouseToWorldCoordY(mouseY);
            hsim_ElemDragged(selected);
            }
             */
        }

        void checkSelect() {
            /*
            double selection_distanceSq = selection_distance*selection_distance;
            if (selected == null) {
            for (int i = 0; i < obj.size(); i++) {
            Vertex oi = (Vertex) obj.get(i);
            float dx = oi.x - hnav.MouseToWorldCoordX(mouseX);
            float dy = oi.y - hnav.MouseToWorldCoordY(mouseY);
            float distanceSq = (dx * dx + dy * dy);
            if (distanceSq < (selection_distanceSq)) {
            selected = oi;
            hsim_ElemClicked(oi);
            return;
            }
            }
            }
             */
        }
    }

    //Hamlib handlers
    class Hamlib {

        void Init() {
            noStroke();
            hnav.Init();
            hsim.Init();
        }

        void mousePressed() {
            hnav.mousePressed();
            hsim.mousePressed();
        }

        void mouseDragged() {
            hnav.mouseDragged();
            hsim.mouseDragged();
        }

        void mouseReleased() {
            hnav.mouseReleased();
            hsim.mouseReleased();
        }

        public void mouseMoved() {
        }

        void keyPressed() {
            hnav.keyPressed();
        }

        void mouseScrolled() {
            hnav.mouseScrolled();
        }

        void Camera() {
        }
    }

    static List<PVector> Reconstruct_Taken_Path(Map<PVector, PVector> parent, PVector start, PVector target) {
        List<PVector> path = new ArrayList<PVector>();
        path.add( target );
        while (!path.get(path.size()-1).equals(start)) {
            //since bfs found a solution, start is included for sure and thus this loop terminates for sure
            path.add(parent.get(path.get(path.size()-1)));
        }
        Collections.reverse(path);
        return path;
    }

    static List<PVector> Shortest_Path(Grid2DSpace s, GridAgent a, PVector start, PVector target) {
        Set<PVector> avoid = new HashSet<PVector>();
        Map<PVector, PVector> parent = new HashMap<PVector, PVector>();
        ArrayDeque<PVector> queue = new ArrayDeque<PVector>();
        queue.add(start);


        while (!queue.isEmpty()) {
            PVector active = queue.removeFirst();

            if (avoid.contains(active))
                continue;

            avoid.add(active);

            

            if (active.equals(target)) {
                return Reconstruct_Taken_Path(parent, start, target);
            }

            for (int i = 0; i < 4; i++) {
                PVector x = new PVector();
                switch (i) {
                    case 0: 
                        x.set(active.x+1, active.y);
                        break;
                    case 1: 
                        x.set(active.x-1, active.y);
                        break;
                    case 2: 
                        x.set(active.x, active.y+1);
                        break;
                    case 3: 
                        x.set(active.x, active.y-1);
                        break;
                }

                
                if (avoid.contains(x) || 
                        s.whyNonTraversible(a, (int)active.x, (int)active.y, (int)x.x, (int)x.y)!=null)
                    continue;
                
                parent.put(x, active);
                queue.add(x);
            }
        }        
        return null;        
    }

}