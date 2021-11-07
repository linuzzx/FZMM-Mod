package fzmm.zailer.me.client.gui.imagetext;

import com.google.gson.*;
import fzmm.zailer.me.utils.ArmorStandUtils;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.InventoryUtils;
import fzmm.zailer.me.utils.LoreUtils;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagetextLogic {
    private final BufferedImage image;
    private final String characters;
    private final boolean smoothRescaling;
    private NbtList imagetext;

    public ImagetextLogic(BufferedImage image, String characters, byte width, byte height, boolean smoothRescaling) {
        this.smoothRescaling = smoothRescaling;
        this.image = this.resizeImage(image, width, height);
        this.characters = characters.isEmpty() ? ImagetextLine.DEFAULT_TEXT : characters;
    }

    private void generateImagetext(boolean disableItalic) {
        NbtList tooltipList = new NbtList();
        int height = image.getHeight(),
                width = image.getWidth();
        Style style = disableItalic ? Style.EMPTY.withItalic(false) : Style.EMPTY;

        for (int y = 0; y != height; y++) {
            ImagetextLine line = new ImagetextLine(this.characters, style);
            for (int x = 0; x != width; x++) {
                line.add(this.image.getRGB(x, y));
            }
            tooltipList.add(NbtString.of(String.valueOf(line.getLine())));
        }
        imagetext = tooltipList;
    }

    private BufferedImage resizeImage(BufferedImage img, byte width, byte height) {
        Image tmp = img.getScaledInstance(width, height, smoothRescaling ? Image.SCALE_SMOOTH : Image.SCALE_REPLICATE);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

    public void setToLore(ItemStack stack) {
        if (stack.isEmpty())
            stack = Items.PAPER.getDefaultStack();

        this.generateImagetext(true);
        addResolution();

        LoreUtils.setLore(stack, this.imagetext);

        FzmmUtils.giveItem(stack);
    }

    public void addToLore(ItemStack stack) {
        if (stack.isEmpty())
            stack = Items.PAPER.getDefaultStack();

        this.generateImagetext(true);
        addResolution();

        LoreUtils.addLoreList(stack, this.imagetext);

        FzmmUtils.giveItem(stack);
    }

    public void giveBook(String author, String bookText) throws Exception {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;
        ItemStack book = Items.WRITTEN_BOOK.getDefaultStack();
        NbtCompound tag = new NbtCompound();
        NbtList pages = new NbtList();
        JsonParser parser = new JsonParser();

        JsonElement jsonMessage = parser.parse(getImagetextJSON());

        pages.add(FzmmUtils.textToNbtString(
                new LiteralText(Formatting.BLUE + bookText)
                        .setStyle(Style.EMPTY
                                .withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(jsonMessage))), false
        ));

        tag.putString(WrittenBookItem.TITLE_KEY, "Imagebook");
        tag.putString(WrittenBookItem.AUTHOR_KEY, author);
        tag.put(WrittenBookItem.PAGES_KEY, pages);

        book.setNbt(tag);
		assert book.getNbt() != null;
		if (book.getNbt().toString().length() > 32500) {
			throw new Exception();
		} else {
            FzmmUtils.giveItem(book);
		}
    }

    private void addResolution() {
        LoreUtils.addLoreToList(this.imagetext, "Resolution: " + this.image.getWidth() + "x" + this.image.getHeight(), 7455391);
    }

    public void giveAsHologram(int x, float y, int z) {
        final float Y_DISTANCE = 0.23f;
        ItemStack hopper = Items.HOPPER.getDefaultStack();
        NbtCompound hopperBlockEntityTag = new NbtCompound(),
                display = new NbtCompound();
        NbtList hopperItems = new NbtList(),
                shulkerItems = new NbtList(),
                lore = new NbtList();

        this.generateImagetext(false);
        byte size = (byte) this.imagetext.size(),
            hopperIndex = 0;

        LoreUtils.addLoreToList(lore, x + " " + y + " " + z, 0x796957);
        LoreUtils.addLoreToList(lore, "Imagetext: Hologram", 0x796957);

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

        display.put(ItemStack.LORE_KEY, lore);
        hopperBlockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, hopperItems);
        hopper.setSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY, hopperBlockEntityTag);
        hopper.setSubNbt(ItemStack.DISPLAY_KEY, display);

        FzmmUtils.giveItem(hopper);
    }

    private void addShulker(NbtList shulkerItems, NbtList hopperItems, byte hopperIndex) {
        NbtCompound shulkerBlockEntityTag = new NbtCompound();
        ItemStack shulker = new ItemStack(Items.LIGHT_BLUE_SHULKER_BOX);
        shulkerBlockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, shulkerItems);
        shulker.setSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY, shulkerBlockEntityTag);

        InventoryUtils.addSlot(hopperItems, shulker, hopperIndex);
    }

    public String getImagetextJSON() {
        this.generateImagetext(false);

        JsonParser parser = new JsonParser();
        JsonArray json = new JsonArray();

        for (NbtElement lineTag : this.imagetext) {
            String line = lineTag.asString().replaceAll(String.valueOf(Formatting.field_33292), "\\\\\\u00a7");
            JsonElement jsonLine = parser.parse(line);
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

        return json.toString();
    }
}