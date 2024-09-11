package org.demoth.booom;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class BackgroundStage extends Stage {
    public BackgroundStage() {
        setViewport(new ScreenViewport());

        Texture backgroundTexture = new Texture("background-forest.png");
        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setColor(0.5f, 0.5f, 0.5f, 1f);
        // make camera look at the center of the background image
        getViewport().getCamera().position.set((float) backgroundTexture.getWidth() / 2, (float) backgroundTexture.getHeight() / 2, 0);
        addActor(backgroundImage);
    }
}
