package com.crm.personal.crm.shared;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/login",
            "/dashboard",
            "/403", "/500",
            "/app",
            "/app/{path:[^\\.]*}",
            "/app/{path:^(?!api$).*$}/**",
            "/customer-360/{customerId}",
            "/maintenance"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
