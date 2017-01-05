package com.greatdevs.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.util.ArrayList;

public class Shader {
	
	public static int compileShader(int shaderType, String shaderCode) {
		int shader = glCreateShader(shaderType);
		
        glShaderSource(shader, shaderCode);
        glCompileShader(shader);
        
		int status = glGetShaderi(shader, GL_COMPILE_STATUS);
		if (status == GL_FALSE) {
    		System.out.println(glGetShaderInfoLog(shader, 500));
    		System.err.println("Could not compile shader");
			glDeleteShader(shader);
		}      
		
		return shader;
	}
	
	public static int linkProgram(ArrayList<Integer> shaders) {
		int program = glCreateProgram();

		return linkProgram(program, shaders);
	}
	
	private static int linkProgram(int program, ArrayList<Integer> shaders) {
		for (Integer shader : shaders) {
			glAttachShader(program, shader);
		}

		glLinkProgram(program);
		
		int status = glGetProgrami(program, GL_LINK_STATUS);
		if (status == GL_FALSE) {
    		System.out.println(glGetShaderInfoLog(program, 500));
    		System.err.println("Could not compile shader");
			glDeleteShader(program);
		}

		for (Integer shader : shaders) {
			glDetachShader(program, shader);
		}
	    
	    return program;
	}
	
	/*private static class CompileLinkShaderException extends RuntimeException {
		private static final long serialVersionUID = 5490603440382398244L;

		CompileLinkShaderException(int shader) {
			super(glGetShaderInfoLog(
					shader, 
					glGetShaderi(shader, GL_INFO_LOG_LENGTH)));
		}
	}
	
	private static class CompileLinkProgramException extends RuntimeException {
		private static final long serialVersionUID = 7321217286524434327L;

		CompileLinkProgramException(int program) {
			super(glGetShaderInfoLog(
					program, 
					glGetShaderi(program, GL_INFO_LOG_LENGTH)));
		}
	}*/
}
