package featureSelection.repository.entity.opt;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import featureSelection.basic.model.optimization.AttributeEncoding;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.razaChromosome.RazaChromosome;
import lombok.Getter;

/**
 * Optimization reduct entity wihch contains info. of reduct coding in a specific optimization
 * algorithm and the inspected one.
 * 
 * @author Benjamin_L
 */
public class OptimizationReduct {
	/**
	 * The {@link AttributeEncoding} instance which contains the solution encoding info. in a
	 * specific optimization.
	 */
	@Getter private AttributeEncoding<?> redsCodingBeforeInspection;
	/**
	 * Reduct attributes(not indexes) after inspection(/skipping inspection).
	 * <p>
	 * For streaming algorithms, doesn't contains previous reduct attributes.
	 */
	@Getter private Collection<Integer> redsAfterInspection;
	
	public OptimizationReduct(
			AttributeEncoding<?> redsBeforeInspectation, Collection<Integer> redsAfterInspection
	) {
		this.redsCodingBeforeInspection = redsBeforeInspectation;
		this.redsAfterInspection = redsAfterInspection;
	}
	
	/**
	 * Count redundant of the reduct of {@link #redsCodingBeforeInspection}, and compare with
	 * {@link #redsAfterInspection} to check out redundant attributes.
	 * 
	 * @return the number of redundant attributes.
	 */
	public int countRedundant() {
		// GA - Raza chromosme with integers as gene values.
		if (redsCodingBeforeInspection instanceof RazaChromosome) {
			final Collection<Integer> redsAfterInspectionHash = new HashSet<>(redsAfterInspection);
			return Long.valueOf(
						Arrays.stream(redsCodingBeforeInspection.getAttributes())
							.filter(attr->!redsAfterInspectionHash.contains(attr))
							.count()
					).intValue();
		// Common.
		}else {
			return redsCodingBeforeInspection.getAttributes().length- redsAfterInspection.size();
		}
	}
}
