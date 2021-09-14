package featureSelection.repository.entity.alg.activeSampleSelection.samplePair;

import java.util.Collection;
import java.util.HashSet;

import lombok.Data;

/**
 * An entity for Sample Pair attribute info. With fields:
 * <ul>
 *  <li><strong>{@link #frequency}</strong>
 * 	    <p>The frequency of the attribute indexes in Sample pair selection results.
 *  </li>
 *  <li><strong>{@link #attributeIndexes}</strong>
 * 	    <p>Correspondent attribute indexes with the frequency.
 *  </li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Data
public class SamplePairAttributeInfo {
	private int frequency;
	private Collection<Integer> attributeIndexes;
	
	public SamplePairAttributeInfo() {
		attributeIndexes = new HashSet<>();
	}
	
	public void add(int attributeIndex) {
		attributeIndexes.add(attributeIndex);
		frequency++;
	}
}
