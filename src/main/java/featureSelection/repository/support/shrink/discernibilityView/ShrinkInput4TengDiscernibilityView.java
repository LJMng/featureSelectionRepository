package featureSelection.repository.support.shrink.discernibilityView;

import java.util.Collection;

import featureSelection.basic.model.universe.instance.Instance;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShrinkInput4TengDiscernibilityView {

	private Collection<Collection<Instance>> globalEquClasses;
	private Collection<Collection<Instance>> decEquClasses;

	private Collection<Collection<Instance>> equClasses2BRemoved;
}
