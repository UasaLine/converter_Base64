package servis;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;
import service.FileStorageImpl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStorageImplTest {

    String pathZipIntoFile = "src\\test\\test_resource\\ElitBuket_SCALE_1000-90_002.zip";
    String nameFolder = "src\\test\\test_resource\\unpackZipFile";
    String zipFolder = "filestorageZip";

    @Test
    public void unpackZipFile_exist_files_processFilesFolder_js_file_is_injected() {

        FileStorageImpl fileStorage = new FileStorageImpl();

        fileStorage.unpackZipFile(pathZipIntoFile, nameFolder);

        File js = fileStorage.getFileFromFolder(nameFolder, ".js");
        File html = fileStorage.getFileFromFolder(nameFolder, ".html");

        Assert.assertTrue(js.exists() && html.exists());

        String replasJsString = js.getName();
        String resoultPath;
        int intoJsToHtml = 0;
        try{
            resoultPath = fileStorage.processFilesFolder(nameFolder, Paths.get(zipFolder));
            String htmlString = FileUtils.readFileToString(new File(zipFolder+"\\"+resoultPath));
            //into js to html
            intoJsToHtml  = htmlString.indexOf(replasJsString);
        }
        catch(
        Exception e){
            System.out.println(e.getMessage());
        }

        Assert.assertTrue(intoJsToHtml==-1);

    }


    @After
    public void deleteFileAndFolder() {
        try {
            FileSystemUtils.deleteRecursively(Paths.get(nameFolder));
            FileSystemUtils.deleteRecursively(Paths.get(zipFolder));
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

}