/**
 *
 */
package de.klassenserver7b.k7bot.util;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

/**
 * @author K7
 */
public class EntityHttpClientResponseHandler implements HttpClientResponseHandler<HttpEntity> {

    @Override
    public HttpEntity handleResponse(ClassicHttpResponse response) throws IOException {

        final HttpEntity entity = response.getEntity();
        if (response.getCode() >= HttpStatus.SC_REDIRECTION) {
            EntityUtils.consume(entity);
            throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
        }
        return entity;
    }

}
