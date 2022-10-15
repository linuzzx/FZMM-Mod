package fzmm.zailer.me.client.gui.widgets.image.mode;

import fzmm.zailer.me.client.gui.widgets.image.source.IImageSource;
import fzmm.zailer.me.client.gui.widgets.image.source.ImageFileSource;
import fzmm.zailer.me.client.gui.widgets.image.source.ImagePlayerNameSource;
import net.minecraft.text.Text;

public enum SkinMode implements IImageMode {
    NAME("name", new ImagePlayerNameSource()),
    PATH("path", new ImageFileSource());

    private static final String BASE_TRANSLATION_KEY = "fzmm.gui.option.skin.";
    private final String translationKey;
    private final IImageSource sourceType;

    SkinMode(String translationKey, IImageSource sourceType) {
        this.translationKey = translationKey;
        this.sourceType = sourceType;
    }

    public Text getTranslation() {
        return Text.translatable(BASE_TRANSLATION_KEY + this.translationKey);
    }

    @Override
    public IImageSource getSourceType() {
        return this.sourceType;
    }
}
