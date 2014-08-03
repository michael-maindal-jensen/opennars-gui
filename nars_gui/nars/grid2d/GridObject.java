package nars.grid2d;

/**
 *
 * @author me
 */


public interface GridObject {

    public void init(Grid2DSpace space);
    
    public void update(Effect nextEffect);
    
    public void draw();
    
}
