package tn.esprit.clientmanagmentctinetwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
/*@EnableScheduling // Add this to allow background tasks*/
public class ClientManagmentCtiNetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientManagmentCtiNetworkApplication.class, args);
    }

}
