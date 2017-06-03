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

package org.apache.hyracks.storage.am.lsm.rtree.tuples;

import org.apache.hyracks.api.dataflow.value.ITypeTraits;
import org.apache.hyracks.storage.am.common.tuples.TypeAwareTupleWriterFactory;
import org.apache.hyracks.storage.am.lsm.common.api.ILSMTreeTupleWriter;

public class LSMRTreeTupleWriterFactoryForPointMBR extends TypeAwareTupleWriterFactory {

    private static final long serialVersionUID = 1L;
    private final int keyFieldCount;
    private final int valueFieldCount;
    private final boolean antimatterAware;
    private final boolean isAntimatter;

    public LSMRTreeTupleWriterFactoryForPointMBR(ITypeTraits[] typeTraits, int keyFieldCount, int valueFieldCount,
            boolean antimatterAware, boolean isDelete) {
        super(typeTraits);
        this.keyFieldCount = keyFieldCount;
        this.valueFieldCount = valueFieldCount;
        this.antimatterAware = antimatterAware;
        this.isAntimatter = isDelete;
    }

    @Override
    public ILSMTreeTupleWriter createTupleWriter() {
        return new LSMRTreeTupleWriterForPointMBR(typeTraits, keyFieldCount, valueFieldCount, antimatterAware,
                isAntimatter);
    }

}