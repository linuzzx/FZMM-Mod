package fzmm.zailer.me.client.logic.playerStatue.statueHeadSkin;

import fzmm.zailer.me.utils.position.PosI;

public record SkinPart(int x, int y, int hatX, int hatY) {

    // https://imgur.com/3LlJdua
    public static final SkinPart RIGHT_LEG = new SkinPart(16, 48, 0, 48);
    public static final SkinPart RIGHT_ARM = new SkinPart(32, 48, 48, 48);
    public static final SkinPart LEFT_LEG = new SkinPart(0, 16, 0, 32);
    public static final SkinPart LEFT_ARM = new SkinPart(40, 16, 40, 32);
    public static final SkinPart BODY = new SkinPart(16, 16, 16, 32);
    public static final SkinPart HEAD = new SkinPart(0, 0, 32, 0);

    public PosI getNormalLayer() {
        return new PosI(this.x, this.y);
    }

    public PosI getHatLayer() {
        return new PosI(this.hatX, this.hatY);
    }

}
