package com.greatdevs.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.greatdevs.shapes.SimpleShape;
import com.greatdevs.tools.BufferTools;
import com.greatdevs.tools.Frustum;
import com.greatdevs.tools.MatrixHandler;

public class ObjectHandler {
	private FloatBuffer matrix44Buffer;
	private MatrixHandler modelMatrix;
	private Vertex[] v;
	private int texture = -1;
	private boolean shadow = true;
	private boolean transparent = false;
	private boolean hover = false;
	private float specularIntensity = 0; 
	private float specularPower = 0;
	private Vector3f boundingBox = new Vector3f(1, 1, 1);
	private int vbo;
	private int ibo;
	private int abo;
	private int size;
	
	public ObjectHandler(SimpleShape shape) {
		this(shape.getVertices(), shape.getIndices());
	}
	
	public ObjectHandler(Vertex[] vertices, int[] indices) {
		matrix44Buffer = BufferTools.reserveFloatData(16);
		modelMatrix = new MatrixHandler();
		vbo = glGenBuffers();
		ibo = glGenBuffers();
		abo = glGenVertexArrays();
		size = 0;
		
		setVertices(vertices, indices);
		setUp();
	}

	private void setUp() {
		glBindVertexArray(abo);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, Vertex.SIZE * 4, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, Vertex.SIZE * 4, 12);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, Vertex.SIZE * 4, 28);
		glVertexAttribPointer(3, 2, GL_FLOAT, false, Vertex.SIZE * 4, 40);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);

		glBindVertexArray(0);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glDisableVertexAttribArray(3);

		if (shadow) setUpShadowBuffer();
	}

	public void draw(int shaderProgram, int modelMatrixID, int normalMatrixID, int useTextureID, int textureID, int mat_specularIntensityID, int mat_specularPowerID, Frustum frustum) {
		if (frustum == null || frustum.cubeInFrustum(this.modelMatrix.getPosition().x, this.modelMatrix.getPosition().y, this.modelMatrix.getPosition().z, boundingBox.x * this.modelMatrix.getScale().x, boundingBox.y * this.modelMatrix.getScale().y, boundingBox.z * this.modelMatrix.getScale().z)) {
			glUseProgram(shaderProgram);

			modelMatrix.store(matrix44Buffer);
			matrix44Buffer.flip();
			glUniformMatrix4(modelMatrixID, false, matrix44Buffer);

			MatrixHandler normalMatrix = new MatrixHandler();
			normalMatrix.setAngle(modelMatrix.getAngle());
			normalMatrix.store(matrix44Buffer);
			matrix44Buffer.flip();
			glUniformMatrix4(normalMatrixID, false, matrix44Buffer);
			
			if (texture != -1){
				glUniform1i(useTextureID, 1);
				glActiveTexture(GL13.GL_TEXTURE31);
				glBindTexture(GL_TEXTURE_2D, texture);
				glUniform1i(textureID, 31);
			} else {
				glUniform1i(useTextureID, 0);
			}
			
			glUniform1f(mat_specularIntensityID, specularIntensity);
			glUniform1f(mat_specularPowerID, specularPower);
			
			if (transparent) {
				glDisable(GL_CULL_FACE);
				glEnable(GL_BLEND);
				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			}
			if (hover) glDisable(GL_DEPTH_TEST);

			glBindVertexArray(abo);
			glDrawElements(GL_TRIANGLES, size, GL_UNSIGNED_INT, 0);
			glBindVertexArray(0);

			if (transparent) {
				glEnable(GL_CULL_FACE);
				glDisable(GL_BLEND);
			}
			if (hover) glEnable(GL_DEPTH_TEST);
			
			glUseProgram(0);
		}
	}
	
	private void genBoudingBox(Vertex[] vertices) {
		float farestX = 0;
		float farestY = 0;
		float farestZ = 0;
		for (int i = 0; i < vertices.length; i ++){
			if (vertices[i].getPos().x > farestX) farestX = vertices[i].getPos().x;
			if (vertices[i].getPos().y > farestY) farestY = vertices[i].getPos().y;
			if (vertices[i].getPos().z > farestZ) farestZ = vertices[i].getPos().z;
		}
		boundingBox.set(farestX, farestY, farestZ);
	}

	public void drawGUI(int guiShaderProgram, int guiModelMatrixID, Vector2f position, Vector2f scale) {
		glUseProgram(guiShaderProgram);
		
		modelMatrix.setPosition(new Vector3f(position.x, position.y, 0));		
		modelMatrix.setScale(new Vector3f(scale.x, scale.y, 0));
		
		modelMatrix.store(matrix44Buffer);
		matrix44Buffer.flip();
		glUniformMatrix4(guiModelMatrixID, false, matrix44Buffer);

		glBindVertexArray(abo);
		glDrawElements(GL_TRIANGLES, size, GL_UNSIGNED_INT, 0);
		glBindVertexArray(0);

		glUseProgram(0);
	}
	
	private void setVertices(Vertex[] vertices, int[] indices) {
		v = vertices.clone();
		size = indices.length;

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, createFlippedBuffer(vertices), GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, createFlippedBuffer(indices), GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		genBoudingBox(vertices);
	}

	private static FloatBuffer createFlippedBuffer(Vertex[] vertices) {
		FloatBuffer buffer = BufferTools.reserveFloatData(vertices.length * Vertex.SIZE);

		for (int i = 0; i < vertices.length; i++) {
			buffer.put(vertices[i].getPos().getX());
			buffer.put(vertices[i].getPos().getY());
			buffer.put(vertices[i].getPos().getZ());
			buffer.put(vertices[i].getColor().getX());
			buffer.put(vertices[i].getColor().getY());
			buffer.put(vertices[i].getColor().getZ());
			buffer.put(vertices[i].getColor().getW());
			buffer.put(vertices[i].getNormal().getX());
			buffer.put(vertices[i].getNormal().getY());
			buffer.put(vertices[i].getNormal().getZ());
			buffer.put(vertices[i].getTexCoord().getX());
			buffer.put(vertices[i].getTexCoord().getY());
		}

		buffer.flip();

		return buffer;
	}

	private static IntBuffer createFlippedBuffer(int[] indices) {
		IntBuffer buffer = BufferTools.reserveIntData(indices.length);

		buffer.put(indices);

		buffer.flip();

		return buffer;
	}

	public void setUpShadowBuffer() {
		if (shadow) {
			glBindVertexArray(abo);
	
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, Vertex.SIZE * 4, 0);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
	
			glBindVertexArray(0);
		}
	}

	public void updateShadowBuffer(int shadowShaderProgram, int depthModelMatrixID, Frustum frustum) {
		if (shadow) {
			if (frustum == null || frustum.cubeInFrustum(this.modelMatrix.getPosition().x, this.modelMatrix.getPosition().y, this.modelMatrix.getPosition().z, boundingBox.x * this.modelMatrix.getScale().x, boundingBox.y * this.modelMatrix.getScale().y, boundingBox.z * this.modelMatrix.getScale().z)) {
				glUseProgram(shadowShaderProgram);
				modelMatrix.store(matrix44Buffer);
				matrix44Buffer.flip();
				glUniformMatrix4(depthModelMatrixID, false, matrix44Buffer);
				
				glBindVertexArray(abo);
				glDrawElements(GL_TRIANGLES, size, GL_UNSIGNED_INT, 0);
				glBindVertexArray(0);
		
				glUseProgram(0);
			}
		}
	}
	
	public CollisionShape getCollisionShape(){
		ObjectArrayList<javax.vecmath.Vector3f> points = new ObjectArrayList<javax.vecmath.Vector3f>();
		for (int i = 0; i < v.length; i++) points.add(new javax.vecmath.Vector3f(v[i].getPos().x * modelMatrix.getScale().x, v[i].getPos().y * modelMatrix.getScale().y, v[i].getPos().z * modelMatrix.getScale().z)); 
		CollisionShape collShape = new ConvexHullShape(points);
		return collShape;
	}

	public boolean isShadow() {
		return shadow;
	}

	public void setShadow(boolean shadow) {
		this.shadow = shadow;
	}

	public void setPosition(Vector3f position) {
		modelMatrix.setPosition(position);
	}
	
	public void setScale(Vector3f scale) {
		modelMatrix.setScale(scale);
	}

	public void rotate(Vector3f angle) {
		modelMatrix.setAngle(angle);
	}
	
	public MatrixHandler getModelMatrix() {
		return modelMatrix;
	}

	public boolean isHover() {
		return hover;
	}

	public void setHover(boolean hover) {
		this.hover = hover;
	}

	public boolean isTransparent() {
		return transparent;
	}

	public void setTransparent(boolean transparent) {
		this.transparent = transparent;
	}

	public float getSpecularIntensity() {
		return specularIntensity;
	}

	public void setSpecularIntensity(float specularIntensity) {
		this.specularIntensity = specularIntensity;
	}

	public float getSpecularPower() {
		return specularPower;
	}

	public void setSpecularPower(float specularPower) {
		this.specularPower = specularPower;
	}

	public int getTexture() {
		return texture;
	}

	public void setTexture(int texture) {
		this.texture = texture;
	}
}
