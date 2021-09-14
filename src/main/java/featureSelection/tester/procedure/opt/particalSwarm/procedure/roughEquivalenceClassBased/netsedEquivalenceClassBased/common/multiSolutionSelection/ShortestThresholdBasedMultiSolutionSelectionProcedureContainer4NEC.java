package featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.multiSolutionSelection;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import common.utils.LoggerUtil;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Particle Swarm Optimization</strong> multi solution selection.
 * Mainly, this contains only 1 {@link ProcedureComponent}:
 * <ul>
 *  <li>
 *  	<strong>Multi solution selection controller</strong>
 *  	<p>Select limited solution based on the given threshold(
 *  		{@link ParameterConstants#PARAMETER_OPTIMIZATION_MULTI_SOLUTION_THRESHOLD}, 0.25 by default,
 *  		meaning limit is set by the size of a quarter of the {@link ReductionParameters#getPopulation
 *  		()}). if solutions in {@link GenerationRecord#getGlobalBestFitnessCollection()} exceeds,
 *  		select the ones with shortest length.
 *  </li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Slf4j
public class ShortestThresholdBasedMultiSolutionSelectionProcedureContainer4NEC<Velocity,
																				Posi extends Position<?>,
																				FValue extends FitnessValue<?>>
	extends DefaultProcedureContainer<GenerationRecord<Velocity, Posi, FValue>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private final Object DEFAULT_THRESHOLD_4_MULTI_SOLUTION_SELECTION = 0.25;
	
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;

	public ShortestThresholdBasedMultiSolutionSelectionProcedureContainer4NEC(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	}

	@Override
	public String shortName() {
		return "PSO multi-solution Selection";
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
			// 1. Multi solution selection controller.
			new TimeCountedProcedureComponent<GenerationRecord<Velocity, Posi, FValue>>(
					ComponentTags.TAG_SOLUTION_SELECTION,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_MULTI_SOLUTION_THRESHOLD),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Velocity, Posi, FValue> params =
								(ReductionParameters<Velocity, Posi, FValue>)
								parameters[p++];
						GenerationRecord<Velocity, Posi, FValue> geneRecord = 
								(GenerationRecord<Velocity, Posi, FValue>)
								parameters[p++];
						Object threshold =
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						if (threshold==null){
							threshold = DEFAULT_THRESHOLD_4_MULTI_SOLUTION_SELECTION;
						}
						int limit =
								threshold instanceof Double?
									(int) (FastMath.ceil((double) threshold * params.getPopulation())):
									(int) threshold;
						
						if (geneRecord.getGlobalBestFitnessCollection().size()>limit) {
							geneRecord.setGlobalBestFitnessCollection(
								geneRecord.getGlobalBestFitnessCollection()
										.stream()
										.sorted((f1, f2)->
											f1.getPosition().getAttributes().length - f2.getPosition().getAttributes().length
										).limit(limit)
										.collect(Collectors.toList())
							);
						}
						/* ------------------------------------------------------------------------------ */
						return geneRecord;
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Single solution selection controller"),	
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public GenerationRecord<Velocity, Posi, FValue> exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (GenerationRecord<Velocity, Posi, FValue>) componentArray[0].exec();
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}