package com.geocento.webapps.earthimages.emis.admin.server.servlets;

import com.geocento.webapps.earthimages.emis.application.server.websocket.NotificationSocket;
import com.geocento.webapps.earthimages.emis.common.server.ServerUtil;
import com.geocento.webapps.earthimages.emis.common.server.domain.ProductFetchTask;
import com.geocento.webapps.earthimages.emis.common.server.domain.ProductOrder;
import com.geocento.webapps.earthimages.emis.common.server.publishapi.PublishAPIUtils;
import com.geocento.webapps.earthimages.emis.common.server.utils.EMF;
import com.geocento.webapps.earthimages.emis.common.server.utils.OrderHelper;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.STATUS;
import com.metaaps.webapps.libraries.server.KeyGenerator;
import com.metaaps.webapps.libraries.server.Measurer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class ProductImportService extends HttpServlet {

	static private Logger logger = null;
	static private Measurer measurer = null;

    private KeyGenerator keyGenerator;

    public ProductImportService() {
        // create the logger
        logger = Logger.getLogger(ProductImportService.class);
        logger.info("Starting aoi import servlet");
        keyGenerator = new KeyGenerator(8);
    }

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String userName = ServerUtil.validateUserAdministrator(request);
            System.out.println("Start");
            // default values
            InputStream filecontent = null;
            String productOrderId = null;
            String fileName = null;
            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
            System.out.println("Items " + items.size());
            for (FileItem item : items) {
                if (item.isFormField()) {
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString();
                    if(fieldName.equalsIgnoreCase("productId")) {
                        productOrderId = fieldValue;
                    }
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
            if(productOrderId == null) {
                throw new EIException("Product order not found for product order id #" + productOrderId);
            }
            EntityManager em = EMF.get().createEntityManager();
            try {
                ProductOrder productOrder = em.find(ProductOrder.class, productOrderId);
                if(productOrder == null) {
                    throw new EIException("Product order not found for product order id #" + productOrderId);
                }
                em.getTransaction().begin();
                File productDirectory = OrderHelper.getProductDirectory(productOrder, true);
                File productFile = new File(productDirectory, fileName);
                FileUtils.copyInputStreamToFile(filecontent, productFile);
                productOrder.setFileLocation(productFile.getAbsolutePath());
                productOrder.setDeliveredTime(new Date());
                productOrder.setStatus(PRODUCTORDER_STATUS.Completed);
                // now look for existing product fetch task or start a new product fetch task
                ProductFetchTask productFetchTask = null;
                try {
                    TypedQuery<ProductFetchTask> query = em.createQuery("select p from ProductFetchTask p where p.productOrder = :productOrder", ProductFetchTask.class);
                    query.setParameter("productOrder", productOrder);
                    productFetchTask = query.getSingleResult();
                } catch (Exception e) {
                    productFetchTask = PublishAPIUtils.createProductFetchTask(productOrder);
                }
                productFetchTask.setStatus(STATUS.downloaded);
                productFetchTask.setFetchDate(new Date());
                em.persist(productFetchTask);
                em.getTransaction().commit();
                NotificationSocket.notifyProductOrderStatusChanged(productOrder);
            } finally {
                if(em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
            response.setStatus(200);
            response.setContentType("text/html");
            response.getWriter().print("<html><body><value>" + fileName + "</value></body></html>");
        } catch (FileUploadException e) {
            logger.error(e.getMessage(), e);
            writeError(response, "Could not read file");
        } catch(IOException e) {
            logger.error(e.getMessage(), e);
            writeError(response, "File type not supported!");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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
