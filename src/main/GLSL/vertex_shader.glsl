#version 330
in vec3 v;
uniform mat4 modelToWorld;
uniform mat4 worldToCamera;
uniform mat4 cameraToScreen;
out vec4 position;
void main() {
    position = vec4(v, 1.0);
    gl_Position = cameraToScreen * worldToCamera * modelToWorld * position;
}