package utils.custom_shaders;

import utils.Shader;

public class FrameBufferShader extends Shader {
    public FrameBufferShader(String vertexPath, String fragmentPath) {
        super(vertexPath, fragmentPath);
        use();
        setInt("screenTexture", 0);
        Shader.unuseAll();
    }
}
