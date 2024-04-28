package su.plo.voice.client.gui.settings;

import su.plo.slib.api.chat.component.McTextComponent;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UKeyboard;
import gg.essential.universal.UMatrixStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.screen.GuiScreen;
import su.plo.lib.mod.client.gui.screen.ScreenWrapper;
import su.plo.lib.mod.client.render.RenderUtil;

public abstract class HudPositionScreen<E extends Enum<E>> extends GuiScreen {

    protected static final int BUTTON_OFFSET = 25;
    protected static final int BUTTON_WIDTH = 100;

    protected final VoiceSettingsScreen parent;
    protected final EnumConfigEntry<E> entry;
    protected final McTextComponent chooseText;

    public HudPositionScreen(
            @NotNull VoiceSettingsScreen parent,
            @NotNull EnumConfigEntry<E> entry,
            @NotNull McTextComponent chooseText
    ) {
        this.parent = parent;
        this.entry = entry;
        this.chooseText = chooseText;
    }

    @Override
    public void render(@NotNull UMatrixStack stack, int mouseX, int mouseY, float delta) {
        screen.renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);

        RenderUtil.drawString(
                stack,
                chooseText,
                getWidth() / 2 - RenderUtil.getTextWidth(chooseText) / 2,
                getHeight() / 2 - UGraphics.getFontHeight(),
                16777215
        );
    }

    @Override
    public boolean keyPressed(int keyCode, UKeyboard.@Nullable Modifiers modifiers) {
        if (keyCode == UKeyboard.KEY_ESCAPE) {
            parent.setPreventEscClose(true);
            ScreenWrapper.openScreen(parent);
            return true;
        }

        return super.keyPressed(keyCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected abstract Button createPositionButton(int x, int y, E iconPosition);

}
