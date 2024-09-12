package org.demoth.booom;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
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


public class GameStage extends Stage implements GestureDetector.GestureListener {
    private static final int WIDTH = 8;
    private static final int HEIGHT = 8;

    // logical pixels
    int worldWidth;
    int worldHeight;
    int tileWidth;
    int tileHeight;

    int touchedTileX;
    int touchedTileY;

    public GameStage() {

        // logical state
        GameActor[][] gameState = new GameActor[WIDTH][HEIGHT];

        worldWidth = 1024;
        worldHeight = 1024;

        tileWidth = worldWidth / WIDTH;
        tileHeight = worldHeight / HEIGHT;

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

    @Override
    public boolean touchDown(float screenX, float screenY, int pointer, int button) {
        // convert the screen coords into world
        Vector2 worldCoords = getViewport().unproject(new Vector2(screenX, screenY));
        // determine tile on the world coords
        touchedTileX = (int) worldCoords.x / tileWidth;
        touchedTileY = (int) worldCoords.y / tileHeight;

        System.out.println("Touched tile: " + touchedTileX + " " + touchedTileY);

        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }
}

class GameActor extends Image {
    private final String name;

    public GameActor(String name, Sprite sprite) {
        super(sprite);
        this.name = name;
    }
}
