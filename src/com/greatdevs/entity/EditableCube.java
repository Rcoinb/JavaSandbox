package com.greatdevs.entity;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.greatdevs.shapes.Cube;
import com.greatdevs.shapes.QuadPolygon;
import com.greatdevs.tools.Vec3;
import com.greatdevs.util.ObjectHandler;
import com.greatdevs.util.Vertex;

public class EditableCube {
    public static final Vector3f[] defVP = {     
            new Vector3f(-1, 1, -1), //0
            new Vector3f(-1, 1, 1), //1
            new Vector3f(1, 1, 1), //2
            new Vector3f(1, 1, -1), //3
            new Vector3f(-1, -1, -1), //4
            new Vector3f(-1, -1, 1), //5
            new Vector3f(1, -1, 1), //6
            new Vector3f(1, -1, -1) //7
        };
    
    public Vector3f[] vp = new Vector3f[8];
    public QuadPolygon[] p = new QuadPolygon[6];
    
    public ObjectHandler cubeObject;
    
    private boolean inverse = false;
    
    private Vector4f color = new Vector4f(1, 1, 1, 1);
    public Vec3 position;
    public Vec3 extend;
    
    public EditableCube (Vec3 pos, Vec3 exd){
        this.position = pos;
        this.extend = exd;
        
        initVertices();     
        updateGeometry();
    }
    
    private void initVertices(){
        for(int i = 0; i < defVP.length; i ++){
        	vp[i] = new Vector3f(defVP[i].x, defVP[i].y, defVP[i].z);
        }
    }
    
    public void updateGeometry(){    
        inverse = false;
        if (isHided(0) || isHided(2) || isHided(4) || isHided(6)) inverse = true;
        
        if (getTopHided() == 3){
            for(int i = 0; i < 4; i ++){
                if (!isHided(i)) {
                    if (i == 0 || i == 2) inverse = false;
                    break;
                }
            }
        }
        
        if (getBottomHided() == 3){
            for(int i = 4; i < 8; i ++){
                if (!isHided(i)) {
                    if (i == 4 || i == 6) inverse = false;
                    break;
                }
            }
        }
        
        p[0] = new QuadPolygon(vp[0], vp[1], vp[2], vp[3], inverse);
        p[1] = new QuadPolygon(vp[6], vp[5], vp[4], vp[7], inverse);
        p[2] = new QuadPolygon(vp[2], vp[1], vp[5], vp[6], inverse);
        p[3] = new QuadPolygon(vp[4], vp[0], vp[3], vp[7], inverse);
        p[4] = new QuadPolygon(vp[7], vp[3], vp[2], vp[6], inverse);
        p[5] = new QuadPolygon(vp[1], vp[0], vp[4], vp[5], inverse);
        
        for(int i = 0; i < p.length; i ++){
        	p[i].setColor(color);
            p[i].updateGeometry();
        }
        
        Vertex[] cV = new Vertex[p.length * 6];
    	int[] indices = new int[p.length * 6];	
		for(int i = 0; i < p.length; i ++){
			for (int ii = 0; ii < p[i].getVertices().length; ii ++){
				cV[i * 6 + ii] = p[i].getVertices()[ii];
				indices[i * 6 + ii] = p[i].getIndices()[ii] + i * 6;
			}
		}
		
		cubeObject = new ObjectHandler(cV, indices);
    }
    
    public void setPosition(Vec3 newPos){
    	this.position = newPos;
    }
    
    public void setExd(Vec3 newExd){
    	this.extend = newExd;
    }
    
    public void move(Vec3 pos){
    	this.position.add(pos);
    }
    
    public void addExd(Vec3 exd){
    	this.extend.add(exd);
    }
    
    public void updateTrans(){
    	this.cubeObject.setPosition(new Vector3f((float) position.x / 50 + (float) extend.x / 100, (float) position.y / 50 + (float) extend.y / 100, (float) position.z / 50 + (float) extend.z / 100));
    	this.cubeObject.setScale(new Vector3f((float) extend.x / 100, (float) extend.y / 100, (float) extend.z / 100));
    }
    
    public void setColor(Vector4f color){
    	this.color = color;
        updateGeometry();
    }
    
    public void hideVertex(int id){
        if (hideNumber(id) == 0) {
            if (id <= 3 && hideNumber(id + 4) == 0){
                if (!isHided(getNextTop(id)) || !isHided(getPreviousBottom(id + 4))){
                    if (!isHided(getPreviousTop(id)) || !isHided(getNextBottom(id + 4))){
                        if (!isHided(getPreviousTop(id) + 4) && !isHided(getNextTop(id) + 4)){
                            if (getBottomHided() < 3 && getTopHided() < 3) vp[id] = vp[4 + id];
                        }
                    }
                }
            }
            else if (id >= 4 && hideNumber(id - 4) == 0){
                if (!isHided(getNextBottom(id)) || !isHided(getPreviousTop(id - 4))){
                    if (!isHided(getPreviousBottom(id)) || !isHided(getNextTop(id - 4))){
                        if (!isHided(getPreviousBottom(id) - 4) && !isHided(getNextBottom(id) - 4)){
                            if (getTopHided() < 3 && getBottomHided() < 3) vp[id] = vp[id - 4];
                        }
                    }
                }
            }
        }
        if (hideNumber(id) == 1){
            if (!isHided(id)){
                if (id <= 3) {
                    if (!isHided(getPreviousTop(id)) || !isHided(getPreviousBottom(id + 4))){
                        if (!isHided(getNextTop(id)) && !isHided(getNextBottom(id + 4))){
                            vp[id] = vp[getNextTop(id)];
                            vp[id + 4] = vp[getNextBottom(id + 4)];   
                        }
                    }
                }
                if (id >= 4) {
                    if (!isHided(getPreviousBottom(id)) || !isHided(getPreviousTop(id - 4))){
                        if (!isHided(getNextBottom(id)) && !isHided(getNextTop(id - 4))){
                            vp[id] = vp[getNextBottom(id)];
                            vp[id - 4] = vp[getNextTop(id - 4)];
                        }
                    }
                }
            }
        }
        
        updateGeometry();
    }
    
    public void resetVertex(int id){       
        if (hideNumber(id) == 1) {
            if (id <= 3 && !isHided(id + 4)) vp[id] = new Vector3f(defVP[id].x, defVP[id].y, defVP[id].z);
            if (id >= 4 && !isHided(id - 4)) vp[id] = new Vector3f(defVP[id].x, defVP[id].y, defVP[id].z);
            
            
            if (id <= 3 && isHided(id + 4)){
                vp[id] = new Vector3f(defVP[id].x, defVP[id].y, defVP[id].z);
                vp[id + 4] = new Vector3f(defVP[id + 4].x, defVP[id + 4].y, defVP[id + 4].z);
                hideVertex(id + 4);
            }
            
            if (id >= 4 && isHided(id - 4)){
                vp[id] = new Vector3f(defVP[id].x, defVP[id].y, defVP[id].z);;
                vp[id - 4] = new Vector3f(defVP[id - 4].x, defVP[id - 4].y, defVP[id - 4].z);
                hideVertex(id - 4);
            }
        }
        updateGeometry();
    }
    
    public int getTopHided(){
        int topHided = 0;
        for(int i = 0; i < 4; i ++){
            if (isHided(i)) topHided ++;
        }
        return topHided;
    }
    
    public int getBottomHided(){
        int botHided = 0;
        for(int i = 4; i < 8; i ++){
            if (isHided(i)) botHided ++;
        }
        return botHided;
    }
    
    public int getPreviousTop(int id){
        int res = id - 1;
        if (res <= -1) res = 3;
        return res;
    }
    
    public int getNextTop(int id){
        int res = id + 1;
        if (res >= 4) res = 0;
        return res;
    }
    
    public int getPreviousBottom(int id){
        int res = id - 1;
        if (res <= 3) res = 7;
        return res;
    }
    
    public int getNextBottom(int id){
        int res = id + 1;
        if (res >= 8) res = 4;
        return res;
    }
    
    public boolean isHided(int id){
        if (vp[id].equals(defVP[id])) return false;
        return true;
    }
    
    public int hideNumber(int id){
        int number = -1;
        for (int i = 0; i < vp.length; i ++){
            if (vp[id].equals(vp[i])){
                number ++;
            }
        }
        return number;
    }
    
    public int getNearestFace(Vector3f position){
        if (position.y > cubeObject.getModelMatrix().getPosition().y + cubeObject.getModelMatrix().getScale().y * 0.5f) return 0;
        if (position.y < cubeObject.getModelMatrix().getPosition().y - cubeObject.getModelMatrix().getScale().y * 0.5f) return 1;
        if (position.x > cubeObject.getModelMatrix().getPosition().x + cubeObject.getModelMatrix().getScale().x * 0.5f) return 4;
        if (position.x < cubeObject.getModelMatrix().getPosition().x - cubeObject.getModelMatrix().getScale().x * 0.5f) return 5;
        if (position.z > cubeObject.getModelMatrix().getPosition().z + cubeObject.getModelMatrix().getScale().z * 0.5f) return 2;
        if (position.z < cubeObject.getModelMatrix().getPosition().z - cubeObject.getModelMatrix().getScale().z * 0.5f) return 3;
        return -1;
    }
    
    public ObjectHandler getFaceOverlay(int side){
        float toAdd = 0.0015f;
        QuadPolygon[] quad = new QuadPolygon[6];
        
        quad[0] = new QuadPolygon(defVP[0], defVP[1], defVP[2], defVP[3], false);
        quad[1] = new QuadPolygon(defVP[6], defVP[5], defVP[4], defVP[7], false);
        quad[2] = new QuadPolygon(defVP[2], defVP[1], defVP[5], defVP[6], false);
        quad[3] = new QuadPolygon(defVP[4], defVP[0], defVP[3], defVP[7], false);
        quad[4] = new QuadPolygon(defVP[7], defVP[3], defVP[2], defVP[6], false);
        quad[5] = new QuadPolygon(defVP[1], defVP[0], defVP[4], defVP[5], false);
        
        quad[side].setColor(new Vector4f(0.75f, 0.75f, 0.75f, 0.3f));
        quad[side].updateGeometry();
        ObjectHandler faceObject = new ObjectHandler(quad[side]);
        faceObject.setPosition(this.cubeObject.getModelMatrix().getPosition());
        faceObject.setScale(new Vector3f(this.cubeObject.getModelMatrix().getScale().x + toAdd, this.cubeObject.getModelMatrix().getScale().y + toAdd, this.cubeObject.getModelMatrix().getScale().z + toAdd));
        faceObject.setShadow(false);
        faceObject.setTransparent(true);
        faceObject.setHover(true);
        
        return faceObject;
    }
    
    public ObjectHandler getCubeOutline(){
    	float toAdd = 0.001f;
    	Cube oCube = new Cube(new Vector3f(this.cubeObject.getModelMatrix().getScale().x + toAdd, this.cubeObject.getModelMatrix().getScale().y + toAdd, this.cubeObject.getModelMatrix().getScale().z + toAdd), new Vector4f(1, 1, 1, 0.15f));
    	oCube.setAllNormals(new Vector3f(0, 1, 0));
    	oCube.objectHandler = new ObjectHandler(oCube);
    	oCube.objectHandler.setPosition(this.cubeObject.getModelMatrix().getPosition());
    	oCube.objectHandler.setShadow(false);
    	oCube.objectHandler.setTransparent(true);
    	return oCube.objectHandler;
    }
    
    public QuadPolygon getFaceCollPoly(int f){
        QuadPolygon[] quad = new QuadPolygon[6];
        quad[0] = new QuadPolygon(defVP[0], defVP[1], defVP[2], defVP[3], false);
        quad[1] = new QuadPolygon(defVP[6], defVP[5], defVP[4], defVP[7], false);
        quad[2] = new QuadPolygon(defVP[2], defVP[1], defVP[5], defVP[6], false);
        quad[3] = new QuadPolygon(defVP[4], defVP[0], defVP[3], defVP[7], false);
        quad[4] = new QuadPolygon(defVP[7], defVP[3], defVP[2], defVP[6], false);
        quad[5] = new QuadPolygon(defVP[1], defVP[0], defVP[4], defVP[5], false);
        
        quad[f].updateGeometry();
        quad[f].objectHandler = new ObjectHandler(quad[f]);
        quad[f].objectHandler.setPosition(this.cubeObject.getModelMatrix().getPosition());
        quad[f].objectHandler.setScale(this.cubeObject.getModelMatrix().getScale());
        return quad[f];
    }
    
    public Cube getVertexCollCube(int id){
    	Cube collCube = new Cube(new Vector3f(1f, 1f, 1f), new Vector4f(0, 0, 0, 1));
    	collCube.objectHandler = new ObjectHandler(collCube);
    	collCube.objectHandler.setPosition(new Vector3f(defVP[id].x * cubeObject.getModelMatrix().getScale().x + cubeObject.getModelMatrix().getPosition().x, defVP[id].y * cubeObject.getModelMatrix().getScale().y + cubeObject.getModelMatrix().getPosition().y, defVP[id].z * cubeObject.getModelMatrix().getScale().z + cubeObject.getModelMatrix().getPosition().z));
    	collCube.objectHandler.setScale(new Vector3f(0.025f, 0.025f, 0.025f));
    	return collCube;
    }
}
