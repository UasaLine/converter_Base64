package hello;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.Assert.*;

public class FileStorageImplTest {
    MultipartFile mFile;

    @Before
    public void setUp() throws Exception {
        mFile = new MockMultipartFile("image1", "filename1.zip", "text/plain", "some xml".getBytes());
    }

    @Test
    @Ignore
    public void store_NO_NULL() {
        FileStorage fileStorage = new FileStorageImpl();
        String expected = fileStorage.store(mFile);


    }

}