package fzmm.zailer.me.client.gui.textformat;

import fzmm.zailer.me.client.gui.textformat.tabs.TextFormatRainbowTab;
import fzmm.zailer.me.client.gui.textformat.tabs.TextFormatTwoColorsTab;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public enum TextFormatTabs implements ITextFormatTab {
    TWO_COLORS(new TextFormatTwoColorsTab()),
    RAINBOW(new TextFormatRainbowTab());

    private final ITextFormatTab tab;

    TextFormatTabs(ITextFormatTab tab) {
        this.tab = tab;
    }

    public String getId() {
        return this.tab.getId();
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.tab.setupComponents(rootComponent);
    }

    @Override
    public Text getText(TextFormatLogic logic) {
        return this.tab.getText(logic);
    }

    @Override
    public void setRandomValues() {
        this.tab.setRandomValues();
    }

    @Override
    public void componentsCallback(Consumer<Object> callback) {
        this.tab.componentsCallback(callback);
    }
}