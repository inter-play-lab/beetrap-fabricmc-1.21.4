package beetrap.btfmc.screen;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import beetrap.btfmc.networking.MultipleChoiceSelectionResultC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class MultipleChoiceScreen extends Screen {
    private final ScreenQueue sq;
    private final Screen parent;
    private final String questionId;
    private final String question;
    private final String[] choices;
    private TextWidget questionWidget;
    private ButtonWidget[] choiceWidgets;
    
    public MultipleChoiceScreen(ScreenQueue sq, String questionId, String question, String... choices) {
        super(Text.literal("Multiple Choice: " + question));
        this.sq = sq;
        if(this.client == null) {
            this.client = MinecraftClient.getInstance();
        }

        this.parent = this.client.currentScreen;
        this.questionId = questionId;
        this.question = question;
        this.choices = new String[choices.length];
        System.arraycopy(choices, 0, this.choices, 0, this.choices.length);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW_KEY_ESCAPE) {
            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void init() {
        this.questionWidget = new TextWidget(Text.literal(this.question), this.textRenderer);
        this.questionWidget.setPosition(
                (this.width - this.questionWidget.getWidth()) / 2,
                (int)((double)(this.height - this.questionWidget.getHeight()) / 2 * 0.35)
        );
        this.addDrawableChild(this.questionWidget);
        
        this.choiceWidgets = new ButtonWidget[this.choices.length];
        for(int i = 0; i < this.choiceWidgets.length; ++i) {
            String choice = this.choices[i];
            int finalI = i;
            ButtonWidget choiceWidget = ButtonWidget.builder(Text.literal(choice),
                    button -> {
                        ClientPlayNetworking.send(new MultipleChoiceSelectionResultC2SPayload(this.questionId,
                                finalI));
                        this.close();
                    }).build();
            choiceWidget.setWidth(300);
            choiceWidget.setPosition(
                    (this.width - choiceWidget.getWidth()) / 2,
                    (int)((double)(this.height - choiceWidget.getHeight()) / 2 * (0.55 + i * 0.20))
            );
            this.addDrawableChild(choiceWidget);

            this.choiceWidgets[i] = choiceWidget;
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
        this.sq.setActive(false);
    }
}
