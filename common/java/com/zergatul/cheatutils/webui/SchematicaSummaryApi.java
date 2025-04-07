package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.automation.Schematica;

public class SchematicaSummaryApi extends ApiBase {

    @Override
    public String getRoute() {
        return "schematica-summary";
    }

    @Override
    public String get() throws Throwable {
        return gson.toJson(Schematica.instance.getSummary());
    }

    @Override
    public String delete(String id) throws Throwable {
        if (id.equals("all")) {
            Schematica.instance.clear();
            return "{}";
        }

        Schematica.instance.remove(Integer.parseInt(id));
        return "{}";
    }
}