package utils.custom_shaders;

import utils.Shader;

public class OurShader extends Shader {

    public OurShader(String vertexPath, String fragmentPath) {
        super(vertexPath, fragmentPath);
        use();
        setInt("material.diffuse", 0);
//        setInt("material.specular", 1); //Disable specular.
        setInt("displacement", 2);
        Shader.unuseAll();
    }
}
