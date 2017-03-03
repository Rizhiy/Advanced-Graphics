#version 330
in vec4 position;
out vec4 frag_color;
void main() {
    vec3 color = int((position.x+1)*8)%2 == int((position.y+1)*8)%2 ? vec3(1.0,1.0,1.0) : vec3(0.0,0.0,0.0);
    frag_color = vec4(color,1.0);
}