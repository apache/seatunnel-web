package org.apache.seatunnel.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HttpClientService {

    @Autowired private RestTemplate restTemplate;

    public String fetchDataFromApi(String taskId) {
        try {
            String apiUrl = "http://api.example.com/task/" + taskId;
            return restTemplate.getForObject(apiUrl, String.class);
        } catch (Exception e) {
            log.error("调用API失败，taskId: {}", taskId, e);
            throw new RuntimeException("API调用失败", e);
        }
    }
}
