package featureSelection.repository.support.calculation.dependency.classic;

import java.util.Collection;
import java.util.List;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialIDAlgorithm;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialIDCalculation;
import featureSelection.repository.support.calculation.dependency.DefaultDependencyCalculation;
import lombok.Getter;

public class DependencyCalculation4ClassicSequentialID
	extends DefaultDependencyCalculation
	implements ClassicSequentialIDCalculation<Double>
{
	@Getter private Double positive;
	@Override
	public Double getResult() {
		return positive;
	}

	public DependencyCalculation4ClassicSequentialID calculate(
			Collection<List<Instance>> eClasses, Collection<List<Instance>> decEClasses,
			int attributeLength, Object...args
	) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		int universeSize = (int) args[0];
		positive = (Double) new Double(eClasses==null || eClasses.isEmpty()? 0.0: 
									positiveRegion(eClasses, decEClasses) / (universeSize+0.0)
					);
		return this;
	}
	
	private static int positiveRegion(Collection<List<Instance>> eClasses, Collection<List<Instance>> decEClasses) {
		// POS = 0
		int pos = 0;
		// for i = 1 to number of EClass of P.
		//		循环每个 PEClass. 统计当前PEClass[i]属于同一个DEClass的数量
		int position;
		Step2:
		for (List<Instance> eclassList : eClasses) {
			// number=0. 原来记录可以认为是排序的
			//		意思是，按照原来dataset，每一个record认为有一个id，形成等价类过程中，
			//		顺序不变，不需要额外排序算法，不过需要dataset的每个record有一个ID
			int number = 0;
			// for j=1 to number of EClass of D
			Step4: 
			for (List<Instance> decEClass_j: decEClasses) {
				//for (int j=0; j<decEClasses.size(); j++) {
				//	position = 0, 是上一条PEClass记录找到的DEClass的位置
				position = 0;
				//	for k=1 to number of records in PEClass[k]
				for (int k=0; k<eclassList.size(); k++) {
					// for m=position+1 to number of records in decEClass[j]
					//		从DEClass这个位置的下一条开始，接着判断PEClass的下一条记录
					//	if (k==1). PEClass[i]中的第一条记录
					if (k==0) {
						// 9 position = getPosition (DEClass[j] , 0, PEClass[i][k]);
						//		PEClass[i][k]表示当前PEClass是第i个，需要判断其中的第k条记录
						position = ClassicAttributeReductionSequentialIDAlgorithm
									.Basic
									.getPosition(decEClass_j, 0, eclassList.get(k).getNum());
						// if (position==-1)
						if (position==-1) {
							// goto stet4. 当前PEClass[i]第一条记录都不在DEClass[j]中，转下一个DEClass[j+1]
							continue Step4;
						// else
						}else {
							// number++; 当前PEClass[i]的记录在DEClass[j]中.
							number++;
						}
					// else.	如果是PEClass[i]中的非第一条记录
					}else {
						// position = getPosition (DEClass[j] , position, PEClass[k]);
						position = ClassicAttributeReductionSequentialIDAlgorithm
									.Basic
									.getPosition(decEClass_j, position, eclassList.get(k).getNum());
						// if (position==-1)	没找到
						if (position==-1) {
							// goto Step2		当前PEClass[i]肯定是不一致的，非POS
							continue Step2;
						// else
						}else {
							// number++
							number++;
						}
					}
				}
			}
			// if (number == len of PEClass[i])
			if (number==eclassList.size()) {
				// pos = pos + number
				pos += number;
				continue Step2;
			}
		}
		return pos;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}

	@Override
	public Double difference(Double v1, Double v2) {
		return v1.doubleValue()-v2.doubleValue();
	}
}