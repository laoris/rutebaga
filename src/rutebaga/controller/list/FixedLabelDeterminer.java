package rutebaga.controller.list;

public class FixedLabelDeterminer implements LabelDeterminer {
	
	private final String label;
	
	public FixedLabelDeterminer(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
}
