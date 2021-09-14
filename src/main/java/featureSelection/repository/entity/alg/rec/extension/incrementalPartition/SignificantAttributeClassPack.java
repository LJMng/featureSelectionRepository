package featureSelection.repository.entity.alg.rec.extension.incrementalPartition;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.incrementalPartition.RoughEquivalenceClassDummy;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@Getter
@AllArgsConstructor
@ReturnWrapper
public class SignificantAttributeClassPack<Attr> {
	private Attr sigAttribute;
	private Collection<RoughEquivalenceClassDummy> roughClasses;

	@Override
	public String toString() {
		return "SignificantAttributeClassPack [sigAttribute=" + sigAttribute + ", roughClasses=" + roughClasses + "]";
	}
}