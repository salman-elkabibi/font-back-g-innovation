import org.springframework.web.bind.annotation.GetMapping;

public class TestController {
    @GetMapping("/")
    public String HOME (){
        return "backend ";
    }
}
