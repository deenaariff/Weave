package test;

import configuration.Configuration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
/*import java.nio.file.Files;
import java.nio.file.Paths;*/
import java.util.Map;

public class TestConfigurations {

    @Test
    public void testConfigData() {

        Configuration config;

        String path = System.getProperty("user.dir") + "/src/config.yaml";

        // Create a new Raft Node
        /*Yaml yaml = new Yaml();
        try( InputStream in = Files.newInputStream(Paths.get(path))) {
            config = yaml.loadAs(in,Configuration.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String,Integer> ports = config.getPorts();

        assertEquals(8080,(int) ports.get("heartbeat"));
        assertEquals(8081,(int) ports.get("voting"));*/

    }
}
