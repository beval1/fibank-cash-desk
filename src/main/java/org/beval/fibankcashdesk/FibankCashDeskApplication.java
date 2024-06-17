package org.beval.fibankcashdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FibankCashDeskApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(FibankCashDeskApplication.class, args);
    }

}
