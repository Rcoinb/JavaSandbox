#version 330

in vec2 textureCoords;

out vec4 outputColor;

uniform sampler2D guiTexture;
uniform vec4 color;

void main(void){
    outputColor = texture(guiTexture, textureCoords);
    outputColor = outputColor * color;
}