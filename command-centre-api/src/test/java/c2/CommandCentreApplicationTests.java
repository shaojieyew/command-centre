package c2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
    properties = {
      "spring.autoconfigure.exclude=comma.seperated.ClassNames,com.example.FooAutoConfiguration"
    })
@SpringBootTest
class CommandCentreApplicationTests {

  @Test
  void contextLoads() {}
}
