package org.demoth.booom;

import com.badlogic.gdx.Game;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends Game {
    private GameScreen gameScreen;

    @Override
    public void create() {
        gameScreen = new GameScreen(new BackgroundStage(), new GameStage());
        setScreen(gameScreen);
    }
}
