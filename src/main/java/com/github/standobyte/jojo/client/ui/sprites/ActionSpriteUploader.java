package com.github.standobyte.jojo.client.ui.sprites;

import java.util.stream.Stream;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.ModActions;

import net.minecraft.client.renderer.texture.SpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

public class ActionSpriteUploader extends SpriteUploader {
    public ActionSpriteUploader(TextureManager textureManager) {
        super(textureManager, new ResourceLocation("textures/atlas/actions.png"), "action");
    }

    @Override
    protected Stream<ResourceLocation> getResourcesToLoad() {
        return ModActions.Registry.getRegistry().getKeys().stream();
    }

    public TextureAtlasSprite getSprite(Action action) {
        return getSprite(ModActions.Registry.getRegistry().getKey(action));
    }
}
