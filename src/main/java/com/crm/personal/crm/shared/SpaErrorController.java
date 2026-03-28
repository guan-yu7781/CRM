package com.crm.personal.crm.shared;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Catch-all error handler for the Vue SPA.
 * Any URL that doesn't match a Spring controller or static asset (e.g. /asdasdas)
 * gets a 404 from the servlet container, which routes here. We forward to
 * index.html so Vue Router can display its own 404 page.
 */
@Controller
public class SpaErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // Always serve the SPA shell — Vue Router handles 404/403/500 pages
        return "forward:/index.html";
    }
}
