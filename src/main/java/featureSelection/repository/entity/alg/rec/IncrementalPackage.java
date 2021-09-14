package featureSelection.repository.entity.alg.rec;

import java.util.Set;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.RoughEquivalenceClass;
import lombok.Getter;

/**
 * An entity for incremental calculation results of <strong>IP-REC</strong>.
 * 
 * @author Benjamin_L
 *
 * @param <Rough>
 * 		Type of {@link RoughEquivalenceClass}
 */
@ReturnWrapper
public class IncrementalPackage<Rough extends RoughEquivalenceClass<? extends EquivalenceClass>>  {
	private boolean redundant;
	@Getter private int positive, negative;
	@Getter private Set<Rough> filteredClass;
	
	public IncrementalPackage(Set<Rough> filteredClass, boolean redundant, int positive, int negative) {
		this.filteredClass = filteredClass;
		this.redundant = redundant;
		this.positive = positive;
		this.negative = negative;
	}
	
	public boolean isRedundant() {
		return redundant;
	}
}