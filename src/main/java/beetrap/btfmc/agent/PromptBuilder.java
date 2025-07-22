package beetrap.btfmc.agent;

public class PromptBuilder {
    private StringBuilder baseInstruction;
    private StringBuilder stateInstruction;
    private StringBuilder contextInstruction;

    public PromptBuilder() {
        this.baseInstruction = new StringBuilder();
        this.stateInstruction = new StringBuilder();
        this.contextInstruction = new StringBuilder();
    }

    public StringBuilder baseInstruction() {
        return this.baseInstruction;
    }

    public StringBuilder stateInstruction() {
        return this.stateInstruction;
    }

    public StringBuilder contextInstruction() {
        return this.contextInstruction;
    }

    public String build() {
        return this.baseInstruction
                + System.lineSeparator()
                + this.stateInstruction
                + System.lineSeparator()
                + this.contextInstruction
                + System.lineSeparator();
    }
}
