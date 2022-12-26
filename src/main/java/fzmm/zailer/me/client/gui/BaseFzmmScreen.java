package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.ScreenTabContainer;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.image.ImageButtonWidget;
import fzmm.zailer.me.client.gui.components.row.*;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.config.ui.component.ConfigToggleButton;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public abstract class BaseFzmmScreen extends BaseUIModelScreen<FlowLayout> {
    @Nullable
    protected Screen parent;
    protected final String baseScreenTranslationKey;
    public static final int BUTTON_TEXT_PADDING = 8;
    public static final int COMPONENT_DISTANCE = 8;

    public BaseFzmmScreen(String screenPath, String baseScreenTranslationKey, @Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(new Identifier(FzmmClient.MOD_ID, screenPath)));
        this.baseScreenTranslationKey = baseScreenTranslationKey;
        this.parent = parent;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        assert this.client != null;
        ButtonComponent backButton = rootComponent.childById(ButtonComponent.class, "back-button");
        if (backButton != null)
            backButton.onPress(button -> this.client.setScreen(this.parent));

        this.setupButtonsCallbacks(rootComponent);
    }

    protected abstract void setupButtonsCallbacks(FlowLayout rootComponent);

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }

    public void selectScreenTab(FlowLayout rootComponent, IScreenTab selectedTab) {
        for (var tab : selectedTab.getClass().getEnumConstants()) {
            ScreenTabContainer screenTabContainer = rootComponent.childById(ScreenTabContainer.class, ScreenTabContainer.getScreenTabId(tab));
            ButtonWidget screenTabButton = rootComponent.childById(ButtonWidget.class, ScreenTabRow.getScreenTabButtonId(tab));

            if (screenTabContainer != null)
                screenTabContainer.setSelected(selectedTab == tab);

            if (screenTabButton != null)
                screenTabButton.active = tab != selectedTab;
        }
    }

    public String getBaseScreenTranslationKey() {
        return this.baseScreenTranslationKey;
    }

    static {
        UIParsing.registerFactory("boolean-row", BooleanRow::parse);
        UIParsing.registerFactory("button-row", ButtonRow::parse);
        UIParsing.registerFactory("color-row", ColorRow::parse);
        UIParsing.registerFactory("predicate-text-box-row", ConfigTextBoxRow::parse);
        UIParsing.registerFactory("enum-row", EnumRow::parse);
        UIParsing.registerFactory("image-rows", ImageRows::parse);
        UIParsing.registerFactory("number-row", NumberRow::parse);
        UIParsing.registerFactory("screen-tab-row", ScreenTabRow::parse);
        UIParsing.registerFactory("slider-row", SliderRow::parse);
        UIParsing.registerFactory("text-box-row", TextBoxRow::parse);

        // these are necessary in case you want to create the fields manually with XML
        UIParsing.registerFactory("toggle-button", element -> new ConfigToggleButton());
        UIParsing.registerFactory("number-slider", element -> new SliderWidget());
        UIParsing.registerFactory("text-option", element -> new ConfigTextBox());
        UIParsing.registerFactory("image-option", element -> new ImageButtonWidget());
        UIParsing.registerFactory("enum-option", element -> new EnumWidget());
        UIParsing.registerFactory("screen-tab", ScreenTabContainer::parse);
    }

    @Contract(value = "null, _, _ -> fail;", pure = true)
    public static void checkNull(Component component, String componentTagName, String id) {
        Objects.requireNonNull(component, String.format("No '%s' found with component id '%s'", componentTagName, id));
    }
}
