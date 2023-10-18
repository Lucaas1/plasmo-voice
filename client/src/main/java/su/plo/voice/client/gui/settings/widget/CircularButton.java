package su.plo.voice.client.gui.settings.widget;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.slib.api.chat.component.McTextComponent;

import java.util.List;

public final class CircularButton extends Button implements UpdatableWidget {

    private final List<McTextComponent> values;
    private final @Nullable UpdateAction updateAction;
    @Setter
    private int index;

    public CircularButton(
            @NotNull List<McTextComponent> values,
            int index,
            int x,
            int y,
            int width,
            int height,
            @Nullable UpdateAction updateAction,
            @NotNull OnTooltip tooltipAction
    ) {
        super(x, y, width, height, McTextComponent.empty(), NO_ACTION, tooltipAction);

        this.values = values;
        this.index = index;
        this.updateAction = updateAction;
        updateValue();
    }

    @Override
    public void updateValue() {
        setText(values.get(index));
    }

    @Override
    public void onPress() {
        super.onPress();

        this.index = (index + 1) % values.size();
        updateValue();
        if (updateAction != null) updateAction.onUpdate(index);
    }

    public interface UpdateAction {

        void onUpdate(int index);
    }
}
