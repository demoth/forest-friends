package org.demoth.booom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/*

high level plan:
 - make logical state: grid n x n
 - fill it - reset()
 - make a component/actor
 - handle inputs z B GestureListener
 -

 */



public class GameStage extends Stage {
    private static final int WIDTH = 8;
    private static final int HEIGHT = 8;

    public GameStage() {
        int worldWidth = 1024;
        int worldHeight = 1024;

        int tileWidth = worldWidth / WIDTH;
        int tileHeight = worldHeight / HEIGHT;

        setViewport(new ExtendViewport(worldWidth, worldHeight));
        getCamera().position.set((float) worldWidth / 2, (float) worldHeight / 2, 0f);

        TextureAtlas atlas = new TextureAtlas("objects-no-bg.atlas");


        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                addActor(createActor(atlas, "Mushroom_0", tileWidth * x, tileHeight * y));
            }
        }
    }

    private static Image createActor(TextureAtlas objectTexture, String name, int x, int y) {
        Image mushroom1 = new Image(objectTexture.createSprite(name));
        mushroom1.setPosition(x, y);
        return mushroom1;
    }
}
