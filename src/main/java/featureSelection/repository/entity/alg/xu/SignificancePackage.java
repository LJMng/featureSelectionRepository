package featureSelection.repository.entity.alg.xu;

import java.util.Collection;

import common.utils.LoggerUtil;
import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.basic.model.universe.instance.Instance;
import org.slf4j.Logger;

@ReturnWrapper
public class SignificancePackage {
	private int attribute;
	private int significance;
	private Collection<Instance> positiveRegion;
	private Collection<Instance> negativeRegion;
	private Collection<Collection<Instance>> siginificantClass;
	
	public SignificancePackage(Collection<Collection<Instance>> siginificantClass,
								Collection<Instance> positiveRegion, Collection<Instance> negativeRegion
	) {
		attribute = -1;
		this.positiveRegion = positiveRegion;
		this.negativeRegion = negativeRegion;
		this.siginificantClass = siginificantClass;
		significance = positiveRegion.size()+negativeRegion.size();
	}
	
	public SignificancePackage setAttribute(int attribute) {
		this.attribute = attribute;
		return this;
	}
	
	public int getAttribute() {
		return attribute;
	}
	
	public int getSignificance() {
		return significance;
	}
	
	public int getPositiveRegionSize() {
		return positiveRegion.size();
	}
	
	public int getNegativeRegionSize() {
		return negativeRegion.size();
	}
	
	public Collection<Instance> getPositiveRegion(){
		return positiveRegion;
	}
	
	public Collection<Instance> getNegativeRegion(){
		return negativeRegion;
	}
	
	public Collection<Collection<Instance>> getSignificanceClass(){
		return siginificantClass;
	}

	public void simpleDisplay(Logger logger) {
		LoggerUtil.printLine(logger, "-", 50);
		logger.info("	"+"Significance simple display : ");
		LoggerUtil.printLine(logger, "-", 50);
		logger.info("	"+" > attribute : "+attribute);
		logger.info("	"+" > significance : "+significance);
		//logger.info("	"+" > significant class type's size : "+siginificant_class.size());
		logger.info("	"+" > positive region size : "+positiveRegion.size());
		logger.info("	"+" > negative region size : "+negativeRegion.size());
		LoggerUtil.printLine(logger, "-", 50);
	}
	
	public void display(Logger logger) {
		LoggerUtil.printLine(logger, "-", 50);
		logger.info("·"+"【SignificancePackage】"+"Display : ");
		LoggerUtil.printLine(logger, "-", 50);
		logger.info("	"+"> significance : "+significance);
		logger.info("	"+"> attribute : "+attribute);
		logger.info("	"+"> significant classes : ");
		int type = 1;
		for (Collection<Instance> sub_equ : siginificantClass) {
			logger.info("		"+"Type "+(type++)+" : ");
			for (Instance u : sub_equ)	logger.info("		"+"> "+u);
		}
		logger.info("	"+"> positive region : ");
		for (Instance u : positiveRegion)	logger.info("		"+"> "+u);
		logger.info("	"+"> negative region : ");
		for (Instance u : negativeRegion)	logger.info("		"+"> "+u);
		
		LoggerUtil.printLine(logger, "-", 50);
	}
}