package featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish.action.update;

import common.utils.RandomUtils;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish.Fish4BytePosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.action.FishGroupUpdateAlgorithm;

import java.lang.reflect.Array;
import java.util.Random;

public class FishGroupUpdateAlgorithm4ByteArray
	implements FishGroupUpdateAlgorithm<ByteArrayPosition, Fish4BytePosition>
{
	@Override
	public Fish4BytePosition[] generateFishGroup(
			int fishGroupNumber, int attributeLength, ReductionParameters params, Random random
	) {
		Fish4BytePosition[] fish = (Fish4BytePosition[]) Array.newInstance(
										Fish4BytePosition.class, 
										fishGroupNumber
									);
		for (int i=0; i<fish.length; i++)	fish[i] = new Fish4BytePosition();
		byte[] positionValue;
		for (Fish4BytePosition f: fish) {
			f.setPosition(new ByteArrayPosition(positionValue = new byte[attributeLength]));
			positionValue[RandomUtils.randomUniqueInt(1, attributeLength, random)-1] = 1;
		}
		return fish;
	}
}
