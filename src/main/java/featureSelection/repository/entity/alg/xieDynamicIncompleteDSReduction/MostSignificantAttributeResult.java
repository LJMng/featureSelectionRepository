package featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction;

import featureSelection.basic.annotation.common.ReturnWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ReturnWrapper
public class MostSignificantAttributeResult<Sig extends Number> {
	private int attribute;
	private Sig significance;
}
