package com.greatdevs.entity;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import com.greatdevs.renderer.Light;
import com.greatdevs.screens.GameScreen;
import com.greatdevs.tools.Maths;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.linearmath.Transform;
import com.greatdevs.GameWorld;

public class Player {	
	private float speed = 0.09f;
	
    private KinematicCharacterController character;
    private PairCachingGhostObject ghostObject;
    
    private Editor editor = new Editor();
    
	public Player(){
	}
	
	public void init(){
		editor.init();
		Vector3f position = new Vector3f(0, 7, 0);
		
		//ConvexShape pCollShape = new CapsuleShape(0.2f, 1.3f);
		ConvexShape pCollShape = new BoxShape(new javax.vecmath.Vector3f(0.2f, 0.85f, 0.2f));
		
		Transform startTransform = new Transform();
	    startTransform.setIdentity();
	    startTransform.origin.set(position.x, position.y, position.z);
		
		ghostObject = new PairCachingGhostObject();
		ghostObject.setWorldTransform(startTransform);
		ghostObject.setCollisionShape(pCollShape);
	    ghostObject.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);
	    character = new KinematicCharacterController(ghostObject, pCollShape, 0.3f); //0.3f is step height
	    character.setJumpSpeed(3.75f);
	    character.setMaxSlope(0.5f);
	    character.setGravity(10);
	    character.setFallSpeed(10);
	    character.warp(new javax.vecmath.Vector3f(position.x, position.y, position.z));
	    GameWorld.dynamicsWorld.addCollisionObject(ghostObject, CollisionFilterGroups.CHARACTER_FILTER, (short)(CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));
	    GameWorld.dynamicsWorld.addAction(character);
	}
	
	public void update() {
		move();
		editor.update();
		input();	
		
		//RayTest
		/*ClosestRayResultCallback crrc = new ClosestRayResultCallback(new javax.vecmath.Vector3f(rS.x, rS.y, rS.z), new javax.vecmath.Vector3f(rE.x, rE.y, rE.z));
		GameWorld.dynamicsWorld.rayTest(new javax.vecmath.Vector3f(rS.x, rS.y, rS.z), new javax.vecmath.Vector3f(rE.x, rE.y, rE.z), crrc);
		if (crrc.hasHit()) System.out.println(crrc.hitPointWorld);*/
		
		//spotlight
		Vector3f spotLightPos = new Vector3f(GameWorld.camera.getPosition().x, GameWorld.camera.getPosition().y - 0.1f, GameWorld.camera.getPosition().z);
		spotLightPos.z += (float) (0.075 * Math.sin(Math.toRadians(GameWorld.camera.getHorizontalAngle())));
		spotLightPos.x += (float) -(0.075 * Math.cos(Math.toRadians(GameWorld.camera.getHorizontalAngle())));
		GameWorld.spotLight.setPosition(spotLightPos);
		GameWorld.spotLight.setDirection((Vector3f) Maths.addVec(GameWorld.camera.getDirection(), new Vector3f(0, -0.1f, 0)).normalise());
	}
	
	public void move(){
		if (Mouse.isButtonDown(0) && !Mouse.isGrabbed()) {
			Mouse.setGrabbed(true);
		}
		
		if (Mouse.isGrabbed()) {
			GameWorld.camera.applyMouse();
		}
		
		Vector3f addToPos = new Vector3f(0, 0, 0);
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W) && Mouse.isGrabbed()){
			addToPos.z = (float) (speed * Math.cos(Math.toRadians(GameWorld.camera.getHorizontalAngle())));
			addToPos.x = (float) (speed * Math.sin(Math.toRadians(GameWorld.camera.getHorizontalAngle())));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_S) && Mouse.isGrabbed()){
			addToPos.z = (float) -(speed * Math.cos(Math.toRadians(GameWorld.camera.getHorizontalAngle())));
			addToPos.x = (float) -(speed * Math.sin(Math.toRadians(GameWorld.camera.getHorizontalAngle())));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_A) && Mouse.isGrabbed()){
			addToPos.z = (float) -(speed * Math.sin(Math.toRadians(GameWorld.camera.getHorizontalAngle())));
			addToPos.x = (float) (speed * Math.cos(Math.toRadians(GameWorld.camera.getHorizontalAngle())));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_D) && Mouse.isGrabbed()){
			addToPos.z = (float) (speed * Math.sin(Math.toRadians(GameWorld.camera.getHorizontalAngle())));
			addToPos.x = (float) -(speed * Math.cos(Math.toRadians(GameWorld.camera.getHorizontalAngle())));
		}	
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Mouse.isGrabbed()){
			//addToPos = Maths.multVec(addToPos, 24f);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && Mouse.isGrabbed()){
			character.jump();
		}

		character.setWalkDirection(new javax.vecmath.Vector3f(addToPos.x, 0, addToPos.z));
		
		Transform trans = new Transform();
		ghostObject.getWorldTransform(trans);
		GameWorld.camera.setPosition(new Vector3f(trans.origin.x, trans.origin.y + 0.8f, trans.origin.z));
		GameWorld.camera.updateCamera();
	}
	
	private void input(){
		while (Mouse.next()) {
			if (Mouse.isGrabbed()) {
				if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
				}
				if (Mouse.getEventButton() == 0 && !Mouse.getEventButtonState()) {
					editor.edit();
				}
				if (Mouse.getEventButton() == 2 && Mouse.getEventButtonState()) {
					if (GameWorld.BLOCK_TO_PLACE > 0){
						editor.addCube();
						GameWorld.BLOCK_TO_PLACE --;
					}
				}
			}
		}
		
		while (Keyboard.next()) {		
			if (Keyboard.getEventKey() == Keyboard.KEY_TAB && Keyboard.getEventKeyState()){
				editor.switchMode();
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_DELETE && Keyboard.getEventKeyState()){
				editor.removeSelected();
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_F && Keyboard.getEventKeyState()) {
				if (GameWorld.spotLight.getCutoff() == 1) GameWorld.spotLight.setCutoff(0.95f);
				else GameWorld.spotLight.setCutoff(1);
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_L && Keyboard.getEventKeyState()) {
				GameWorld.lights.add(new Light(Maths.addVec(GameWorld.camera.getPosition(), new Vector3f(0, -1f, 0)), new Vector3f(0, 0, 0), -32, new Vector3f(1, 1, 1), 1, 32, 0.8f, 0.5f, 0.25f, 256));
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_C && Keyboard.getEventKeyState()) {
				System.out.println("Camera: " + GameWorld.camera.toString() + "\nPlayer: " + ghostObject.getWorldTransform(new Transform()).origin);
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE && Keyboard.getEventKeyState()) {
				GameScreen.pause();
				Mouse.setGrabbed(false);
			}
			
			if (Keyboard.getEventKey() == Keyboard.KEY_F5 && Keyboard.getEventKeyState()) {
				GameWorld.save();
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_F9 && Keyboard.getEventKeyState()) {
				GameWorld.load();
			}
		}
	}
}
