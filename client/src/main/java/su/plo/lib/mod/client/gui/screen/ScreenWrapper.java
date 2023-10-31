package su.plo.lib.mod.client.gui.screen;

import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.universal.*;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.ModVoiceClient;
import su.plo.voice.client.event.key.KeyPressedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//#if MC>=12000
//$$ import net.minecraft.locale.Language;
//#endif

@ToString
public final class ScreenWrapper
        extends UScreen {

    public static void openScreen(@Nullable GuiScreen screen) {
        if (screen == null) {
            UMinecraft.getMinecraft().execute(() -> UScreen.displayScreen(null));
            return;
        }

        ScreenWrapper wrapped = new ScreenWrapper(screen);

        wrapped.screen.screen = wrapped;
        UMinecraft.getMinecraft().execute(() -> UScreen.displayScreen(wrapped));
    }

    public static Optional<ScreenWrapper> getCurrentWrappedScreen() {
        Screen screen = UScreen.getCurrentScreen();
        if (screen instanceof ScreenWrapper) {
            return Optional.of((ScreenWrapper) screen);
        }

        return Optional.empty();
    }

    @Getter
    private final GuiScreen screen;

    private boolean ignoreFirstMove = true;
    private double lastDraggedMouseX;
    private double lastDraggedMouseY;

    private ScreenWrapper(@NotNull GuiScreen screen) {
        super();

        this.screen = screen;
    }

    // Screen override
    @NotNull
    @Override
    public Component getTitle() {
        return RenderUtil.getTextConverter().convert(screen.getTitle());
    }

    @Override
    public void onTick() {
        screen.tick();
    }

    @Override
    public void initScreen(int width, int height) {
        screen.init();

        ModVoiceClient.INSTANCE.getEventBus().unregister(
                ModVoiceClient.INSTANCE,
                this
        );
        ModVoiceClient.INSTANCE.getEventBus().register(
                ModVoiceClient.INSTANCE,
                this
        );
    }

    @Override
    public void onScreenClose() {
        ModVoiceClient.INSTANCE.getEventBus().unregister(
                ModVoiceClient.INSTANCE,
                this
        );

        screen.removed();
    }

    @Override
    public void onDrawScreen(@NotNull UMatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        screen.render(matrixStack, mouseX, mouseY, partialTicks);
    }

//    @Override
//    protected void updateNarratedWidget(@NotNull NarrationElementOutput narrationOutput) {
//        screen.updateNarratedWidget(new ModScreenNarrationOutput(narrationOutput));
//    }

    // ContainerEventHandler override
    @Override
    public void onMouseClicked(double mouseX, double mouseY, int mouseButton) {
        screen.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, int button) {
        screen.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onMouseDragged(double mouseX, double mouseY, int button, long timeSinceLastClick) {
        if (this.ignoreFirstMove) {
            this.lastDraggedMouseX = mouseX;
            this.lastDraggedMouseY = mouseY;
            this.ignoreFirstMove = false;
        }

        double deltaX = (mouseX - lastDraggedMouseX)
                * (double)UResolution.getScaledWidth() / (double)UResolution.getWindowWidth()
                * UResolution.getScaleFactor();
        double deltaY = (mouseY - lastDraggedMouseY)
                * (double)UResolution.getScaledHeight() / (double)UResolution.getWindowHeight()
                * UResolution.getScaleFactor();

//        System.out.println(UResolution.getScaleFactor());

        this.lastDraggedMouseX = mouseX;
        this.lastDraggedMouseY = mouseY;

//        System.out.println(deltaY);

        screen.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void onMouseScrolled(double delta) {
        screen.mouseScrolled(UMouse.Scaled.getX(), UMouse.Scaled.getY(), delta);
//        super.onMouseScrolled(delta);
    }

    //    @Override
//    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
//        return screen.mouseScrolled(mouseX, mouseY, delta);
//    }

    @Override
    public void onKeyPressed(int keyCode, char typedChar, @Nullable UKeyboard.Modifiers modifiers) {
        if (keyCode == 0) {
            screen.charTyped(typedChar, modifiers);
            return;
        }

        if (screen.keyPressed(keyCode, modifiers)) {
            return;
        }

        if (keyCode == UKeyboard.KEY_TAB) {
            boolean shiftKeyDown = UKeyboard.isShiftKeyDown();

            if (!screen.changeFocus(shiftKeyDown)) {
                screen.changeFocus(shiftKeyDown);
            }
        }
    }

    @Override
    public void onKeyReleased(int keyCode, char typedChar, @Nullable UKeyboard.Modifiers modifiers) {
        if (screen.keyReleased(keyCode, typedChar, modifiers)) {
            return;
        }

        super.onKeyReleased(keyCode, typedChar, modifiers);
    }

    // WAYTOODANK because I can't return true in onKeyPressed
    @EventSubscribe
    public void onWindowKeyPressed(@NotNull KeyPressedEvent event) {
        if (!event.getAction().equals(Hotkey.Action.UP)) return;

        if (shouldCloseOnEsc() &&
                event.getKey().equals(Hotkey.Type.KEYSYM.getOrCreate(UKeyboard.KEY_ESCAPE))
        ) {
            onClose();
        }
    }

    // MinecraftScreen impl
    @Override
    public void onDrawBackground(@NotNull UMatrixStack matrixStack, int tint) {
        super.onDrawBackground(matrixStack, tint);
    }

    public boolean shouldCloseOnEsc() {
        return screen.shouldCloseOnEsc();
    }

    public void onClose() {
        UMinecraft.getMinecraft().execute(() -> UScreen.displayScreen(null));
    }

    public void renderBackground(UMatrixStack stack) {
        super.onDrawBackground(stack, 0);
    }

    public void renderTooltip(UMatrixStack stack, List<McTextComponent> tooltip, int mouseX, int mouseY) {
        //#if MC>=12000
        //$$ ((Screen) this).setTooltipForNextRenderPass(
        //$$         Language.getInstance().getVisualOrder(
        //$$                 new ArrayList<>(
        //$$                         RenderUtil.getTextConverter().convert(tooltip)
        //$$                 )
        //$$         )
        //$$ );
        //#else
        ((Screen) this).renderComponentTooltip(
                stack.toMC(),
                new ArrayList<>(
                        RenderUtil.getTextConverter().convert(tooltip)
                ),
                mouseX,
                mouseY
        );
        //#endif
    }

//    @RequiredArgsConstructor
//    class ModScreenNarrationOutput implements NarrationOutput {
//
//        private final NarrationElementOutput narrationOutput;
//
//        @Override
//        public void add(@NotNull Type type, @NotNull MinecraftTextComponent component) {
//            narrationOutput.add(
//                    NarratedElementType.valueOf(type.name()),
//                    textConverter.convert(component)
//            );
//        }
//
//        @Override
//        public NarrationOutput nest() {
//            return new ModScreenNarrationOutput(narrationOutput.nest());
//        }
//    }
}
