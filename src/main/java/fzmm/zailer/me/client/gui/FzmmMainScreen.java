package fzmm.zailer.me.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import static fzmm.zailer.me.client.gui.ScreenConstants.NORMAL_BUTTON_HEIGHT;

public class FzmmMainScreen extends Screen {
	protected ButtonWidget closeButton;
	protected ButtonWidget imagetextButton;
	protected FzmmMainScreen() {
		super(NarratorManager.EMPTY);
	}

	protected void init() {
		assert this.client != null;
		this.closeButton = this.addButton(new ButtonWidget(this.width - 120, this.height - 40, 100, NORMAL_BUTTON_HEIGHT, new LiteralText("Salir"),
			(buttonWidget) -> this.client.openScreen((Screen)null)
		));
		this.imagetextButton = this.addButton(new ButtonWidget( 120,40, 120, NORMAL_BUTTON_HEIGHT, new LiteralText("Imagetext"),
			(buttonWidget) -> this.client.openScreen(new ImagetextScreen())
		));
	}

	public void resize(MinecraftClient client, int width, int height) {
		this.init(client, width, height);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}
}
