public class ColorSpaceConverter {
    
    // Преобразование RGB в HSV
    public static float[] rgbToHSV(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        
        float[] hsv = new float[3];
        float min = Math.min(Math.min(r, g), b) / 255.0f;
        float max = Math.max(Math.max(r, g), b) / 255.0f;
        float delta = max - min;
        
        // Value
        hsv[2] = max;
        
        // Saturation
        if (max != 0) {
            hsv[1] = delta / max;
        } else {
            hsv[1] = 0;
        }
        
        // Hue
        if (delta == 0) {
            hsv[0] = 0;
        } else if (max == r / 255.0f) {
            hsv[0] = (g / 255.0f - b / 255.0f) / delta;
        } else if (max == g / 255.0f) {
            hsv[0] = 2 + (b / 255.0f - r / 255.0f) / delta;
        } else {
            hsv[0] = 4 + (r / 255.0f - g / 255.0f) / delta;
        }
        
        hsv[0] *= 60;
        if (hsv[0] < 0) hsv[0] += 360;
        hsv[0] /= 360; // Нормализуем Hue
        
        return hsv;
    }
    
    // Преобразование HSV в RGB
    public static int hsvToRGB(float[] hsv) {
        float h = hsv[0] * 360;
        float s = hsv[1];
        float v = hsv[2];
        
        if (s == 0) {
            int gray = (int) (v * 255);
            return (gray << 16) | (gray << 8) | gray;
        }
        
        h /= 60;
        int i = (int) Math.floor(h);
        float f = h - i;
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));
        
        float r, g, b;
        switch (i) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            default: r = v; g = p; b = q; break;
        }
        
        return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }
}