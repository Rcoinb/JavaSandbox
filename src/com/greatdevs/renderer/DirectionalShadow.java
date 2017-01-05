package com.greatdevs.renderer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.greatdevs.GameWorld;
import com.greatdevs.tools.BufferTools;
import com.greatdevs.tools.Frustum;
import com.greatdevs.tools.Maths;
import com.greatdevs.tools.MatrixHandler;
import com.greatdevs.util.DisplayManager;
import com.greatdevs.util.Framework;
import com.greatdevs.util.ObjectHandler;

public class DirectionalShadow {

	private FloatBuffer matrix44Buffer;
	private MatrixHandler depthMatrix;
	public int fbo;
	public int depth;
	
	private int shadowShaderProgram;
	private int depthMatrixID;
	private int depthModelMatrixID;
	
	public Frustum frusum = new Frustum();
	
	private int shadowQuality = 4096;

	public DirectionalShadow() {
		depthMatrix = new MatrixHandler();
		matrix44Buffer = BufferTools.reserveFloatData(16);
		setUpShadowShaders();
	}

	public void setUp() {
		fbo = glGenFramebuffers();

		depth = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, depth);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, shadowQuality, shadowQuality, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glBindTexture(GL_TEXTURE_2D, 0);
		
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
		glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depth, 0);
		
		glDrawBuffer(GL_NONE);
		
		glReadBuffer(GL_NONE);
		
		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			System.err.println("Error setting up shadowbuffer");
		}
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public void updateShadow(List<ObjectHandler> objects, int basicShaderProgram, int depthBiasMatrixID, int shadowMapID, Vector3f camPos, Vector3f dir, int i){
		glCullFace(GL_FRONT);
		updateShadowBuffer(objects, camPos, dir);
		updateShadowBias(basicShaderProgram, depthBiasMatrixID, shadowMapID, i);
		glCullFace(GL_BACK);
	}
	
	private void setUpShadowShaders() {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "dirShadowShader.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "dirShadowShader.frag"));

		shadowShaderProgram = Framework.createProgram(shaderList);
		
		depthMatrixID = glGetUniformLocation(shadowShaderProgram, "depthMatrix");
		depthModelMatrixID = glGetUniformLocation(shadowShaderProgram, "depthModelMatrix");
	}

	public void updateShadowBias(int shaderProgram, int depthBiasMatrixID, int shadowMapID, int i) {
		MatrixHandler biasMatrix = new MatrixHandler();
		MatrixHandler depthBiasMatrix = new MatrixHandler();
		
		biasMatrix.setBias();
		Matrix4f.mul(biasMatrix, depthMatrix, depthBiasMatrix);

		glUseProgram(shaderProgram);

		depthBiasMatrix.store(matrix44Buffer);
		matrix44Buffer.flip();
		glUniformMatrix4(depthBiasMatrixID, false, matrix44Buffer);

		glActiveTexture(GL_TEXTURE0 + i);
		glBindTexture(GL_TEXTURE_2D, depth);
		glUniform1i(shadowMapID, i);
		
		glUseProgram(0);
	}
	
	public void BindForWriting()
	{
		glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fbo);
		glFramebufferTexture(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depth, 0);
	    glDrawBuffer(GL30.GL_DEPTH_ATTACHMENT);
	} 
	
	public void updateShadowBuffer(List<ObjectHandler> objects, Vector3f camPos, Vector3f lightPosition) {
		glViewport(0, 0, shadowQuality, shadowQuality);
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);	
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		BindForWriting();
		
		//glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixHandler depthProjectionMatrix = new MatrixHandler();
		MatrixHandler depthViewMatrix = new MatrixHandler();
		
		Vector3f normLightPosition = new Vector3f();
		lightPosition.normalise(normLightPosition);
		
		depthProjectionMatrix.initOrthographicMatrix(-15, 15, -15, 15, -15, 15);
		depthViewMatrix.lookAt(normLightPosition, new Vector3f(0, 0, 0), new Vector3f(0, 0, 1));
		
		depthViewMatrix.translate(new Vector3f(-(10 * (int) (camPos.x / 10)), -(10 * (int) (camPos.y / 10)), -(10 * (int) (camPos.z / 10))), depthViewMatrix); 
		//MUST BE DONE IN BETTER WAY /** DEFAULT: new Vector3f(-camPos.x, -camPos.y, -camPos.z);
		
		frusum.calculateFrustum(depthProjectionMatrix, depthViewMatrix);
		
		Matrix4f.mul(depthProjectionMatrix, depthViewMatrix, depthMatrix);
		
		for (ObjectHandler obj : objects) {
			obj.updateShadowBuffer(shadowShaderProgram, depthModelMatrixID, null);
		}
		
		//depthMatrix.translate(new Vector3f(camPos.x * normLightPosition.x, camPos.y * normLightPosition.y, camPos.z * normLightPosition.z), depthMatrix);
		
		glUseProgram(shadowShaderProgram);
		depthMatrix.store(matrix44Buffer);
		matrix44Buffer.flip();
		glUniformMatrix4(depthMatrixID, false, matrix44Buffer);
		glUseProgram(0);
		
		glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
	public void polyOffset() {
		glDisable(GL_POLYGON_OFFSET_FILL);
	}
}
