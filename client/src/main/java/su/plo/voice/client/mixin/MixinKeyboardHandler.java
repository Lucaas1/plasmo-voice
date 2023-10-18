package su.plo.voice.client.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.client.ModVoiceClient;
import su.plo.voice.client.event.key.KeyPressedEvent;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(at = @At("RETURN"), method = "keyPress")
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (window != this.minecraft.getWindow().getWindow() || ModVoiceClient.INSTANCE == null) return;

        KeyPressedEvent event = new KeyPressedEvent(
                Hotkey.Type.KEYSYM.getOrCreate(key),
                Hotkey.Action.fromInt(action)
        );

        ModVoiceClient.INSTANCE.getEventBus().fire(event);
    }
}
