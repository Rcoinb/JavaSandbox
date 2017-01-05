package com.greatdevs.shapes;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.greatdevs.util.Vertex;

public class QuadPolygon extends SimpleShape{
	public Vector3f[] pC = new Vector3f[4];
	public Polygon[] p = new Polygon[2];
	
	public Vector4f color = new Vector4f(1, 1, 1, 1);
	
	private boolean inverse = false;
	
	private int[] indices;
	public Vertex[] v;

    public QuadPolygon(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4) {
    	this(p1, p2, p3, p4, false);
    }
	
    public QuadPolygon(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, boolean inverse) {
    	pC[0] = p1;
    	pC[1] = p2;
    	pC[2] = p3;
    	pC[3] = p4;
    	
        this.inverse = inverse;
    }
	
    public void setColor(Vector4f color){
    	this.color = color;
    	updateGeometry();
    }
    
    public void updateGeometry(){
    	if (!inverse){
    		p[0] = new Polygon(pC[0], pC[1], pC[2]);
    		p[1] = new Polygon(pC[2], pC[3], pC[0]);
    	} else if (inverse){
    		p[0] = new Polygon(pC[1], pC[2], pC[3]);
    		p[1] = new Polygon(pC[3], pC[0], pC[1]);
    	}
    	
    	p[0].setColor(color);
    	p[1].setColor(color);
    	
    	v = new Vertex[p.length * 3];
    	indices = new int[p.length * 3];
    	
		for(int i = 0; i < p.length; i ++){
			for (int ii = 0; ii < p[i].getVertices().length; ii ++){
				v[i * 3 + ii] = p[i].getVertices()[ii];
				indices[i * 3 + ii] = p[i].getIndices()[ii] + i * 3;
			}
		}
		
		/*v[0].setTexCoord(new Vector2f(0, 0));
		v[1].setTexCoord(new Vector2f(0, 1));
		v[2].setTexCoord(new Vector2f(1, 1));
		
		v[3].setTexCoord(new Vector2f(1, 1));
		v[4].setTexCoord(new Vector2f(1, 0));
		v[5].setTexCoord(new Vector2f(0, 0));*/
    }
    
	@Override
	public int[] getIndices() {
		return indices;
	}

	@Override
	public Vertex[] getVertices() {
		return v;
	}
}
