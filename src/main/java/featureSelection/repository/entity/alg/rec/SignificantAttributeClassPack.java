package featureSelection.repository.entity.alg.rec;

import java.util.Collection;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.RoughEquivalenceClass;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * A package for attribute and {@link RoughEquivalenceClass}, when calculating significance and
 * searching for the most significant attribute, for {@link RoughEquivalenceClassBasedAlgorithm}.
 *
 * @param <EquClass>
 *     Type of {@link EquivalenceClass}
 * @param <Rough>
 *     Type of {@link RoughEquivalenceClass}
 *
 * @author Benjamin_L
 */
@AllArgsConstructor
@ReturnWrapper
public class SignificantAttributeClassPack<EquClass extends EquivalenceClass,
											Rough extends RoughEquivalenceClass<EquClass>>
{
	@Setter @Getter private int attribute;
	@Setter @Getter private int[] redundancies;
	@Setter @Getter private Collection<Rough> classSet;
	@Setter @Getter private int positive;
	
	public String toString() {
		return attribute+"";
	}
}