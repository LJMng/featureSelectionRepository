package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental;

import java.util.Collection;
import java.util.List;

import featureSelection.basic.annotation.common.ReturnWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ReturnWrapper
public class ReductCandidateResult4VariantObjects {
	private Collection<Integer> previousReductHash;
	private List<Integer> reductCandidate;
}
