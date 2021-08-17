package net.modificationstation.stationapi.api.client.texture.atlas;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.modificationstation.stationapi.api.client.texture.TextureHelper;
import net.modificationstation.stationapi.api.client.texture.binder.AnimatedTextureBinder;
import net.modificationstation.stationapi.api.client.texture.binder.StationTextureBinder;
import net.modificationstation.stationapi.api.registry.Identifier;
import net.modificationstation.stationapi.mixin.render.client.TextureManagerAccessor;
import uk.co.benjiweber.expressions.collection.EnhancedList;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.function.*;

import static net.modificationstation.stationapi.api.StationAPI.MODID;

public class ExpandableAtlas extends Atlas {

    private static final Map<String, ExpandableAtlas> PATH_TO_ATLAS = new HashMap<>();

    public static final ExpandableAtlas
            STATION_TERRAIN = new ExpandableAtlas(Identifier.of(MODID, "terrain"), SquareAtlas.TERRAIN).initTessellator(),
            STATION_GUI_ITEMS = new ExpandableAtlas(Identifier.of(MODID, "gui_items"), SquareAtlas.GUI_ITEMS);

    public ExpandableAtlas(final Identifier identifier) {
        super("/assets/stationapi/atlases/" + identifier, 0);
    }

    public ExpandableAtlas(final Identifier identifier, final SquareAtlas parent) {
        super("/assets/stationapi/atlases/" + identifier, 0, parent);
    }

    @Override
    protected void init() {
        PATH_TO_ATLAS.put(spritesheet, this);
    }

    @Override
    public BufferedImage getImage() {
        return imageCache;
    }

    @Override
    public InputStream getStream() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(imageCache, "png", outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public Texture addTexture(String texture) {
        BufferedImage image = TextureHelper.getTexture(texture);
        int previousAtlasWidth = imageCache == null ? 0 : imageCache.getWidth();
        drawTextureOnSpritesheet(image);
        textures.forEach(Texture::updateUVs);
        refreshTextureID();
        return EnhancedList.enhance(textures).addAndReturn(new FileTexture(
                texture, size++,
                previousAtlasWidth, 0,
                image.getWidth(), image.getHeight()
        ));
    }

    public <T extends StationTextureBinder> T addTextureBinder(String staticReference, Function<Texture, T> initializer) {
        return addTextureBinder(addTexture(staticReference), initializer);
    }

    public AnimatedTextureBinder addAnimationBinder(String animationPath, int animationRate, String staticReference) {
        return addAnimationBinder(animationPath, animationRate, addTexture(staticReference));
    }

    private void drawTextureOnSpritesheet(BufferedImage image) {
        if (imageCache == null) {
            ColorModel cm = image.getColorModel();
            boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
            WritableRaster raster = image.copyData(null);
            imageCache = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        } else {
            int previousAtlasWidth = imageCache.getWidth();
            resizeSpritesheet(imageCache.getWidth() + image.getWidth(), Math.max(image.getHeight(), imageCache.getHeight()));
            Graphics2D graphics = imageCache.createGraphics();
            graphics.drawImage(image, previousAtlasWidth, 0, null);
            graphics.dispose();
        }
    }

    private void resizeSpritesheet(int targetWidth, int targetHeight) {
        BufferedImage previousSpriteSheet = imageCache;
        imageCache = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = imageCache.createGraphics();
        graphics.drawImage(previousSpriteSheet, 0, 0, null);
        graphics.dispose();
    }

    private void refreshTextureID() {
        if (imageCache != null) {
            //noinspection deprecation
            Minecraft minecraft = (Minecraft) FabricLoader.getInstance().getGameInstance();
            TextureManagerAccessor textureManager = (TextureManagerAccessor) minecraft.textureManager;
            textureManager.invokeMethod_1089(imageCache, minecraft.textureManager.getTextureId(spritesheet));
        }
    }

    @Override
    public void refreshTextures() {
        super.refreshTextures();
        textures.forEach(texture -> {
            texture.x = imageCache == null ? 0 : imageCache.getWidth();
            texture.y = 0;
            BufferedImage image = TextureHelper.getTexture(((FileTexture) texture).path);
            texture.width = image.getWidth();
            texture.height = image.getHeight();
            drawTextureOnSpritesheet(image);
        });
        textures.forEach(Texture::updateUVs);
        refreshTextureID();
    }

    public static ExpandableAtlas getByPath(String spritesheet) {
        return PATH_TO_ATLAS == null ? null : PATH_TO_ATLAS.get(spritesheet);
    }

    public class FileTexture extends Texture {

        public final String path;

        protected FileTexture(String path, int index, int x, int y, int width, int height) {
            super(index, x, y, width, height);
            this.path = path;
        }
    }
}