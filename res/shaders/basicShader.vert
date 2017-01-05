#version 440 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec4 color;
layout(location = 2) in vec3 normal;
layout(location = 3) in vec2 texCoord;

out vec4 theColor;
out vec3 normal0;
out vec2 texCoord0;
out vec3 worldPos0;
out vec3 cameraPos0;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 normalMatrix;

void main()
{
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1);
	normal0 = (normalMatrix * vec4(normal, 0.0)).xyz;
	worldPos0 = (modelMatrix * vec4(position, 1.0)).xyz;
	cameraPos0 = (inverse(viewMatrix) * vec4(0.0,0.0,0.0,1.0)).xyz;
	//shadowCoord = depthBiasMatrix * modelMatrix * vec4(position, 1);
	
	theColor = color;
	texCoord0 = texCoord;
}