package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental;

import featureSelection.basic.annotation.common.ReturnWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ReturnWrapper
public class MostSignificantAttributeResult {
	private int attribute;
	private int positiveRegion;
}
