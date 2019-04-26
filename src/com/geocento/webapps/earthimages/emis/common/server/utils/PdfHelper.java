package com.geocento.webapps.earthimages.emis.common.server.utils;

import com.geocento.webapps.earthimages.emis.common.share.EIException;
import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class PdfHelper {

    // create the logger
    static Logger logger = Logger.getLogger(PdfHelper.class);

    public PdfHelper()
    {
        logger.info("Starting the license api service");
    }

    static public void annotatePDF(File licensePdf, HashMap<String, Object> annotations, String targetPdf) throws Exception {
        try {
            PDDocument pdf = PDDocument.load(licensePdf);
            PDDocumentCatalog docCatalog = pdf.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();
            for(String annotationName : annotations.keySet()) {
                PDField field = acroForm.getField(annotationName);
                if (field == null) {
                    throw new EIException("No field " + annotationName + " for the license file requested");
                }
                Object value = annotations.get(annotationName);
                if(value instanceof String) {
                    if(!(field instanceof PDTextField)) {
                        throw new EIException("No suitable text field found for " + annotationName + " for the license file requested");
                    }
                    ((PDTextField) field).setValue((String) value);
                    logger.debug("Text field inserted");
                } else if(value instanceof File) {
                    String imagePath = ((File) value).getAbsolutePath();
                    if (!(field instanceof PDPushButton)) {
                        throw new EIException("No signature field for the license file requested");
                    }
                    PDPushButton pdPushButton = (PDPushButton) field;

                    List<PDAnnotationWidget> widgets = pdPushButton.getWidgets();
                    if (widgets != null && widgets.size() > 0) {
                        PDAnnotationWidget annotationWidget = widgets.get(0); // just need one widget
                        File imageFile = new File(imagePath);

                        if (imageFile.exists()) {
                            PDImageXObject pdImageXObject = PDImageXObject.createFromFile(imagePath, pdf);
                            float imageScaleRatio = (float) pdImageXObject.getHeight() / (float) pdImageXObject.getWidth();

                            PDRectangle buttonPosition = getFieldArea(pdPushButton);
                            float height = buttonPosition.getHeight();
                            float width = height / imageScaleRatio;
                            float x = buttonPosition.getLowerLeftX();
                            float y = buttonPosition.getLowerLeftY();

                            PDAppearanceStream pdAppearanceStream = new PDAppearanceStream(pdf);
                            pdAppearanceStream.setResources(new PDResources());
                            try (PDPageContentStream pdPageContentStream = new PDPageContentStream(pdf, pdAppearanceStream)) {
                                pdPageContentStream.drawImage(pdImageXObject, x, y, width, height);
                            }
                            pdAppearanceStream.setBBox(new PDRectangle(x, y, width, height));

                            PDAppearanceDictionary pdAppearanceDictionary = annotationWidget.getAppearance();
                            if (pdAppearanceDictionary == null) {
                                pdAppearanceDictionary = new PDAppearanceDictionary();
                                annotationWidget.setAppearance(pdAppearanceDictionary);
                            }

                            pdAppearanceDictionary.setNormalAppearance(pdAppearanceStream);
                            System.out.println("Image '" + imagePath + "' inserted");
                        }
                    }
                }
            }
            // you can optionally flatten the document to merge acroform lay to main one
            acroForm.flatten();

            pdf.save(targetPdf);
            pdf.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e instanceof EIException ? e : new EIException("Server side error");
        }
    }

    private static PDRectangle getFieldArea(PDField field) {
        COSDictionary fieldDict = (COSDictionary) field.getCOSObject();
        COSArray fieldAreaArray = (COSArray) fieldDict.getDictionaryObject(COSName.RECT);
        return new PDRectangle(fieldAreaArray);
    }

}
