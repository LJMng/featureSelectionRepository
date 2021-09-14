package featureSelection.repository.entity.alg.rec.nestedEC;

import featureSelection.basic.annotation.common.ReturnWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ReturnWrapper
public class SignificanceCalculationResult<NestedEquClasses, Sig> {
	private Sig significance;
	private NestedEquivalenceClassesInfo<NestedEquClasses> nestedEquivalenceClassesInfo;
}
