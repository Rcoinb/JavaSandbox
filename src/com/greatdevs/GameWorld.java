package com.greatdevs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Quat4f;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.greatdevs.entity.EditableCube;
import com.greatdevs.entity.Player;
import com.greatdevs.renderer.Light;
import com.greatdevs.renderer.Renderer;
import com.greatdevs.shapes.*;
import com.greatdevs.tools.BufferTools;
import com.greatdevs.tools.Frustum;
import com.greatdevs.tools.MatrixHandler;
import com.greatdevs.tools.Vec3;
import com.greatdevs.util.Camera;
import com.greatdevs.util.ObjectHandler;
import com.greatdevs.util.DisplayManager;

public class GameWorld {

	public static List<ObjectHandler> renderable = new ArrayList<ObjectHandler>();
	
	public static List<Light> lights = new ArrayList<Light>();
	
	public static Light spotLight;
	
	public static Camera camera;

	public static DiscreteDynamicsWorld dynamicsWorld;
	
	public static Cube[] walls = new Cube[6];
	public static Cube testCube;
	public static List<EditableCube> cubes = new ArrayList<EditableCube>();
	public static List<RigidBody> staticRigidBodies = new ArrayList<RigidBody>();
	
	private Player player = new Player();
	private static Texture crosshair;
	
	public static Frustum frustum = new Frustum();
	
	public static int BLOCK_TO_PLACE = 1000;
	
	Light defL;
	
	public void init(){	
		try {
			crosshair = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/textures/crosshair.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Camera
		camera = new Camera(Core.renderer.projectionMatrixID, Core.renderer.viewMatrixID, Core.renderer.basicShaderProgram);
		camera.setPerspectiveProjection(60.0f, (float) DisplayManager.WIDTH / (float) DisplayManager.HEIGHT, 0.01f, 250);
		
		camera.setPosition(new Vector3f(0, 0, 0));
		camera.setVerticalAngle(0);
		camera.setHorizontalAngle(0);
		
		initPhysicsWorld();
		
		player.init();
		
		walls[0] = new Cube(new Vector3f(1000, 2, 1000), new Vector4f(1, 1, 1, 1));
		walls[1] = new Cube(new Vector3f(10, 2, 10), new Vector4f(1, 1, 1, 1));
		walls[2] = new Cube(new Vector3f(10, 10, 2), new Vector4f(1, 1, 1, 1));
		walls[3] = new Cube(new Vector3f(10, 10, 2), new Vector4f(1, 1, 1, 1));
		walls[4] = new Cube(new Vector3f(2, 10, 10), new Vector4f(1, 1, 1, 1));
		walls[5] = new Cube(new Vector3f(2, 10, 10), new Vector4f(1, 1, 1, 1));
		
		for(int i = 0; i < walls.length; i ++) walls[i].objectHandler = new ObjectHandler(walls[i]);
		
		walls[0].objectHandler.setPosition(new Vector3f(0, 0, 0));
		walls[1].objectHandler.setPosition(new Vector3f(0, 14, 0));
		walls[2].objectHandler.setPosition(new Vector3f(0, 11, 11));
		walls[3].objectHandler.setPosition(new Vector3f(0, 11, -11));
		walls[4].objectHandler.setPosition(new Vector3f(11, 11, 0));
		walls[5].objectHandler.setPosition(new Vector3f(-11, 11, 0));
		
		testCube = new Cube(new Vector3f(1, 1, 1), new Vector4f(1, 1, 1, 1));
		testCube.objectHandler = new ObjectHandler(testCube);
		testCube.objectHandler.setPosition(new Vector3f(-4, 3, -4));
		//walls = new Cube(-21, -11, -21, new Vector3f(1, 1, 1));
		//walls.objectHandler = new ObjectHandler(walls);
		//walls.objectHandler.setShadow(false);
		//walls.objectHandler.setPosition(new Vector3f(0, 5, 0));
		
		updateObjects();
		
		spotLight = new Light(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.95f, new Vector3f(1, 1, 1), 1, 32, 0.2f, 0.2f, 0.1f, 1024);
		lights.add(spotLight);
		defL = new Light(new Vector3f(0, 26, 0), new Vector3f(0, 0, 0), -32, new Vector3f(1, 1, 1), 1, 32, 1f, 0.01f, 0.00001f, 1024);
		Light defL2 = new Light(new Vector3f(-5, 502, -5), new Vector3f(0, 0, 0), -32, new Vector3f(1, 1, 1), 1, 32, 1f, 0.01f, 0.00001f, 1024);

		//defL.overrideShadowWithDirectionalShadow(new Vector3f(0, 26, 0));
		//defL2.overrideShadowWithDirectionalShadow(new Vector3f(53, 46, 75));
		//lights.add(new Light(new Vector3f(7.3f, 3.5f, -7.47f), new Vector3f(0, 0, 0), -32, new Vector3f(1, 0, 0), 1, 32, 1, 0.7f, 0.5f, 512));

		//lights.add(defL);
		lights.add(defL2);
		
	    lights.add(new Light(new Vector3f(0, 7, 0), new Vector3f(0, 0, 0), -32, new Vector3f(0.8f, 0.3f, 0.746f), 1, 32, 4f, 0.4f, 0.0001f, 1024));
	    lights.add(new Light(new Vector3f(-5, 6, -5), new Vector3f(0, 0, 0), -32, new Vector3f(1, 1, 1), 1, 32, 4f, 0.4f, 0.0001f, 1024));
		lights.add(new Light(new Vector3f(6, 6, -6), new Vector3f(0, 0, 0), -32, new Vector3f(1, 1, 1), 1, 32, 4f, 0.475f, 0.0001f, 1024));
		
		load();
	}
	
	public void render(){
		frustum.calculateFrustum(camera.getProjectionMatrix(), camera.getViewMatrix());
		Core.renderer.renderScene(renderable, lights, frustum);
	    //Core.renderer.renderGUI(15, new Vector4f(1, 1, 1, 1), new Vector2f(0.75f, 0.75f), new Vector2f(0.25f, 0.25f), 0, 0, new Vector2f(0, 0), new Vector2f(0, 0));
		Core.renderer.renderGUI(crosshair.getTextureID(), new Vector4f(1, 1, 1, 1), new Vector2f(0, 0), new Vector2f(0.01f, 0.01f * DisplayManager.WIDTH / DisplayManager.HEIGHT), 0, crosshair.getImageWidth(), new Vector2f(0, 0), new Vector2f(0, 0));
		Core.font.renderString("" + GameWorld.BLOCK_TO_PLACE, -1f, 0.93f, 3f, 0.85f, new Vector4f(1, 1, 1, 1), Core.renderer);
	}
	
	public void update(){
		updateObjects();
		
		player.update();
		defL.dirShadowVec = new Vector3f(defL.getPosition().x - GameWorld.camera.getPosition().x, defL.getPosition().y - GameWorld.camera.getPosition().y, defL.getPosition().z - GameWorld.camera.getPosition().z);
		dynamicsWorld.stepSimulation(1.0f / 60.0f, 32);
	}
	
	public static void updateObjects() {
		renderable.clear();
		for (RigidBody rBody : staticRigidBodies) dynamicsWorld.removeRigidBody(rBody);
		staticRigidBodies.clear();
		
		renderable.add(testCube.objectHandler);
		addStaticRigidBody(testCube.objectHandler.getCollisionShape(), testCube.objectHandler.getModelMatrix());
		
		for(int i = 0; i < 6; i ++){
			renderable.add(walls[i].objectHandler);
			addStaticRigidBody(walls[i].objectHandler.getCollisionShape(), walls[i].objectHandler.getModelMatrix());
		}

		for (EditableCube eCube : cubes){
			eCube.updateTrans();
			renderable.add(eCube.cubeObject);
			addStaticRigidBody(eCube.cubeObject.getCollisionShape(), eCube.cubeObject.getModelMatrix());
		}
	}
	
	public static void initPhysicsWorld(){
		CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
		javax.vecmath.Vector3f worldAabbMin = new javax.vecmath.Vector3f(-1024, -1024, -1024);
		javax.vecmath.Vector3f worldAabbMax = new javax.vecmath.Vector3f(1024, 1024, 1024);
		int maxProxies = 1024;
		AxisSweep3 overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax, maxProxies);
		overlappingPairCache.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, solver, collisionConfiguration);
		dynamicsWorld.setGravity(new javax.vecmath.Vector3f(0, -10, 0));
	}
	
	public static void addStaticRigidBody(CollisionShape collShape, MatrixHandler modelMatrix){
		float mass = 0;
		boolean isDynamic = (mass != 0f);
		javax.vecmath.Vector3f localInertia = new javax.vecmath.Vector3f(0, 0, 0);
		if (isDynamic) collShape.calculateLocalInertia(mass, localInertia);
		DefaultMotionState motionState = new DefaultMotionState();
		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, motionState, collShape, localInertia);
		RigidBody rigidBody = new RigidBody(rbInfo);
		Transform trans = new Transform();
		trans.setIdentity();
		
		/*FloatBuffer floatBuffer = BufferTools.reserveFloatData(16);
		modelMatrix.store(floatBuffer);
		floatBuffer.flip();
		float[] floatArray = new float[16];
		floatBuffer.get(floatArray);
		trans.setFromOpenGLMatrix(floatArray);*/
		//BETTER DO THIS WAY, BUT ITS SOMEHOW NOT WORKING(((
		
		trans.origin.set(modelMatrix.getPosition().x, modelMatrix.getPosition().y, modelMatrix.getPosition().z);
		rigidBody.setWorldTransform(trans);
		
		staticRigidBodies.add(rigidBody);
		dynamicsWorld.addRigidBody(rigidBody);
	}
	
	public static void addCube(Vec3 pos, Vec3 dim, Vector4f color){
    	EditableCube eCube = new EditableCube(pos, dim);
    	eCube.setColor(color);
        cubes.add(eCube);
	}

	public static void removeCube(EditableCube eCube){
		cubes.remove(eCube);
	}
	
	public static void save() {
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File("res/save.dat")));
			System.out.println("Saving...");
			dos.writeUTF("save_test_001");
			dos.write(0);
			for (int i = 0; i < cubes.size(); i++) {
				EditableCube eCube = cubes.get(i);
				dos.write(1);

				dos.writeInt(eCube.position.x);
				dos.writeInt(eCube.position.y);
				dos.writeInt(eCube.position.z);
				
				dos.writeInt(eCube.extend.x);
				dos.writeInt(eCube.extend.y);
				dos.writeInt(eCube.extend.z);
				
				for (int ii = 0; ii < 8; ii ++){
					dos.writeBoolean(eCube.isHided(ii));
				}
			}

			dos.write(2);
			dos.close();
			System.out.println("Saved!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void load() {
		cubes.clear();
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(new File("res/save.dat")));
			System.out.println("Loading: " + dis.readUTF());
			dis.read();
			while (dis.read() == 1) {
				int x = dis.readInt();
				int y = dis.readInt();
				int z = dis.readInt();
				
				int extendX = dis.readInt();
				int extendY = dis.readInt();
				int extendZ = dis.readInt();
				
		    	EditableCube eCube = new EditableCube(new Vec3(x, y, z), new Vec3(extendX, extendY, extendZ));
		    	eCube.setColor(new Vector4f((float) Math.random(), (float) Math.random(), (float) Math.random(), 1));
				
				for (int ii = 0; ii < 8; ii ++){
					if (dis.readBoolean()){
						eCube.hideVertex(ii);
					}
				}
				
		        cubes.add(eCube);
			}
			dis.read();
			dis.close();
			System.out.println("Loaded!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void clear() {
		lights.clear();
		cubes.clear();
		renderable.clear();
		for (RigidBody rBody : staticRigidBodies) dynamicsWorld.removeRigidBody(rBody);
		staticRigidBodies.clear();
	}
}
