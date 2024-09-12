package org.demoth.booom;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.List;

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

        // logical state
        GameActor[][] gameState = new GameActor[WIDTH][HEIGHT];

        int worldWidth = 1024;
        int worldHeight = 1024;

        int tileWidth = worldWidth / WIDTH;
        int tileHeight = worldHeight / HEIGHT;

        setViewport(new ExtendViewport(worldWidth, worldHeight));
        getCamera().position.set((float) worldWidth / 2, (float) worldHeight / 2, 0f);

        TextureAtlas atlas = new TextureAtlas("objects-no-bg.atlas");
        var regions = atlas.getRegions();

        var random = new java.util.Random();

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                String regionName = regions.get(random.nextInt(regions.size)).name;
                GameActor actor = createActor(atlas, regionName, tileWidth * x, tileHeight * y);
                gameState[x][y] = actor;
                addActor(actor); // visual state
            }
        }
    }

    private static GameActor createActor(TextureAtlas objectTexture, String name, int x, int y) {
        GameActor mushroom1 = new GameActor(name, objectTexture.createSprite(name));
        mushroom1.setPosition(x, y);
        return mushroom1;
    }
}

class GameActor extends Image {
    private final String name;

    public GameActor(String name, Sprite sprite) {
        super(sprite);
        this.name = name;
    }
}
