package utils.custom_shaders;

import utils.Shader;

public class SkyboxShader extends Shader {
    public SkyboxShader(String vertexPath, String fragmentPath) {
        super(vertexPath, fragmentPath);
        use();
        setInt("skybox", 0);
        Shader.unuseAll();
    }
}
