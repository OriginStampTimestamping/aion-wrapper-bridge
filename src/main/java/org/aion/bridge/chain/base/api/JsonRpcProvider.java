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

package org.aion.bridge.chain.base.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.aion.bridge.chain.base.rpc.JsonRpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
@ThreadSafe
public class JsonRpcProvider {

    private final Logger log = LoggerFactory.getLogger(JsonRpcProvider.class.getName());
    private final String url;

    private final ObjectMapper mapper = new ObjectMapper();

    private OkHttpClient client;

    private static final long TIMEOUT_DEFAULT = 10;

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf8");

    public JsonRpcProvider(String url, Long timeoutSeconds, OkHttpClient okHttpClient) {
        this.url = url;
        client = okHttpClient;
    }

    public JsonRpcProvider(String url, OkHttpClient okHttpClient) {
        this(url, TIMEOUT_DEFAULT, okHttpClient);
    }

    private ResponseBody makeHttpCall(String jsonPayload) throws IncompleteApiCallException, MalformedApiResponseException {
        Call httpCall = null;
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MEDIA_TYPE_JSON, jsonPayload))
                    .build();

            //log.debug("HTTP Payload: {}", jsonPayload);

            httpCall = client.newCall(request);
            Response response;
            try {
                response = httpCall.execute();
            } catch (IOException e) {
                throw new IncompleteApiCallException(e.getCause());
            }

            ResponseBody body = response.body();

            if (response.code() != 200) {
                String errBody = "not available";
                try {
                    if (body != null)
                        errBody = body.string();
                } catch (Exception e) {
                }

                log.error("url=[" + url + "] Received non-200 response: code=[{}] body=[{}]", response.code(), errBody);
                throw new MalformedApiResponseException(String.format("HTTP invalid response: %d", response.code()));
            }

            if (body == null)
                throw new MalformedApiResponseException("url=[" + url + "] HTTP response body is null");

            return body;
        } catch (Exception e) {
            if (httpCall != null)
                httpCall.cancel();

            throw e;
        }
    }

    public <T> T send(JsonRpcRequest call, Class<T> responseType)
            throws IncompleteApiCallException, MalformedApiResponseException {
        String payload;
        try {
            payload = mapper.writeValueAsString(call);
        } catch (IOException e) {
            throw new IllegalArgumentException("url=[" + url + "] JsonRpcRequest object not serializable");
        }

        ResponseBody body = makeHttpCall(payload);

        T result;
        try {
            String responseBody = body.string();
            if (responseType.equals(String.class))
                result = responseType.cast(responseBody);
            else {
                log.debug(responseBody);
                result = mapper.readValue(responseBody, responseType);
            }
        } catch (Exception e) {
            throw new MalformedApiResponseException(e.getCause());
        }

        return result;
    }

    // Lets us send batch of the same type for now. Also, don't depend on the order of the request objects
    public <T> List<T> sendBatch(List<? extends JsonRpcRequest> call, Class<T> responseType)
            throws IncompleteApiCallException, MalformedApiResponseException {

        String payload;
        try {
            payload = mapper.writeValueAsString(call);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("url=[" + url + "] JsonRpcRequest object not serializable");
        }

        log.trace("HTTP Payload: {}", payload);
        ResponseBody body = makeHttpCall(payload);

        try {
            String body_string = body.string();
            log.trace("http response: {}", body_string);
            return mapper.readValue(body_string, mapper.getTypeFactory().constructCollectionType(List.class, responseType));
        } catch (Exception e) {
            log.trace("HTTP body encoding error ", e);
            throw new MalformedApiResponseException(e.getCause());
        }
    }

    public String getUrl() {
        return url;
    }

    public void evictConnections() {
        log.debug("Closing OkHttp connections.");
        client.dispatcher().executorService().shutdown();
        this.client.connectionPool().evictAll();
    }
}
