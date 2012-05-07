package edu.uci.ics.asterix.runtime.evaluators.functions;

import java.io.DataOutput;
import java.io.IOException;

import edu.uci.ics.asterix.common.exceptions.AsterixException;
import edu.uci.ics.asterix.common.functions.FunctionConstants;
import edu.uci.ics.asterix.dataflow.data.nontagged.serde.AOrderedListSerializerDeserializer;
import edu.uci.ics.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import edu.uci.ics.asterix.om.base.ANull;
import edu.uci.ics.asterix.om.types.ATypeTag;
import edu.uci.ics.asterix.om.types.BuiltinType;
import edu.uci.ics.asterix.om.types.EnumDeserializer;
import edu.uci.ics.asterix.om.util.NonTaggedFormatUtil;
import edu.uci.ics.asterix.runtime.evaluators.base.AbstractScalarFunctionDynamicDescriptor;
import edu.uci.ics.hyracks.algebricks.core.algebra.functions.FunctionIdentifier;
import edu.uci.ics.hyracks.algebricks.core.api.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.runtime.base.IEvaluator;
import edu.uci.ics.hyracks.algebricks.runtime.base.IEvaluatorFactory;
import edu.uci.ics.hyracks.api.dataflow.value.ISerializerDeserializer;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.ArrayBackedValueStorage;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.IDataOutputProvider;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.IFrameTupleReference;
import edu.uci.ics.hyracks.dataflow.common.data.marshalling.IntegerSerializerDeserializer;

public class GetItemDescriptor extends AbstractScalarFunctionDynamicDescriptor {

    private static final long serialVersionUID = 1L;
    private final static FunctionIdentifier FID = new FunctionIdentifier(FunctionConstants.ASTERIX_NS, "get-item", 2,
            true);

    @Override
    public IEvaluatorFactory createEvaluatorFactory(final IEvaluatorFactory[] args) {
        return new GetItemEvalFactory(args);
    }

    @Override
    public FunctionIdentifier getIdentifier() {
        return FID;
    }

    private static class GetItemEvalFactory implements IEvaluatorFactory {

        private static final long serialVersionUID = 1L;

        private IEvaluatorFactory listEvalFactory;
        private IEvaluatorFactory indexEvalFactory;
        private final static byte SER_ORDEREDLIST_TYPE_TAG = ATypeTag.ORDEREDLIST.serialize();
        private final static byte SER_NULL_TYPE_TAG = ATypeTag.NULL.serialize();
        private byte serItemTypeTag;
        private ATypeTag itemTag;
        private boolean selfDescList = false;

        public GetItemEvalFactory(IEvaluatorFactory[] args) {
            this.listEvalFactory = args[0];
            this.indexEvalFactory = args[1];
        }

        @Override
        public IEvaluator createEvaluator(final IDataOutputProvider output) throws AlgebricksException {
            return new IEvaluator() {

                private DataOutput out = output.getDataOutput();
                private ArrayBackedValueStorage outInputList = new ArrayBackedValueStorage();
                private ArrayBackedValueStorage outInputIdx = new ArrayBackedValueStorage();
                private IEvaluator evalList = listEvalFactory.createEvaluator(outInputList);
                private IEvaluator evalIdx = indexEvalFactory.createEvaluator(outInputIdx);
                @SuppressWarnings("unchecked")
                private ISerializerDeserializer<ANull> nullSerde = AqlSerializerDeserializerProvider.INSTANCE
                        .getSerializerDeserializer(BuiltinType.ANULL);
                private int itemIndex;
                private int itemOffset;
                private int itemLength;

                @Override
                public void evaluate(IFrameTupleReference tuple) throws AlgebricksException {

                    try {
                        outInputList.reset();
                        evalList.evaluate(tuple);
                        outInputIdx.reset();
                        evalIdx.evaluate(tuple);
                        byte[] serOrderedList = outInputList.getBytes();

                        if (serOrderedList[0] == SER_NULL_TYPE_TAG) {
                            nullSerde.serialize(ANull.NULL, out);
                            return;
                        }

                        if (serOrderedList[0] != SER_ORDEREDLIST_TYPE_TAG) {
                            throw new AlgebricksException("List's get-item can not be called on values of type"
                                    + EnumDeserializer.ATYPETAGDESERIALIZER.deserialize(serOrderedList[0]));
                        }

                        itemIndex = IntegerSerializerDeserializer.getInt(outInputIdx.getBytes(), 1);
                        if (itemIndex >= AOrderedListSerializerDeserializer.getNumberOfItems(serOrderedList)) {
                            out.writeByte(SER_NULL_TYPE_TAG);
                            return;
                        }
                        if (itemIndex < 0)
                            throw new AlgebricksException("Item index can be negative !!");

                        itemTag = EnumDeserializer.ATYPETAGDESERIALIZER.deserialize(serOrderedList[1]);
                        if (itemTag == ATypeTag.ANY)
                            selfDescList = true;
                        else
                            serItemTypeTag = serOrderedList[1];

                        itemOffset = AOrderedListSerializerDeserializer.getItemOffset(serOrderedList, itemIndex);

                        if (selfDescList) {
                            itemTag = EnumDeserializer.ATYPETAGDESERIALIZER.deserialize(serOrderedList[itemOffset]);
                            itemLength = NonTaggedFormatUtil.getFieldValueLength(serOrderedList, itemOffset, itemTag,
                                    true) + 1;
                            out.write(serOrderedList, itemOffset, itemLength);
                        } else {
                            itemLength = NonTaggedFormatUtil.getFieldValueLength(serOrderedList, itemOffset, itemTag,
                                    false);
                            out.writeByte(serItemTypeTag);
                            out.write(serOrderedList, itemOffset, itemLength);
                        }
                    } catch (IOException e) {
                        throw new AlgebricksException(e);
                    } catch (AsterixException e) {
                        throw new AlgebricksException(e);
                    }
                }
            };
        }

    }

}
