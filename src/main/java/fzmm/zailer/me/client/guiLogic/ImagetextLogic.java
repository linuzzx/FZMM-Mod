package fzmm.zailer.me.client.guiLogic;

import com.google.gson.*;
import fi.dy.masa.malilib.util.Color4f;
import fzmm.zailer.me.config.Configs;
import fzmm.zailer.me.utils.*;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagetextLogic {
    private final BufferedImage image;
    private final String characters;
    private final boolean smoothRescaling;
    private NbtList imagetext;

    public ImagetextLogic(BufferedImage image, String characters, int width, int height, boolean smoothRescaling) {
        this.smoothRescaling = smoothRescaling;
        this.image = this.resizeImage(image, width, height);
        this.characters = characters.isEmpty() ? ImagetextLine.DEFAULT_TEXT : characters;
    }

    private void generateImagetext(boolean disableItalic) {
        NbtList tooltipList = new NbtList();
        int height = image.getHeight();
        int width = image.getWidth();

        for (int y = 0; y != height; y++) {
            ImagetextLine line = new ImagetextLine(this.characters);
            for (int x = 0; x != width; x++) {
                line.add(this.image.getRGB(x, y));
            }
            tooltipList.add(FzmmUtils.toNbtString(line.getLine(disableItalic), false));
        }
        imagetext = tooltipList;
    }

    private BufferedImage resizeImage(BufferedImage image, int width, int height) {
        Image tmp = image.getScaledInstance(width, height, smoothRescaling ? Image.SCALE_SMOOTH : Image.SCALE_REPLICATE);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

    /**
     *
     * @param width                     Width of which you want to preserve the aspect ratio.
     * @param height                    Height of which you want to preserve the aspect ratio.
     * @param unmodifiedSide            The unmodified side of the resized image.
     * @param unmodifiedSideIsWidth     If the variable newSide is width (true) otherwise it is height (false).
     * @return Vec2f.x = width, Vec2f.y = height
     * <p>
     */
    public static Vec2f changeResolutionKeepingAspectRatio(int width, int height, int unmodifiedSide, boolean unmodifiedSideIsWidth) {
        float modifiedSide = ((float) unmodifiedSide / (unmodifiedSideIsWidth ? width : height) * (unmodifiedSideIsWidth ? height : width));

        if (unmodifiedSideIsWidth)
            return new Vec2f(unmodifiedSide, modifiedSide);
        else
            return new Vec2f(modifiedSide, unmodifiedSide);
    }

    public void giveInLore(ItemStack stack, boolean add) {
        if (stack.isEmpty())
            stack = Configs.getConfigItem(Configs.Generic.DEFAULT_IMAGETEXT_ITEM).getDefaultStack();

        this.generateImagetext(true);
        this.addResolution();

        DisplayUtils display = new DisplayUtils(stack);
        if (add)
            display.addLore(this.imagetext).get();
        else
            display.setLore(this.imagetext).get();

        FzmmUtils.giveItem(display.get());
    }

    public void giveBookTooltip(String author, String bookText) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;
        BookUtils bookUtils = new BookUtils("Imagebook", author);
        JsonArray json = getImagetextJSON();

        bookUtils.addPage(new LiteralText(Formatting.BLUE + bookText)
                .setStyle(Style.EMPTY
                        .withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(json)))
        );

        FzmmUtils.giveItem(bookUtils.get());
//        assert book.getNbt() != null;

//        if (FzmmUtils.getNbtLength(book.getNbt()) > 32500) {
//			throw new Exception();
//		} else {
//        FzmmUtils.giveItem(book);
//		}
    }

    public void giveBookPage() {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;
        BookUtils bookUtils = new BookUtils("Imagebook", mc.player.getName().asString());
        bookUtils.addPage(Text.Serializer.fromJson(getImagetextJSON()));

        FzmmUtils.giveItem(bookUtils.get());
    }

    public void addBookPage() {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;
        BookUtils bookUtils = BookUtils.of(mc.player.getMainHandStack());
        if (bookUtils == null) {
            bookUtils = new BookUtils("Imagebook", mc.player.getName().asString());
        }

        bookUtils.addPage(Text.Serializer.fromJson(getImagetextJSON()));

        FzmmUtils.giveItem(bookUtils.get());
    }

    private void addResolution() {
        Text text = new LiteralText("Resolution: " + this.image.getWidth() + "x" + this.image.getHeight())
                .setStyle(Style.EMPTY.withColor(Configs.Colors.IMAGETEXT_MESSAGES.getColor().intValue));
        this.imagetext.add(FzmmUtils.toNbtString(text, true));
    }

    public void giveAsHologram(int x, float y, int z) {
        final float Y_DISTANCE = 0.23f;
        ItemStack hopper = Items.HOPPER.getDefaultStack();
        NbtCompound hopperBlockEntityTag = new NbtCompound();
        NbtList hopperItems = new NbtList();
        NbtList shulkerItems = new NbtList();
        Color4f color = Configs.Colors.IMAGETEXT_HOLOGRAM.getColor();

        this.generateImagetext(false);
        byte size = (byte) this.imagetext.size(),
            hopperIndex = 0;

        for (byte i = 0; i != size; i++) {
            y += Y_DISTANCE;
            ItemStack armorStandHologram = new ArmorStandUtils().setPos(x, y, z).setTags("ImagetextHologram")
                    .setAsHologram(this.imagetext.get(size - i - 1).asString()).getItem(String.valueOf(i));

            InventoryUtils.addSlot(shulkerItems, armorStandHologram, i % 27);
            if (i % 27 == 0 && i != 0) {
                addShulker(shulkerItems, hopperItems, hopperIndex);
                shulkerItems = new NbtList();
                hopperIndex++;
            }
        }
        if (shulkerItems.size() != 0) {
            addShulker(shulkerItems, hopperItems, hopperIndex);
        }

        hopperBlockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, hopperItems);
        hopper.setSubNbt(TagsConstant.BLOCK_ENTITY, hopperBlockEntityTag);
        hopper = new DisplayUtils(hopper).addLore(x + " " + y + " " + z, color).addLore("Imagetext: Hologram", color).get();

        FzmmUtils.giveItem(hopper);
    }

    private void addShulker(NbtList shulkerItems, NbtList hopperItems, byte hopperIndex) {
        NbtCompound shulkerBlockEntityTag = new NbtCompound();
        ItemStack shulker = new ItemStack(Items.LIGHT_BLUE_SHULKER_BOX);
        shulkerBlockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, shulkerItems);
        shulker.setSubNbt(TagsConstant.BLOCK_ENTITY, shulkerBlockEntityTag);

        InventoryUtils.addSlot(hopperItems, shulker, hopperIndex);
    }

    public JsonArray getImagetextJSON() {
        this.generateImagetext(false);

        JsonArray json = new JsonArray();

        for (NbtElement lineTag : this.imagetext) {
            String line = lineTag.asString().replaceAll(String.valueOf(Formatting.FORMATTING_CODE_PREFIX), "\\\\\\u00a7");
            JsonElement jsonLine = JsonParser.parseString(line);
            if (jsonLine instanceof JsonArray jsonArray) {
                int size = jsonArray.size() - 1;
                JsonElement lastElement = jsonArray.get(size);
                if (lastElement instanceof  JsonPrimitive jsonPrimitive && jsonPrimitive.isString()) {
                    jsonArray.set(size, new JsonPrimitive(jsonPrimitive.getAsString() + "\n"));
                } else if (lastElement instanceof JsonObject jsonObject && jsonObject.has("text")) {
                    jsonObject.addProperty("text", jsonObject.get("text").getAsString() + "\n");
                    jsonArray.set(size, jsonObject);
                }
                for (JsonElement element : jsonArray)
                    json.add(element);
            }
        }

        return json;
    }

    public String getImagetextString() {
        return this.getImagetextJSON().toString();
    }
}