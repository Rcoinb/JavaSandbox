package com.greatdevs.shapes;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.greatdevs.tools.Maths;
import com.greatdevs.util.Vertex;

public class Polygon extends SimpleShape{
	public Vertex[] v = new Vertex[3];
	private int[] indices = {0, 1, 2};
	
    public Polygon(Vector3f p1, Vector3f p2, Vector3f p3){
    	v[0] = new Vertex(p1);
    	v[1] = new Vertex(p2);
    	v[2] = new Vertex(p3);
    	updateGeometry();
    }
    
    public void setVertexPos(Vector3f p1, Vector3f p2, Vector3f p3){
        v[0].setPos(p1);
        v[1].setPos(p2);
        v[2].setPos(p3);
    }
    
    public void setColor(Vector4f color){
    	v[0].setColor(color);
    	v[1].setColor(color);
    	v[2].setColor(color);
    }
    
    public void updateGeometry(){
        Vector3f normal = Maths.getNormals(v[0].getPos(), v[1].getPos(), v[2].getPos());
        if (Float.isNaN(normal.x) || Float.isNaN(normal.y) || Float.isNaN(normal.z)) normal.set(0, 0, 0);
        v[0].setNormal(normal);
        v[1].setNormal(normal);
        v[2].setNormal(normal);
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
