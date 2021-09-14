package featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish;

import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Fish4BytePosition 
	implements Fish<ByteArrayPosition>
{
	@Getter @Setter private ByteArrayPosition position;
	@Getter private boolean exited;
	
	public Fish4BytePosition() {
		exited = false;
	}

	@Override
	public Fish<ByteArrayPosition> exit() {
		exited = true;
		return this;
	}
}
