package featureSelection.repository.algorithm.alg.positiveApproximationAccelerator;

import featureSelection.repository.entity.alg.positiveApproximationAccelerator.EquivalenceClass;
import lombok.experimental.UtilityClass;

import java.util.Collection;

/**
 * Utilities for {@link PositiveApproximationAcceleratorOriginalAlgorithm}.
 * 
 * @author Benjamin_L
 */
@UtilityClass
public class PositiveApproximationAcceleratorOriginalUtils {

	public static int universeSize(Collection<EquivalenceClass> equClasses) {
		int size=0;
		for (EquivalenceClass equ : equClasses)	size+= equ.getInstances().size();
		return size;
	}

}