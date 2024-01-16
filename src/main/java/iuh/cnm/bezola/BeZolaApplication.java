package iuh.cnm.bezola;

import iuh.cnm.bezola.models.Role;
import iuh.cnm.bezola.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BeZolaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeZolaApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(RoleRepository roleRepository){
        return args -> {
            Role role = new Role("1", "USER");
            Role role1 = new Role("2", "ADMIN");
            roleRepository.save(role);
            roleRepository.save(role1);
        };
    }
}
