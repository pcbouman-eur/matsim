package floetteroed.opdyts.logging;

import floetteroed.opdyts.DecisionVariable;
import floetteroed.opdyts.trajectorysampling.SamplingStage;
import floetteroed.utilities.statisticslogging.Statistic;

/**
 * 
 * @author Gunnar Flötteröd
 *
 */
public class EquilibriumGapWeight<U extends DecisionVariable> implements
		Statistic<SamplingStage<U>> {

	@Override
	public String label() {
		return "Equilibrium Gap Weight";
	}

	@Override
	public String value(final SamplingStage<U> samplingStage) {
		return Double.toString(samplingStage.getEquilibriumGapWeight());
	}

}