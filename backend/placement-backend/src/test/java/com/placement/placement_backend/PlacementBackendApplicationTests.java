package com.placement.placement_backend;

import com.placement.placement_backend.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = "FIREBASE_PROJECT_ID=test-project")
class PlacementBackendApplicationTests {

	@MockitoBean
	StudentRepository studentRepository;

	@Test
	void contextLoads() {
	}

}
