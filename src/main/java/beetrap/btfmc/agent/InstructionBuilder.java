package beetrap.btfmc.agent;

public class InstructionBuilder {
    private final StringBuilder baseInstructionBuilder;
    private final StringBuilder stateInstructionBuilder;
    private final StringBuilder contextInstructionBuilder;

    public InstructionBuilder() {
        this.baseInstructionBuilder = new StringBuilder();
        this.stateInstructionBuilder = new StringBuilder();
        this.contextInstructionBuilder = new StringBuilder();
    }

    public StringBuilder baseInstructionBuilder() {
        return this.baseInstructionBuilder;
    }

    public StringBuilder stateInstructionBuilder() {
        return this.stateInstructionBuilder;
    }

    public void resetStateInstructionBuilder() {
        this.stateInstructionBuilder.delete(0, this.stateInstructionBuilder.length());
    }

    public StringBuilder contextInstructionBuilder() {
        return this.contextInstructionBuilder;
    }

    public void resetContextInstructionBuilder() {
        this.contextInstructionBuilder.delete(0, this.contextInstructionBuilder.length());
    }

    private void addInstructionSection(StringBuilder sb, String sectionHeading, StringBuilder instruction, String separator) {
        sb.append(sectionHeading)
                .append(System.lineSeparator())
                .append(instruction)
                .append(System.lineSeparator())
                .append(separator)
                .append(System.lineSeparator());
    }

    public String build() {
        final String separator = "------------------------------------------------------------------";
        StringBuilder sb = new StringBuilder();
        this.addInstructionSection(sb, "Base instruction: ", this.baseInstructionBuilder, separator);
        this.addInstructionSection(sb, "State instruction: ", this.stateInstructionBuilder, separator);
        this.addInstructionSection(sb, "Context instruction: ", this.contextInstructionBuilder, separator);
        return sb.toString();
    }
}
