package com.liu.springbootdemo.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @RequestMapping("/hai~")
    public String hello(){
        System.out.println("I'm city!");
        return "Hai,gril! Could you follow me~?";
    }
}
