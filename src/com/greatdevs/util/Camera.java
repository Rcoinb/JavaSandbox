package com.greatdevs.util;

import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.nio.FloatBuffer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.greatdevs.tools.BufferTools;
import com.greatdevs.tools.MatrixHandler;

public class Camera {
	
	private Vector3f position;
	private Vector3f direction;
	private Vector3f up;
	private Vector3f right;
	private Vector3f posDir;
	private Vector3f scaledDirection;
	private Vector3f scaledRight;
	
	private float horizontalAngle;
	private float verticalAngle;
	private float speed;
	private float mouseSpeed;
	
	public MatrixHandler viewMatrix;
	public MatrixHandler projectionMatrix;
	private int viewMatrixID;
	private int projectionMatrixID;
	private int shaderProgram;
	private FloatBuffer matrix44Buffer;
	
	public Camera(int projectionMatrixID, int viewMatrixID, int shaderProgram) {
		direction = new Vector3f();
		right = new Vector3f();
		position = new Vector3f();
		up = new Vector3f();
		posDir = new Vector3f();
		horizontalAngle = 3.14f;
		verticalAngle = 0.0f;
		speed = 10.0f;
		mouseSpeed = 0.00075f;
		viewMatrix = new MatrixHandler();
		matrix44Buffer = BufferTools.reserveFloatData(16);
		
		this.projectionMatrixID = projectionMatrixID;
		this.viewMatrixID = viewMatrixID;
		this.shaderProgram = shaderProgram;
		updateCamera();
	}
	
	public void updateCamera() {
		Vector3f.cross(right, direction, up);
		scaledDirection = new Vector3f(direction.x * speed, direction.y * speed, direction.z * speed);
		scaledRight = new Vector3f(right.x * speed, right.y * speed, right.z * speed);
		Vector3f.add(position, direction, posDir);
		viewMatrix.lookAt(position, posDir, up);
		updateViewShader();
	}
	
	public void updateViewShader() {
		glUseProgram(shaderProgram);
		
		viewMatrix.store(matrix44Buffer);
		matrix44Buffer.flip();
		glUniformMatrix4(viewMatrixID, false, matrix44Buffer);
		
		glUseProgram(0);
	}
	
	public void updateProjectionShader() {
		glUseProgram(shaderProgram);
		
		projectionMatrix.store(matrix44Buffer);
		matrix44Buffer.flip();
		glUniformMatrix4(projectionMatrixID, false, matrix44Buffer);
		
		glUseProgram(0);
	}
	
	public void updateDirections() {
		direction = new Vector3f((float)(Math.cos(verticalAngle) * Math.sin(horizontalAngle)),
				(float)(Math.sin(verticalAngle)),
				(float)(Math.cos(verticalAngle) * Math.cos(horizontalAngle)));
		right = new Vector3f((float)(Math.sin(horizontalAngle - 3.14f / 2.0f)),
				0,
				(float)(Math.cos(horizontalAngle - 3.14f / 2.0f)));
	}
	
	public void setLookAt(Vector3f position, Vector3f direction, Vector3f up) {
		viewMatrix.lookAt(position, direction, up);
		updateViewShader();
	}
	
	public void setPerspectiveProjection(float fov, float aspectRatio, float zNear, float zFar) {
		projectionMatrix = new MatrixHandler();
		projectionMatrix.initPerspectiveMatrix(fov, aspectRatio, zNear, zFar);
		updateProjectionShader();
	}
	
	public void setOrthogonalProjection(float left, float right, float bottom, float top, float near, float far) {
		projectionMatrix = new MatrixHandler();
		projectionMatrix.initOrthographicMatrix(left, right, bottom, top, near, far);
		updateProjectionShader();
	}
	
	public void applyMouse() {
		horizontalAngle -= Mouse.getDX() * mouseSpeed;
		verticalAngle += Mouse.getDY() * mouseSpeed;
		if (verticalAngle > (float) (Math.PI / 2.0f)) {
			verticalAngle = (float) (Math.PI / 2.0f);
		}
		if (verticalAngle < (float) (-Math.PI / 2.0f)) {
			verticalAngle = (float) (-Math.PI / 2.0f);
		}
		
		if (getHorizontalAngle() >= 360 || getHorizontalAngle() <= -360) setHorizontalAngle(0);
		updateDirections();
	}
	
	public void applyKeyboard() {
		boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W);
		boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S);
		boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A);
		boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D);
		boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
		boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		
		if (keyUp) {
			Vector3f.add(position, scaledDirection, position);
		}
		if (keyDown) {
			Vector3f.sub(position, scaledDirection, position);
		}
		if (keyRight) {
			Vector3f.add(position, scaledRight, position);
		}
		if (keyLeft) {
			Vector3f.sub(position, scaledRight, position);
		}
		if (flyUp && !flyDown) {
			position.setY(position.y + speed);
		}
		if (flyDown && !flyUp) {
			position.setY(position.y - speed);
		}
	}
	
	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}
	
	public Vector3f getDirection() {
		return direction;
	}

	public Vector3f getRight() {
		return right;
	}

	public float getHorizontalAngle() {
		return (float) Math.toDegrees(horizontalAngle);
	}

	public void setHorizontalAngle(float horizontalAngleDeg) {
		this.horizontalAngle = (float) Math.toRadians(horizontalAngleDeg);
		updateDirections();
	}

	public float getVerticalAngle() {
		return (float) Math.toDegrees(verticalAngle);
	}

	public void setVerticalAngle(float verticalAngleDeg) {
		this.verticalAngle = (float) Math.toRadians(verticalAngleDeg);
		updateDirections();
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getMouseSpeed() {
		return mouseSpeed;
	}

	public void setMouseSpeed(float mouseSpeed) {
		this.mouseSpeed = mouseSpeed;
	}
	
	public Matrix4f getProjectionMatrix(){
		return projectionMatrix;
	}
	
	public Matrix4f getViewMatrix(){
		return viewMatrix;
	}
	
	public String toString() {
		return "Position: (" + position.x + ", " + position.y + ", " + position.z + ") Vertical angle: " + getVerticalAngle() + " Horizontal angle: " + getHorizontalAngle();
	}
}
