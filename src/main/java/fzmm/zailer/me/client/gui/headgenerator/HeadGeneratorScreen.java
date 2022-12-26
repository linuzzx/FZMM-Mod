package fzmm.zailer.me.client.gui.headgenerator;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.image.ImageButtonWidget;
import fzmm.zailer.me.client.gui.components.image.mode.SkinMode;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.ImageButtonRow;
import fzmm.zailer.me.client.gui.components.row.ImageRows;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.headgenerator.components.HeadComponentEntry;
import fzmm.zailer.me.client.gui.headgenerator.components.HeadLayerComponentEntry;
import fzmm.zailer.me.client.logic.headGenerator.HeadData;
import fzmm.zailer.me.client.logic.headGenerator.HeadGenerator;
import fzmm.zailer.me.client.logic.headGenerator.HeadGeneratorResources;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.HeadUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HeadGeneratorScreen extends BaseFzmmScreen {
    private static final String HEAD_GENERATOR_WIKI_LINK = "https://github.com/Zailer43/FZMM-Mod/wiki/Head-Generator-Wiki";
    private static final Path SKIN_SAVE_FOLDER_PATH = Path.of(FabricLoader.getInstance().getGameDir().toString(), FzmmClient.MOD_ID, "skins");
    private static final String SKIN_ID = "skin";
    private static final String SKIN_SOURCE_TYPE_ID = "skinSourceType";
    private static final String HEAD_NAME_ID = "headName";
    private static final String SEARCH_ID = "search";
    private static final String HEAD_LIST_ID = "head-list";
    private static final String LAYER_LIST_ID = "layer-list";
    private static final String GIVE_MERGED_HEAD_ID = "give";
    private static final String SAVE_SKIN_ID = "save-skin";
    private static final String OPEN_SKIN_FOLDER_ID = "open-folder";
    private static final String TOGGLE_FAVORITE_LIST_ID = "toggle-favorite-list";
    private static final String WIKI_BUTTON_ID = "wiki-button";
    private static final Text SHOW_ALL_TEXT = Text.translatable("fzmm.gui.headGenerator.button.toggleFavoriteList.all");
    private static final Text SHOW_FAVORITES_TEXT = Text.translatable("fzmm.gui.headGenerator.button.toggleFavoriteList.favorite");
    private ImageButtonWidget skinButton;
    private TextFieldWidget headNameField;
    private TextFieldWidget searchField;
    private FlowLayout headListLayout;
    private FlowLayout layerListLayout;
    private ButtonWidget giveMergedHeadButton;
    private ButtonWidget toggleFavoriteList;
    private boolean showFavorites;
    private BufferedImage baseSkin;

    public HeadGeneratorScreen(@Nullable Screen parent) {
        super("head_generator", "headGenerator", parent);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        //general
        this.skinButton = ImageRows.setup(rootComponent, SKIN_ID, SKIN_SOURCE_TYPE_ID, SkinMode.NAME);
        this.skinButton.setButtonCallback(this::imageCallback);
        this.headNameField = TextBoxRow.setup(rootComponent, HEAD_NAME_ID, "");
        rootComponent.childById(TextFieldWidget.class, ImageButtonRow.getImageValueFieldId(SKIN_ID))
                .setChangedListener(this.headNameField::setText);
        this.searchField = TextBoxRow.setup(rootComponent, SEARCH_ID, "", s -> this.applyFilters());
        this.headListLayout = rootComponent.childById(FlowLayout.class, HEAD_LIST_ID);
        this.layerListLayout = rootComponent.childById(FlowLayout.class, LAYER_LIST_ID);
        checkNull(this.headListLayout, "flow-layout", HEAD_LIST_ID);
        checkNull(this.layerListLayout, "flow-layout", LAYER_LIST_ID);
        //bottom buttons
        this.giveMergedHeadButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId(GIVE_MERGED_HEAD_ID), true, button -> this.getMergedHead().ifPresent(this::giveHead));
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(SAVE_SKIN_ID), true, this::saveSkinExecute);
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(OPEN_SKIN_FOLDER_ID), true, button -> Util.getOperatingSystem().open(SKIN_SAVE_FOLDER_PATH.toFile()));
        //other buttons
        this.toggleFavoriteList =  ButtonRow.setup(rootComponent, TOGGLE_FAVORITE_LIST_ID, true, this::toggleFavoriteListExecute);
        checkNull(this.toggleFavoriteList, "button", TOGGLE_FAVORITE_LIST_ID);
        this.showFavorites = false;
        int toggleFavoriteListWidth = Math.max(this.textRenderer.getWidth(SHOW_ALL_TEXT), this.textRenderer.getWidth(SHOW_FAVORITES_TEXT)) + BUTTON_TEXT_PADDING;
        this.toggleFavoriteList.horizontalSizing(Sizing.fixed(toggleFavoriteListWidth));
        ButtonRow.setup(rootComponent, WIKI_BUTTON_ID, true, this::wikiExecute);

    }

    private void imageCallback(BufferedImage skinBase) {
        assert this.client != null;

        if (skinBase == null)
            return;
        this.baseSkin = skinBase;

        this.client.execute(() -> {
            this.tryFirstSkinLoad();
            this.updatePreviews();
        });
    }

    private void tryFirstSkinLoad() {
        if (this.headListLayout.children().isEmpty()) {
            Set<HeadData> headDataList = HeadGeneratorResources.loadHeads();
            List<Component> headEntries = headDataList.stream()
                    .sorted(Comparator.comparing(HeadData::displayName))
                    .map(headData -> (Component) new HeadComponentEntry(headData, this))
                    .toList();

            this.headListLayout.children(headEntries);
            this.applyFilters();
        }

        if (this.layerListLayout.children().isEmpty()) {
            BufferedImage emptyImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            HeadData baseSkinData = new HeadData(emptyImage, Text.translatable("fzmm.gui.headGenerator.label.baseSkin").getString(), "base");
            this.addLayer(baseSkinData, false);
        }

    }

    private void updatePreviews() {
        for (var component : this.headListLayout.children()) {
            if (component instanceof HeadComponentEntry headEntry)
                headEntry.update(this.baseSkin);
        }

        for (var component : this.layerListLayout.children()) {
            if (component instanceof HeadLayerComponentEntry layerEntry)
                layerEntry.update(this.baseSkin);
        }
    }

    private void applyFilters() {
        if (this.searchField == null)
            return;
        String searchValue = this.searchField.getText().toLowerCase();
        List<Component> componentList = this.headListLayout.children();

        for (var component : componentList) {
            if (component instanceof HeadComponentEntry headEntry)
                headEntry.filter(searchValue, this.showFavorites);
        }
    }

    public void giveHead(BufferedImage image) {
        MinecraftClient.getInstance().execute(() -> {
            try {
                this.setUndefinedDelay();
                String headName = this.getHeadName();
                if (headName == null)
                    headName = "NULL";

                HeadUtils headUtils = new HeadUtils().uploadHead(image, headName);
                int delay = (int) TimeUnit.MILLISECONDS.toSeconds(headUtils.getDelayForNextInMillis());
                ItemStack head = headUtils.getHead(headName);
                FzmmUtils.giveItem(head);
                this.setDelay(delay);
            } catch (IOException ignored) {
            }
        });
    }

    private Optional<BufferedImage> getMergedHead() {
        if (this.baseSkin == null)
            return Optional.empty();
        HeadGenerator headGenerator = new HeadGenerator(this.baseSkin);

        for (var entry : this.layerListLayout.children()) {
            // it's a flowlayout so there can be any type of component here
            if (!(entry instanceof HeadLayerComponentEntry layerEntry))
                continue;

            headGenerator.addTexture(layerEntry.getHeadSkin());
        }

        return Optional.of(headGenerator.getHeadTexture());
    }

    public void setUndefinedDelay() {
        Text waitMessage = Text.translatable("fzmm.gui.headGenerator.wait");
        this.updateButtons(this.getHeadEntries(), waitMessage, false);
    }

    private List<HeadComponentEntry> getHeadEntries() {
        return this.headListLayout.children().stream().map(component -> (HeadComponentEntry) component).toList();
    }

    public void setDelay(int seconds) {
        List<HeadComponentEntry> entryList = this.getHeadEntries();

        for (int i = 0; i != seconds; i++) {
            Text message = Text.translatable("fzmm.gui.headGenerator.wait_seconds", seconds - i);
            CompletableFuture.delayedExecutor(i, TimeUnit.SECONDS).execute(() -> this.updateButtons(entryList, message, false));
        }

        CompletableFuture.delayedExecutor(seconds, TimeUnit.SECONDS).execute(() -> this.updateButtons(entryList, HeadComponentEntry.GIVE_BUTTON_TEXT, true));
    }

    public void updateButtons(List<HeadComponentEntry> entryList, Text message, boolean active) {
        this.giveMergedHeadButton.active = active;
        this.giveMergedHeadButton.setMessage(message);
        for (var entry : entryList)
            entry.updateGiveButton(active, message);
    }

    public String getHeadName() {
        return this.headNameField.getText();
    }

    public void addLayer(HeadData headData) {
        this.addLayer(headData, true);
    }

    public void addLayer(HeadData headData, boolean active) {
        HeadLayerComponentEntry entry = new HeadLayerComponentEntry(headData, this.layerListLayout);
        entry.setEnabled(active);
        this.skinButton.getImage().ifPresent(entry::update);
        this.layerListLayout.child(entry);
    }

    public void saveSkinExecute(ButtonWidget button) {
        Optional<BufferedImage> optionalSkin = this.getMergedHead();
        ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
        if (optionalSkin.isEmpty()) {
            chatHud.addMessage(Text.translatable("fzmm.gui.headGenerator.saveSkin.thereIsNoSkin")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)));
            return;
        }

        BufferedImage skin = optionalSkin.get();
        this.addBody(skin);
        File file = SKIN_SAVE_FOLDER_PATH.toFile();
        if (file.mkdirs())
            FzmmClient.LOGGER.info("Skin save folder created");

        file = ScreenshotRecorder.getScreenshotFilename(file);
        try {
            ImageIO.write(skin, "png", file);
            chatHud.addMessage(Text.translatable("fzmm.gui.headGenerator.saveSkin.saved")
                    .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR)));
        } catch (IOException e) {
            e.printStackTrace();
            chatHud.addMessage(Text.translatable("fzmm.gui.headGenerator.saveSkin.saveError")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)));
        }
    }

    private void addBody(BufferedImage head) {
        this.skinButton.getImage().ifPresent(image -> {
                    Graphics2D g2d = head.createGraphics();
                    g2d.drawImage(image, 0, 16, 64, 64, 0, 16, 64, 64, null);
                    g2d.dispose();
                }
        );
    }

    public BufferedImage getBaseSkin() {
        return this.baseSkin;
    }

    private void toggleFavoriteListExecute(ButtonComponent buttonComponent) {
        this.showFavorites = !this.showFavorites;
        this.toggleFavoriteList.setMessage(this.showFavorites ? SHOW_FAVORITES_TEXT : SHOW_ALL_TEXT);
        this.applyFilters();
    }

    private void wikiExecute(ButtonWidget buttonWidget) {
        assert this.client != null;

        this.client.setScreen(new ConfirmLinkScreen(bool -> {
            if (bool)
                Util.getOperatingSystem().open(HEAD_GENERATOR_WIKI_LINK);

            this.client.setScreen(this);
        }, HEAD_GENERATOR_WIKI_LINK, true));
    }

    @Override
    public void close() {
        super.close();
        FzmmClient.CONFIG.save();
    }
}