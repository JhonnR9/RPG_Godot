shader_type canvas_item;

uniform float h_factor = 1.0;
uniform float s_factor = 1.0;
uniform float l_factor = 1.0;

vec3 rgb_to_hsl(vec3 color) {
    float maxc = max(max(color.r, color.g), color.b);
    float minc = min(min(color.r, color.g), color.b);
    float l = (maxc + minc) * 0.5;
    float s = 0.0;
    float h = 0.0;
    
    if (maxc != minc) {
        float d = maxc - minc;
        s = l > 0.5 ? d / (2.0 - maxc - minc) : d / (maxc + minc);
        if (maxc == color.r) {
            h = (color.g - color.b) / d + (color.g < color.b ? 6.0 : 0.0);
        } else if (maxc == color.g) {
            h = (color.b - color.r) / d + 2.0;
        } else {
            h = (color.r - color.g) / d + 4.0;
        }
        h /= 6.0;
    }
    return vec3(h, s, l);
}

vec3 hsl_to_rgb(vec3 hsl) {
    float h = hsl.x * 6.0;
    float s = hsl.y;
    float l = hsl.z;
    
    float c = (1.0 - abs(2.0 * l - 1.0)) * s;
    float x = c * (1.0 - abs(mod(h, 2.0) - 1.0));
    float m = l - c * 0.5;
    
    vec3 rgb = vec3(0.0);
    if (0.0 <= h && h < 1.0) rgb = vec3(c, x, 0.0);
    else if (1.0 <= h && h < 2.0) rgb = vec3(x, c, 0.0);
    else if (2.0 <= h && h < 3.0) rgb = vec3(0.0, c, x);
    else if (3.0 <= h && h < 4.0) rgb = vec3(0.0, x, c);
    else if (4.0 <= h && h < 5.0) rgb = vec3(x, 0.0, c);
    else if (5.0 <= h && h < 6.0) rgb = vec3(c, 0.0, x);
    
    return rgb + m;
}

vec4 apply_shadow(vec4 c) {
    vec3 new_color = rgb_to_hsl(vec3(c.rgb));
    new_color.r = mod(new_color.r + h_factor, 1.0);
    new_color.g = clamp(new_color.g + s_factor, 0.0, 1.0);
    new_color.b = clamp(new_color.b - l_factor, 0.0, 1.0);
    return vec4(hsl_to_rgb(new_color), 1.0);
}

void fragment() {
    vec2 pixel_size = vec2(1.0) / vec2(textureSize(TEXTURE, 0));
    vec4 center_color = texture(TEXTURE, UV);
    
    vec4 left_color = texture(TEXTURE, UV - vec2(pixel_size.x, 0));
    vec4 right_color = texture(TEXTURE, UV + vec2(pixel_size.x, 0));
    vec4 up_color = texture(TEXTURE, UV - vec2(0.0, pixel_size.y));
    vec4 down_color = texture(TEXTURE, UV + vec2(0.0, pixel_size.y));
    
    if (left_color.a > 0.1 && center_color.a < 0.1) {
        center_color = apply_shadow(left_color);
    }
    if (right_color.a > 0.1 && center_color.a < 0.1) {
        center_color = apply_shadow(right_color);
    }
    if (up_color.a > 0.1 && center_color.a < 0.1) {
         center_color = apply_shadow(up_color);
    }
    if (down_color.a > 0.1 && center_color.a < 0.1) {
         center_color = apply_shadow(down_color);
    }

    COLOR = center_color;
}
