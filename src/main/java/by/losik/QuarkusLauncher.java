package by.losik;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.extern.slf4j.Slf4j;

@QuarkusMain
@Slf4j
public class QuarkusLauncher implements QuarkusApplication {
    @Override
    public int run(String... args) throws Exception {
        return 0;
    }
}
