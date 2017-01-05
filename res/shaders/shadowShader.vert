#version 440 core

layout(location = 0) in vec3 position;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

out vec3 worldPos0;
out vec3 lightPos0;

void main(){  
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1);
	worldPos0 = (modelMatrix * vec4(position, 1.0)).xyz;
	lightPos0 = (inverse(viewMatrix) * vec4(0.0,0.0,0.0,1.0)).xyz;
}