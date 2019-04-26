package com.geocento.webapps.earthimages.emis.application.server.servlets;

import com.geocento.webapps.earthimages.emis.common.server.ServerUtil;
import com.geocento.webapps.earthimages.emis.application.server.imageapi.AoIsImport;
import com.geocento.webapps.earthimages.emis.application.server.imageapi.EIAPIUtil;
import com.google.gson.Gson;
import com.metaaps.webapps.libraries.server.KeyGenerator;
import com.metaaps.webapps.libraries.server.Measurer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class AoIsImportService extends HttpServlet {

	static private Logger logger = null;
	static private Measurer measurer = null;

    private KeyGenerator keyGenerator;

    public AoIsImportService() {
        // create the logger
        logger = Logger.getLogger(AoIsImportService.class);
        logger.info("Starting aoi import servlet");
        keyGenerator = new KeyGenerator(8);
    }

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String userName = ServerUtil.validateUser(request);
            System.out.println("Start");
            // default values for width and height
            InputStream filecontent = null;
            String fileName = null;
            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
            System.out.println("Items " + items.size());
            for (FileItem item : items) {
                if (item.isFormField()) {
                } else {
                    // Process form file field (input type="file").
                    String fieldName = item.getFieldName();
                    System.out.println("field name " + fieldName);
                    fileName = FilenameUtils.getName(item.getName());
                    System.out.println("file name " + fileName);
                    filecontent = item.getInputStream();
                }
            }
            System.out.println("Processing " + fileName);
            // check values
            if(filecontent == null) {
                throw new FileNotFoundException("no file provided");
            }
            AoIsImport importAoIsDTO = EIAPIUtil.importAoI(fileName, filecontent);
            if(importAoIsDTO != null) {
                response.setStatus(200);
                response.setContentType("text/html");
                response.getWriter().print("<html><body><value>" + new Gson().toJson(importAoIsDTO) + "</value></body></html>");
            } else {
                throw new Exception("Could not create AoI");
            }
        } catch (FileUploadException e) {
            writeError(response, "Could not read file");
        } catch(IOException e) {
            writeError(response, "File type not supported!");
        } catch (Exception e) {
            writeError(response, "Error whilst processing and storing aoi: " + e.getMessage());
        } finally {
            System.out.println("done");
        }
	}

    protected void writeError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(200);
        response.setContentType("text/html");
        response.getWriter().print("<html><body><error>" + message + "</error></body></html>");
    }

}
