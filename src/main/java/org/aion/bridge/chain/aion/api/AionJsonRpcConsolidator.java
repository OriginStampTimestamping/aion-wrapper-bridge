/*
 * This code is licensed under the MIT License
 *
 * Copyright (c) 2019 Aion Foundation https://aion.network/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.aion.bridge.chain.aion.api;

import org.aion.bridge.chain.aion.types.AionAddress;
import org.aion.bridge.chain.aion.types.AionBlock;
import org.aion.bridge.chain.aion.types.AionLog;
import org.aion.bridge.chain.aion.types.AionReceipt;
import org.aion.bridge.chain.base.api.ConsolidatedChainConnection;
import org.aion.bridge.chain.base.api.StatelessChainConnection;

import java.time.Duration;
import java.util.List;

public class AionJsonRpcConsolidator {
    private ConsolidatedChainConnection<AionBlock, AionReceipt, AionLog, AionAddress> api;

    public AionJsonRpcConsolidator(List<StatelessChainConnection<AionBlock, AionReceipt, AionLog, AionAddress>> connections) {
        api = new ConsolidatedChainConnection<>(connections);
    }

    public AionJsonRpcConsolidator(List<StatelessChainConnection<AionBlock, AionReceipt, AionLog, AionAddress>> connections, Duration timeout) {
        api = new ConsolidatedChainConnection<>(connections, timeout);
    }

    public ConsolidatedChainConnection<AionBlock, AionReceipt, AionLog, AionAddress> getApi() {
        return api;
    }
}
