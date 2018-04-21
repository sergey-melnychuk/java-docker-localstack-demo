package edu.blog.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class FileServiceImplTest {
    private static final String S3_SERVICE_ENDPOINT = "http://localhost:4572/";

    private static final String BUCKET = "test-bucket";
    private static final String KEY = "path/to/file.json";

    @Test
    public void testFileServiceAgainstLocalStack() throws IOException {
        FileService fileService = FileServiceFactory.custom(BUCKET, S3_SERVICE_ENDPOINT);
        fileService.createBucket(BUCKET);
        fileService.put(KEY, "{\"created\":true}\n");
        String content = fileService.get(KEY);
        assertEquals("{\"created\":true}\n", content);
    }


    private static DockerClient docker;
    private static String containerId;

    @BeforeClass
    public static void beforeClass() throws IOException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();

        docker = DockerClientBuilder.getInstance(config).build();

        ExposedPort tcp443 = ExposedPort.tcp(443);
        ExposedPort tcp8080 = ExposedPort.tcp(8080);
        ExposedPort tcp4572 = ExposedPort.tcp(4572);

        Ports bindings = new Ports();
        bindings.bind(tcp443, new Ports.Binding("0.0.0.0", "443/tcp"));
        bindings.bind(tcp8080, new Ports.Binding("0.0.0.0", "8080/tcp"));
        bindings.bind(tcp4572, new Ports.Binding("0.0.0.0", "4572/tcp"));

        Volume volume = new Volume("/tmp/localstack");
        String temp = getTmpDir("testing", "localstack");

        CreateContainerResponse container = docker.createContainerCmd("localstack/localstack")
                .withName("testing-with-localstack")
                .withVolumes(volume)
                .withBinds(
                        new Bind(temp, volume, AccessMode.DEFAULT, SELContext.DEFAULT),
                        new Bind("/var/run/docker.sock", new Volume("/var/run/docker.sock")))
                .withEnv(
                        "LOCALSTACK_HOSTNAME=localhost",
                        "HOST_TMP_FOLDER=" + temp + "",
                        "DOCKER_HOST=unix:///var/run/docker.sock")
                .withExposedPorts(
                        tcp443,
                        tcp8080,
                        tcp4572)
                .withPortBindings(bindings)
                .exec();

        containerId = container.getId();

        docker.startContainerCmd(containerId).exec();

        delay(6000);
    }

    @AfterClass
    public static void afterClass() {
        if (docker != null && containerId != null) {
            docker.stopContainerCmd(containerId).exec();
            docker.removeContainerCmd(containerId).exec();
        }
    }

    private static String getTmpDir(String prefix, String name) throws IOException {
        Path dir = Files.createTempDirectory(prefix);
        dir.toFile().deleteOnExit();
        Path last = dir.resolve(name);
        Files.createDirectories(last);
        return last.toRealPath().toString();
    }

    private static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }
}
