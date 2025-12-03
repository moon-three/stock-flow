package com.seohee.online.controller;

import com.seohee.online.service.DataInitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DataInitController {

    private final DataInitService dataInitService;

    @GetMapping("/data-init")
    public String dataInit() {
        dataInitService.init();
        return "success";
    }

}
