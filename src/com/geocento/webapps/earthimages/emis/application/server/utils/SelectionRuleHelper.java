package com.geocento.webapps.earthimages.emis.application.server.utils;

import com.google.gson.Gson;
import org.apache.log4j.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Created by thomas on 04/03/2016.
 */
public class SelectionRuleHelper {

    static Logger logger = Logger.getLogger(SelectionRuleHelper.class);

    public static SelectionRule runFilterRule(String selectionRule, String aoiWKT) throws Exception {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        engine.put("aoiWKT", aoiWKT);
        try {
            engine.eval(selectionRule + " " +
                            "function getSelectionRule() {return '{sensorFilters:' + JSON.stringify(sensorFilters) + ', productFilters: ' + JSON.stringify(productFilters) + '}'};"
            );
            String selectionRuleString = (String) ((Invocable) engine).invokeFunction("getSelectionRule");
            return new Gson().fromJson(selectionRuleString, SelectionRule.class);
        } catch (Exception e) {
            throw new Exception("Failed to run the rule script");
        }
    }

}
