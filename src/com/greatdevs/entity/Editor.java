package com.greatdevs.entity;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.greatdevs.GameWorld;
import com.greatdevs.shapes.Cube;
import com.greatdevs.tools.Collision;
import com.greatdevs.tools.Maths;
import com.greatdevs.tools.PointerPicker;
import com.greatdevs.tools.Vec3;

public class Editor {
	private float rayEnd = 32;
	
	private PointerPicker picker;
	private Vector3f rS;
	private Vector3f rE;
	private boolean editMode = false;
    private EditableCube selCube;
    private float disToCube;
    private Vector3f selOffset = new Vector3f(0, 0, 0);
    private int selV;
    private int nFace;
    private Vec3 startRes;
    private boolean resizeMode = false;
    
    
    public void init(){
		picker = new PointerPicker(GameWorld.camera);
    }
    
    public void update() {
		picker.update(0, 0);
		rS = picker.getPointOnRay(picker.getCurrentRay(), 0);
		rE = picker.getPointOnRay(picker.getCurrentRay(), rayEnd);
		
		if (!editMode) resizeMode = false;
        if (!Mouse.isButtonDown(2) && !resizeMode){
            selCube = null;
        }
        
        if (!Mouse.isButtonDown(2) && !resizeMode && editMode){
            float closeDis = -1;
            for (EditableCube cube : GameWorld.cubes){
                Vector3f lSelCubePos = getCubeSelected(cube);
                if (lSelCubePos != null){
                    float dis = Maths.disBetween2Vecs(lSelCubePos, GameWorld.camera.getPosition());
                    if (closeDis > dis || closeDis == -1){
                        selCube = cube;
                        //selCubePos = lSelCubePos;
                        disToCube = dis;
                        closeDis = dis;
                        selOffset.x = selCube.cubeObject.getModelMatrix().getPosition().x - selCube.cubeObject.getModelMatrix().getScale().x - lSelCubePos.x;
                        selOffset.y = selCube.cubeObject.getModelMatrix().getPosition().y - selCube.cubeObject.getModelMatrix().getScale().y - lSelCubePos.y;
                        selOffset.z = selCube.cubeObject.getModelMatrix().getPosition().z - selCube.cubeObject.getModelMatrix().getScale().z - lSelCubePos.z;
                    }
                }
            }
        } 
        
        if (selCube != null && !resizeMode && editMode){
            selV = getCubeSelectedVertex(selCube);
            nFace = getFace(selCube, false);
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) nFace = getFace(selCube, true);
            
            if (Mouse.isButtonDown(2)){
            	Vector3f newPos = Maths.multVec(Maths.addVec(Maths.addVec(GameWorld.camera.getPosition(), Maths.multVec(GameWorld.camera.getDirection(),disToCube)) , selOffset), 50);
                tryPosCube(selCube, new Vec3((int) newPos.x, selCube.position.y, selCube.position.z));
                tryPosCube(selCube, new Vec3(selCube.position.x, (int) newPos.y, selCube.position.z));
                tryPosCube(selCube, new Vec3(selCube.position.x, selCube.position.y, (int) newPos.z));
            }
            
            if (selV == -1){
            	if (Mouse.isButtonDown(0)){
            		resizeMode = true;
            		startRes = new Vec3(Maths.multVec(Maths.addVec(GameWorld.camera.getPosition(), Maths.normalizeVec(GameWorld.camera.getDirection())), 100));
            	}
            }
            
            if (!Mouse.isButtonDown(2)){
            	if (nFace != -1) GameWorld.renderable.add(selCube.getFaceOverlay(nFace));
            	GameWorld.renderable.add(selCube.getCubeOutline());
            }
        }
        
        Vec3 curRes = new Vec3(Maths.multVec(Maths.addVec(GameWorld.camera.getPosition(), Maths.normalizeVec(GameWorld.camera.getDirection())), 100));
        if (resizeMode && selCube != null){
        	if (nFace == 0) tryResizeCube(selCube, new Vec3(0, curRes.y - startRes.y, 0), 0);
        	if (nFace == 1) tryResizeCube(selCube, new Vec3(0, startRes.y - curRes.y, 0), -1);
        	if (nFace == 2) tryResizeCube(selCube, new Vec3(0, 0, curRes.z - startRes.z), 0);
        	if (nFace == 3) tryResizeCube(selCube, new Vec3(0, 0, startRes.z - curRes.z), -1);
        	if (nFace == 4) tryResizeCube(selCube, new Vec3(curRes.x - startRes.x, 0, 0), 0);
        	if (nFace == 5) tryResizeCube(selCube, new Vec3(startRes.x - curRes.x, 0, 0), -1);

        	startRes = curRes.clone();
        }
	}
    
	public void addCube(){
		if (selCube == null && editMode){
			GameWorld.addCube(new Vec3(Maths.multVec(Maths.addVec(new Vector3f(GameWorld.camera.getPosition().x, GameWorld.camera.getPosition().y, GameWorld.camera.getPosition().z), GameWorld.camera.getDirection()), 50)), new Vec3(20, 20, 20), new Vector4f(Maths.random.nextFloat(), Maths.random.nextFloat(), Maths.random.nextFloat(), 1));
		}
	}
	
	public void switchMode(){
		if (editMode) editMode = false;
		else editMode = true;
	}
	
	public void removeSelected(){
		if (selCube != null) GameWorld.removeCube(selCube);
	}
	
	public void edit(){
		if (selCube != null && editMode){
			if (selV != -1){
                if (!selCube.isHided(selV)) selCube.hideVertex(selV);
                else if (selCube.isHided(selV)) selCube.resetVertex(selV);				
			} else if (selV == -1){
				if (resizeMode) resizeMode = false;
			}
		}
	}
	
	public int getFace(EditableCube eCube, boolean far){
		int selFace = -1;
		float disToFace = -1;
		for (int i = 0; i < 6; i ++){
			Vector3f fColl = Collision.getLineQuadPolygonIntersection(eCube.getFaceCollPoly(i), rS, rE);
			if (fColl != null) {
				float cdisToFace = Maths.disBetween2Vecs(GameWorld.camera.getPosition(), fColl);
				if (selFace == -1){
					selFace = i;
					disToFace = cdisToFace;
				}
				if (!far) {
					if (selFace != -1 && cdisToFace < disToFace) {
						selFace = i;
						disToFace = cdisToFace;
					}
				} else if (far) {
					if (selFace != -1 && cdisToFace > disToFace) {
						selFace = i;
						disToFace = cdisToFace;
					}
				}
			}
		}
		return selFace;
	}
	
    private boolean tryPosCube(EditableCube cube, Vec3 pos){
        for(EditableCube c : GameWorld.cubes){
            if (!c.equals(cube)){
                if (Collision.cubeInCubeWithExtend(pos, cube.extend, c.position, c.extend)){
                	return false;
                } 
            }
        }
        for(Cube c : GameWorld.walls){
        	Vector3f cubePosition = new Vector3f(c.objectHandler.getModelMatrix().getPosition().x, c.objectHandler.getModelMatrix().getPosition().y, c.objectHandler.getModelMatrix().getPosition().z);
        	if (Collision.cubeInCubeWithExtend(pos, cube.extend, new Vec3((int) (cubePosition.x - c.width) * 50, (int) (cubePosition.y - c.height) * 50, (int) (cubePosition.z - c.depth) * 50), new Vec3((int) c.width * 100, (int) c.height * 100, (int) c.depth * 100))){
        		return false;
        	}
        }
    	cube.setPosition(pos);
    	return true;
    }
    
    private boolean tryResizeCube(EditableCube cube, Vec3 extend, int moveDir){
    	Vec3 newExd = cube.extend.clone().add(extend);
    	Vec3 newPos = cube.position.clone().add(extend.clone().mult(moveDir));
        for(EditableCube c : GameWorld.cubes){
            if (!c.equals(cube)){
            	if (Collision.cubeInCubeWithExtend(newPos, newExd, c.position, c.extend)){
            		return false;
            	}
            }
        }
        for(Cube c : GameWorld.walls){
        	Vector3f cubePosition = new Vector3f(c.objectHandler.getModelMatrix().getPosition().x, c.objectHandler.getModelMatrix().getPosition().y, c.objectHandler.getModelMatrix().getPosition().z);
        	if (Collision.cubeInCubeWithExtend(newPos, newExd, new Vec3((int) (cubePosition.x - c.width) * 50, (int) (cubePosition.y - c.height) * 50, (int) (cubePosition.z - c.depth) * 50), new Vec3((int) c.width * 100, (int) c.height * 100, (int) c.depth * 100))){
        		return false;
        	}
        }
        if (newExd.x <= 1 || newExd.y <= 1 || newExd.z <= 1) return false;
        
        cube.addExd(extend);
        cube.setPosition(newPos);
        return true;
    }
	
    public int getCubeSelectedVertex(EditableCube cube){
        for (int i = 0; i < 8; i ++){
        	if (Collision.checkLineBox(cube.getVertexCollCube(i).objectHandler.getModelMatrix().getPosition(), cube.getVertexCollCube(i).objectHandler.getModelMatrix().getScale(), rS, rE))
        		return i;
        }
        return -1;
    }
	
    public Vector3f getCubeSelected(EditableCube cube){
    	if (Collision.checkLineBox(cube.cubeObject.getModelMatrix().getPosition(), cube.cubeObject.getModelMatrix().getScale(), rS, rE))
    		return Collision.Hit;
        return null;
    }
}
