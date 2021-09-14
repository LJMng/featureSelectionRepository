package featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge;

public interface NestedEquivalenceClassesMerger<Params extends NestedEquivalenceClassesMergerParameters,
												Result>
{
	/**
	 * Execute merging.
	 * 
	 * @param params
	 * 		{@link Params} for merging.
	 * @return {@link Result}.
	 */
	Result merge(Params params);
}