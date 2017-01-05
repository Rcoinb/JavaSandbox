package com.greatdevs.renderer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT32;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.greatdevs.renderer.Renderer;
import com.greatdevs.tools.BufferTools;
import com.greatdevs.tools.Frustum;
import com.greatdevs.tools.MatrixHandler;
import com.greatdevs.util.Camera;
import com.greatdevs.util.DisplayManager;
import com.greatdevs.util.Framework;
import com.greatdevs.util.ObjectHandler;

public class Light {
	
	private Vector3f position;
	private Vector3f direction;
	private float cutoff;
	private Vector3f color;
	private float ambient;
	private float range;
	private float attenConstant;
	private float attenLinear;
	private float attenExponent;
	private int shadowQuality;
	
	private int projectionMatrixID;
	private int viewMatrixID;
	private int modelMatrixID;
	
	private DirectionalShadow dirShadow = null;
	public Vector3f dirShadowVec = null;
	private int useDirShadow = 0;
	
	private int shadowShaderProgram;
	
	public int depth;
	public int fbo;
	public int shadowCubeMap;
	
	private Camera camera;
	private Frustum frusum;
	
	public Light(Vector3f position, Vector3f direction, float cutoff, Vector3f color, float ambient, float range, float attenConstant, float attenLinear, float attenExponent, int shadowQuality) {
		this.position = position;
		this.direction = direction;
		this.cutoff = cutoff;
		this.color = color;
		this.ambient = ambient;
		this.range = range;
		this.attenConstant = attenConstant;
		this.attenLinear = attenLinear;
		this.attenExponent = attenExponent;
		this.shadowQuality = shadowQuality;
		
		setUpShadowShaders();
		setUpShadow();
	}
	
	public void overrideShadowWithDirectionalShadow(Vector3f sDir){
		useDirShadow = 1;
		dirShadow = new DirectionalShadow();
		dirShadowVec = new Vector3f(sDir.x, sDir.y, sDir.z);
		dirShadow.setUp();
	}
	
	public void update(int shaderProgram, int lightPositionID, int lightDirectionID, int lightCutoffID, int lightColorID, int ambientID, int lightRangeID, int attenConstantID, int attenLinearID, int attenExponentID, int ovrdWDirShadowID) {
		glUseProgram(shaderProgram);
		
		glUniform3f(lightPositionID, position.x, position.y, position.z);
		glUniform3f(lightDirectionID, direction.x, direction.y, direction.z);
		glUniform1f(lightCutoffID, cutoff);
		glUniform3f(lightColorID, color.x, color.y, color.z);
		glUniform1f(ambientID, ambient);
		glUniform1f(lightRangeID, range);
		glUniform1f(attenConstantID, attenConstant);
		glUniform1f(attenLinearID, attenLinear);
		glUniform1f(attenExponentID, attenExponent);
		
		glUniform1i(ovrdWDirShadowID, useDirShadow);
		
		glUseProgram(0);
	}
	
	public void updateShadow(List<ObjectHandler> objects, int basicShaderProgram, int depthBiasMatrixID, int shadowMapID, int shadowCubeMapID, Vector3f camPos, int i){
		if (useDirShadow == 0 && dirShadowVec == null) updateCubeShadow(objects, basicShaderProgram, shadowCubeMapID, i);	
		if (useDirShadow == 1 && dirShadowVec != null){
			dirShadow.updateShadow(objects, basicShaderProgram, depthBiasMatrixID, shadowMapID, camPos, this.dirShadowVec, i);
		}
	}
	
	public void updateCubeShadow(List<ObjectHandler> objects, int basicShaderProgram, int shadowCubeMapID, int i){
	    //glCullFace(GL_FRONT);
		updateShadowBuffer(objects);
		//Renderer.clearScreen(new Vector4f(0, 0, 0, 0));		
	    //DisplayManager.bindAsRenderTarget();
		glUseProgram(basicShaderProgram);
		glActiveTexture(GL_TEXTURE0 + i);
		glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, this.shadowCubeMap);
		glUniform1i(shadowCubeMapID, i);
		glUseProgram(0);
		//glCullFace(GL_BACK);
	}
	
	//SHADOW
	private void setUpShadowShaders() {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "shadowShader.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "shadowShader.frag"));

		shadowShaderProgram = Framework.createProgram(shaderList);
		
		projectionMatrixID = glGetUniformLocation(shadowShaderProgram, "projectionMatrix");
		viewMatrixID = glGetUniformLocation(shadowShaderProgram, "viewMatrix");
		modelMatrixID = glGetUniformLocation(shadowShaderProgram, "modelMatrix");
	}

	public void setUpShadow() {
		fbo = glGenFramebuffers();

		depth = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, depth);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, shadowQuality, shadowQuality, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glBindTexture(GL_TEXTURE_2D, 0);
		 
		shadowCubeMap = glGenTextures();
	    glBindTexture(GL_TEXTURE_CUBE_MAP, shadowCubeMap);
	    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
	    for (int i = 0 ; i < 6 ; i++) {
	        glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL30.GL_R32F, shadowQuality, shadowQuality, 0, GL_RED, GL_FLOAT, (ByteBuffer) null);
	    }
	    
	    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
	    GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depth, 0);
		
		glDrawBuffer(GL_NONE);
		
		glReadBuffer(GL_NONE);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			System.err.println("Error setting up shadowbuffer");
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		
		camera = new Camera(projectionMatrixID, viewMatrixID, shadowShaderProgram);
		camera.setPerspectiveProjection(90, 1, 1, range);
		frusum = new Frustum();
	}

	public void BindForWriting(int CubeFace)
	{
		glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fbo);
	    GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, CubeFace, shadowCubeMap, 0);
	    glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
	} 
	
	public void updateShadowBuffer(List<ObjectHandler> objects) {
		glViewport(0, 0, shadowQuality, shadowQuality);
		glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
		glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT); 
				
		for(int i = 0; i < 6; i ++){
			BindForWriting(cd1[i]);
			glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT); 
			
			Vector3f posDir = new Vector3f();
			Vector3f.add(position, cd2[i], posDir);
			camera.setLookAt(position, posDir,  new Vector3f(cd3[i].x, cd3[i].y, cd3[i].z));
			frusum.calculateFrustum(camera.getProjectionMatrix(), camera.getViewMatrix());
			
			for (ObjectHandler obj : objects) {
				obj.updateShadowBuffer(shadowShaderProgram, modelMatrixID, frusum);
			}
			
			camera.updateCamera();
		}
		
		glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
	int cd1[] = {GL_TEXTURE_CUBE_MAP_POSITIVE_X, GL_TEXTURE_CUBE_MAP_NEGATIVE_X, GL_TEXTURE_CUBE_MAP_POSITIVE_Y, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, GL_TEXTURE_CUBE_MAP_POSITIVE_Z, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z};
	Vector3f[] cd2 = {new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(-1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 0.0f, -1.0f)};
	Vector3f[] cd3 = {new Vector3f(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 0.0f, -1.0f),  new Vector3f(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f)};
	
	public void cleanUp(){
		glDeleteShader(shadowShaderProgram);
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public Vector3f getColor() {
		return color;
	}
	
	public void setColor(Vector3f color) {
		this.color = color;
	}

	public float getAmbient() {
		return ambient;
	}
	
	public void setAmbient(float ambient) {
		this.ambient = ambient;
	}
	
	public float getRange() {
		return range;
	}
	
	public void setRange(float range) {
		this.range = range;
	}
	
	public float getAttenConstant() {
		return attenConstant;
	}
	
	public void setAttenConstant(float attenConstant) {
		this.attenConstant = attenConstant;
	}
	
	public float getAttenLinear() {
		return attenLinear;
	}
	
	public void setAttenLinear(float attenLinear) {
		this.attenLinear = attenLinear;
	}
	
	public float getAttenExponent() {
		return attenExponent;
	}
	
	public void setAttenExponent(float attenExponent) {
		this.attenExponent = attenExponent;
	}

	public Vector3f getDirection() {
		return direction;
	}

	public void setDirection(Vector3f direction) {
		this.direction = direction;
	}

	public float getCutoff() {
		return cutoff;
	}

	public void setCutoff(float cutoff) {
		this.cutoff = cutoff;
	}

	public int getShadowQuality() {
		return shadowQuality;
	}

	public void setShadowQuality(int shadowQuality) {
		this.shadowQuality = shadowQuality;
	}
}
