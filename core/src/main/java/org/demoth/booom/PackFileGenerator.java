/*
package org.demoth.booom;
import java.io.FileWriter;
import java.io.IOException;

public class PackFileGenerator {

    public static void generatePackFile(String name, int atlasWidth, int atlasHeight, int gridCols, int gridRows) {
        int regionWidth = atlasWidth / gridCols;
        int regionHeight = atlasHeight / gridRows;

        try (FileWriter writer = new FileWriter(name + ".atlas")) {
            writer.write(name + ".png\n");
            writer.write("  format: RGBA8888\n");
            writer.write("  filter: Nearest,Nearest\n");
            writer.write("  repeat: none\n");

            int index = 0;

            for (int row = 0; row < gridRows; row++) {
                for (int col = 0; col < gridCols; col++) {
                    int x = col * regionWidth;
                    int y = row * regionHeight;

                    writer.write("Region_" + index + "\n");
                    writer.write("  rotate: false\n");
                    writer.write("  xy: " + x + ", " + y + "\n");
                    writer.write("  size: " + regionWidth + ", " + regionHeight + "\n");
                    writer.write("  orig: " + regionWidth + ", " + regionHeight + "\n");
                    writer.write("  offset: 0, 0\n");
                    writer.write("  index: -1\n");

                    index++;
                }
            }

            System.out.println("Pack file generated successfully!");

        } catch (IOException e) {
            System.err.println("Error while writing the pack file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Example usage
        generatePackFile("objects-no-bg", 1024, 1024, 7, 7);
    }
}
*/
