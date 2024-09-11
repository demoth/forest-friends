package org.demoth.booom;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
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

        TextureAtlas objectTexture = new TextureAtlas("objects-no-bg.txt");
        Sprite sprite =  objectTexture.createSprite("Region_0");
        Image actor = new Image(sprite);
        addActor(actor);
    }

}
