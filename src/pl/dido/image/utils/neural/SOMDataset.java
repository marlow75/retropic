package pl.dido.image.utils.neural;

import java.util.ArrayList;

public class SOMDataset<T> {
	protected final ArrayList<T> set = new ArrayList<T>(10);
	protected int index = 0;
	
	public void add(final T vec) {
		set.add(vec);
	}
	
	public T getNext() {
		if (index < set.size())
			return set.get(index++);
		
		return null;
	}
	
	public void reset() {
		index = 0;
	}
	
	public int size() {
		return set.size();
	}

	public void addAll(final SOMDataset<T> dataset) {
		set.addAll(dataset.set);
	}
}
