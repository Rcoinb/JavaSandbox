package com.greatdevs.renderer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.greatdevs.GameWorld;
import com.greatdevs.tools.Frustum;
import com.greatdevs.util.*;

public class Renderer {
	
	public int basicShaderProgram;
	
	public int projectionMatrixID;
	public int viewMatrixID;
	public int modelMatrixID;
	public int normalMatrixID;
	public int useTextureID;
	public int textureID;
	
	
	//directional shadow
	//directional shadow
	
	
	public int ditherSamplerID;
	
	private static final int maxLightCount = 16;
	
	public int lightPositionID[] = new int[maxLightCount];
	public int lightDirectionID[] = new int[maxLightCount];
	public int lightCutoffID[] = new int[maxLightCount];
	public int lightColorID[] = new int[maxLightCount];
	public int ambientID[] = new int[maxLightCount];
	public int lightRangeID[] = new int[maxLightCount];
	public int attenConstantID[] = new int[maxLightCount];
	public int attenLinearID[] = new int[maxLightCount];
	public int attenExponentID[] = new int[maxLightCount];
	
	public int shadowCubeMapID[] = new int[maxLightCount];
	
	private int depthBiasMatrixID[] = new int[maxLightCount];
	private int shadowMapID[] = new int[maxLightCount];
	
	private int ovrdWDirShadowID[] = new int[maxLightCount];
	
	public int lightCountID;

	public int mat_specularIntensityID;
	public int mat_specularPowerID;
	
	public List<Light> allLights = new ArrayList<Light>();
	
	public int guiShaderProgram;
	
	public int guiModelMatrixID;
	public int guiTextureID;
	public int guiColorID;
	public int useSubTexID;
	public int imageDimID;
	public int subTexCorID;
	public int subTexDimID;
	
	private Vertex[] guiVertices = new Vertex[]{new Vertex(new Vector3f(-1, 1, 0)), new Vertex(new Vector3f(1, 1, 0)), new Vertex(new Vector3f(1, -1, 0)), new Vertex(new Vector3f(-1, -1, 0))};
	private int guiIndices[] = new int[] {2, 1, 0, 3, 2, 0};
	private ObjectHandler guiObject;
	
	public static int emptyTex;
	public static int ditherTex;
	
	public static byte pattern[] = {
		    0, 32,  8, 40,  2, 34, 10, 42,
		    48, 16, 56, 24, 50, 18, 58, 26,
		    12, 44,  4, 36, 14, 46,  6, 38,
		    60, 28, 52, 20, 62, 30, 54, 22,
		    3, 35, 11, 43,  1, 33,  9, 41,
		    51, 19, 59, 27, 49, 17, 57, 25,
		    15, 47,  7, 39, 13, 45,  5, 37,
		    63, 31, 55, 23, 61, 29, 53, 21 };
	
	//DirectionalShadow[] shadow = new DirectionalShadow[3];
	
	public void init() {
		//initiate openGL and shaders
		setUpStates();
		setUpBasicShader();
		
		//shadow[0] = new DirectionalShadow();
		//shadow[1] = new DirectionalShadow();
		//shadow[2] = new DirectionalShadow();
				
		//shadow[0].setUp(); 
		//shadow[1].setUp();
		//shadow[2].setUp();
		
		
		glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		setUpGUIShader();
		emptyTex = glGenTextures();
		ditherTex = glGenTextures();
		
		glBindTexture(GL_TEXTURE_2D, ditherTex);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		ByteBuffer bb = BufferUtils.createByteBuffer(pattern.length);
		bb.put(pattern);
		bb.flip();
		glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 8, 8, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, bb);
		
		guiObject = new ObjectHandler(guiVertices, guiIndices);
		guiObject.setShadow(false);
		
		/*for (ObjectHandler obj : objects) {
			obj.setUpShadowBuffer();;
		}*/
	}

	public void renderScene(List<ObjectHandler> objects, List<Light> lights, Frustum frustum) {
		Renderer.clearScreen(new Vector4f(0, 0, 0, 0));	
		for (Light light : allLights) light.cleanUp();
		allLights.clear();
		allLights.addAll(lights);
		glUseProgram(basicShaderProgram);
		glUniform1i(lightCountID, allLights.size());
		glUseProgram(0);
		
	    DisplayManager.bindAsRenderTarget();
		
		for(int i = 0; i < allLights.size(); i ++){
			allLights.get(i).update(basicShaderProgram, lightPositionID[i], lightDirectionID[i], lightCutoffID[i], lightColorID[i], ambientID[i], lightRangeID[i], attenConstantID[i], attenLinearID[i], attenExponentID[i], ovrdWDirShadowID[i]);						
			allLights.get(i).updateShadow(objects, basicShaderProgram, depthBiasMatrixID[i], shadowMapID[i], shadowCubeMapID[i], GameWorld.camera.getPosition(), i);
		}		
		/*glUseProgram(basicShaderProgram);
		glActiveTexture(GL13.GL_TEXTURE30);
		glBindTexture(GL_TEXTURE_2D, ditherTex);
		glUniform1i(ditherSamplerID, 30);
		
		glUseProgram(0);*/
		
		glViewport(0, 0, DisplayManager.WIDTH * DisplayManager.SCALE, DisplayManager.HEIGHT * DisplayManager.SCALE);
		for (ObjectHandler obj : objects) {
			obj.draw(basicShaderProgram, modelMatrixID, normalMatrixID, useTextureID, textureID, mat_specularIntensityID, mat_specularPowerID, frustum);
		}
	}

	public void renderGUI(int texture, Vector4f color, Vector2f position, Vector2f scale, int useSubTex, float imageDim, Vector2f subTexCor, Vector2f subTexDim) {
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glDisable(GL_DEPTH_TEST);
		glUseProgram(guiShaderProgram);
		
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texture);
		glUniform1i(guiTextureID, 0);
		
		glUniform4f(guiColorID, color.x, color.y, color.z, color.w);
		
		glUniform1i(useSubTexID, useSubTex);
		glUniform1f(imageDimID, imageDim);
		glUniform2f(subTexCorID, subTexCor.x, subTexCor.y);
		glUniform2f(subTexDimID, subTexDim.x, subTexDim.y);
		
		glUseProgram(0);
		guiObject.drawGUI(guiShaderProgram, guiModelMatrixID, position, scale);
		glEnable(GL_DEPTH_TEST);
		glDisable(GL11.GL_BLEND);
	}
	
	private void setUpStates() {
		glClearColor(0, 0, 0, 0);
		
		//glShadeModel(GL_SMOOTH);
		glDisable(GL_DITHER);
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_DEPTH_CLAMP);
		glEnable(GL_BLEND);
		
		glEnable(GL_TEXTURE_2D);
		glEnable(GL12.GL_TEXTURE_3D);
		glEnable(GL13.GL_TEXTURE_CUBE_MAP);
		glEnable(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS); 
	}
	
	private void setUpBasicShader() {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "basicShader.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "basicShader.frag"));

		basicShaderProgram = Framework.createProgram(shaderList);

		projectionMatrixID = glGetUniformLocation(basicShaderProgram, "projectionMatrix");
		viewMatrixID = glGetUniformLocation(basicShaderProgram, "viewMatrix");
		modelMatrixID = glGetUniformLocation(basicShaderProgram, "modelMatrix");
		normalMatrixID = glGetUniformLocation(basicShaderProgram, "normalMatrix");
		textureID = glGetUniformLocation(basicShaderProgram, "texture0");
		useTextureID = glGetUniformLocation(basicShaderProgram, "useTexture");
		
		
		ditherSamplerID = glGetUniformLocation(basicShaderProgram, "ditherSampler");
		
		lightCountID = glGetUniformLocation(basicShaderProgram, "lightCount");
		
		for(int i = 0; i < maxLightCount; i ++){
			lightPositionID[i] = glGetUniformLocation(basicShaderProgram, "lightPosition[" + i + "]");
			lightDirectionID[i] = glGetUniformLocation(basicShaderProgram, "lightDirection[" + i + "]");
			lightCutoffID[i] = glGetUniformLocation(basicShaderProgram, "lightCutoff[" + i + "]");
			lightColorID[i] = glGetUniformLocation(basicShaderProgram, "lightColor[" + i + "]");
			ambientID[i] = glGetUniformLocation(basicShaderProgram, "ambient[" + i + "]");
			lightRangeID[i] = glGetUniformLocation(basicShaderProgram, "lightRange[" + i + "]");
			attenConstantID[i] = glGetUniformLocation(basicShaderProgram, "attenConstant[" + i + "]");
			attenLinearID[i] = glGetUniformLocation(basicShaderProgram, "attenLinear[" + i + "]");
			attenExponentID[i] = glGetUniformLocation(basicShaderProgram, "attenExponent[" + i + "]");	
			
			shadowCubeMapID[i] = glGetUniformLocation(basicShaderProgram, "shadowCubeMap[" + i + "]");
			
			depthBiasMatrixID[i] = glGetUniformLocation(basicShaderProgram, "depthBiasMatrix[" + i + "]");
			shadowMapID[i] = glGetUniformLocation(basicShaderProgram, "shadowMap[" + i + "]");
			
			ovrdWDirShadowID[i] = glGetUniformLocation(basicShaderProgram, "ovrdWDirShadow[" + i + "]");
		}

		mat_specularIntensityID = glGetUniformLocation(basicShaderProgram, "mat_specularIntensity");
		mat_specularPowerID = glGetUniformLocation(basicShaderProgram, "mat_specularPower");
	}
	
	private void setUpGUIShader() {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "guiShader.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "guiShader.frag"));

		guiShaderProgram = Framework.createProgram(shaderList);

		guiModelMatrixID = glGetUniformLocation(guiShaderProgram, "modelMatrix");
		guiTextureID = glGetUniformLocation(guiShaderProgram, "guiTexture");
		guiColorID = glGetUniformLocation(guiShaderProgram, "color");
		useSubTexID = glGetUniformLocation(guiShaderProgram, "useSubTex");
		imageDimID = glGetUniformLocation(guiShaderProgram, "imageDim");
		subTexCorID = glGetUniformLocation(guiShaderProgram, "subTexCor");
		subTexDimID = glGetUniformLocation(guiShaderProgram, "subTexDim");
	}
	
	public static void clearScreen(Vector4f clearColor) {
		glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}

	public void cleanUp() {
		glDeleteShader(basicShaderProgram);
		glDeleteShader(guiShaderProgram);
		for(Light light : allLights) light.cleanUp();
		glUseProgram(0);
	}
}
