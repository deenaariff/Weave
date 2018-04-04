package test;

import configuration.Configuration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestConfigurations {

    @Test
    public void testConfigData() {

        Configuration config;

        String path = "./config.yaml";

        // Create a new Raft Node
        Yaml yaml = new Yaml();
        try( InputStream in = Files.newInputStream(Paths.get(path))) {
            config = yaml.loadAs(in,Configuration.class);
        } catch (Exception e) {
            throw new RuntimeException("Unable to Locate Configuration File");
        }

        assertEquals(8080,(int) config.getHeartBeatPort());
        assertEquals(8081,(int) config.getVotingPort());

    }
}
