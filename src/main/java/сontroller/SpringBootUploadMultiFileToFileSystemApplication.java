package сontroller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import service.FileStorage;

import javax.annotation.Resource;


@SpringBootApplication
@ComponentScan(basePackages = {"сontroller","servis"})
public class SpringBootUploadMultiFileToFileSystemApplication implements CommandLineRunner {

    @Resource
    FileStorage fileStorage;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootUploadMultiFileToFileSystemApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        fileStorage.deleteAll();
        fileStorage.init();
    }
}
