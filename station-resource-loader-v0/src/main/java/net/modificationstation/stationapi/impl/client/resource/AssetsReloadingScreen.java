package net.modificationstation.stationapi.impl.client.resource;

import com.google.common.base.Suppliers;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.longs.Long2DoubleFunction;
import net.minecraft.client.gui.screen.ScreenBase;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.maths.MathHelper;
import net.modificationstation.stationapi.api.resource.ResourceReload;
import net.modificationstation.stationapi.api.resource.ResourceReloader;
import net.modificationstation.stationapi.api.resource.ResourceReloaderProfilers;
import net.modificationstation.stationapi.api.util.Util;
import net.modificationstation.stationapi.api.util.profiler.ProfileResult;
import net.modificationstation.stationapi.api.util.profiler.ProfilerSystem;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.*;

public class AssetsReloadingScreen extends ScreenBase {

    private static final long
            MAX_FPS = 60,
            BACKGROUND_START = 0,
            BACKGROUND_FADE_IN = 1000,
            STAGE_0_START = BACKGROUND_START + BACKGROUND_FADE_IN,
            STAGE_0_FADE_IN = 2000,
            GLOBAL_FADE_OUT = 1000,
            RELOAD_START = STAGE_0_START + STAGE_0_FADE_IN;
    private static final Double2DoubleFunction
            SIN_90_DELTA = delta -> MathHelper.sin((float) (delta * Math.PI / 2));
    private static final Long2DoubleFunction
            BACKGROUND_FADE_IN_DELTA = time -> (double) Longs.constrainToRange(time - BACKGROUND_START, 0, BACKGROUND_FADE_IN) / BACKGROUND_FADE_IN,
            STAGE_0_FADE_IN_DELTA = time -> (double) Longs.constrainToRange(time - STAGE_0_START, 0, STAGE_0_FADE_IN) / STAGE_0_FADE_IN;
    private static final Function<LongSupplier, Long2DoubleFunction>
            GLOBAL_FADE_OUT_DELTA_FACTORY = fadeOutStartGetter -> time -> 1 - (double) Longs.constrainToRange(time - fadeOutStartGetter.getAsLong(), 0, GLOBAL_FADE_OUT) / GLOBAL_FADE_OUT;

    private static UnaryOperator<Long2DoubleFunction> when(BooleanSupplier condition, Long2DoubleFunction ifTrue) {
        return ifFalse -> value -> (condition.getAsBoolean() ? ifTrue : ifFalse).applyAsDouble(value);
    }

    private final ScreenBase parent;
    private final Runnable done;
    private final Supplier<ResourceReload> reload;
    private boolean firstRenderTick = true;
    private long initTimestamp;
    private long currentTime;
    private float progress;
    private final Runnable
            backgroundEmitter,
            stage0Emitter;
    private boolean finished;
    private long fadeOutStart;
    private final List<String> locations = Collections.synchronizedList(new ArrayList<>());
    private float scrollProgress;

    private static class ReloaderProfiler extends ProfilerSystem {
        private final ResourceReloader reloader;
        private ReloaderProfiler(
                ResourceReloader reloader,
                LongSupplier timeGetter,
                IntSupplier tickGetter,
                boolean checkTimeout
        ) {
            super(timeGetter, tickGetter, checkTimeout);
            this.reloader = reloader;
        }

        @Override
        protected String getRoot() {
            return reloader.getName();
        }
    }

    private class ScreenProfiler extends ReloaderProfiler {
        private final String stage;
        private ScreenProfiler(ResourceReloader reloader, String stage) {
            super(reloader, Util.nanoTimeSupplier, () -> 0, false);
            this.stage = stage;
        }

        @Override
        public void push(String location) {
            super.push(location);
            locations.add(stage + ": " + ProfileResult.getHumanReadableName(getFullPath()));
        }
    }

    public AssetsReloadingScreen(
            ScreenBase parent,
            Runnable done,
            Function<ResourceReloaderProfilers.Factory, ResourceReload> reloadFactory
    ) {
        this.parent = parent;
        this.done = done;
        this.reload = Suppliers.memoize(() -> reloadFactory.apply(ResourceReloaderProfilers.Factory.of(ScreenProfiler::new, "Prepare", "Apply")));
        UnaryOperator<Long2DoubleFunction> globalFadeOutComposer = when(() -> finished, GLOBAL_FADE_OUT_DELTA_FACTORY.apply(() -> fadeOutStart));
        Long2DoubleFunction
                backgroundDelta = globalFadeOutComposer.apply(BACKGROUND_FADE_IN_DELTA),
                stage0Delta = globalFadeOutComposer.apply(STAGE_0_FADE_IN_DELTA);
        backgroundEmitter = setupEmitter(backgroundDelta.andThenDouble(SIN_90_DELTA), this::renderBackground);
        stage0Emitter = setupEmitter(stage0Delta.andThenDouble(SIN_90_DELTA), this::renderProgressBar, this::renderLogo);
    }

    private Runnable setupEmitter(final Long2DoubleFunction deltaFunc, final DoubleConsumer... renderers) {
        DoubleConsumer renderer = Arrays.stream(renderers).reduce(DoubleConsumer::andThen).orElse(delta -> {});
        return () -> {
            double delta = deltaFunc.applyAsDouble(currentTime);
            if (delta > 0)
                renderer.accept(delta);
        };
    }

    private void renderBackground(double delta) {
        fill(0, 0, width, height,
                this.parent == null ?
                        0xFF << 24 |
                                (int) (delta * 0x35) << 16 |
                                (int) (delta * 0x86) << 8 |
                                (int) (delta * 0xE7) :
                        (int) (delta * 0xFF) << 24 | 0x3586E7
        );
    }

    private void renderProgressBar(double delta) {
        int color = (int) (delta * 0xFF) << 24 | 0xFFFFFF;
        float v = (float) (10 - delta * 10);
        drawLineHorizontal(40, width - 40 - 1, (int) (height - 90 + v * 5), color);
        drawLineHorizontal(40, width - 40 - 1, (int) (height - 50 + v), color);
        drawLineHorizontal(40, width - 40 - 1, (int) (height - 40 - 1 + v), color);
        drawLineVertical(40, (int) (height - 40 + v), (int) (height - 90 + v * 5), color);
        drawLineVertical(width - 40 - 1, (int) (height - 40 + v), (int) (height - 90 + v * 5), color);
        fill(40 + 3, (int) (height - 50 + 3 + v), net.modificationstation.stationapi.api.util.math.MathHelper.ceil((width - (40 + 3) * 2) * progress + 40 + 3), (int) (height - 40 - 3 + v), color);
        float
                xScale = (float) minecraft.actualWidth / width,
                yScale = (float) minecraft.actualHeight / height;
        int scissorsHeight = (int) ((40 - 1 - v * 4) * yScale);
        if (scissorsHeight > 0) {
            int to = MathHelper.floor(scrollProgress);
            float scrollDelta = scrollProgress - to;
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glScissor((int) ((40 + 3) * xScale), (int) ((50 - v) * yScale), (int) ((width - (40 + 3) * 2) * xScale), scissorsHeight);
            for (int i = 0; i < to; i++) {
                int y = net.modificationstation.stationapi.api.util.math.MathHelper.ceil(height - 100 + (10 * i) + (scrollDelta * 10) + v * 5);
                if (y > height - 50 + v) break;
                drawTextWithShadow(textManager, locations.get(to - i - 1), 40 + 3, y, color);
            }
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    private void renderLogo(double delta) {
        double v = 10 - delta * 10;
        minecraft.textureManager.bindTexture(minecraft.textureManager.getTextureId("/assets/station-resource-loader-v0/textures/gui/stationapi_reload.png"));
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.INSTANCE;
        tessellator.start();
        tessellator.colour(0xFF, 0xFF, 0xFF, (int) (0xFF * delta));
        tessellator.vertex(width / 2D - 120, (height - 90D) / 2 - 20 - v, 0, 0, 0);
        tessellator.vertex(width / 2D - 120, (height - 90D) / 2 + 20 - v, 0, 0, 1);
        tessellator.vertex(width / 2D + 120, (height - 90D) / 2 + 20 - v, 0, 1, 1);
        tessellator.vertex(width / 2D + 120, (height - 90D) / 2 - 20 - v, 0, 1, 0);
        tessellator.draw();
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void init() {
        super.init();
        firstRenderTick = true;
    }

    @Override
    public void renderBackground() {
        backgroundEmitter.run();
    }

    private long lastRender;

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (firstRenderTick) {
            firstRenderTick = false;
            initTimestamp = System.currentTimeMillis();
        }
        currentTime = System.currentTimeMillis() - initTimestamp;
        final boolean partial = currentTime - lastRender < (1000 / MAX_FPS);
        if (partial) currentTime = lastRender;
        else lastRender = currentTime;
        int locationsSize = locations.size();
        if (!finished && !(scrollProgress + .1 < locationsSize) && !(progress + .1 < 1) && reloadComplete()) {
            reload.get().throwException();
            finished = true;
            fadeOutStart = currentTime;
        }
        if (!partial) {
            progress = Floats.constrainToRange(progress * .95F + (canAccessReload() ? reload.get().getProgress() : 0) * .05F, 0, 1);
            scrollProgress = Floats.constrainToRange(scrollProgress * .95F + locationsSize * .05F, 0, locationsSize);
        }
        if ((finished ? currentTime <= fadeOutStart + GLOBAL_FADE_OUT : currentTime < BACKGROUND_START + BACKGROUND_FADE_IN) && parent != null)
            parent.render(mouseX, mouseY, delta);
        renderBackground();
        super.render(mouseX, mouseY, delta);
        stage0Emitter.run();
        if (finished && currentTime - fadeOutStart > GLOBAL_FADE_OUT) done.run();
    }

    private boolean reloadComplete() {
        return canAccessReload() && reload.get().isComplete();
    }

    private boolean canAccessReload() {
        return currentTime > RELOAD_START;
    }
}
