package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.config.FzmmConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class ImagetextScreen extends FzmmBaseScreen {
	private ButtonWidget executeButton,
		modeButton,
		howGetImageButton,
		loadImageButton;
	private TextFieldWidget imageTextField;
	private NumberFieldWidget widthNumberField;
	protected static TextFieldWidget pixelTextField,
		bookAuthorTextField,
		bookMessageTextField;
	protected static CheckboxWidget showResolutionCheckbox;
	protected static boolean errorImage,
		bookNbtTooLong;
	private boolean loadedImage;
	protected static TranslatableText errorImageMessage;
	private TranslatableText howGetImageMessage;
	private ImagetextMode mode;
	private HowGetImageFile howGetImage;
	private BufferedImage image;

	protected ImagetextScreen() {
		super(new TranslatableText("imagetext.title"));
	}

	protected void init() {
		super.init();
		assert this.client != null;
		assert this.client.player != null;

		this.mode = ImagetextMode.ADD_LORE;
		this.howGetImage = HowGetImageFile.URL;
		this.image = null;
		this.loadedImage = false;
		errorImage = false;
		bookNbtTooLong = false;

		this.executeButton = this.addDrawableChild(new ButtonWidget(20, this.height - 40, 100, NORMAL_BUTTON_HEIGHT, new TranslatableText("gui.execute"),
			(buttonWidget) -> this.execute()
		));

		this.imageTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE1, 300, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("imagetext.imageUrl"));
		this.imageTextField.setMaxLength(1024);
		this.imageTextField.setChangedListener(this::imagetextListener);
		this.setInitialFocus(this.imageTextField);

		pixelTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE2, 20, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("imagetext.pixelChar"));
		pixelTextField.setMaxLength(1);
		pixelTextField.setText("█");

		bookAuthorTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 126, LINE2, 100, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("imagetext.author"));
		bookAuthorTextField.setMaxLength(100);
		bookAuthorTextField.setText(this.client.player.getName().getString());

		this.widthNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 - 22, LINE2, 36, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("imagetext.imageWidth"), 2, 127);
		this.widthNumberField.setMaxLength(3);
		this.widthNumberField.setText("42");
		this.widthNumberField.setChangedListener(this::widthNumberListener);

		bookMessageTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE3, 300, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("book.message"));
		bookMessageTextField.setMaxLength(256);
		bookMessageTextField.setText(AutoConfig.getConfigHolder(FzmmConfig.class).getConfig().imagetext.defaultBookMessage);

		this.howGetImageButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 150, LINE4 - 10, NORMAL_BUTTON_WIDTH - 3, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.url"), (buttonWidget) -> {
			this.cycleHowGetImage();
			this.updateHowGetImage();
		}));
		this.loadImageButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 49, LINE4 - 10, NORMAL_BUTTON_WIDTH - 2, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.loadImage"),
			(buttonWidget) -> this.loadImage()
		));
		this.modeButton = this.addDrawableChild(new ButtonWidget(this.width / 2 + 53, LINE4 - 10, NORMAL_BUTTON_WIDTH - 3, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.addToLore"), (buttonWidget) -> {
			this.cycleMode();
			this.updateMode();
		}));

		showResolutionCheckbox = this.addDrawableChild(new CheckboxWidget(this.width / 2 - 150, LINE4 + 15, 20, 20, new TranslatableText("imagetext.showResolution"), true));

		this.addSelectableChild(this.widthNumberField);
		this.addSelectableChild(pixelTextField);
		this.addSelectableChild(pixelTextField);
		this.addSelectableChild(this.imageTextField);
		this.addSelectableChild(bookAuthorTextField);
		this.addSelectableChild(bookMessageTextField);

		this.executeButton.active = false;
		this.updateHowGetImage();
		this.updateMode();
	}

	public void resize(MinecraftClient client, int width, int height) {
		String imageTextField2 = this.imageTextField.getText(),
			widthTextField2 = this.widthNumberField.getText(),
			pixelTextField2 = pixelTextField.getText(),
			bookAuthorTextField2 = bookAuthorTextField.getText(),
			bookMessageTextField2 = bookMessageTextField.getText();
		ImagetextMode mode2 = this.mode;
		HowGetImageFile howGetImage2 = this.howGetImage;
		boolean loadedImage2 = this.loadedImage,
			errorImage2 = errorImage,
			bookNbtTooLong2 = bookNbtTooLong,
			showResolution2 = showResolutionCheckbox.isChecked();
		BufferedImage image2 = this.image;

		this.init(client, width, height);

		this.imageTextField.setText(imageTextField2);
		this.widthNumberField.setText(widthTextField2);
		pixelTextField.setText(pixelTextField2);
		bookAuthorTextField.setText(bookAuthorTextField2);
		bookMessageTextField.setText(bookMessageTextField2);
		this.mode = mode2;
		this.howGetImage = howGetImage2;
		this.updateMode();
		this.updateHowGetImage();
		this.loadedImage = loadedImage2;
		errorImage = errorImage2;
		bookNbtTooLong = bookNbtTooLong2;
		this.image = image2;
		if (showResolutionCheckbox.isChecked() != showResolution2)
			showResolutionCheckbox.onPress();
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, this.howGetImageMessage, this.width / 2, LINE1 - 10, TEXT_COLOR);
		this.imageTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("imagetext.pixelChar"), this.width / 2 - 140, LINE2 - 10, TEXT_COLOR);
		pixelTextField.render(matrices, mouseX, mouseY, delta);

		if (this.mode == ImagetextMode.GIVE_BOOK) {
			drawCenteredText(matrices, this.textRenderer, new TranslatableText("book.author"), this.width / 2 - 76, LINE2 - 10, TEXT_COLOR);
			bookAuthorTextField.render(matrices, mouseX, mouseY, delta);
			drawCenteredText(matrices, this.textRenderer, new TranslatableText("book.message"), this.width / 2, LINE3 - 10, TEXT_COLOR);
			bookMessageTextField.render(matrices, mouseX, mouseY, delta);
		}

		if (this.loadedImage && this.image != null) {
			if (!bookNbtTooLong)
				drawCenteredText(matrices, this.textRenderer, new TranslatableText("imagetext.loadedImage"), this.width / 2, LINE5 - 10, TEXT_COLOR);
			drawCenteredText(matrices, this.textRenderer, new TranslatableText("imagetext.imageWidth"), this.width / 2 - 4, LINE2 - 10, TEXT_COLOR);
			widthNumberField.render(matrices, mouseX, mouseY, delta);
			int imageHeight = Math.round(((float) this.widthNumberField.getNumber() / this.image.getWidth()) * this.image.getHeight());
			drawCenteredText(matrices, this.textRenderer, new TranslatableText("imagetext.imageHeight", imageHeight), this.width / 2 + 60, LINE2, TEXT_COLOR);
			this.executeButton.active = true;
		}

		if (bookNbtTooLong) {
			drawCenteredText(matrices, this.textRenderer, new TranslatableText("imagetext.error.bookNbtTooLong"), this.width / 2, LINE5 - 10, TEXT_ERROR_COLOR);
			this.executeButton.active = false;
		} else if (errorImage) {
			drawCenteredText(matrices, this.textRenderer, errorImageMessage, this.width / 2, LINE5 - 10, TEXT_ERROR_COLOR);
			this.loadImageButton.active = false;
			this.executeButton.active = false;
		} else {
			this.loadImageButton.active = true;
		}
	}

	public void execute() {
		int width = this.widthNumberField.getNumber();

		pixelTextField.setText(pixelTextField.getText().isEmpty() ? "█" : pixelTextField.getText());
		switch (this.mode) {
			case ADD_LORE -> ImagetextLogic.addLoreImagetext(image, width);
			case GIVE_BOOK -> ImagetextLogic.giveBookImagetext(image, width, bookAuthorTextField.getText(), bookMessageTextField.getText());
		}
	}

	public void cycleMode() {
		switch (this.mode) {
			case ADD_LORE -> this.mode = ImagetextMode.GIVE_BOOK;
			case GIVE_BOOK -> this.mode = ImagetextMode.ADD_LORE;
		}
	}

	public void updateMode() {
		switch (this.mode) {
			case ADD_LORE -> this.modeButton.setMessage(new TranslatableText("imagetext.addToLore"));
			case GIVE_BOOK -> this.modeButton.setMessage(new TranslatableText("imagetext.getInABook"));
		}
	}

	public void cycleHowGetImage() {
		switch (this.howGetImage) {
			case URL -> this.howGetImage = HowGetImageFile.FROM_THIS_PC;
			case FROM_THIS_PC -> this.howGetImage = HowGetImageFile.URL;
		}
	}

	public void updateHowGetImage() {
		switch (this.howGetImage) {
			case URL -> {
				this.howGetImageButton.setMessage(new TranslatableText("imagetext.url"));
				this.howGetImageMessage = new TranslatableText("imagetext.howGetImage.url");
				this.imagetextListener(this.imageTextField.getText());
			}
			case FROM_THIS_PC -> {
				this.howGetImageButton.setMessage(new TranslatableText("imagetext.fromThisPc"));
				this.howGetImageMessage = new TranslatableText("imagetext.howGetImage.fromThisPc");
				this.imagetextListener(this.imageTextField.getText());
			}
		}
	}

	private enum ImagetextMode {
		ADD_LORE,
		GIVE_BOOK;

		ImagetextMode() {
		}
	}

	private enum HowGetImageFile {
		URL,
		FROM_THIS_PC;

		HowGetImageFile() {
		}
	}

	private void widthNumberListener(String text) {
		if (bookNbtTooLong) {
			bookNbtTooLong = false;
		}
	}

	private void imagetextListener(String text) {
		//TODO: añadir sugerencias para cuando se busca archivo de la pc
		if (this.howGetImage == HowGetImageFile.FROM_THIS_PC) {
			MinecraftClient mc = MinecraftClient.getInstance();
			assert mc.player != null;

			try {
				if (Files.exists(Paths.get(text)))
					errorImage = false;
				else {
					errorImage = true;
					errorImageMessage = new TranslatableText("imagetext.error.pathNotExists");
				}
			} catch (InvalidPathException e) {
				errorImage = true;
				errorImageMessage = new TranslatableText("imagetext.error.invalidPath");
			}
		} else
			errorImage = false;
	}

	private void loadImage() {
		String fileLocate = this.imageTextField.getText();

		switch (this.howGetImage) {
			case URL:
				this.image = ImagetextLogic.getImageFromUrl(fileLocate);
				if (this.image == null) {
					errorImage = true;
					errorImageMessage = new TranslatableText("imagetext.error.imageNotFound");
					this.loadedImage = false;
				}
				break;
			case FROM_THIS_PC:
				try {
					this.image = ImagetextLogic.getImageFromPc(fileLocate);
				} catch (IOException e) {
					e.printStackTrace();
					errorImage = true;
					errorImageMessage = new TranslatableText("imagetext.error.unexpectedGettingImage");
					this.loadedImage = false;
				}
				break;
		}
		this.loadedImage = true;
	}

}