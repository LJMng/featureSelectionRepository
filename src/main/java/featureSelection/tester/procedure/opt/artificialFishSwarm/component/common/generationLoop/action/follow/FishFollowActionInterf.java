package featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.generationLoop.action.follow;

import featureSelection.basic.procedure.component.action.ComponentExecution;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;

public interface FishFollowActionInterf<Posi extends Position<?>>
	extends ComponentExecution<Posi>
{
	int getCalculationCount();
}
