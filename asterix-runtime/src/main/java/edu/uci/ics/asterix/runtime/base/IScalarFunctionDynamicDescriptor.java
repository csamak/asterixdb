package edu.uci.ics.asterix.runtime.base;


import edu.uci.ics.asterix.om.functions.IFunctionDescriptor;
import edu.uci.ics.hyracks.algebricks.core.api.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.runtime.base.IEvaluatorFactory;

public interface IScalarFunctionDynamicDescriptor extends IFunctionDescriptor {
    public IEvaluatorFactory createEvaluatorFactory(IEvaluatorFactory[] args) throws AlgebricksException;
}
