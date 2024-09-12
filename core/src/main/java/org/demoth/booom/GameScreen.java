package org.demoth.booom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class GameScreen extends ScreenAdapter {
    private final Stage background;
    final Stage activeStage;

    public GameScreen(Stage background, Stage activeStage) {
        this.background = background;
        this.activeStage = activeStage;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(activeStage, new GestureDetector((GestureDetector.GestureListener) activeStage)));

    }

    @Override
    public void render(float delta) {
        // clear the screen before rendering stages
        Gdx.gl.glClearColor(0, 0.3f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        background.draw();
        activeStage.act(delta);
        activeStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        background.getViewport().update(width, height, false);
        activeStage.getViewport().update(width, height, true);
    }

}
