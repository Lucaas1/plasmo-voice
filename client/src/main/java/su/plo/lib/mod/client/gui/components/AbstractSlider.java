package su.plo.lib.mod.client.gui.components;

import net.minecraft.util.Mth;
import su.plo.lib.mod.client.gui.widget.GuiWidgetTexture;
import su.plo.voice.universal.UGraphics;
import su.plo.voice.universal.UKeyboard;
import su.plo.voice.universal.UMatrixStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.render.RenderUtil;

public abstract class AbstractSlider extends GuiAbstractWidget {

    protected double value;
    protected boolean dragging;

    public AbstractSlider(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected @NotNull GuiWidgetTexture getButtonTexture(boolean hovered) {
        return GuiWidgetTexture.BUTTON_DISABLED;
    }

    @Override
    public void renderButton(@NotNull UMatrixStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack, mouseX, mouseY);
        renderTrack(stack, mouseX, mouseY);
        renderText(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBackground(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        int width = getSliderWidth();
        GuiWidgetTexture sprite = getButtonTexture(hovered);

        UGraphics.bindTexture(0, sprite.getLocation());
        UGraphics.color4f(1F, 1F, 1F, alpha);

        UGraphics.enableBlend();
        RenderUtil.defaultBlendFunc();
        UGraphics.enableDepth();

        RenderUtil.blitSprite(stack, sprite, x, y, 0, 0, width / 2, height);
        RenderUtil.blitSprite(stack, sprite, x + width / 2, y, sprite.getSpriteWidth() - width / 2, 0, width / 2, height);
        UGraphics.color4f(1F, 1F, 1F, 1F);
    }

    @Override
    protected boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y &&
                mouseX < x + getSliderWidth() - 1 &&
                mouseY < y + height;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (isHovered(mouseX, mouseY)) {
            this.dragging = true;
            setValueFromMouse(mouseX);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, @Nullable UKeyboard.Modifiers modifiers) {
        boolean rightPressed = keyCode == 263; // GLFW_KEY_RIGHT
        if (rightPressed || keyCode == 262) { // GLFW_KEY_LEFT
            float delta = rightPressed ? -1.0F : 1.0F;
            setValue(value + (double) (delta / (float) (getSliderWidth() - 8)));
        }

        return false;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (isHovered(mouseX, mouseY)) {
            setValueFromMouse(mouseX);
            this.dragging = true;
            super.onDrag(mouseX, mouseY, deltaX, deltaY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (dragging) {
            this.dragging = false;
            super.playDownSound();
        }
    }

    @Override
    protected void playDownSound() {
    }

    protected void renderTrack(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        GuiWidgetTexture sprite;
        if (isHoveredOrFocused()) {
            sprite = GuiWidgetTexture.BUTTON_ACTIVE;
        } else {
            sprite = GuiWidgetTexture.BUTTON_DEFAULT;
        }

        UGraphics.bindTexture(0, sprite.getLocation());
        UGraphics.color4f(1F, 1F, 1F, 1F);

        int x0 = x + (int) (value * (double) (getSliderWidth() - 8));
        RenderUtil.blitSprite(stack, sprite, x0, y, 0, 0, 4, 20);
        RenderUtil.blitSprite(stack, sprite, x0 + 4, y, sprite.getSpriteWidth() - 4, 0, 4, 20);
    }

    @Override
    protected void renderText(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        int textColor = active ? COLOR_WHITE : COLOR_GRAY;
        RenderUtil.drawCenteredString(
                stack,
                getText(),
                x + getSliderWidth() / 2,
                y + height / 2 - UGraphics.getFontHeight() / 2,
                textColor | ((int) Math.ceil(this.alpha * 255.0F)) << 24
        );
    }

    protected int getSliderWidth() {
        return width;
    }

    private void setValueFromMouse(double mouseX) {
        setValue((mouseX - (double) (x + 4)) / (double) (getSliderWidth() - 8));
    }

    private void setValue(double value) {
        double oldValue = this.value;
        this.value = Mth.clamp(value, 0.0, 1.0);
        if (oldValue != this.value) applyValue();

        updateText();
    }

    protected abstract void updateText();

    protected abstract void applyValue();
}
