package crossjoin.challenge.controller;

import crossjoin.challenge.parser.ThreadDumpParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class ParserController {

    @GetMapping("/parseFThreadDumps")
    public String parseFThreadDumps(@RequestParam String path){

        ThreadDumpParser.parse(path);
        return "Success";
    }

}
