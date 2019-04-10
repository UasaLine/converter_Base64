package сontroller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import service.FileStorage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UploadFileController {

    @Autowired
    FileStorage fileStorage;

    @GetMapping("/")
    public String index() {
        //return "uploadform";

        return "index_2";
    }

    @PostMapping("/")
    public String uploadMultipartFile(@RequestParam("files") MultipartFile[] files, @RequestParam(defaultValue = "0") String fullScreen, Model model) {

        List<String> fileNames = null;
        fileStorage.setFullScreen(fullScreen);

        try {
            fileNames = Arrays.asList(files)
                    .stream()
                    .map(file -> {
                         return fileStorage.store(file);
                    })
                    .collect(Collectors.toList());

            model.addAttribute("message", "Files uploaded successfully!");
            model.addAttribute("files", fileNames);
        } catch (Exception e) {
            model.addAttribute("message", "Fail!");
            model.addAttribute("files", fileNames);
        }

        //return "uploadform";

        return "index_2";
    }

}
