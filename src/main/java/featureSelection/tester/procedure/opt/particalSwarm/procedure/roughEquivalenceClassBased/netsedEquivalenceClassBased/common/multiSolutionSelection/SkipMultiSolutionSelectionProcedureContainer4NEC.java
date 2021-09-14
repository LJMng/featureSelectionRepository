package featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.multiSolutionSelection;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Particle Swarm Optimization</strong> multi solution selection. Mainly, this contains 
 * only 1 {@link ProcedureComponent}:
 * <ul>
 * 	<li>
 * 		<strong>Multi solution selection controller</strong>
 * 		<p>Skip limited solution selection.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Slf4j
public class SkipMultiSolutionSelectionProcedureContainer4NEC<Velocity,
																Posi extends Position<?>,
																FValue extends FitnessValue<?>>
	extends DefaultProcedureContainer<GenerationRecord<Velocity, Posi, FValue>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;

	public SkipMultiSolutionSelectionProcedureContainer4NEC(ProcedureParameters parameters, boolean logOn) {
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
			new ProcedureComponent<GenerationRecord<Velocity, Posi, FValue>>(
					ComponentTags.TAG_SOLUTION_SELECTION,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
						});
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						GenerationRecord<Velocity, Posi, FValue> geneRecord = 
								(GenerationRecord<Velocity, Posi, FValue>)
								parameters[p++];
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