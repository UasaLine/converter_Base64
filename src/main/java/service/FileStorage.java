package service;




import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
public interface FileStorage {
    public String store(MultipartFile file);
    public Resource loadFile(String filename);
    public void deleteAll();
    public void init();
    public Stream<Path> loadFiles();
}
