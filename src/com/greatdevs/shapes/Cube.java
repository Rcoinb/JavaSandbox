package com.greatdevs.shapes;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.greatdevs.util.Vertex;

public class Cube extends SimpleShape{
	public float width;
	public float height;
	public float depth;
	
	private Vector4f color = new Vector4f();
	private Vertex[] vertices;
	private int[] indices;
	
	public Cube(Vector3f dimensions, Vector4f color) {
		this.width = dimensions.x;
		this.height = dimensions.y;
		this.depth = dimensions.z;
		this.color = color;
		
		update();
	}
	
	private void calcVertices() {
		QuadPolygon[] p = new QuadPolygon[6];
		
		Vector3f[] vp = {     
	            new Vector3f(-width, height, -depth), //0
	            new Vector3f(-width, height, depth), //1
	            new Vector3f(width, height, depth), //2
	            new Vector3f(width, height, -depth), //3
	            new Vector3f(-width, -height, -depth), //4
	            new Vector3f(-width, -height, depth), //5
	            new Vector3f(width, -height, depth), //6
	            new Vector3f(width, -height, -depth) //7
	        };
		
        p[0] = new QuadPolygon(vp[0], vp[1], vp[2], vp[3]);
        p[1] = new QuadPolygon(vp[6], vp[5], vp[4], vp[7]);
        p[2] = new QuadPolygon(vp[2], vp[1], vp[5], vp[6]);
        p[3] = new QuadPolygon(vp[4], vp[0], vp[3], vp[7]);
        p[4] = new QuadPolygon(vp[7], vp[3], vp[2], vp[6]);
        p[5] = new QuadPolygon(vp[1], vp[0], vp[4], vp[5]);
        
        for(int i = 0; i < p.length; i ++){
        	p[i].setColor(color);
            p[i].updateGeometry();
        }
        
        vertices = new Vertex[p.length * 6];
        indices = new int[p.length * 6];	
		for(int i = 0; i < p.length; i ++){
			for (int ii = 0; ii < p[i].getVertices().length; ii ++){
				vertices[i * 6 + ii] = p[i].getVertices()[ii];
				indices[i * 6 + ii] = p[i].getIndices()[ii] + i * 6;
			}
		}
	}
		
	public void setDimensions(float width, float height, float depth) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		
		update();
	}
	
	public void update() {
		calcVertices();
	}
	
	public void setAllNormals(Vector3f normal){
		for (int i = 0; i < vertices.length; i ++) vertices[i].setNormal(normal);
	}
	
	public int[] getIndices() {
		return indices;
	}

	public Vertex[] getVertices() {
		return vertices;
	}
	
	public Vector4f getColor() {
		return color;
	}

	public void setColor(Vector4f color) {
		this.color = color;
	}
}
