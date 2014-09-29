package com.johannesbrodwall.infrastructure.web;

import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

public interface JsonController {

    JSONObject getJSON(HttpServletRequest req);

}
