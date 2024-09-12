package org.demoth.booom;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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

public class GameStage extends Stage implements GestureDetector.GestureListener {
    private static final int WIDTH = 8;
    private static final int HEIGHT = 8;

    GameState state = GameState.READY_FOR_INPUT;
    private static final float ANIMATION_DURATION = 0.5f;
    private float animationTimeLeft = 0;

    // logical pixels
    int worldWidth;
    int worldHeight;
    int tileWidth;
    int tileHeight;

    int touchedTileX;
    int touchedTileY;
    GameActor[][] board;

    public GameStage() {

        // logical state
        board = new GameActor[WIDTH][HEIGHT];

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
                board[x][y] = actor;
                addActor(actor); // visual state
            }
        }
    }

    @Override
    public void act(float delta) {
        switch (state) {
            case READY_FOR_INPUT:
                break;
            case ANIMATION:
                animationTimeLeft -= delta;
                if (animationTimeLeft <= 0f) {
                    state = GameState.RUN_LOGIC;
                    animationTimeLeft = 0f;
                }
                break;
            case RUN_LOGIC:
                // todo: swap pieces, calculate score, merge, etc...
                state = GameState.READY_FOR_INPUT;
                break;
        }
        super.act(delta);
    }

    private static GameActor createActor(TextureAtlas objectTexture, String name, int x, int y) {
        GameActor mushroom1 = new GameActor(name, objectTexture.createSprite(name));
        mushroom1.setPosition(x, y);
        return mushroom1;
    }

    @Override
    public boolean touchDown(float screenX, float screenY, int pointer, int button) {
        if (state != GameState.READY_FOR_INPUT) {
            return false;
        }
        // convert the screen coords into world
        Vector2 worldCoords = getViewport().unproject(new Vector2(screenX, screenY));
        // determine tile on the world coords
        touchedTileX = (int) worldCoords.x / tileWidth;
        touchedTileY = (int) worldCoords.y / tileHeight;

        System.out.println("touchDown: Touched tile: " + touchedTileX + " " + touchedTileY);

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
        if (state != GameState.READY_FOR_INPUT) {
            return false;
        }
        // todo: ensure that tap was executed with corresponding tap event

        Direction swipeDirection = null;
        // calculate direction
        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            if (velocityX > 0) {
                swipeDirection = Direction.RIGHT;
            } else if (velocityX < 0) {
                swipeDirection = Direction.LEFT;
            }
        } else {
            if (velocityY > 0) {
                swipeDirection = Direction.DOWN;
            } else if (velocityY < 0) {
                swipeDirection = Direction.UP;
            }
        }

        System.out.println("fling: direction: " + swipeDirection);

        // check borders, for example, do nothing if swiping to the right from the very right column
        if (swipeDirection == Direction.RIGHT && touchedTileX == WIDTH - 1
            || swipeDirection == Direction.LEFT && touchedTileX == 0
            || swipeDirection == Direction.DOWN && touchedTileY == 0
            || swipeDirection == Direction.UP && touchedTileY == HEIGHT - 1) {
            return true;
        }

        // calculate destination tile
        int destTileX = touchedTileX;
        if (swipeDirection == Direction.RIGHT)
            destTileX = touchedTileX + 1;
        else if (swipeDirection == Direction.LEFT)
            destTileX = touchedTileX - 1;

        int destTileY = touchedTileY;
        if (swipeDirection == Direction.DOWN)
            destTileY = touchedTileY - 1;
        else if (swipeDirection == Direction.UP)
            destTileY = touchedTileY + 1;

        System.out.println("fling: Destination: " + destTileX + " " + destTileY);

        // calculate origin tile
        GameActor originTile = board[touchedTileX][touchedTileY];
        GameActor destinationTile = board[destTileX][destTileY];

        originTile.addAction(Actions.moveTo(destinationTile.getX(), destinationTile.getY(), ANIMATION_DURATION));
        destinationTile.addAction(Actions.moveTo(originTile.getX(), originTile.getY(), ANIMATION_DURATION));

        // switch actors on the board
        GameActor temp = board[touchedTileX][touchedTileY];
        board[touchedTileX][touchedTileY] = board[destTileX][destTileY];
        board[destTileX][destTileY] = temp;

        // start animation
        state = GameState.ANIMATION;
        animationTimeLeft = ANIMATION_DURATION;

        // todo: cleanup, making sure fling will not be executed with stale parameters
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

//    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
//        touchedTileY = -1;
//        touchedTileX = -1;
        System.out.println("touchUp: Touch up");
        return false;
    }
}

class GameActor extends Image {
    private final String name;

    public GameActor(String name, Sprite sprite) {
        super(sprite);
        this.name = name;
    }
}

enum Direction {
    UP, DOWN, LEFT, RIGHT
}

enum GameState {
    READY_FOR_INPUT, // nothing happens, ready to receive input
    ANIMATION, // animation is in progress (moving tiles), no input expected
    RUN_LOGIC, // animation is done, merge tiles of equal tiers and generate new tiles
}
