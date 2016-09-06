/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.asterix.runtime.operators.joins;

import org.apache.asterix.om.pointables.nonvisitor.AIntervalPointable;
import org.apache.asterix.runtime.evaluators.comparisons.ComparisonHelper;
import org.apache.asterix.runtime.evaluators.functions.temporal.IntervalLogic;
import org.apache.hyracks.api.comm.IFrameTupleAccessor;
import org.apache.hyracks.api.exceptions.HyracksDataException;
import org.apache.hyracks.data.std.api.IPointable;
import org.apache.hyracks.data.std.primitive.TaggedValuePointable;
import org.apache.hyracks.data.std.primitive.VoidPointable;
import org.apache.hyracks.dataflow.std.buffermanager.ITupleAccessor;

public abstract class AbstractIntervalMergeJoinChecker implements IIntervalMergeJoinChecker {

    private static final long serialVersionUID = 1L;

    protected final int idLeft;
    protected final int idRight;

    protected final IntervalLogic il = new IntervalLogic();

    protected final TaggedValuePointable tvp = (TaggedValuePointable) TaggedValuePointable.FACTORY.createPointable();
    protected final AIntervalPointable ipLeft = (AIntervalPointable) AIntervalPointable.FACTORY.createPointable();
    protected final AIntervalPointable ipRight = (AIntervalPointable) AIntervalPointable.FACTORY.createPointable();

    protected final ComparisonHelper ch = new ComparisonHelper();
    protected final IPointable startLeft = VoidPointable.FACTORY.createPointable();
    protected final IPointable endLeft = VoidPointable.FACTORY.createPointable();
    protected final IPointable startRight = VoidPointable.FACTORY.createPointable();
    protected final IPointable endRight = VoidPointable.FACTORY.createPointable();

    public AbstractIntervalMergeJoinChecker(int idLeft, int idRight) {
        this.idLeft = idLeft;
        this.idRight = idRight;
    }

    @Override
    public boolean checkToRemoveLeftActive() {
        return true;
    }

    @Override
    public boolean checkToRemoveRightActive() {
        return true;
    }

    @Override
    public boolean checkToSaveInMemory(ITupleAccessor accessorLeft, ITupleAccessor accessorRight)
            throws HyracksDataException {
        IntervalJoinUtil.getIntervalPointable(accessorLeft, idLeft, tvp, ipLeft);
        IntervalJoinUtil.getIntervalPointable(accessorRight, idRight, tvp, ipRight);
        ipLeft.getEnd(endLeft);
        ipRight.getStart(startRight);
        return ch.compare(ipLeft.getTypeTag(), ipRight.getTypeTag(), endLeft, startRight) > 0;
    }

    @Override
    public boolean checkToRemoveInMemory(ITupleAccessor accessorLeft, ITupleAccessor accessorRight)
            throws HyracksDataException {
        IntervalJoinUtil.getIntervalPointable(accessorLeft, idLeft, tvp, ipLeft);
        IntervalJoinUtil.getIntervalPointable(accessorRight, idRight, tvp, ipRight);
        ipLeft.getStart(startLeft);
        ipRight.getEnd(endRight);
        return !(ch.compare(ipLeft.getTypeTag(), ipRight.getTypeTag(), startLeft, endRight) < 0);
    }

    @Override
    public boolean checkToLoadNextRightTuple(ITupleAccessor accessorLeft, ITupleAccessor accessorRight)
            throws HyracksDataException {
        return checkToSaveInMemory(accessorLeft, accessorRight);
    }

    @Override
    public boolean checkToSaveInResult(ITupleAccessor accessorLeft, ITupleAccessor accessorRight)
            throws HyracksDataException {
        return checkToSaveInResult(accessorLeft, accessorLeft.getTupleId(), accessorRight, accessorRight.getTupleId(),
                false);
    }

    @Override
    public boolean checkToRemoveInMemory(IFrameTupleAccessor accessorLeft, int leftTupleIndex,
            IFrameTupleAccessor accessorRight, int rightTupleIndex) throws HyracksDataException {
        IntervalJoinUtil.getIntervalPointable(accessorLeft, leftTupleIndex, idLeft, tvp, ipLeft);
        IntervalJoinUtil.getIntervalPointable(accessorRight, rightTupleIndex, idRight, tvp, ipRight);
        ipLeft.getStart(startLeft);
        ipRight.getEnd(endRight);
        return !(ch.compare(ipLeft.getTypeTag(), ipRight.getTypeTag(), startLeft, endRight) < 0);
    }

    @Override
    public boolean checkToSaveInResult(IFrameTupleAccessor accessorLeft, int leftTupleIndex,
            IFrameTupleAccessor accessorRight, int rightTupleIndex, boolean reversed) throws HyracksDataException {
        if (reversed) {
            IntervalJoinUtil.getIntervalPointable(accessorLeft, leftTupleIndex, idLeft, tvp, ipRight);
            IntervalJoinUtil.getIntervalPointable(accessorRight, rightTupleIndex, idRight, tvp, ipLeft);
        } else {
            IntervalJoinUtil.getIntervalPointable(accessorLeft, leftTupleIndex, idLeft, tvp, ipLeft);
            IntervalJoinUtil.getIntervalPointable(accessorRight, rightTupleIndex, idRight, tvp, ipRight);
        }
        return compareInterval(ipLeft, ipRight);
    }

    @Override
    public abstract boolean compareInterval(AIntervalPointable ipLeft, AIntervalPointable ipRight)
            throws HyracksDataException;

    @Override
    public abstract boolean compareIntervalPartition(int s1, int e1, int s2, int e2);

}