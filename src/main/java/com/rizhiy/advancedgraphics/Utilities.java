package com.rizhiy.advancedgraphics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rizhiy on 01/03/17.
 */
public class Utilities {
    public static String loadShaderSource(final String shaderFilePath) {
        String shaderSourceString = new String();
        try {
            shaderSourceString = new String(Files.readAllBytes(Paths.get(shaderFilePath)));
        } catch (IOException e) {
            System.err.println(e);
        }

        return shaderSourceString;
    }

    public static List<Float> offTofloatArray(final String offFilePath) {
        List<Float> result = new ArrayList<>();
        try {
            String                 offFile       = new String(Files.readAllBytes(Paths.get(offFilePath)));
            String[]               lines         = offFile.split("\n");
            int                    numOfVertices = 0;
            int                    numOfFaces    = 0;
            boolean                firstpass     = true;
            int                    counter       = 0;
            List<ArrayList<Float>> vertices      = new ArrayList<>();
            for (String line : lines) {
                if (line.equals("OFF") || line.contains("#")) {
                    continue;
                }
                if (firstpass) {
                    numOfVertices = Integer.parseInt(line.split(" ")[0]);
                    numOfFaces = Integer.parseInt(line.split(" ")[1]);
                    for (int i = 0; i < numOfVertices; i++) {
                        vertices.add(new ArrayList<Float>());
                    }
                    firstpass = false;
                    continue;
                }
                if (counter >= numOfVertices) {
                    if (counter >= numOfFaces + numOfVertices) {
                        break;
                    }
                    String[] tmp              = line.split(" ");
                    int      numberOfVertices = Integer.parseInt(tmp[0]);
                    for (int i = 0; i < numberOfVertices; i++) {
                        int vertexNum = Integer.parseInt(tmp[i + 1]);
                        for (int j = 0; j < 3; j++) {
                            result.add(vertices.get(vertexNum).get(j));
                        }
                    }
                } else {
                    String[] tmp = line.split(" ");
                    for (String s : tmp) {
                        vertices.get(counter).add(Float.parseFloat(s));
                    }
                }
                counter++;
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }
}
