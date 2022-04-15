package com.gnc.task.application.data.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Component
public class GNCApiClient {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private ObjectMapper mapper;
 //   @Value("${gnc-client.url}")
 //   private String URL_SERVICE;

    private Gson gson = new Gson();

    public GNCApiClient() {
        SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate
                .getRequestFactory();
        requestFactory.setReadTimeout(60000);
        requestFactory.setConnectTimeout(60000);
    }

    public <T> T get(String url, Class<T> responseType) {
        final String handledUrl = handleUrl(url);
        return execute(() -> {
            HttpEntity<Object> requestEntity = new HttpEntity<>(buildHeaders());
            LOG.debug("connecting with MockAPIClient using {}", requestEntity);
            return restTemplate.exchange(handledUrl, HttpMethod.GET, requestEntity, responseType);
        });
    }

    public <T> T post(String url, Object request, Class<T> responseType) {
        final String handledUrl = handleUrl(url);
        return execute(() -> {
            MultiValueMap<String, String> headers = buildHeaders();
            HttpEntity<Object> requestEntity = new HttpEntity<>(request, headers);
            LOG.debug("connecting with MockAPIClient using {}", requestEntity);
            return restTemplate.exchange(handledUrl, HttpMethod.POST, requestEntity, responseType);
        });
    }

    private MultiValueMap<String, String> buildHeaders() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private String handleUrl(String url) {
        Assert.hasLength(url, "url should be not null and not empty");
        return  url;
    }

    public <T> T execute(Supplier<ResponseEntity<T>> fn) {
        LOG.debug("executing " + this.getClass());
        return fn.get().getBody();
    }
}
