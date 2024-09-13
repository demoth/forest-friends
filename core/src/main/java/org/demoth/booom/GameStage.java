package org.demoth.booom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ParticleEffectActor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.*;

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

    int score = 0;

    // logical pixels
    int boardWidth;
    int boardHeight;
    int tileWidth;
    int tileHeight;

    int touchedTileX;
    int touchedTileY;
    int destTileX;
    int destTileY;

    GameActor[][] board;
    Group boardGroup;
    TextureAtlas atlas;
    Label scoreLabel;

    Skin skin;

    List<String> spritesPool; // recalculated randomly on every new game

    public GameStage() {
        boardWidth = 1024;
        boardHeight = 1024;

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        scoreLabel = new Label("", skin);
        scoreLabel.setAlignment(Align.left, Align.left);
        scoreLabel.setPosition(0, (float) boardHeight + scoreLabel.getHeight());
        addActor(scoreLabel);

        Label versionLabel = new Label("v0.1 13 Sep 2024 demoth.dev", skin);
        versionLabel.setAlignment(Align.right, Align.left);
        versionLabel.setPosition(boardWidth - versionLabel.getWidth(), (float) boardHeight);
        addActor(versionLabel);

        // logical state
        board = new GameActor[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            board[x] = new GameActor[HEIGHT];
        }

        boardGroup = new Group();
        addActor(boardGroup);


        tileWidth = boardWidth / WIDTH;
        tileHeight = boardHeight / HEIGHT;

        setViewport(new ExtendViewport(boardWidth, boardHeight + 50));
        getCamera().position.set((float) boardWidth / 2, (float) boardHeight / 2, 0f);

        atlas = new TextureAtlas("objects-no-bg.atlas");

        respawnTiles();
    }

    private void respawnTiles() {

        // pick 4 random regions from the atlas for the spritePool
        spritesPool = createNewTilesPool();


        boardGroup.clear();
        score = 0;
        scoreLabel.setText("Score: " + score);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                spawnActor(x, y);
            }
        }
    }

    private List<String> createNewTilesPool() {
        List<String> result = new ArrayList<>();
        while (result.size() < 4) {
            int randomIndex = MathUtils.random(atlas.getRegions().size - 1);
            String regionName = atlas.getRegions().get(randomIndex).name;
            if (!result.contains(regionName)) {
                result.add(regionName);
            }
        }
        return result;
    }

    private void respawnMatchedTiles() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                var actor = board[x][y];
                if (actor.matched) {
                    // Visual state: actor is removed from the stage at the end of the animation.
                    // Logical state: updated upon spawn
                    spawnActor(x, y);
                }
            }
        }
    }

    private void spawnActor(int x, int y) {
        String regionName = selectRandomTile();
        GameActor actor = new GameActor(regionName, atlas.createSprite(regionName), tileWidth * x, tileHeight * y);
        board[x][y] = actor; // logical state
        actor.playSpawnAnimation();
        boardGroup.addActor(actor); // visual state
    }

    private String selectRandomTile() {
        var index = MathUtils.random(spritesPool.size() - 1);
        return spritesPool.get(index);
    }

    private void addScore(int bonus) {
        score += bonus;
        scoreLabel.setText("Score: " + score);
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
                    respawnMatchedTiles();
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

                    // check matched tiles when a different tile is found
                    if (markMatchedTiles(columnMatched)) {
                        result = true;
                    }

                    columnMatched.clear();
                    columnMatched.add(newActor);
                }
            }
            // check matched tiles when we reach the end of the column
            if (markMatchedTiles(columnMatched)) {
                result = true;
            }
        }

        // check horizontal matches (copy-paste of the code above)
        for (int y = 0; y < HEIGHT; y++) {

            // check all rows for a 3+ match
            List<GameActor> rowsMatched = new ArrayList<>(Collections.singletonList(board[0][y]));
            for (int x = 1; x < WIDTH; x++) {

                // if two neighbouring tiles match, add them to the matched list, else, remove everything and add a second one
                var newActor = board[x][y];
                if (rowsMatched.get(0).spriteName.equals(newActor.spriteName)) {
                    rowsMatched.add(newActor);
                } else {

                    // check matched tiles when a different tile is found
                    if (markMatchedTiles(rowsMatched)) {
                        result = true;
                    }

                    rowsMatched.clear();
                    rowsMatched.add(newActor);
                }
            }
            // check matched tiles when we reach the end of the column
            if (markMatchedTiles(rowsMatched)) {
                result = true;
            }
        }

        return result;
    }

    private boolean markMatchedTiles(List<GameActor> matched) {
        if (matched.size() >= 3) {
            System.out.println("matched vertical " + matched.size());
            for (GameActor actor : matched) {
                actor.applyMatch();
            }
            addScore(matched.size());
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(float screenX, float screenY, int pointer, int button) { // todo: use only left button
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

    @Override
    public boolean keyUp(int keyCode) {
        switch (keyCode) {
            case Input.Keys.ESCAPE:
                Gdx.app.exit();
                return true;
            case Input.Keys.R:
                respawnTiles();
                return true;
        }
        return false;
    }
}

class GameActor extends Group {
    final String spriteName;
    boolean matched; // todo: decide what to do with them
    private final ParticleEffectActor effect;
    private final Image imageActor;

    public GameActor(String spriteName, Sprite sprite, int x, int y) {
        setPosition(x, y);

        ParticleEffect starExplosion1 = new ParticleEffect();
        starExplosion1.load(Gdx.files.internal("effects/explosion.p"), Gdx.files.internal("effects"));
        effect = new ParticleEffectActor(starExplosion1, true);
        effect.setPosition(sprite.getWidth() / 2f, sprite.getHeight() / 2f);

        imageActor = new Image(sprite);
        addActor(imageActor);

        addActor(effect);

        this.spriteName = spriteName;
    }

    public void die() {
        effect.dispose();
        remove();
    }

    public void applyMatch() {
        matched = true;
        imageActor.addAction(
            Actions.sequence(scaleCentered(false),
                Actions.run(this::die)));
        effect.start();
    }

    ParallelAction scaleCentered(boolean up) {
        float scaleFactor = up ? 1f : 0f;
        float positionX = imageActor.getWidth() / 2;
        float positionY = imageActor.getHeight() / 2;

        if (up) {
            imageActor.moveBy(positionX, positionY);
            imageActor.setScale(0f);
        }

        return Actions.parallel(
            Actions.scaleTo(scaleFactor, scaleFactor, GameStage.ANIMATION_DURATION),
            Actions.moveBy(up ? -positionX : positionX, up ? - positionY: positionY, GameStage.ANIMATION_DURATION)
        );
    }

    @Override
    public String toString() {
        return spriteName;
    }

    public void playSpawnAnimation() {
        imageActor.addAction(scaleCentered(true));
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
