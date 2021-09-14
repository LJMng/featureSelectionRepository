package featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component.dynamicUpdateStrategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * An abstract {@link DefaultProcedureContainer} for tolerance classes update.
 *
 * @author Benjamin_L
 */
@Slf4j
public abstract class AbstractTolerancesUpdateProcedureContainer
	extends DefaultProcedureContainer<Object[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	protected boolean logOn;
	@Getter protected Statistics statistics;
	@Getter protected Map<String, Map<String, Object>> report;
	protected Collection<String> reportKeys;
	
	protected Map<String, Object> localParameters;
	
	public AbstractTolerancesUpdateProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String staticsName() {
		return shortName();
	}
	
	@Override
	public String reportName() {
		return shortName();
	}
	
	@Override
	public Object[] exec() throws Exception {
		ProcedureComponent<?>[] components = initComponents();
		for (ProcedureComponent<?> each : components)	this.getComponents().add(each);
		return (Object[]) components[0].exec();
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	public String reportMark() {
		return localParameters.get("report target sig attributes").toString();
	}
	public String reportMark(boolean withDec) {
		return localParameters.get("report target sig attributes").toString() +" "+
				(withDec? "B": "B∪D");
	}


	protected Collection<Integer> generateWithDecisionAttribute(Collection<Integer> attributes){
		Collection<Integer> generated = new ArrayList<>(attributes.size()+1);
		generated.addAll(attributes);
		generated.add(0);
		return generated;
	}
	
	protected void validateToleranceClassUpdate(
			Map<Instance, Collection<Instance>> tolerances2BValidated,
			Collection<Instance> originalIns, Collection<Instance> up2DateIns,
			IntegerIterator attributes, ToleranceClassObtainer toleranceClassObtainer
	) {
		log.info(LoggerUtil.spaceFormat(1, "Validate tolerance classes, attributes={}"), attributes);
		
		Map<Instance, Collection<Instance>> tolerancesByTcpr =
			toleranceClassObtainer.obtain(
				up2DateIns, up2DateIns, attributes, 
				toleranceClassObtainer.getCacheInstanceGroups(up2DateIns)
			);
		
		
		for (Map.Entry<Instance, Collection<Instance>> tcprEntry:
			tolerancesByTcpr.entrySet()
		) {
			Instance ins = tcprEntry.getKey();
			Collection<Instance> tcprTolerance = tcprEntry.getValue();
			
			Collection<Instance> didsTolerance = tolerances2BValidated.get(ins);
			
			if (tcprTolerance==null && didsTolerance==null) {
				continue;
			}else if (tcprTolerance==null) {
				throw new IllegalStateException("Tolerance of TCPR is null, DIDS's is not, ins="+ins);
			}else if (didsTolerance==null) {
				System.out.println(ins);
				System.out.println(tolerances2BValidated.keySet());
				throw new IllegalStateException("Tolerance of DIDS is null, TCPR's is not, ins="+ins);
			}else if (tcprTolerance.size()!=didsTolerance.size()) {
				System.out.println("Attributes: "+attributes);

				boolean updated = !originalIns.contains(tcprEntry.getKey());
				
				System.out.println("Ins: ("+(updated? "updated": "not-updated")+")"+ins);
				System.out.println("TCPR: "+tcprTolerance.size());
				for (Instance each: tcprTolerance) {
					System.out.println("	"+each);
				}
				System.out.println("DIDS: "+didsTolerance.size());
				for (Instance each: didsTolerance) {
					System.out.println("	"+each);
				}
				
				throw new IllegalStateException("Tolerances size abnormal, should be "+
						tcprTolerance.size()+", get: "+didsTolerance.size()+", ins: "+
						ins
				);
			}else {
				for (Instance t: tcprTolerance) {
					if (!didsTolerance.contains(t)) {
//						System.out.println("Attributes: "+attributes);
//						System.out.println("Ins: "+ins);
//						System.out.println("TCPR: "+tcprTolerance.size());
//						for (Instance each: tcprTolerance) {
//							System.out.println("	"+each);
//						}
//						System.out.println("DIDS: "+didsTolerance.size());
//						for (Instance each: didsTolerance) {
//							System.out.println("	"+each);
//						}
						
						throw new IllegalStateException("Tolerance missing in DIDS: "+t+", of ins="+ins);
					}
				}
			}
		}
	}

}