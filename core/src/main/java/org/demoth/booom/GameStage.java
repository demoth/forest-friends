package org.demoth.booom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ParticleEffectActor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
    static final float ANIMATION_DURATION = 0.5f;
    private float animationTimeLeft = 0;
    GameActor originTile;
    GameActor destinationTile;

    // logical pixels
    int worldWidth;
    int worldHeight;
    int tileWidth;
    int tileHeight;

    int touchedTileX;
    int touchedTileY;
    int destTileX;
    int destTileY;

    GameActor[][] board;

    public GameStage() {

        // logical state
        board = new GameActor[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            board[x] = new GameActor[HEIGHT];
        }

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
                String regionName = generateNewTile(regions, random);
                GameActor actor = createActor(atlas, regionName, tileWidth * x, tileHeight * y);
                board[x][y] = actor;
                addActor(actor); // visual state
            }
        }
    }

    private static String generateNewTile(Array<TextureAtlas.AtlasRegion> regions, Random random) {
//        int randomIndex = random.nextInt(regions.size);
        List<String> pool = List.of("Mushroom_0", "Leaf_4", "Box_5", "Region_25");
        return pool.get(random.nextInt(pool.size()));
//        int randomIndex = random.nextInt(4);
//        return regions.get(randomIndex).name;
    }

    @Override
    public void act(float delta) {
        switch (state) {
            case READY_FOR_INPUT:
                break;
            case ANIMATION:
                animationTimeLeft -= delta;
                if (animationTimeLeft <= 0f) {
                    animationTimeLeft = 0f;

                    if (originTile != null) { // assert destinationTile != null
                        state = GameState.RUN_LOGIC;
                    } else {
                        state = GameState.READY_FOR_INPUT;
                    }
                }
                break;
            case RUN_LOGIC:
                // todo: swap pieces, calculate score, merge, etc...
                if (calculateMatches()) {
                    state = GameState.READY_FOR_INPUT;
                    // todo: calculate score
                    // todo: generate new tiles, etc...
                } else {
                    state = GameState.ANIMATION;
                    animationTimeLeft = ANIMATION_DURATION;

                    swap(touchedTileX, touchedTileY, destTileX, destTileY);

                    // start the reverse animation
                    originTile.addAction(Actions.moveTo(destinationTile.getX(), destinationTile.getY(), ANIMATION_DURATION));
                    destinationTile.addAction(Actions.moveTo(originTile.getX(), originTile.getY(), ANIMATION_DURATION));

                    originTile = null;
                    destinationTile = null;
                }
                break;
        }
        super.act(delta);
    }

    /**
     * iterate over the board of actors
     * check if there are 3 or more same objects in a line
     *
     * @return if the matches were found, else the animation should be reversed
     */
    private boolean calculateMatches() {
        var result = false;

        // check vertical matches
        for (int x = 0; x < WIDTH; x++) {
            // check all columns for a 3+ match
            List<GameActor> columnMatched = new ArrayList<>(Collections.singletonList(board[x][0]));
            for (int y = 1; y < HEIGHT; y++) {
                // if two neighbouring tiles match, add them to the matched list, else, remove everything and add a second one
                var newActor = board[x][y];
                if (columnMatched.get(0).spriteName.equals(newActor.spriteName)) {
                    columnMatched.add(newActor);
                } else {

                    if (markMatchedTiles(columnMatched))
                        result = true;

                    columnMatched.clear();
                    columnMatched.add(newActor);
                }
            }
            if (markMatchedTiles(columnMatched))
                result = true;
        }

        // todo: check horizontal matches

        return result;
    }

    private static boolean markMatchedTiles(List<GameActor> matched) {
        if (matched.size() >= 3) {
            System.out.println("matched vertical " + matched.size());
            for (GameActor actor : matched) {
                actor.applyMatch();
            }
            return true;
        }
        return false;
    }

    private static GameActor createActor(TextureAtlas objectTexture, String name, int x, int y) {
        GameActor actor = new GameActor(name, objectTexture.createSprite(name));
        actor.setPosition(x, y);
        return actor;
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
        destTileX = touchedTileX;
        if (swipeDirection == Direction.RIGHT)
            destTileX = touchedTileX + 1;
        else if (swipeDirection == Direction.LEFT)
            destTileX = touchedTileX - 1;

        destTileY = touchedTileY;
        if (swipeDirection == Direction.DOWN)
            destTileY = touchedTileY - 1;
        else if (swipeDirection == Direction.UP)
            destTileY = touchedTileY + 1;

        System.out.println("fling: Destination: " + destTileX + " " + destTileY);

        // calculate origin tile
        originTile = board[touchedTileX][touchedTileY];
        destinationTile = board[destTileX][destTileY];

        originTile.addAction(Actions.moveTo(destinationTile.getX(), destinationTile.getY(), ANIMATION_DURATION));
        destinationTile.addAction(Actions.moveTo(originTile.getX(), originTile.getY(), ANIMATION_DURATION));

        swap(touchedTileX, touchedTileY, destTileX, destTileY);

        // start animation
        state = GameState.ANIMATION;
        animationTimeLeft = ANIMATION_DURATION;

        // todo: cleanup, making sure fling will not be executed with stale parameters
        return false;
    }

    private void swap(int touchedTileX, int touchedTileY, int destTileX, int destTileY) {
        // switch actors on the board
        GameActor temp = board[touchedTileX][touchedTileY];
        board[touchedTileX][touchedTileY] = board[destTileX][destTileY];
        board[destTileX][destTileY] = temp;
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

class GameActor extends Group {
    final String spriteName;
    private boolean matched; // todo: decide what to do with them
    private final ParticleEffectActor effect;
    private final Image imageActor;

    public GameActor(String spriteName, Sprite sprite) {
        ParticleEffect starExplosion1 = new ParticleEffect();
        starExplosion1.load(Gdx.files.internal("effects/explosion.p"), Gdx.files.internal("effects"));
        effect = new ParticleEffectActor(starExplosion1, true);
        effect.setPosition(sprite.getWidth() / 2f, sprite.getHeight() / 2f);

        imageActor = new Image(sprite);
        addActor(imageActor);

        addActor(effect);

        this.spriteName = spriteName;
    }

    public void applyMatch() {
        matched = true;
        imageActor.addAction(Actions.parallel(
            Actions.scaleTo(0f, 0f, GameStage.ANIMATION_DURATION),
            Actions.moveBy(imageActor.getImageWidth() / 2, imageActor.getImageHeight() / 2, GameStage.ANIMATION_DURATION)
        ));
        effect.start();
    }

    @Override
    public String toString() {
        return spriteName;
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
