package beetrap.btfmc.agent;

public class InstructionBuilder {
    private final StringBuilder baseInstruction;
    private final StringBuilder stateInstruction;
    private final StringBuilder contextInstruction;

    public InstructionBuilder() {
        this.baseInstruction = new StringBuilder();
        this.stateInstruction = new StringBuilder();
        this.contextInstruction = new StringBuilder();
    }

    public StringBuilder baseInstructionBuilder() {
        return this.baseInstruction;
    }

    public StringBuilder stateInstructionBuilder() {
        return this.stateInstruction;
    }

    public StringBuilder contextInstructionBuilder() {
        return this.contextInstruction;
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
        this.addInstructionSection(sb, "Base instruction: ", this.baseInstruction, separator);
        this.addInstructionSection(sb, "State instruction: ", this.stateInstruction, separator);
        this.addInstructionSection(sb, "Context instruction: ", this.contextInstruction, separator);
        return sb.toString();
    }
}
