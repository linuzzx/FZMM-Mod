package fzmm.zailer.me.client.gui.enums.options;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.gui.widgets.IMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public enum ImagetextBookOption implements IMode {
    CREATE_BOOK("createBook", () -> BookBuilder.builder().title(Text.translatable("imagetext.book.title").getString())),
    ADD_PAGE("addPage", () -> {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        BookBuilder builder = BookBuilder.of(client.player.getMainHandStack());

        return builder == null ? CREATE_BOOK.bookBuilderSupplier.get() : builder;
    });


    private final String name;
    private final Supplier<BookBuilder> bookBuilderSupplier;

    ImagetextBookOption(String name, Supplier<BookBuilder> getBookSupplier) {
        this.name = name;
        this.bookBuilderSupplier = getBookSupplier;
    }

    @Override
    public Text getTranslation() {
        return Text.translatable("fzmm.gui.option.book." + this.name);
    }

    public BookBuilder getBookBuilder() {
        return this.bookBuilderSupplier.get();
    }
}