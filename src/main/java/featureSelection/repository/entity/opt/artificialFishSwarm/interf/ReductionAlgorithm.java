package featureSelection.repository.entity.opt.artificialFishSwarm.interf;

import featureSelection.basic.model.optimization.OptimizationAlgorithm;
import featureSelection.basic.support.calculation.FeatureImportance;

import java.util.Collection;

/**
 * Reduction algorithm used to calculate dependency in Attribute Reduction.
 *
 * @param <FI>
 *     Type of implemented feature (subset) importance calculation.
 * @param <Sig>
 *     Type of feature (subset) importance value.
 * @param <CollectionItem>
 *     Type of collection item of data.
 * 
 * @author Benjamin_L
 */
public interface ReductionAlgorithm<FI extends FeatureImportance<Sig>, Sig extends Number, CollectionItem>
	extends OptimizationAlgorithm
{
	Sig dependency(FI calculation, Collection<CollectionItem> collectionItems, Position<?> position);
	Sig dependency(FI calculation, Collection<CollectionItem> collectionItems, int[] attributes);
	
	int[] inspection(FI calculation, Sig sigDeviation, Collection<CollectionItem> collectionItems, int[] attributes);
}