package hello;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;


import javax.imageio.ImageIO;


@Service
public class FileStorageImpl implements FileStorage{

    Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final Path rootLocation = Paths.get("filestorage");
    private static final Path rootLocationZip = Paths.get("filestorageZip");

    @Override
    public void store(MultipartFile file){
        try {
            Files.copy(file.getInputStream(), this.rootLocation.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("FAIL! -> message = " + e.getMessage());
        }

        //processing
        List<String> listFoldrtForProc= unpackZip(String.valueOf(rootLocation));
        processFolderFiles(listFoldrtForProc);
    }


    @Override
    public Resource loadFile(String filename) {
        try {
            Path file = rootLocationZip.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) {
                return resource;
            }else{
                throw new RuntimeException("FAIL!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error! -> message = " + e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
        FileSystemUtils.deleteRecursively(rootLocationZip.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectory(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage!");
        }
    }

    @Override
    public Stream<Path> loadFiles() {
        try {
            return Files.walk(this.rootLocationZip, 1)
                    .filter(path -> !path.equals(this.rootLocationZip))
                    .map(this.rootLocationZip::relativize);
        }
        catch (IOException e) {
            throw new RuntimeException("\"Failed to read stored file");
        }
    }

    public List<String> unpackZip(String pathDir){

        List<String> listFFR = new ArrayList<>();

        File dir = new File(pathDir);
        File[] arrFiles = dir.listFiles();
        List<File> lst = Arrays.asList(arrFiles);

        for(File myFile:lst) {

            if (!myFile.getName().endsWith(".zip")){//filters only zip
               continue;
            }

            String nameFolder = myFile.getName().replace(".zip","");
            Path pathFolder = Paths.get(nameFolder);

            if (Files.exists(pathFolder)) {//filters only unprocessed
                continue;
            }

            try (ZipFile zip = new ZipFile(myFile.getPath())) {
                Files.createDirectory(pathFolder);
                Enumeration entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    String filePath = entry.getName().replace("/","\\");
                    if (entry.isDirectory()) {
                        new File(nameFolder+"\\"+filePath).mkdirs();
                        log.info("unpackZip Directory: "+nameFolder+"\\"+filePath);
                    } else {
                        try(BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(nameFolder+"\\"+filePath)));
                        InputStream inputStream = zip.getInputStream(entry)) {
                            write(inputStream, bufferedOutputStream);
                            log.info("unpackZip file: "+nameFolder+"\\"+filePath);
                        }
                        catch (Exception e){
                            log.info("unpackZip: "+e.getMessage());
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            listFFR.add(String.valueOf(pathFolder));
        }
        return listFFR;
    }

    public void processFolderFiles(List<String> listFoldrtForProc){

        for(String foldr:listFoldrtForProc) {

            File dirIntoPars = new File(foldr);
            File[] arrFilesIntoPars = dirIntoPars.listFiles();
            List<File> lstIntoPars = Arrays.asList(arrFilesIntoPars);

            File js = null;
            File html = null;
            File folderPicter = null;

            for (File parsFile : lstIntoPars){
                if(parsFile.isDirectory()){
                    folderPicter =  parsFile;
                }
                else if(parsFile.getName().endsWith(".html")){
                    html = parsFile;
                }
                else if(parsFile.getName().endsWith(".js")){
                    js = parsFile;
                }
            }

            if(!(js ==null)){
                try {
                    String replasJsString = js.getName();
                    String toReplasJsString = FileUtils.readFileToString(js);

                    String htmlString = FileUtils.readFileToString(html);
                    //into js to html
                    htmlString = htmlString.replace("src=\""+replasJsString+"\">", ">"+toReplasJsString);

                    File newHtmlFile = new File(foldr+"/"+html.getName().replace(".html","DFP.html"));
                    FileUtils.writeStringToFile(newHtmlFile, htmlString);
                    html = newHtmlFile;

                    newHtmlFile = null;
                }
                catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }


            }

            if (!(html ==null)){
                try {
                    BufferedReader bufTextFile = new BufferedReader(new FileReader(html));
                    Map<String, String> replacementMap = new HashMap<>();
                    boolean bodyTagOopen = false;
                    boolean scriptTagOopen = false;

                    while (bufTextFile.ready()) {

                        String lineFile = bufTextFile.readLine();

                        //check on the picture
                        int startingIndex = lineFile.indexOf("{src:");
                        int finalIndex = lineFile.indexOf("\", id:\"");
                        if ((startingIndex != -1) && (finalIndex != -1)) {
                            String replaceKey = lineFile.substring(startingIndex, finalIndex);
                            String replaceValue = convertPictureFromLineToBase64(replaceKey.replace("{src:\"",""), foldr);

                            if(replaceValue=="null") {
                                log.error("replaceValue: null");
                            }

                            replacementMap.put(replaceKey, replaceValue);
                        }
                        //check on body
                        if(!bodyTagOopen && lineFile.indexOf("<body")!=-1 ){
                            bodyTagOopen = true;
                            continue;
                        }
                        if(bodyTagOopen && lineFile.indexOf("<a href")!=-1){
                            replacementMap.put(lineFile, "<a href=\"%%CLICK_URL_UNESC%%http://ngs.ru\" target=\"_blank\">");
                            bodyTagOopen = false;
                        }
                        else if (bodyTagOopen){
                            bodyTagOopen = false;
                        }
                        //check script
                        if(!scriptTagOopen && lineFile.indexOf("script")!=-1){
                            scriptTagOopen =  true;
                            continue;
                        }
                        if(scriptTagOopen && lineFile.indexOf("var")!=-1){
                            replacementMap.put(lineFile, "");
                            scriptTagOopen =  false;
                        }
                        else if(scriptTagOopen){
                            scriptTagOopen =  false;
                        }

                   }

                    bufTextFile.close();

                    String htmlString = FileUtils.readFileToString(html);
                    for (Map.Entry<String, String> par : replacementMap.entrySet()) {
                        htmlString = htmlString.replace(par.getKey(), par.getValue());
                    }
                    File newHtmlFile = new File(rootLocationZip+"/"+html.getName().replace(".html","DFP.html"));
                    FileUtils.writeStringToFile(newHtmlFile, htmlString);

                    newHtmlFile = null;
                }
                catch (Exception ex){
                    System.out.println(ex.getMessage());
                }
            }
            html = null;
            js = null;
            dirIntoPars = null;



            deleteToList(foldr);
        }

    }

    public void packZip(List<String> listFoldrtForProc,String folderZipEnd){
        for(String pathForder:listFoldrtForProc){

            File dir = new File(pathForder);
            File[] arrFiles = dir.listFiles();
            List<File> lst = Arrays.asList(arrFiles);

            for (File delFile:lst){
                if(!delFile.isDirectory()){
                    delFile.delete();
                }
            }

            dir = new File(pathForder+"/zip");
            arrFiles = dir.listFiles();
            lst = Arrays.asList(arrFiles);

            for (File delFile:lst){
                if(!delFile.isDirectory()){
                    delFile.renameTo(new File(pathForder, delFile.getName()));
                }
            }
        }
    }

    private static void write(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        out.close();
        in.close();
    }

    public String convertPictureFromLineToBase64(String replaceKey,String lstIntoPars) {

        String pathPicture = lstIntoPars+"\\"+replaceKey.replace("/","\\");
        log.info("dirPicture: "+pathPicture);

        try {
            File picture = new File(pathPicture);
            log.info("picture: "+picture.getPath());
            String imgstr = encodeFileToBase64Binary(picture);
            log.info("picture: Base64."+imgstr);
            picture = null;
            return "{type:createjs.AbstractLoader.IMAGE, src:\"data:image/png;base64,"+imgstr;

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return "";
    }

    public static String encodeToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            //BASE64Encoder encoder = new BASE64Encoder();
            //imageString = encoder.encode(imageBytes);

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }

    private String encodeFileToBase64Binary(File file){
        String encodedfile = null;
        log.info("picture in encode1: "+file.getPath());
        log.info("picture in encode length 1: "+file.length());
        try (FileInputStream fileInputStreamReader = new FileInputStream(file)) {
            log.info("picture in encode2: "+file.getPath());
            byte[] bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = new String(Base64.encodeBase64(bytes), "UTF-8");
            log.info("encodedfile: "+encodedfile);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            log.error(e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            log.error(e.getMessage());
        }

        return encodedfile;
    }

    private void deleteToList(String strintPath){

            FileSystemUtils.deleteRecursively(Paths.get(strintPath).toFile());

    }
}

