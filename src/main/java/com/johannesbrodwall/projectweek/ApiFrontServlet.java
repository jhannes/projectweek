package com.johannesbrodwall.projectweek;

import com.johannesbrodwall.infrastructure.web.GetController;
import com.johannesbrodwall.infrastructure.web.JsonGetController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApiFrontServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        GetController controller = getControllers().get(req.getPathInfo());
        if (controller != null) {
            controller.doGet(req, resp);
        } else {
            super.doGet(req, resp);
        }
    }

    private Map<String,GetController> getControllers() {
        Map<String, GetController> controllers = new HashMap<>();

        controllers.put("/menu", new JsonGetController(new MenuController()));

        return controllers;
    }

}
