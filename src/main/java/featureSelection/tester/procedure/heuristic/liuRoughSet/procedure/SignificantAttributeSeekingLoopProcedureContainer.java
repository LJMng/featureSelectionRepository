package featureSelection.tester.procedure.heuristic.liuRoughSet.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.alg.liuRoughSet.LiuRoughSetAlgorithm;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiuRoughSet;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Most significant attribute searching for <strong>Quick Reduct - Liu Rough Set</strong>
 * Feature Selection. This procedure contains 4 {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Check if sig(red)==sig(C), if does, break : true-continue, false-break</strong>
 * 		<p>Check if significance of current reduct equals to the global significance(i.e.
 * 			<code>sig(red)==sig(C)</code>). Return <code>true</code> if it does.
 * 	</li>
 * 	<li>
 * 		<strong>Seek sig attribute</strong>
 * 		<p>Search for the attribute with the max. significance value in the rest of the
 * 			attributes(i.e. attributes outside the reduct), and return as an attribute
 * 			of the reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Add sig attribute into red</strong>
 * 		<p>Add the most significant attribute into the reduct.
 * 	</li>
 * 	<li>
 * 	    <strong>Update rough equivalence classes</strong>
 * 	    <p>Use the current reduct to partition and get the correspondent Rough Equivalence
 * 	    	Classes.
 * 	</li>
 * </ul>
 * <p>
 * Loop is controlled by {@link #exec()}.
 *
 * @author Benjamin_L
 */
@Slf4j
public class SignificantAttributeSeekingLoopProcedureContainer<Sig extends Number> 
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	
	private int loopCount = 0;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public SignificantAttributeSeekingLoopProcedureContainer(
			ProcedureParameters parameters, boolean logOn
	) {
		super(parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Sig seeking loop";
	}
	
	@Override
	public String staticsName() {
		return shortName();
	}
	
	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Check if break the loop: true-break / false-continue.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 1/{}. {}"),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("globalSig"),
								getParameters().get("redSig"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Sig globalSig =
								(Sig) parameters[p++];
						Sig redSig =
								(Sig) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						FeatureImportance<Sig> calculation =
								(FeatureImportance<Sig>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// true-break / false-continue
						return !calculation.value1IsBetter(globalSig, redSig, sigDeviation);
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(2, "globalSig = {} | redSig = {}"),
									getParameters().get("globalSig"),
									getParameters().get("redSig")
							);
							log.info(
									LoggerUtil.spaceFormat(2, "deviation = {}"),
									getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION).toString()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						reportKeys.add(reportMark);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Check if break the loop."),
			// 2. Seek sig attribute.
			new TimeCountedProcedureComponent<Integer>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}."),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get("roughClass"),
								getParameters().get("redSig"),
							});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						FeatureImportance4LiuRoughSet<Sig> calculation =
								(FeatureImportance4LiuRoughSet<Sig>) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						Collection<Collection<Instance>> roughClass =
								(Collection<Collection<Instance>>) parameters[p++];
						Sig redSig =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return LiuRoughSetAlgorithm
									.mostSignificantAttribute(
											calculation,
											sigDeviation,
											roughClass, 
											red, 
											attributes, 
											instances.size(),
											redSig 
								);
					}, 
					(component, significance) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("sig", significance);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(2, "+ attribute {}"),
									significance
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistic
						//	[STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH]
						Collection<Integer> reduct = getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						ProcedureUtils.Statistics.push(
							statistics.getData(),
							StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH,
							reduct.size()+1
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY,
								significance
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek sig attribute"),
			// 3. Add sig attribute into red.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 3/{}. {}"),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
									getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
									getParameters().get("sig"),
							});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						int sig =
								(int) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						if (sig==-1)	
							throw new IllegalStateException("abnormal most significant attribute result : "+sig);
						else			
							reduct.add(sig);
						return reduct;
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Add sig attribute into red"),
			// 4. Update rough equivalence classes.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(LoggerUtil.spaceFormat(1, "Loop {} | 4/{}. {}."), loopCount, getComponents().size(), component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get("sig"),
							});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						FeatureImportance4LiuRoughSet<Sig> calculation =
								(FeatureImportance4LiuRoughSet<Sig>) parameters[p++];
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Collection<Collection<Instance>> roughClasses =
								LiuRoughSetAlgorithm
									.Basic
									.equivalenceClass(
											instances,
											new IntegerCollectionIterator(red)
									);
						Sig redSig = calculation.calculate(instances, new IntegerCollectionIterator(red), instances.size())
												.getResult();
						return new Object[] {
								roughClasses,
								redSig,
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Collection<Instance>> roughClass = (Collection<Collection<Instance>>) result[r++];
						Sig redSig = (Sig) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("roughClass", roughClass);
						getParameters().setNonRoot("redSig", redSig);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_POS_HISTORY]
						List<Sig> increment =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (increment==null){
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY,
									increment = new LinkedList<>()
							);
						}
						increment.add(redSig);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_SIG_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark, 
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								redSig
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}	
				
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update rough equivalence class"),
		};
	}

	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps) {
			this.getComponents().add(each);
			//reportKeys.add(each.getDescription());
		}
		ComponentLoop:
		while (true) {
			loopCount++;
			for (int i=0; i<this.getComponents().size(); i++) {
				if (i==0) {
					if (((Boolean) comps[i].exec()))	break ComponentLoop;
				}else {
					comps[i].exec();
				}
			}
		}
		// Statistics
		//	[Sig loop times]
		statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_LOOP_TIMES, loopCount);
		return this.getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
	
	private String reportMark() {
		return "Loop["+loopCount+"]";
	}
}