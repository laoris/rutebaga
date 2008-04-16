package rutebaga.controller.command;

import java.util.Collection;
import java.util.Iterator;

public class ConcreteListElementSource<E> implements ListElementSource<E> {

	private LabelDeterminer label;
	private Collection<E> source;
	
	public ConcreteListElementSource(LabelDeterminer label, Collection<E> source) {
		this.label = label;
		this.source = source;
	}
	
	public String getLabel() {
		return label.getLabel();
	}

	public Iterator<E> iterator() {
		return source.iterator();
	}

	public int contentSize() {
		return source.size();
	}

}
