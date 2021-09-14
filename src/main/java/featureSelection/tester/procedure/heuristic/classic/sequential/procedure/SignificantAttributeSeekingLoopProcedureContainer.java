package featureSelection.tester.procedure.heuristic.classic.sequential.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialAlgorithm;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reduct searching through significance for <strong>Quick Reduct - Classic
 * reduction</strong> using  <code>Sequential Search</code> when searching is
 * involved.
 * <p>
 * This procedure contains 4 {@link ProcedureComponent}s refer to steps:
 * <ul>
 * 	<li>
 * 		<strong>Calculate pos of red</strong>
 * 		<p>Calculate the significance of the current reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Check if break the loop: true-break / false-continue</strong>
 * 		<p>Check if able to break the searching loop, i.e. if sig(reduct)==sig(C), break.
 * 	</li>
 * 	<li>
 * 		<strong>Seek sig attribute</strong>
 * 		<p>Seek the most significant attribute in the rest of the attributes outside of current reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Add sig attribute into red</strong>
 * 		<p>Add the most siginificant attribute into reduct.
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 * <li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * <li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * <li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * <li>[preset] {@link ParameterConstants#PARAMETER_SIG_DEVIATION}</li>
 * <li>{@link ParameterConstants#PARAMETER_REDUCT_LIST}</li>
 * <li>{@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 * </ul>
 * 
 * @param <Sig>
 * 		Type of Feature(subset) significance.
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
			ProcedureParameters paramaters, boolean logOn
	) {
		super(paramaters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "QR-Classic(Sequential) - Sig seeking loop";
	}

	@Override
	public String staticsName() {
		return shortName();
	};

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Calculate pos of red.
			new TimeCountedProcedureComponent<Sig>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 1/{}. {}."),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("decClasses"),
							});
						
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						ClassicSequentialCalculation<Sig> calculation = 
								(ClassicSequentialCalculation<Sig>) 
								parameters[p++];
						Collection<List<Instance>> decClasses =
								(Collection<List<Instance>>)
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return calculation.calculate(
									instances,
									new IntegerCollectionIterator(red),
									decClasses
								).getResult();
					},
					(component, redSig) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("redSig", redSig);
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "Sig(red) = {}"),
									redSig
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistic
						//	[STATISTIC_SIG_HISTORY]
						List<Sig> increment = statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
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
								report, reportMark(),
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								redSig
						);
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Calculate pos of red"),
			// 2. Check if break the loop: true-break / false-continue.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("redSig"),
								getParameters().get("globalSig"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
							});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Sig redSig =
								(Sig) parameters[p++];
						Sig globalSig =
								(Sig) parameters[p++];
						ClassicSequentialCalculation<Sig> calculation =
								(ClassicSequentialCalculation<Sig>) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
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
							if (result){
								log.info(LoggerUtil.spaceFormat(2, "break!"));
							}
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
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Check if break the loop."),
			// 3. Seek sig attribute.
			new TimeCountedProcedureComponent<Integer>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 3/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
									getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
									getParameters().get("decClasses"),
									getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
									getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
									getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
									getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
									getParameters().get("redSig"),
							});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						Collection<List<Instance>> decClasses =
								(Collection<List<Instance>>)
								parameters[p++];
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						ClassicSequentialCalculation<Sig> calculation =
								(ClassicSequentialCalculation<Sig>) 
								parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						Sig redSig =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return ClassicAttributeReductionSequentialAlgorithm
								.mostSignificantAttribute(
										calculation, 
										sigDeviation, 
										instances,
										decClasses, 
										red, 
										redSig, 
										new IntegerArrayIterator(attributes)
								);
					}, 
					(component, sigAttr) -> {
						/* ------------------------------------------------------------------------------ */
						if (sigAttr==-1)	throw new RuntimeException("abnormal most significant attribute result : "+sigAttr);
						else 				localParameters.put("sigAttr", sigAttr);
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "+ attribute {}"),
									sigAttr
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
								sigAttr
						);
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek sig attribute"),
			// 4. Add sig attribute into red.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 4/{}. {}."),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
									getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
									localParameters.get("sigAttr"),
							});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						int sigAttr =
								(int) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						red.add(sigAttr);
						return red;
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "|red| = {}"),
									red.size()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark(),
								(TimeCountedProcedureComponent<?>) component
						);
					}
				){
					@Override public void init() {}	
				
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Add sig attribute into red")
		};
	}

	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps)	this.getComponents().add(each);
		ComponentLoop:
		while (true) {
			loopCount++;
			for (int i=0; i<this.getComponents().size(); i++) {
				if (i==1) {
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