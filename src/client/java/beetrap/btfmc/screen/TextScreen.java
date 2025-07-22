package beetrap.btfmc.screen;

import static beetrap.btfmc.BeetrapfabricmcClient.beetrapLog;
import static beetrap.btfmc.networking.BeetrapLogS2CPayload.BEETRAP_LOG_ID_TEXT_SCREEN_CONFIRMATION_BUTTON_PRESSED;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TextScreen extends Screen {

    private static final int TEXT_WIDGET_WIDTH = 800;
    private static final int TEXT_WIDGET_HEIGHT = 12;
    private static final int IMAGE_WIDTH = 200;
    private static final int IMAGE_HEIGHT = 200;
    private final ScreenQueue tss;
    private final Screen parent;
    private final List<TextWidget> linesOfText;
    private ButtonWidget confirmation;
    private final String text;
    private Identifier imageId;
    private boolean hasImage;

    public TextScreen(ScreenQueue tss, String s) {
        super(Text.of(s));
        this.tss = tss;
        if(this.client == null) {
            this.client = MinecraftClient.getInstance();
        }

        this.parent = this.client.currentScreen;
        this.linesOfText = new ArrayList<>();
        this.text = s;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW_KEY_ESCAPE) {
            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private Text orderedTextToText(OrderedText ot) {
        StringBuilder sb = new StringBuilder();

        ot.accept((index, style, codePoint) -> {
            sb.append((char)codePoint);
            return true;
        });

        return Text.literal(sb.toString());
    }

    @Override
    protected void init() {
        this.tss.setActive(true);
        List<OrderedText> ot = this.textRenderer.wrapLines(
                StringVisitable.plain(this.title.getString()), TEXT_WIDGET_WIDTH);
        int a = 0;
        for(OrderedText text : ot) {
            TextWidget tw = new TextWidget(TEXT_WIDGET_WIDTH, TEXT_WIDGET_HEIGHT,
                    this.orderedTextToText(text), this.textRenderer);
            this.linesOfText.add(tw);
            tw.setPosition(
                    (this.width - tw.getWidth()) / 2,
                    (int)((double)(this.height - tw.getHeight()) / 2 * 0.4 + a)
            );

            this.addDrawableChild(tw);

            a = a + TEXT_WIDGET_HEIGHT + 2;
        }

        this.confirmation = ButtonWidget.builder(Text.of("Confirm"),
                button -> {
                    this.close();
                    beetrapLog(BEETRAP_LOG_ID_TEXT_SCREEN_CONFIRMATION_BUTTON_PRESSED, "");
                }).build();
        this.confirmation.setWidth(200);
        this.confirmation.setPosition(
                (this.width - this.confirmation.getWidth()) / 2,
                (int)((double)(this.height - this.confirmation.getHeight()) / 2)
        );
        this.addDrawableChild(this.confirmation);
    }

    @Override
    public void tick() {

    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
        this.tss.setActive(false);
    }

    @Override
    public String toString() {
        return "TextScreen{" +
                "text='" + text + '\'' +
                '}';
    }
}
