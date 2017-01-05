#version 440 core

layout(location = 0) out float fragmentdepth;

in vec3 worldPos0;
in vec3 lightPos0;

uniform vec3 LightPos;

void main(){
    vec3 LightToVertex = worldPos0 - lightPos0;
    float LightToPixelDistance = length(LightToVertex);
    //LightToPixelDistance = LightToPixelDistance / 32;
    fragmentdepth = LightToPixelDistance;
}