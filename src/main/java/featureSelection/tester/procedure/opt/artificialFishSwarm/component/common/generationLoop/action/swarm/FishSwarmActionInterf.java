package featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.generationLoop.action.swarm;

import featureSelection.basic.procedure.component.action.ComponentExecution;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;

public interface FishSwarmActionInterf<Posi extends Position<?>>
	extends ComponentExecution<Posi>
{
	int getCalculationCount();
}
