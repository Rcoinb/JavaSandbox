#version 440 core

const int maxLightCount = 16;

in vec4 theColor;
in vec2 texCoord0;
in vec3 normal0;
in vec3 worldPos0;
in vec3 cameraPos0;
in vec4 shadowCoord;

out vec4 outputColor;

uniform int useTexture;
uniform sampler2D texture0;
uniform sampler2D ditherSampler;

uniform mat4 depthBiasMatrix[maxLightCount];
uniform sampler2D shadowMap[maxLightCount];

uniform int lightCount;

uniform samplerCube shadowCubeMap[maxLightCount];
uniform vec3 lightPosition[maxLightCount];
uniform vec3 lightDirection[maxLightCount];
uniform float lightCutoff[maxLightCount];
uniform vec3 lightColor[maxLightCount];
uniform float ambient[maxLightCount];
uniform float lightRange[maxLightCount];
uniform float attenConstant[maxLightCount];
uniform float attenLinear[maxLightCount];
uniform float attenExponent[maxLightCount];
uniform int ovrdWDirShadow[maxLightCount];

uniform float mat_specularIntensity;
uniform float mat_specularPower;

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

bool inPointLightShadow(samplerCube ShadowCubeMap, vec3 LightDirection)
{   
    float closestDepth = texture(ShadowCubeMap, LightDirection).r;
    //closestDepth = closestDepth * 32;
    float currentDepth = length(LightDirection);
    float bias = 0.025;
    bool shadow = currentDepth - bias >= closestDepth ? true : false;
    return shadow;
}

vec4 calcPointLight(vec3 normal, int light)
{
    vec3 lightDir = worldPos0 - lightPosition[light];
    float distanceToPoint = length(lightDir);

    bool inShadow = inPointLightShadow(shadowCubeMap[light], lightDir);
    
    if (ovrdWDirShadow[light] == 1) inShadow = false;
    
    if (ovrdWDirShadow[light] == 1){
        float curVis = 0;
        vec4 shadowCoord = depthBiasMatrix[light] * vec4(worldPos0, 1);
        if (texture(shadowMap[light], shadowCoord.xy).z  <  shadowCoord.z - 0.0005){
            inShadow = true;
        }
            
        if (shadowCoord.x >= 0.99 || shadowCoord.y >= 0.99 || shadowCoord.z >= 0.99
        || shadowCoord.x <= 0.01 || shadowCoord.y <= 0.01 || shadowCoord.z <= 0.01) inShadow = false;
    }
    
    lightDir = normalize(lightDir);   
    
    float diffuseFactor = dot(normal, -lightDir);
    
    vec4 difColor = vec4(lightColor[light], 1.0) * ambient[light]; //diffuse lighting is combined with ambient (better to change it in future!)
    vec4 specColor = vec4(0, 0, 0, 0);
    
    if (diffuseFactor > 0){
        //diffuse lighting
        difColor = difColor * diffuseFactor;
        
        //specular lighting
        if (mat_specularIntensity > 0){
            vec3 vertexToEye = normalize(cameraPos0 - worldPos0);
            vec3 lightReflect = normalize(reflect(lightDir, normal));
            float specularFactor = dot(vertexToEye, lightReflect);
            if (specularFactor > 0) {
                specularFactor = pow(specularFactor, mat_specularPower);
                specColor = vec4(lightColor[light].xyz * mat_specularIntensity * specularFactor, 1.0f);
            }
        }
    }
    else difColor = difColor * 0.0;
    
    float attenuation = 1 / (attenConstant[light] + attenLinear[light] * distanceToPoint + attenExponent[light] * distanceToPoint * distanceToPoint);  
    
    difColor = difColor * attenuation;
    specColor = specColor * attenuation;  
    
    if (inShadow && diffuseFactor > 0){ 
        difColor = difColor * 0;
        specColor = specColor * 0;
    }
                         
    return difColor + specColor;
}

vec4 calcSpotLight(vec3 normal, int light)
{
    vec3 LightToPixel = normalize(worldPos0 - lightPosition[light]);
    float SpotFactor = dot(LightToPixel, lightDirection[light]);
    vec4 Color = calcPointLight(normal, light);
    
    if (SpotFactor > lightCutoff[light]) return Color * (1.0 - (1.0 - SpotFactor) * 1.0 / (1.0 - lightCutoff[light]));
    else return vec4(0, 0, 0, 0);
}

void main()
{
    vec4 lightColor = vec4(0, 0, 0, 0);
    
    for(int i = 0; i < lightCount; i ++) {
    	lightColor += calcSpotLight(normalize(normal0), i);
    }
    
    vec4 resColor = theColor;
    if (useTexture == 1){
        resColor = texture(texture0, texCoord0); 
    }
    
    resColor = resColor * lightColor;
    
    if (theColor.w < 0.9) resColor = theColor; 
        
    //resColor += vec4(texture(ditherSampler, resColor.yz * 8192) / 32);  
	
	outputColor = resColor;
}