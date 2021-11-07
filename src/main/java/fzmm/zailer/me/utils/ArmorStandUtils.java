package fzmm.zailer.me.utils;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class ArmorStandUtils {

    private final NbtCompound entityTag;

    public ArmorStandUtils() {
        this.entityTag = new NbtCompound();
    }

    public NbtCompound getItemNbt(@Nullable String itemName) {
        NbtCompound tag = new NbtCompound();

        if (itemName != null) {
            NbtCompound display = new NbtCompound();
            display.put(ItemStack.NAME_KEY, FzmmUtils.stringToNbtString(itemName, true));
            tag.put(ItemStack.DISPLAY_KEY, display);
        }

        tag.put(EntityType.ENTITY_TAG_KEY, entityTag);
        return tag;
    }

    public ItemStack getItem(@Nullable String itemName) {
        ItemStack armorStand = new ItemStack(Items.ARMOR_STAND);
        armorStand.setNbt(this.getItemNbt(itemName));
        return armorStand;
    }

    public ArmorStandUtils setAsHologram(String name) {
        this.setImmutableAndInvisible();
        this.entityTag.putString("CustomName", name);
        this.entityTag.putBoolean("CustomNameVisible", true);
        return this;
    }

    public ArmorStandUtils setImmutableAndInvisible() {
        this.entityTag.putInt("DisabledSlots", 4144959);
        this.entityTag.putBoolean("NoGravity", true);
        this.entityTag.putBoolean("Invisible", true);
        return this;
    }

    public ArmorStandUtils setPos(double x, double y, double z) {
        NbtList coordinates = new NbtList();
        coordinates.add(NbtDouble.of(x));
        coordinates.add(NbtDouble.of(y));
        coordinates.add(NbtDouble.of(z));
        this.setPos(coordinates);
        return this;
    }

    public ArmorStandUtils setPos(NbtList coordinates) {
        this.entityTag.put("Pos", coordinates);
        return this;
    }

    public ArmorStandUtils setRightHandItem(ItemStack stack) {
        NbtList handItem = new NbtList();
        NbtCompound itemTag = InventoryUtils.stackToTag(stack);
        handItem.add(itemTag);

        this.entityTag.put("HandItems", handItem);
        return this;
    }

    public ArmorStandUtils setTags(String... tagsList) {
        NbtList tags = new NbtList();

        for (String tag : tagsList) {
            tags.add(NbtString.of(tag));
        }

        this.entityTag.put("Tags", tags);
        return this;
    }

    public ArmorStandUtils setShowArms() {
        this.entityTag.putBoolean("ShowArms", true);
        return this;
    }

    public ArmorStandUtils setSmall() {
        this.entityTag.putBoolean("Small", true);
        return this;
    }
}