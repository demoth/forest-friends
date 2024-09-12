package org.demoth.booom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GameStage extends Stage {
    public GameStage() {
        int worldWidth = 1024;
        int worldHeight = 1024;

        setViewport(new ExtendViewport(worldWidth, worldHeight));
        getCamera().position.set((float) worldWidth / 2, (float) worldHeight / 2, 0f);

        TextureAtlas objectTexture = new TextureAtlas("objects-no-bg.atlas");
        Image mushroom0 = new Image(objectTexture.createSprite("Mushroom_0"));
        addActor(mushroom0);

        Image mushroom1 = new Image(objectTexture.createSprite("Mushroom_1"));
        mushroom1.setPosition(mushroom0.getX() + mushroom0.getWidth(), mushroom0.getY());
        addActor(mushroom1);
    }

}
