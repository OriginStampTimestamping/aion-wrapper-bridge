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

package org.aion.bridge.chain.base;

import com.google.common.base.Stopwatch;
import org.aion.bridge.chain.base.api.ApiFunction;
import org.aion.bridge.chain.base.api.QuorumNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InterruptedIOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class Consolidator {
    private static final Logger log = LoggerFactory.getLogger(Consolidator.class);

    // Consolidates calls into a single response
    public static <T, I> T batchCall(ApiFunction<I, T> method, List<I> inputs,
                                     long timeout, TimeUnit timeoutUnit)
            throws QuorumNotAvailableException {
        T response = null;
        Stopwatch timer = Stopwatch.createStarted();
        for (I input : inputs) {
            if (timer.elapsed(timeoutUnit) > timeout) {
                break;
            }
            try {
                response = method.apply(input);
                break;
            } catch (Exception e) {
                if (!(e.getCause() instanceof InterruptedIOException)) {
                    log.trace("Consolidator encountered non-critical exception; Exception Message: {}", e.getCause());
                }
            }
        }


        if (response == null) {
            throw new QuorumNotAvailableException("Could not achieve quorum.");
        }

        return response;
    }
}
