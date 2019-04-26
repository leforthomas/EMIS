package com.geocento.webapps.earthimages.emis.common.server.utils;

import com.geocento.webapps.earthimages.emis.common.server.ScriptException;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * Created by thomas on 21/09/2017.
 */
public class ScriptsUtil {

    static private Logger logger = Logger.getLogger(ScriptsUtil.class);

    public static String callPythonScript(String scriptPath,
                                        String... parameters) throws Exception {
        File scriptFile = new File(scriptPath);
        if(!scriptFile.exists()) {
            throw new Exception("script does not exist at specified location");
        }
        // TODO - use ulimit -m RAM_VALUE to make sure we don't over use the resources? also nice -n to give lower priority?

        List<String> commands = ListUtil.toList(new String[] {
                "python",
                scriptFile.getAbsolutePath(),
        });
        commands.addAll(ListUtil.toList(parameters));
        // TODO - add a listener to send processing outputs to a websocket
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        // TODO - add parameters
        try {
            Process process = processBuilder.start();
            logger.debug("Running python " + StringUtils.join(commands, " "));
            String output = executeCommand(process);
            logger.debug("Script finished");
            return output;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw
                    e instanceof ScriptException ? new Exception("Error running script, reason is: " + e.getMessage()) :
                            new Exception("Could not run script " + scriptFile.getAbsolutePath() + ", reason " + e.getMessage());
        }
    }

    public static String executeCommand(Process process) throws Exception {
        StringBuffer outputStringBuffer = new StringBuffer();
        StringBuffer errorStringBuffer = new StringBuffer();
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), errorStringBuffer);
        // any output?
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), outputStringBuffer);
        // kick them off
        errorGobbler.start();
        outputGobbler.start();
        // any error???
        int exitVal = process.waitFor();
        System.out.println("ExitValue: " + exitVal);

        if (errorStringBuffer.length() > 0) {
            System.out.println("ERROR: " + errorStringBuffer.toString());
            System.out.println("OUTPUT: " + outputStringBuffer.toString());
            throw new ScriptException(errorStringBuffer.toString());
        }
        return outputStringBuffer.toString();
    }

}
