package servis;

import org.junit.Test;
import service.FileStorageImpl;

public class FileStorageImplTest {


    @Test
    public void unpackZipFile_exist_files() {

        String pathZipIntoFile = "src\\test\\test_resource\\ElitBuket_SCALE_1000-90_002.zip";
        String nameFolder = "src\\test\\test_resource\\unpackZipFile";
        FileStorageImpl fileStorage = new FileStorageImpl();

        fileStorage.unpackZipFile(pathZipIntoFile,nameFolder);

    }

}