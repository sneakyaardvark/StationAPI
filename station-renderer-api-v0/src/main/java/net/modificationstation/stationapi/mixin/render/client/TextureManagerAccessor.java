package net.modificationstation.stationapi.mixin.render.client;

import net.minecraft.client.TexturePackManager;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {

    @Accessor
    GameOptions getGameOptions();

    @Accessor
    List<TextureBinder> getTextureBinders();

    @Accessor
    HashMap<String, Integer> getTextures();

    @Accessor
    ByteBuffer getCurrentImageBuffer();

    @Accessor
    void setCurrentImageBuffer(ByteBuffer byteBuffer);

    @Invoker
    BufferedImage invokeReadImage(InputStream var1);

    @Accessor("isBlurTexture")
    boolean stationapi$isBlurTexture();

    @Accessor("isClampTexture")
    boolean stationapi$isClampTexture();

    @Invoker("method_1098")
    int stationapi$method_1098(int i, int j);

    @Accessor("texturePackManager")
    TexturePackManager stationapi$getTexturePackManager();
}
