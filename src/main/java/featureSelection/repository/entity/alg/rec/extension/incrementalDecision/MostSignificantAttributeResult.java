package featureSelection.repository.entity.alg.rec.extension.incrementalDecision;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.RoughEquivalenceClassDecMapXtension;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

/**
 * An entity for most significant attribute searching result.
 * 
 * @author Benjamin_L
 *
 * @param <Sig>
 * 		Type of significance of attribute.
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public class MostSignificantAttributeResult<Sig extends Number> {
	private Sig significance;
	private Sig globalStaticSiginificance;
	private int attribute;
	private Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses;
}
