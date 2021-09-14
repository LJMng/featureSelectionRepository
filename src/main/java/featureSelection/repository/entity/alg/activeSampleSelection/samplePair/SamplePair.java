package featureSelection.repository.entity.alg.activeSampleSelection.samplePair;

import featureSelection.basic.model.universe.instance.Instance;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;

/**
 * An entity to contain a Sample Pair: (x[i], x[j]), where x is an {@link Instance} and i, j is
 * {@link Instance#getNum() num} and i&lt;j.
 * <p>
 * Get more details in the original paper:
 * <a href="https://ieeexplore.ieee.org/document/6308684/">"Sample Pair Selection for Attribute
 * Reduction with Rough Set"</a> by Degang Chen, Suyun Zhao, Lei Zhang, Yongping Yang, Xiao Zhang.
 * 
 * @author Benjamin_L
 */
@Data
@AllArgsConstructor
public class SamplePair {
	private Instance[] pair;
	
	public SamplePair(Instance ins1, Instance ins2) {
		pair = ins1.getNum() < ins2.getNum()?
				new Instance[] {	ins1, ins2	}:
				new Instance[] {	ins2, ins1	};
	}
	
	@Override
	public boolean equals(Object v) {
		if (v instanceof Instance[]) {
			return Arrays.equals((Instance[]) v, pair);
		}else if (v instanceof SamplePair) {
			return Arrays.equals(((SamplePair) v).pair, pair);
		}else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(pair);
	}
	
	@Override
	public String toString() {
		return String.format("(x[%d], x[%d])", pair[0].getNum(), pair[1].getNum());
	}
}
