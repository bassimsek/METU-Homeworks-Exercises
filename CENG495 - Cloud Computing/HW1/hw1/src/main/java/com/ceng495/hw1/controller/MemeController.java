package com.ceng495.hw1.controller;

import com.ceng495.hw1.model.Meme;
import com.ceng495.hw1.model.MemeURL;
import com.ceng495.hw1.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.HashMap;
import java.util.Map;


@Controller
public class MemeController {



    @Autowired
    private RestService restService;

    @GetMapping("/")
    public String homepage(Model theModel) {
        Meme meme = new Meme();
        theModel.addAttribute("meme",meme);
        return "index";
    }



    @PostMapping("/createMeme")
    public String createMeme(@ModelAttribute("meme") Meme theMeme, RedirectAttributes redirectAttrs) {

        String url = "https://api.memegen.link/images/custom";


        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);


        String[] textLines = new String[2];
        textLines[0] = theMeme.getTopText();
        textLines[1] = theMeme.getBottomText();

        Map<String, Object> map = new HashMap<>();
        map.put("image_url", theMeme.getWebURL());
        map.put("text_lines", textLines);

        // build the request
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

        // send POST request
        ResponseEntity<MemeURL> response = this.restService.getRestTemplate().postForEntity(url, entity, MemeURL.class);

        String createdMemeURL;

        // check response status code
        if (response.getStatusCode() == HttpStatus.CREATED) {
            createdMemeURL = response.getBody().getUrl();
        } else {
            createdMemeURL = null;
        }


        // add returned image url to the model
        redirectAttrs.addFlashAttribute("createdMemeURL", createdMemeURL);

        redirectAttrs.addFlashAttribute("message", "You successfully created the meme:");

        // return html template
        return "redirect:/";
    }

}
