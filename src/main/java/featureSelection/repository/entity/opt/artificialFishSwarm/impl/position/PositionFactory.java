package featureSelection.repository.entity.opt.artificialFishSwarm.impl.position;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Random;

import common.utils.RandomUtils;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PositionFactory {
	public static <Posi extends Position<?>> Posi fullPosition(
			Class<Posi> positionClass, int positionLength
	) {
		byte[] position = new byte[positionLength];
		Arrays.fill(position, (byte) 1);
		try {
			return positionClass.getConstructor(byte[].class)
								.newInstance(position);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | 
				InvocationTargetException | NoSuchMethodException | SecurityException e
		) {
			log.error("", e);
			return null;
		}
	}
	
	public static <Posi extends Position<?>> Posi newPosition(
			Class<Posi> positionClass, int[] attributes, int positionLength
	) {
		try {
			return positionClass.getConstructor(int[].class, int.class)
								.newInstance(attributes, positionLength);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | 
				InvocationTargetException | NoSuchMethodException | SecurityException e
		) {
			log.error("", e);
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <PosiValue, Posi extends Position<PosiValue>> Posi randomPositionGrow(
			Posi position, int positionLength, Random random
	) {
		// * Deal with {@link ByteArrayPosition} growth.
		if (position instanceof ByteArrayPosition) {
			// * Get how many position left to grow.
			int optionSize = positionLength-position.getAttributes().length;
			if (optionSize!=0) {
				byte[] positionValue = (byte[]) position.getPosition();
				// * Get next index to be updated. 
				//		Note: updateIndex, starts from 0, is the order index of position bytes free elements.
				int updateIndex = RandomUtils.randomUniqueInt(0, optionSize, random);
				for (int i=0, opt=0; i<positionValue.length;i++) {
					if (Byte.compare(positionValue[i], (byte) 1)!=0) {
						if (opt==updateIndex) {
							// located the position byte to be updated.
							positionValue[i] = 1;
							break;
						}else {
							// next free elements whose byte value==0.
							opt++;
						}
					}
				}
				position.setPosition((PosiValue) positionValue);
				return position;
			}else {
				return position;
			}
		}else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <Posi extends Position<?>> Posi randomPosition(
			Class<Posi> positionClass, int positionLength, Random random
	) {
		if (ByteArrayPosition.class.equals(positionClass)) {
			return (Posi) randomByteArrayPosition(positionLength, random);
		}else {
			return null;
		}
	}
	
	public static Position<byte[]> randomByteArrayPosition(int positionLength, Random random) {
		byte[] position = new byte[positionLength];
		for (int i=0; i<position.length; i++)
			position[i] = RandomUtils.probability(0.5, random)? (byte) 1: (byte) 0;
		ByteArrayPosition posi = new ByteArrayPosition(position);
		return posi;
	}
}