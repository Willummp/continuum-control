package br.infnet.continuum.control;

import org.springframework.boot.SpringApplication;

public class TestContinuumControlApplication {

	public static void main(String[] args) {
		SpringApplication.from(ContinuumControlApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
