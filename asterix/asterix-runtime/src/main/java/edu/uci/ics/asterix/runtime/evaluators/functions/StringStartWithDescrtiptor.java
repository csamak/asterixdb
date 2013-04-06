package edu.uci.ics.asterix.runtime.evaluators.functions;

import java.io.DataOutput;

import edu.uci.ics.asterix.om.functions.AsterixBuiltinFunctions;
import edu.uci.ics.asterix.om.functions.IFunctionDescriptor;
import edu.uci.ics.asterix.om.functions.IFunctionDescriptorFactory;
import edu.uci.ics.asterix.runtime.evaluators.base.AbstractScalarFunctionDynamicDescriptor;
import edu.uci.ics.hyracks.algebricks.common.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.core.algebra.functions.FunctionIdentifier;
import edu.uci.ics.hyracks.algebricks.runtime.base.ICopyEvaluator;
import edu.uci.ics.hyracks.algebricks.runtime.base.ICopyEvaluatorFactory;
import edu.uci.ics.hyracks.data.std.api.IDataOutputProvider;
import edu.uci.ics.hyracks.data.std.primitive.UTF8StringPointable;
import edu.uci.ics.hyracks.data.std.util.ArrayBackedValueStorage;

/**
 * @author Xiaoyu Ma
 */
public class StringStartWithDescrtiptor extends AbstractScalarFunctionDynamicDescriptor {
    private static final long serialVersionUID = 1L;

    public static final IFunctionDescriptorFactory FACTORY = new IFunctionDescriptorFactory() {
        public IFunctionDescriptor createFunctionDescriptor() {
            return new StringStartWithDescrtiptor();
        }
    };

    @Override
    public ICopyEvaluatorFactory createEvaluatorFactory(final ICopyEvaluatorFactory[] args) throws AlgebricksException {

        return new ICopyEvaluatorFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public ICopyEvaluator createEvaluator(IDataOutputProvider output) throws AlgebricksException {

                DataOutput dout = output.getDataOutput();

                return new AbstractBinaryStringBoolEval(dout, args[0], args[1],
                        AsterixBuiltinFunctions.STRING_START_WITH) {

                    @Override
                    protected boolean compute(byte[] lBytes, int lLen, int lStart, byte[] rBytes, int rLen, int rStart,
                            ArrayBackedValueStorage array0, ArrayBackedValueStorage array1) {
                        int patternLength = UTF8StringPointable.getUTFLength(rBytes, 1);
                        if (patternLength > UTF8StringPointable.getUTFLength(lBytes, 1))
                            return false;

                        int pos = 3;
                        while (pos < patternLength + 3) {
                            char c1 = UTF8StringPointable.charAt(lBytes, pos);
                            char c2 = UTF8StringPointable.charAt(rBytes, pos);
                            if (c1 != c2)
                                return false;

                            pos += UTF8StringPointable.charSize(lBytes, pos);
                        }

                        return true;
                    }

                };
            }
        };
    }

    @Override
    public FunctionIdentifier getIdentifier() {
        return AsterixBuiltinFunctions.STRING_START_WITH;
    }
}
