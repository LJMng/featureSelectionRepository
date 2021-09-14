package featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalDecision;

import java.util.Collection;

import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.RoughEquivalenceClassDecMapXtension;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShrinkInput4RECIncrementalDecisionExtension<Sig extends Number> {
	private Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses;
	private int attributesInvolvedNumber;
}