package org.beval.fibankcashdesk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FibankConfig
{
    @Value("${fibank.api-key}")
    private String apiKey;

    public String getApiKey()
    {
        return apiKey;
    }
}
